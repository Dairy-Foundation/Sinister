package dev.frozenmilk.sinister.sloth.tasks

import org.gradle.api.file.DirectoryProperty
import org.gradle.api.provider.Property
import org.gradle.api.tasks.Input
import org.gradle.api.tasks.OutputDirectory
import org.gradle.jvm.tasks.Jar

abstract class DexSloth : Jar() {
	@OutputDirectory
	abstract fun getOutputDir(): DirectoryProperty

	@Input
	abstract fun getDexBaseName(): Property<String>

	init {
		this.dependsOn("dexBuilderDebug")
		this.destinationDirectory.set(this.getOutputDir())
		this.archiveBaseName.set(this.getDexBaseName())
		this.from(project.buildDir.resolve("intermediates/project_dex_archive/debug/out")) {
			it.exclude("*.jar")
		}
	}
}