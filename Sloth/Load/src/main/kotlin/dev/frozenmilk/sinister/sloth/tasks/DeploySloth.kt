package dev.frozenmilk.sinister.sloth.tasks

import org.gradle.api.DefaultTask
import org.gradle.api.file.DirectoryProperty
import org.gradle.api.file.RegularFileProperty
import org.gradle.api.provider.Property
import org.gradle.api.tasks.Input
import org.gradle.api.tasks.InputDirectory
import org.gradle.api.tasks.InputFile
import org.gradle.api.tasks.TaskAction
import org.gradle.process.internal.ExecException

/**
 * Uses ADB to copy the merged dex jar to the robot controller.
 */
abstract class DeploySloth : DefaultTask() {
	@InputDirectory
	abstract fun getOutputDir(): DirectoryProperty

	@Input
	abstract fun getBundleBaseName(): Property<String>

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
					"push",
					getOutputDir().file("${getBundleBaseName().get()}.jar").get().asFile.absolutePath,
					getDeployLocation().get(),
				)
			}
		} catch (e: ExecException) {
			error("Failed to connect to robot, ensure ADB connected to robot.")
		}
	}
}