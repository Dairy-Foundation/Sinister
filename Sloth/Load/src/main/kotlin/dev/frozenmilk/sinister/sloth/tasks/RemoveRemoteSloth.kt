package dev.frozenmilk.sinister.sloth.tasks

import org.gradle.api.DefaultTask
import org.gradle.api.file.RegularFileProperty
import org.gradle.api.provider.Property
import org.gradle.api.tasks.Input
import org.gradle.api.tasks.InputFile
import org.gradle.api.tasks.TaskAction
import org.gradle.process.internal.ExecException

abstract class RemoveRemoteSloth : DefaultTask() {
	@InputFile
	abstract fun getAdbExecutable(): RegularFileProperty

	@Input
	abstract fun getDeployLocation(): Property<String>

	@TaskAction
	fun execute() {
		try {
			project.exec {
				it.commandLine(
					getAdbExecutable().get(),
					"shell",
					"rm -f ${getDeployLocation().get()}/loaded.jar"
				)
			}
		} catch (e: ExecException) {
			error("Failed to connect to robot, ensure ADB connected to robot.")
		}
	}
}