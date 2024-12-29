package dev.frozenmilk.sinister.apphooks

import dev.frozenmilk.sinister.Scanner
import dev.frozenmilk.sinister.targeting.SearchTarget
import dev.frozenmilk.sinister.targeting.WideSearch
import dev.frozenmilk.util.graph.Graph
import dev.frozenmilk.util.graph.rule.AdjacencyRule
import java.util.function.Consumer

abstract class AppHookScanner<AppHook: Any> : Scanner {
    override val targets: SearchTarget = WideSearch()
    override val adjacencyRule: AdjacencyRule<Scanner, Graph<Scanner>> = Scanner.INDEPENDENT

    private val appHooks = mutableMapOf<ClassLoader, MutableList<AppHook>>()
    private var registrationHelper: RegistrationHelper? = null

    final override fun beforeScan(loader: ClassLoader) {
        registrationHelper = RegistrationHelper(
            appHooks.getOrPut(loader) { mutableListOf() }
        )
    }
    final override fun scan(cls: Class<*>) = scan(cls, registrationHelper!!)
    abstract fun scan(cls: Class<*>, registrationHelper: RegistrationHelper)
    final override fun afterScan(loader: ClassLoader) {
        registrationHelper = null
    }

    final override fun beforeUnload(loader: ClassLoader) {}
    final override fun unload(cls: Class<*>) {}
    final override fun afterUnload(loader: ClassLoader) {
        appHooks.remove(loader)
    }

    inner class RegistrationHelper(private val appHooks: MutableList<AppHook>) {
        fun register(appHook: AppHook) {
            appHooks.add(appHook)
        }
    }

    fun iterateAppHooks(f: Consumer<AppHook>) = appHooks.values.forEach { list -> list.forEach { f.accept(it) } }
}