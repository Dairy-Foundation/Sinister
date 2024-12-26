package dev.frozenmilk.sinister.apphooks

import android.content.Context
import dev.frozenmilk.sinister.Scanner
import dev.frozenmilk.sinister.loading.NoUnload
import dev.frozenmilk.sinister.loading.Preload

/**
 * a more type-safe version of [org.firstinspires.ftc.ftccommon.external.OnCreate]
 *
 * static implementations of this class will be run as [org.firstinspires.ftc.ftccommon.external.OnCreate] methods are
 */
@Preload
@NoUnload
@FunctionalInterface
@JvmDefaultWithoutCompatibility
fun interface OnCreate {
	/**
	 * provides an easy way to perform initialization when the robot controller activity is created.
	 *
	 * @see org.firstinspires.ftc.ftccommon.external.OnCreate
	 */
	fun onCreate(context: Context)
}

@Suppress("unused")
object OnCreateScanner : HookScanner<OnCreate>(OnCreate::class.java) {
	override val adjacencyRule = Scanner.INDEPENDENT

	internal fun onCreate(context: Context) {
		allHooks.forEach { it.onCreate(context) }
	}
}
