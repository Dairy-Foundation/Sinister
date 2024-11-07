package dev.frozenmilk.sinister.apphooks

import android.content.Context
import dev.frozenmilk.sinister.loading.NoUnload
import dev.frozenmilk.sinister.loading.Preload

/**
 * a more type-safe version of [org.firstinspires.ftc.ftccommon.external.OnDestroy]
 *
 * static implementations of this class will be run as [org.firstinspires.ftc.ftccommon.external.OnDestroy] methods are
 */
@Preload
@NoUnload
@FunctionalInterface
@JvmDefaultWithoutCompatibility
fun interface OnDestroy {
	fun onDestroy(context: Context)
}

@Suppress("unused")
object OnDestroyScanner : HookScanner<OnDestroy>(OnDestroy::class.java) {
	@JvmStatic
	@org.firstinspires.ftc.ftccommon.external.OnDestroy
	fun onDestroy(context: Context) {
		allHooks.forEach { it.onDestroy(context) }
	}
}