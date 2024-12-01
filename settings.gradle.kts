pluginManagement {
	repositories {
		gradlePluginPortal()
		mavenCentral()
		google()
		maven("https://repo.dairy.foundation/releases")
	}
}

includeBuild("Util") {
	dependencySubstitution {
		substitute(module("dev.frozenmilk.dairy:Util")).using(project(":"))
	}
}
