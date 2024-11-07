package dev.frozenmilk.sinister.loading

import dalvik.system.PathClassLoader
import dev.frozenmilk.sinister.inheritsAnnotation


class SinisterClassLoader : PathClassLoader {
	constructor(dexPath: String, parent: ClassLoader?) : super(dexPath, parent)
	constructor(dexPath: String, librarySearchPath: String, parent: ClassLoader?) : super(dexPath, librarySearchPath, parent)

	override fun loadClass(name: String, resolve: Boolean): Class<*>? {
		// loads classes from the new dex first before looking in the APK
		var loadedClass = findLoadedClass(name)
		if (loadedClass == null) {
			try {
				loadedClass = findClass(name)
				if (loadedClass.inheritsAnnotation(NoUnload::class.java)) {
					try { loadedClass = super.loadClass(name, resolve) }
					catch (_: ClassNotFoundException) {}
				}
			} catch (e: ClassNotFoundException) {
				if (name.contains("org.firstinspires.ftc.teamcode")) {
					// prevents classes that were deleted or renamed in fast load from being loaded from original APK
					throw e
				}
				loadedClass = super.loadClass(name, resolve)
			}
		}
		if (resolve) {
			resolveClass(loadedClass)
		}
		return loadedClass
	}
}