package dev.frozenmilk.sinister.sloth

import com.acmerobotics.dashboard.FtcDashboard
import com.acmerobotics.dashboard.config.Config
import com.acmerobotics.dashboard.config.reflection.ReflectionConfig
import com.qualcomm.robotcore.eventloop.opmode.Disabled
import dev.frozenmilk.sinister.Scanner
import dev.frozenmilk.sinister.targeting.NarrowSearch

object DashFix : Scanner {
	override val targets = NarrowSearch()
	override fun scan(cls: Class<*>) {
		if (!cls.isAnnotationPresent(Config::class.java) || cls.isAnnotationPresent(Disabled::class.java)) return
		FtcDashboard.getInstance().withConfigRoot {
			val name = cls.getAnnotation(Config::class.java)!!.value.ifEmpty { cls.simpleName }
			it.putVariable(name, ReflectionConfig.createVariableFromClass(cls))
		}
	}

	override fun unload(cls: Class<*>) {
		if (!cls.isAnnotationPresent(Config::class.java) || cls.isAnnotationPresent(Disabled::class.java)) return
		FtcDashboard.getInstance().withConfigRoot {
			val name = cls.getAnnotation(Config::class.java)!!.value.ifEmpty { cls.simpleName }
			it.removeVariable(name)
		}
	}
}