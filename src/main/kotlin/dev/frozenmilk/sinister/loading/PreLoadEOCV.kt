package dev.frozenmilk.sinister.loading

import com.qualcomm.robotcore.util.RobotLog

@Preload
private object PreLoadEOCV {
	init {
		RobotLog.vv("PreLoadEOCV", "preloading EOCV")
		System.loadLibrary("EasyOpenCV")
	}
}