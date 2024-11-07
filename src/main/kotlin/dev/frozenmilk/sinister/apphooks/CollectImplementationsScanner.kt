package dev.frozenmilk.sinister.apphooks

import com.qualcomm.robotcore.util.RobotLog
import dev.frozenmilk.sinister.Scanner
import dev.frozenmilk.sinister.staticInstancesOf
import dev.frozenmilk.sinister.targeting.NarrowSearch

abstract class CollectImplementationsScanner<T: Any>(private val cls: Class<T>) : Scanner {
	protected open var tag = this::class.java.simpleName
	override val targets = NarrowSearch()
	protected val found = mutableSetOf<T>()
	override fun scan(cls: Class<*>) {
		cls.staticInstancesOf(this.cls)
				.forEach {
					found.add(it)
					RobotLog.vv(tag, "found implementing instance: ${it::class.java.name}")
				}
	}

	override fun unload(cls: Class<*>) {
		cls.staticInstancesOf(this.cls)
			.forEach {
				found.remove(it)
				RobotLog.vv(tag, "unloading implementing instance: ${it::class.java.name}")
			}
	}
}