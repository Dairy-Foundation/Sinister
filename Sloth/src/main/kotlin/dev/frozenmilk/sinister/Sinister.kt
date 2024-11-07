@file:Suppress("DEPRECATION")

package dev.frozenmilk.sinister

import android.content.Context
import com.qualcomm.robotcore.util.RobotLog
import com.qualcomm.robotcore.util.ThreadPool
import dalvik.system.DexFile
import dev.frozenmilk.sinister.Sinister.TAG
import dev.frozenmilk.sinister.apphooks.OnCreateScanner
import dev.frozenmilk.sinister.loading.NoUnload
import dev.frozenmilk.sinister.loading.SinisterClassLoader
import dev.frozenmilk.sinister.opmode.loadShim
import dev.frozenmilk.sinister.targeting.FullSearch
import dev.frozenmilk.sinister.targeting.TeamCodeSearch
import org.firstinspires.ftc.ftccommon.external.OnCreate
import org.firstinspires.ftc.robotcore.internal.files.RecursiveFileObserver
import org.firstinspires.ftc.robotcore.internal.system.AppUtil
import java.io.File
import java.lang.ref.WeakReference
import java.util.concurrent.CompletableFuture
import java.util.concurrent.ExecutorService

private object Sinister : RecursiveFileObserver.Listener {
	private val rootSearch = FullSearch()
	private val parentLoader: ClassLoader? = Sinister::class.java.classLoader
	private val teamCodeSearch = TeamCodeSearch()
	private var teamCodeLoader: ClassLoader? = parentLoader
	// note, we make sure not to include @NoUnload classes in this list,
	// so that they don't get scanned or unloaded after the initial sinister scan
	private var teamCodeClasses = emptyList<Class<*>>()
	private lateinit var scanners: List<Scanner>
	private var run = false
	const val TAG = "Sinister"
	private val dir = File("${AppUtil.FIRST_FOLDER}/dairy/sloth")
	private val to_load = File("$dir/to_load.jar")
	private val loaded = File("$dir/loaded.jar")

	init {
		if (!dir.exists()) {
			RobotLog.dd(TAG, "making sloth dir")
			dir.mkdirs()
		}
		else if (!dir.isDirectory) {
			RobotLog.dd(TAG, "remaking sloth dir")
			dir.delete()
			dir.mkdirs()
		}
		if (to_load.exists()) {
			RobotLog.vv(TAG, "deleting dead to_load")
			to_load.delete()
		}
		if (loaded.exists()) {
			if (loaded.isFile) {
				RobotLog.vv(TAG, "found loaded teamcode, switching to it")
				teamCodeLoader = SinisterClassLoader(loaded.path, parentLoader)
			}
			else {
				loaded.delete()
			}
		}
	}
	private val watcher = RecursiveFileObserver(dir, RecursiveFileObserver.ALL_FILE_OBSERVER_EVENTS, RecursiveFileObserver.Mode.RECURSIVE, this).apply {
		this.startWatching()
	}

	override fun onEvent(event: Int, file: File) {
		RobotLog.dd(TAG, "fs watch event: $event occurred for $file")
		if (event and RecursiveFileObserver.CREATE != 0 && file.isFile && file.name == "to_load.jar") {
			switchLoader()
		}
		else if (event and RecursiveFileObserver.IN_Q_OVERFLOW != 0) {
			RobotLog.ee(TAG, "IN_Q_OVERFLOW occurred, this needs to be handled")
			// todo, rescan
		}
	}

	/**
	 * 1. new file comes in under the name 'to_load.apk' or smth
	 * 2. old file is called 'loaded.apk'
	 * 3. unload old loader
	 * 4. upon gc collect, move incoming.apk to replace loaded.apk
	 * 5. then instantiate a new classloader, and sinister it
	 */
	private fun switchLoader() {
		RobotLog.vv(TAG, "sloth loading...")

		unload(teamCodeLoader!!, teamCodeClasses)

		if (teamCodeLoader != parentLoader) {
			val weak = WeakReference(teamCodeLoader)
			val start = System.nanoTime()
			teamCodeLoader = null
			// prompt unload
			System.gc()

			RobotLog.vv(TAG, "waiting for loader gc attempt...")
			while (weak.get() != null) {
				if (System.nanoTime() - start < 5e8) {
					RobotLog.vv(TAG, "waited too long for loader gc, giving up, this might need to be an error")
					break
				}
				// wait for gc for up to 0.5 seconds
			}
			RobotLog.vv(TAG, "loader gc attempted...")
		}

		loaded.delete()
		to_load.renameTo(loaded)

		RobotLog.vv(TAG, "loading new classes")

		val dex = openDex(loaded)

		teamCodeLoader = SinisterClassLoader(loaded.path, parentLoader)

		teamCodeClasses = dex.allClasses(teamCodeLoader!!).filterNot { it.inheritsAnnotation(NoUnload::class.java) }
		runScanners(teamCodeLoader!!, teamCodeClasses)

		RobotLog.vv(TAG, "... sloth loaded")

		dex.close()
	}

