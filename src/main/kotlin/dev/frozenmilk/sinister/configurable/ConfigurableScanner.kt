package dev.frozenmilk.sinister.configurable

import com.qualcomm.robotcore.util.RobotLog
import dev.frozenmilk.sinister.Scanner
import dev.frozenmilk.sinister.configurable.dswarn.DSWarn
import dev.frozenmilk.sinister.staticInstancesOf
import dev.frozenmilk.sinister.targeting.EmptySearch
import dev.frozenmilk.sinister.targeting.WideSearch

internal object ConfigurableScanner : Scanner {
	override val targets = WideSearch()
	private val dairySearch = EmptySearch().include("dev.frozenmilk")
	private val teamCodeSearch = EmptySearch().include("org.firstinspires.ftc.teamcode")

	private val configurations = mutableListOf<Configuration<*>>()
	private val configurables = mutableListOf<Configurable>()

	override fun scan(cls: Class<*>) {
		configurables.addAll(cls.staticInstancesOf(Configurable::class.java))
		configurations.addAll(cls.staticInstancesOf(Configuration::class.java))
	}

	override fun afterScan(loader: ClassLoader) {
		DSWarn.configure()
		configurations.removeIf {
			val res = it.detectCycle()
			if (res) {
				RobotLog.ee("Configuration", "cycle detected during configuration collection for $it")
				DSWarn.warn("Cycle detected during configuration collection for $it.\nTake a look at the Dairy Sinister docs on Configuration. If you cannot resolve this error, please contact the Dairy team for assistance.")
			}
			res
		}
		configurables.forEach {
			it.configure()
		}
	}

	override fun unload(cls: Class<*>) {
		configurables.removeAll(cls.staticInstancesOf(Configurable::class.java))
		configurations.removeAll(cls.staticInstancesOf(Configuration::class.java))
	}

	override fun afterUnload(loader: ClassLoader) {
		afterScan(loader)
	}

	private fun Configuration<*>.detectCycle(): Boolean {
		return prioritisedOver.any { it == this@detectCycle } || prioritisedOver.any { it.detectCycle(
			this@detectCycle
		) }
	}

	private fun Configuration<*>.detectCycle(toCheck: Configuration<*>): Boolean {
		return prioritisedOver.any { it == toCheck } || prioritisedOver.any { it.detectCycle(toCheck) }
	}
	enum class Level {
		DAIRY,
		LIBRARY,
		TEAM_CODE
	}

	@Suppress("UNCHECKED_CAST")
	fun <CONFIGURABLE: Configurable> configure(configurable: CONFIGURABLE) {
		val cls = configurable.javaClass
		val classConfigurations: Map<Level, List<Configuration<CONFIGURABLE>>> = configurations
			.filter { it.configurableClass.isAssignableFrom(cls) }
			.groupBy {
				if (teamCodeSearch.determineInclusion(it.javaClass.name)) Level.TEAM_CODE
				else if (dairySearch.determineInclusion(it.javaClass.name)) Level.DAIRY
				else Level.LIBRARY
			} as Map<Level, List<Configuration<CONFIGURABLE>>>

		classConfigurations.getOrElse(Level.TEAM_CODE) {
			classConfigurations.getOrElse(Level.LIBRARY) {
				classConfigurations.getOrElse(Level.DAIRY) {
					emptyList()
				}
			}
		}
			.reduceOrNull { l, r ->
				return@reduceOrNull if (l.recursePrioritisedOver(r)) l
				else if (r.recursePrioritisedOver(l)) r
				else {
					RobotLog.ee("Configuration", "Found two competing configurations: $l, $r\nThis may cause issues, will apply $l, then $r, try removing the conflicting configurations, or applying your own configuration that prioritises itself over these to resolve this issue.")
					DSWarn.warn("Found two competing configurations: $l, $r\nThis may cause issues, will apply $l, then $r, try removing the conflicting configurations, or applying your own configuration that prioritises itself over these to resolve this issue.\nTake a look at the Dairy Sinister docs on Configuration. If you cannot resolve this error, please contact the Dairy team for assistance.")
					object : Configuration<CONFIGURABLE> {
						override val configurableClass: Class<CONFIGURABLE> = l.configurableClass
						override val prioritisedOver: List<Configuration<in CONFIGURABLE>> = listOf(l, r)
						override fun configure(configurable: CONFIGURABLE) {
							l.configure(configurable)
							r.configure(configurable)
						}
					}
				}
			}
			?.also {
				try {
					it.configure(configurable)
				}
				catch (e: Throwable) {
					RobotLog.ee("Configuration", "thrown while applying $it to $configurable:\n%s", e)
				}
			}
	}

	private fun <CONFIGURABLE: Configurable> Configuration<in CONFIGURABLE>.recursePrioritisedOver(r: Configuration<CONFIGURABLE>): Boolean = prioritisedOver.contains(r) || prioritisedOver.any { it.recursePrioritisedOver(r) }
}