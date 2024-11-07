package dev.frozenmilk.sinister.opmode

import com.qualcomm.ftccommon.CommandList
import com.qualcomm.robotcore.eventloop.opmode.Autonomous
import com.qualcomm.robotcore.eventloop.opmode.OpMode
import com.qualcomm.robotcore.eventloop.opmode.TeleOp
import com.qualcomm.robotcore.robocol.Command
import com.qualcomm.robotcore.util.RobotLog
import com.qualcomm.robotcore.util.ThreadPool
import dev.frozenmilk.sinister.Scanner
import dev.frozenmilk.sinister.loading.Preload
import dev.frozenmilk.sinister.targeting.TeamCodeSearch
import org.firstinspires.ftc.robotcore.internal.collections.SimpleGson
import org.firstinspires.ftc.robotcore.internal.network.NetworkConnectionHandler
import org.firstinspires.ftc.robotcore.internal.opmode.OpModeMeta
import java.util.concurrent.ScheduledFuture
import java.util.concurrent.TimeUnit

/**
 * manages OpModes, allows for dynamic registering and deregistering of [OpMode]s at runtime
 */
@Preload
object SinisterOpModes : Scanner {
	//
	// Filtering
	//
	private const val TAG = "SinisterOpModes"
	override val targets = TeamCodeSearch()
	private val foundOpModes = mutableListOf<Pair<OpModeMeta, Class<out OpMode>>>()

	internal fun reregister() {
		foundOpModes.forEach { (meta, cls) -> register(meta, cls) }
	}

	override fun scan(cls: Class<*>) {
		if (OpMode::class.java.isAssignableFrom(cls)) {
			val meta = if (cls.isAnnotationPresent(TeleOp::class.java)) {
				val annotation = cls.getAnnotation(TeleOp::class.java)!!
				OpModeMeta.Builder()
					.setName(annotation.name.ifBlank { cls.simpleName })
					.setGroup(annotation.group)
					.setFlavor(OpModeMeta.Flavor.TELEOP)
					.setSource(OpModeMeta.Source.ANDROID_STUDIO)
					.build()
			}
			else if (cls.isAnnotationPresent(Autonomous::class.java)) {
				val annotation = cls.getAnnotation(Autonomous::class.java)!!
				OpModeMeta.Builder()
					.setName(annotation.name.ifBlank { cls.simpleName })
					.setGroup(annotation.group)
					.setFlavor(OpModeMeta.Flavor.AUTONOMOUS)
					.setSource(OpModeMeta.Source.ANDROID_STUDIO)
					.build()
			}
			else {
				null
			}
			if (meta != null) {
				RobotLog.vv(TAG, "registering opmode $meta")
				@Suppress("UNCHECKED_CAST")
				register(meta, cls as Class<out OpMode>)
				foundOpModes.add(meta to cls)
			}
		}
	}

	override fun unload(cls: Class<*>) {
		foundOpModes.forEach { (meta, opModeCls) ->
			if (cls != opModeCls) return@forEach
			RobotLog.vv(TAG, "unloading opmode $meta")
			deregister(meta)
		}
	}

	//
	// OpMode Shim interaction
	//

	private var lazyTask: ScheduledFuture<*>? = null
	private fun refresh() {
		lazyTask?.cancel(false)
		lazyTask = ThreadPool.getDefaultScheduler().schedule({
			val opModeList = SimpleGson.getInstance().toJson(Shim.opModes)
			NetworkConnectionHandler.getInstance().sendCommand(
				Command(
					CommandList.CMD_NOTIFY_OP_MODE_LIST,
					opModeList
				)
			)
		}, 1, TimeUnit.SECONDS)
	}
	fun register(meta: OpModeMeta, cls: Class<out OpMode>) {
		Shim.register(meta, cls)
		refresh()
	}
	fun deregister(meta: OpModeMeta) {
		Shim.unregister(meta)
		refresh()
	}
}