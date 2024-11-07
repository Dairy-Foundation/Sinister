package dev.frozenmilk.sinister.apphooks

import android.content.Context
import com.qualcomm.robotcore.util.WebHandlerManager
import dev.frozenmilk.sinister.loading.NoUnload
import dev.frozenmilk.sinister.loading.Preload

/**
 * a more type-safe version of [org.firstinspires.ftc.ftccommon.external.WebHandlerRegistrar]
 *
 * static implementations of this class will be run as [org.firstinspires.ftc.ftccommon.external.WebHandlerRegistrar] methods are
 */
@Preload
@NoUnload
@FunctionalInterface
@JvmDefaultWithoutCompatibility
fun interface WebHandlerRegistrar {
	fun webHandlerRegistrar(context: Context, webHandlerManager: WebHandlerManager)
}

@Suppress("unused")
object WebHandlerRegistrarScanner : HookScanner<WebHandlerRegistrar>(WebHandlerRegistrar::class.java) {
	@JvmStatic
	@org.firstinspires.ftc.ftccommon.external.WebHandlerRegistrar
	fun webHandlerRegistrar(context: Context, webHandlerManager: WebHandlerManager) {
		allHooks.forEach { it.webHandlerRegistrar(context, webHandlerManager) }
	}
}