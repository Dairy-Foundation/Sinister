package dev.frozenmilk.sinister.opmode

import com.qualcomm.robotcore.eventloop.opmode.OpMode
import com.qualcomm.robotcore.eventloop.opmode.OpModeManagerImpl
import com.qualcomm.robotcore.eventloop.opmode.OpModeRegister
import com.qualcomm.robotcore.exception.DuplicateNameException
import com.qualcomm.robotcore.util.RobotLog
import dev.frozenmilk.sinister.loading.Preload
import org.firstinspires.ftc.robotcore.internal.opmode.InstanceOpModeRegistrar
import org.firstinspires.ftc.robotcore.internal.opmode.OpModeMeta
import org.firstinspires.ftc.robotcore.internal.opmode.OpModeMetaAndClass
import org.firstinspires.ftc.robotcore.internal.opmode.OpModeMetaAndInstance
import org.firstinspires.ftc.robotcore.internal.opmode.RegisteredOpModes
import org.firstinspires.ftc.robotcore.internal.system.Assert

/**
 * shims [RegisteredOpModes.getInstance] to be this, and exposes the underlying data structures and methods for more customisation
 */
@Preload
internal object Shim : RegisteredOpModes() {
	init {
		RobotLog.vv(TAG, "replacing RegisteredOpModes instance with shim instance")

		val instanceOpModeRegistrarsField = getInstance().javaClass.getDeclaredField("instanceOpModeRegistrars")
		instanceOpModeRegistrarsField.isAccessible = true
		@Suppress("UNCHECKED_CAST")
		instanceOpModeRegistrars.addAll(instanceOpModeRegistrarsField.get(getInstance()) as Collection<InstanceOpModeRegistrar>)

		val opModeClassesField = getInstance().javaClass.getDeclaredField("opModeClasses")
		opModeClassesField.isAccessible = true
		@Suppress("UNCHECKED_CAST")
		opModeClasses.putAll(opModeClassesField.get(getInstance()) as Map<out String, OpModeMetaAndClass>)

		val opModeInstancesField = getInstance().javaClass.getDeclaredField("opModeInstances")
		opModeInstancesField.isAccessible = true
		@Suppress("UNCHECKED_CAST")
		opModeInstances.putAll(opModeInstancesField.get(getInstance()) as Map<out String, OpModeMetaAndInstance>)

		val instanceHolder = RegisteredOpModes::class.java.declaredClasses.first()
		val theInstanceField = instanceHolder.getDeclaredField("theInstance")
		theInstanceField.isAccessible = true
		theInstanceField.set(null, this)
	}

	override fun register(meta: OpModeMeta, opMode: Class<*>) {
		lockOpModesWhile {
			if (reportIfOpModeAlreadyRegistered(meta)) {
				@Suppress("UNCHECKED_CAST")
				opModeClasses[meta.name] =
					OpModeMetaAndClass(
						meta,
						opMode as Class<OpMode>
					)
				RobotLog.vv(
					TAG,
					"registered {${opMode.simpleName}} as {${meta.name}}"
				)
			} else {
				throw DuplicateNameException("Duplicate for " + meta.name)
			}
		}
	}

	override fun registerAllOpModes(userOpmodeRegister: OpModeRegister) {
		lockOpModesWhile {
			opModeClasses.clear()
			opModeInstances.clear()

			// register our default OpMode first, that way the user can override it (eh?)
			register(
				DEFAULT_OP_MODE_METADATA,
				OpModeManagerImpl.DefaultOpMode::class.java
			)

			// Somewhat arbitrary, but do the annotated ones LAST so we
			// can get the same 'duplicate name' behavior on reregistration
			// as we do on original registration
			callInstanceOpModeRegistrars()
			userOpmodeRegister.register(this)
			// NOTE: we are removing this line, no more silly sdk opmode registration, its all mine now
			// AnnotatedOpModeClassFilter.getInstance().registerAllClasses(this)
			SinisterOpModes.reregister()
			opmodesAreRegistered = true
		}
	}


	public override fun unregister(meta: OpModeMeta) {
		lockOpModesWhile {
			RobotLog.vv(TAG, "unregistering {${meta.name}}")
			opModeClasses.remove(meta.name)?.apply {
				RobotLog.vv(TAG, "unregistered {${this.clazz.simpleName}} known as {${meta.name}}")
			}
			opModeInstances.remove(meta.name)?.apply {
				RobotLog.vv(TAG, "unregistered {${this}} known as {${meta.name}}")
			}
			Assert.assertFalse(isOpmodeRegistered(meta))
		}
	}

	override fun getOpModes(): List<OpModeMeta> {
		return lockOpModesWhile<List<OpModeMeta>> {
			opModeClasses.values.map { it.meta } + opModeInstances.values.map { it.meta }
		}
	}
}