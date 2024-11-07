package dev.frozenmilk.sinister.configurable.dswarn

import com.qualcomm.robotcore.util.RobotLog
import dev.frozenmilk.sinister.configurable.Configurable
import dev.frozenmilk.sinister.configurable.Configuration
import java.util.function.Consumer

/**
 * USER CODE MUST NOT CONFIGURE DSWarn
 *
 * If a user chooses to do so, then they must be careful not to cause cycles
 */
object DSWarn : Configurable {
	var warn = Consumer<String> {}
	fun warn(message: String) = warn.accept(message)
}

object OnBotDSWarn : Configuration<DSWarn> {
	override val configurableClass = DSWarn::class.java
	override val prioritisedOver: List<Configuration<in DSWarn>> = emptyList()
	override fun configure(configurable: DSWarn) {
		configurable.warn = Consumer { RobotLog.addGlobalWarningMessage(it) }
	}
}