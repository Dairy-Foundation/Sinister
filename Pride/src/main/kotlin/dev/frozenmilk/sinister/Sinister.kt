@file:Suppress("DEPRECATION")

package dev.frozenmilk.sinister

import android.content.Context
import com.qualcomm.robotcore.util.RobotLog
import com.qualcomm.robotcore.util.ThreadPool
import dalvik.system.DexFile
import dev.frozenmilk.sinister.Sinister.TAG
import dev.frozenmilk.sinister.apphooks.OnCreateScanner
import dev.frozenmilk.sinister.loading.NoUnload
import dev.frozenmilk.sinister.loading.Preload
import dev.frozenmilk.sinister.targeting.FullSearch
import org.firstinspires.ftc.ftccommon.external.OnCreate
import java.util.concurrent.CompletableFuture
import java.util.concurrent.ExecutorService

private object Sinister {
	private val loader = this::class.java.classLoader
	private val rootSearch = FullSearch()
	private var run = false
	private lateinit var scanners: List<Scanner>
	const val TAG = "Sinister"

	@OnCreate
	@JvmStatic
	@Suppress("unused")
	fun onCreate(context: Context) {
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
				if (cls.inheritsAnnotation(NoUnload::class.java)) Class.forName(it, false, loader) else cls
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
		val allClasses = dexFile.allClasses(loader!!)

		val preloaded = preload(loader, allClasses)

		scanners = preloaded
			.flatMap {
				it.staticInstancesOf(Scanner::class.java)
			}

		scanners.forEach {
			RobotLog.vv(TAG, "found scanner ${it.javaClass.simpleName}")
		}

		runScanners(loader, allClasses)

		RobotLog.vv(TAG, "...booted")
	}

	private fun preload(loader: ClassLoader, classes: List<Class<*>>) =
		classes
			.filter {
				try {
					if (it.inheritsAnnotation(Preload::class.java)) {
						RobotLog.vv(TAG, "preloading: ${it.simpleName}")
						it.preload(loader)
						true
					}
					else false
				}
				catch (_: Throwable) {
					false
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
