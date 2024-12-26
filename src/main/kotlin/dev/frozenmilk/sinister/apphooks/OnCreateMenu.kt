package dev.frozenmilk.sinister.apphooks

import android.content.Context
import android.view.Menu
import dev.frozenmilk.sinister.Scanner
import dev.frozenmilk.sinister.loading.NoUnload
import dev.frozenmilk.sinister.loading.Preload

/**
 * a more type-safe version of [org.firstinspires.ftc.ftccommon.external.OnCreateMenu]
 *
 * static implementations of this class will be run as [org.firstinspires.ftc.ftccommon.external.OnCreateMenu] methods are
 */
@Preload
@NoUnload
@FunctionalInterface
@JvmDefaultWithoutCompatibility
fun interface OnCreateMenu {
	fun onCreateMenu(context: Context, menu: Menu)
}

@Suppress("unused")
object OnCreateMenuScanner : HookScanner<OnCreateMenu>(OnCreateMenu::class.java) {
	override val adjacencyRule = Scanner.INDEPENDENT

	/**
	 * prevents [onCreateMenu] from being exposed publicly
	 */
	private object CALLSITE {
		@JvmStatic
		@org.firstinspires.ftc.ftccommon.external.OnCreateMenu
		fun onCreateMenu(context: Context, menu: Menu) {
			allHooks.forEach { it.onCreateMenu(context, menu) }
		}
	}
}