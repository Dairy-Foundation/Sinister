package dev.frozenmilk.sinister.sloth

import com.android.build.api.variant.AndroidComponentsExtension
import com.android.build.gradle.internal.crash.afterEvaluate
import dev.frozenmilk.sinister.sloth.tasks.AssembleSloth
import dev.frozenmilk.sinister.sloth.tasks.DeploySloth
import dev.frozenmilk.sinister.sloth.tasks.DexSloth
import dev.frozenmilk.sinister.sloth.tasks.RemoveRemoteSloth
import org.gradle.api.Plugin
import org.gradle.api.Project

private const val DEX_BASE_NAME_CONVENTION = "sloth_intermediate"
private const val BUNDLE_BASE_NAME_CONVENTION = "to_load"
private const val DEPLOY_LOCATION_CONVENTION = "/storage/emulated/0/FIRST/dairy/sloth"

class Load : Plugin<Project> {
	override fun apply(project: Project) {
		val androidComponents = project.extensions.getByType(AndroidComponentsExtension::class.java)

		val outputDir = project.layout.buildDirectory.dir("libs")
		@Suppress("UnstableApiUsage")
		val adb = androidComponents.sdkComponents.adb

		val dexSloth = project.tasks.register("dexSloth", DexSloth::class.java) { task ->
			task.group = "build"

			task.getOutputDir().convention(outputDir)
			task.getDexBaseName().convention(DEX_BASE_NAME_CONVENTION)

			task.dependsOn("dexBuilderDebug")
		}
		val assembleSloth = project.tasks.register("assembleSloth", AssembleSloth::class.java) { task ->
			task.group = "build"

			task.getOutputDir().convention(outputDir)
			task.getDexBaseName().convention(DEX_BASE_NAME_CONVENTION)
			task.getBundleBaseName().convention(BUNDLE_BASE_NAME_CONVENTION)

			task.dependsOn(dexSloth)
		}
		project.tasks.register("deploySloth", DeploySloth::class.java) { task ->
			task.group = "install"

			task.getAdbExecutable().convention(adb)
			task.getOutputDir().convention(outputDir)
			task.getBundleBaseName().convention(BUNDLE_BASE_NAME_CONVENTION)
			task.getDeployLocation().convention(DEPLOY_LOCATION_CONVENTION)

			task.dependsOn(assembleSloth)
		}

		val removeSlothRemote = project.tasks.register("removeSlothRemote", RemoveRemoteSloth::class.java) { task ->
			task.group = "install"

			task.getAdbExecutable().convention(adb)
			task.getDeployLocation().convention(DEPLOY_LOCATION_CONVENTION)
		}

		project.afterEvaluate {
			it.getTasksByName("installDebug", false).forEach { task ->
				task.dependsOn(removeSlothRemote)
			}
			it.getTasksByName("installRelease", false).forEach { task ->
				task.dependsOn(removeSlothRemote)
			}
		}
	}
}