	@OnCreate
	@JvmStatic
	@Suppress("unused")
	fun onCreate(context: Context) {
		loadShim()
		RobotLog.vv(TAG, "attempting boot on create")
		if (run) {
			RobotLog.vv(TAG, "already booted")
			RobotLog.vv(TAG, "finished boot process")
			OnCreateScanner.onCreate(context)
			return
		}
		RobotLog.vv(TAG, "not yet booted, booting")
		val dexFile = DexFile(context.packageCodePath)
		selfBoot(dexFile)
		run = true
		dexFile.close()
		OnCreateScanner.onCreate(context)
		RobotLog.vv(TAG, "finished boot process")
	}

	private fun DexFile.allClasses(loader: ClassLoader): List<Class<*>> {
		return entries().toList().mapNotNull {
			if (!rootSearch.determineInclusion(it)) return@mapNotNull null

			try {
				val cls = Class.forName(it, false, loader)
				if (cls.inheritsAnnotation(NoUnload::class.java)) Class.forName(it, false, parentLoader) else cls
			}
			catch (e: Throwable) {
				RobotLog.ee(TAG, "Error occurred while locating class: $e")
				rootSearch.exclude(it)
				null
			}
		}
	}

	private fun selfBoot(dexFile: DexFile) {
		RobotLog.vv(TAG, "self booting...")
		val allClasses = dexFile.allClasses(teamCodeLoader!!)

		teamCodeClasses = allClasses.filter { !it.inheritsAnnotation(NoUnload::class.java) && teamCodeSearch.determineInclusion(it.name) }

		val preloaded = preload(parentLoader!!, allClasses)

		scanners = preloaded
			.flatMap {
				it.staticInstancesOf(Scanner::class.java)
			}

		scanners.forEach {
			RobotLog.vv(TAG, "found scanner ${it.javaClass.simpleName}")
		}

		runScanners(parentLoader, allClasses)

		RobotLog.vv(TAG, "...booted")
	}

	private fun preload(loader: ClassLoader, classes: List<Class<*>>) =
		classes
			.mapNotNull {
				try {
					it.preload(loader)
					RobotLog.vv(TAG, "preloading: ${it.simpleName}")
					it
				}
				catch (_: Throwable) {
					null
				}
			}

	private fun unload(loader: ClassLoader, classes: List<Class<*>>) {
		val executor = ThreadPool.getDefault()

		RobotLog.vv(TAG, "unloading old classloader")
		scanners.forEach {
			it.beforeUnload(loader)
		}
		try {
			scanners.map {
				spawnUnload(it, classes.iterator(), executor)
			}.toTypedArray().forEach {
				var res = it.get()
				while (res != null) {
					res = res.get() as CompletableFuture<*>?
				}
			}
		}
		catch (e: Throwable) {
			RobotLog.ee(TAG, "Error occurred while running Sinister:\nError: $e\nStackTrace: ${e.stackTraceToString()}")
		}
		scanners.forEach {
			it.afterUnload(loader)
		}
	}

	private fun runScanners(loader: ClassLoader, classes: List<Class<*>>) {
		val executor = ThreadPool.getDefault()

		RobotLog.vv(TAG, "running scanners")
		scanners.forEach {
			it.beforeScan(loader)
		}
		val tasks = scanners.map {
			spawnScanner(it, classes.iterator(), executor)
		}.toTypedArray()
		try {
			tasks.forEach {
				var res = it.get()
				while (res != null) {
					res = res.get() as CompletableFuture<*>?
				}
			}
		}
		catch (e: Throwable) {
			RobotLog.ee(TAG, "Error occurred while running Sinister:\nError: $e\nStackTrace: ${e.stackTraceToString()}")
		}

		scanners.forEach {
			it.afterScan(loader)
		}
	}
}

private fun spawnScanner(scanner: Scanner, classes: Iterator<Class<*>>, executor: ExecutorService): CompletableFuture<CompletableFuture<*>?> {
	if (!classes.hasNext()) return CompletableFuture.completedFuture(null)
	val cls = classes.next()
	return CompletableFuture.runAsync({
		if (scanner.targets.determineInclusion(cls.name)) scanner.scan(cls)
	}, executor)
		.handle { _, err ->
			if (err != null) RobotLog.ee(TAG, "Error occurred while running scanner: ${scanner::class.simpleName} | ${scanner}\nScanning Class:${cls}\nError: $err\nStackTrace: ${err.stackTraceToString()}")
			spawnScanner(scanner, classes, executor)
		}
}

private fun spawnUnload(scanner: Scanner, classes: Iterator<Class<*>>, executor: ExecutorService): CompletableFuture<CompletableFuture<*>?> {
	if (!classes.hasNext()) return CompletableFuture.completedFuture(null)
	val cls = classes.next()
	return CompletableFuture.runAsync({
		if (scanner.targets.determineInclusion(cls.name)) scanner.unload(cls)
	}, executor)
		.handle { _, err ->
			if (err != null) RobotLog.ee(TAG, "Error occurred while running scanner: ${scanner::class.simpleName} | ${scanner}\nScanning Class:${cls}\nError: $err\nStackTrace: ${err.stackTraceToString()}")
			spawnUnload(scanner, classes, executor)
		}
}

private fun openDex(file: File): DexFile =
	try {
		DexFile(file)
	}
	catch (e: Throwable) {
		RobotLog.vv(TAG, "Error occurred while opening dex file $file: $e, trying again")
		openDex(file)
	}