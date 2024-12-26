package dev.frozenmilk.sinister.apphooks

import com.qualcomm.robotcore.eventloop.opmode.AnnotatedOpModeManager
import dev.frozenmilk.sinister.Scanner
import dev.frozenmilk.sinister.loading.NoUnload
import dev.frozenmilk.sinister.loading.Preload

/**
 * a more type-safe version of [com.qualcomm.robotcore.eventloop.opmode.OpModeRegistrar]
 *
 * static implementations of this class will be run as [com.qualcomm.robotcore.eventloop.opmode.OpModeRegistrar] methods are
 */
@Preload
@NoUnload
@FunctionalInterface
@JvmDefaultWithoutCompatibility
fun interface OpModeRegistrar {
	fun registerOpModes(opModeManager: AnnotatedOpModeManager)
}

@Suppress("unused")
object OpModeRegistrarScanner : HookScanner<OpModeRegistrar>(OpModeRegistrar::class.java) {
	override val adjacencyRule = Scanner.INDEPENDENT

	/**
	 * prevents [registerOpModes] from being exposed publicly
	 */
	private object CALLSITE {
		@JvmStatic
		@com.qualcomm.robotcore.eventloop.opmode.OpModeRegistrar
		fun registerOpModes(opModeManager: AnnotatedOpModeManager) {
			allHooks.forEach { it.registerOpModes(opModeManager) }
		}
	}
}