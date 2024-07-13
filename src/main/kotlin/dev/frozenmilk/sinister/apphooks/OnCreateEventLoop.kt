package dev.frozenmilk.sinister.apphooks

import android.content.Context
import com.qualcomm.ftccommon.FtcEventLoop
import dev.frozenmilk.sinister.Preload

/**
 * a more type-safe version of [org.firstinspires.ftc.ftccommon.external.OnCreateEventLoop]
 *
 * static implementations of this class will be run as [org.firstinspires.ftc.ftccommon.external.OnCreateEventLoop] methods are
 */
@Preload
@FunctionalInterface
@JvmDefaultWithCompatibility
fun interface OnCreateEventLoop {
	fun onCreateEventLoop(context: Context, ftcEventLoop: FtcEventLoop)
}

object OnCreateEventLoopFilter : HookFilter<OnCreateEventLoop>(OnCreateEventLoop::class.java) {
	@JvmStatic
	@org.firstinspires.ftc.ftccommon.external.OnCreateEventLoop
	fun onCreateEventLoop(context: Context, ftcEventLoop: FtcEventLoop) {
		allHooks.forEach {
			it.onCreateEventLoop(context, ftcEventLoop)
		}
	}
}