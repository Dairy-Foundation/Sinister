package dev.frozenmilk.sinister.configurable

import com.qualcomm.robotcore.util.RobotLog
import dev.frozenmilk.sinister.Scanner
import dev.frozenmilk.sinister.configurable.dswarn.DSWarn
import dev.frozenmilk.sinister.staticInstancesOf
import dev.frozenmilk.sinister.targeting.EmptySearch
import dev.frozenmilk.sinister.targeting.WideSearch
import dev.frozenmilk.util.graph.Graph
import dev.frozenmilk.util.graph.GraphImpl
import dev.frozenmilk.util.graph.emitGraph
import dev.frozenmilk.util.graph.rule.AdjacencyRule

internal object ConfigurableScanner : Scanner {
	override val adjacencyRule = Scanner.INDEPENDENT
	override val targets = WideSearch()
	private val dairySearch = EmptySearch().include("dev.frozenmilk")
	private val teamCodeSearch = EmptySearch().include("org.firstinspires.ftc.teamcode")

	private val configurations = mutableSetOf<Configuration<*>>()
	private val configurationCache = mutableMapOf<Class<out Configurable>, Configuration<*>?>()
	private val configurables = mutableSetOf<Configurable>()

	override fun scan(cls: Class<*>) {
		configurables.addAll(cls.staticInstancesOf(Configurable::class.java))
		configurations.addAll(cls.staticInstancesOf(Configuration::class.java))
	}

	private fun Any.level() =
		if (teamCodeSearch.determineInclusion(javaClass.name)) Level.TEAM_CODE
		else if (dairySearch.determineInclusion(javaClass.name)) Level.DAIRY
		else Level.LIBRARY

	private fun Configuration<*>.overrideLevel(): AdjacencyRule<Configuration<*>, Graph<Configuration<*>>> =
		when (level()) {
			Level.DAIRY -> AdjacencyRule { graph ->
				val dependencies = requireNotNull(graph[this]) { { "$this was not in graph" } }
				graph.nodes
					.filter { it.configurableClass.isAssignableFrom(configurableClass) }
					.forEach { dependencies + it }
			}
			Level.LIBRARY -> AdjacencyRule { graph ->
				val dependencies = requireNotNull(graph[this]) { { "$this was not in graph" } }
				graph.nodes
					.filter { it.configurableClass.isAssignableFrom(configurableClass) }
					.filter { it.level() == Level.TEAM_CODE }
					.forEach { dependencies + it }
			}
			Level.TEAM_CODE -> Configuration.INDEPENDENT
		}

	private val graph = GraphImpl<Configuration<*>>()

	override fun afterScan(loader: ClassLoader) {
		configurations.emitGraph(graph) { configuration: Configuration<*> -> configuration.adjacencyRule }

		DSWarn.configure()

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

	enum class Level {
		DAIRY,
		LIBRARY,
		TEAM_CODE
	}

	// single round of 'kahn' (pull out nodes that have no dependencies) sort (we don't care about the remaining ones)
	// res ->
	// 		one item: good!
	//		more than one ->
	//			try resolve with auto levels ->
	//				success: good!
	//				failure: error (no more merge and warn)
	// 		no items ->
	//			if no inputs, configurations should specify if they must be configured or not, might be fine or not
	//			if any inputs, cycle, throw error (we can't have one input)

	// this way we don't need to worry about cycles from ignored configurations, as long as we never hit them
	@Suppress("UNCHECKED_CAST")
	fun <CONFIGURABLE: Configurable> configure(configurable: CONFIGURABLE) {
		val cls = configurable.javaClass

		val configuration = configurationCache.computeIfAbsent(cls) {
			this.graph.run {
				val nodes = mutableSetOf<Configuration<CONFIGURABLE>>()
				this.nodes.forEach {
					@Suppress("unchecked_cast")
					if (it.configurableClass.isAssignableFrom(cls)) nodes.add(it as Configuration<CONFIGURABLE>)
				}

				val configurationsAvailable = nodes.isNotEmpty()

				if (configurationsAvailable) {
					nodes.removeAll(nodes.filter {
						this[it]!!.set.union(nodes).isNotEmpty()
					})

					if (nodes.isEmpty()) {
						RobotLog.ee("Configuration", "cycle(s) detected during configuration collection for configuration of $configurable:\n$nodes")
						DSWarn.warn("Cycle(s) detected during configuration collection.\nTake a look at the Dairy Sinister docs on Configuration. If you cannot resolve this error, please contact the Dairy team for assistance.")
					}
				}

				when (nodes.size) {
					1 -> nodes.first()
					0 -> null
					else -> {
						val levels = nodes.groupBy { it.level() }
						val pool = if (levels[Level.TEAM_CODE]?.isNotEmpty() == true) levels[Level.TEAM_CODE]!!
						else if (levels[Level.LIBRARY]?.isNotEmpty() == true) levels[Level.LIBRARY]!!
						else levels[Level.DAIRY]!!

						if (pool.size == 1) pool.first()
						else error("unable to determine a single configuration to apply to $configurable, remaining options were $pool")
					}
				}
			}
		} as? Configuration<CONFIGURABLE>

		if (!configurable.allowNone) checkNotNull(configuration) { "unable to determine any configurations to apply to $configurable, which requires a configuration" }
		configuration?.let { it ->
			try {
				it.configure(configurable)
			}
			catch (e: Throwable) {
				RobotLog.ee("Configuration", "thrown while applying $it to $configurable:\n%s", e)
				DSWarn.warn("Error was thrown while applying configuration $it to $configurable, check the logcat for more information")
			}
		}
	}
}