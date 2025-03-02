plugins {
	id("dev.frozenmilk.jvm-library") version "10.1.1-0.1.3"
	id("dev.frozenmilk.publish") version "0.0.4"
	id("dev.frozenmilk.doc") version "0.0.4"
}

repositories {
	maven {
		name = "dairyReleases"
		url = uri("https://repo.dairy.foundation/releases")
	}
}

dependencies {
	api("dev.frozenmilk.dairy:Util:1.1.1")
}

group = "dev.frozenmilk.sinister"

publishing {
	publications {
		register<MavenPublication>("release") {
			groupId = "dev.frozenmilk"
			artifactId = "Sinister"

			artifact(dairyDoc.dokkaJavadocJar)
			artifact(dairyDoc.dokkaHtmlJar)

			afterEvaluate {
				from(components["java"])
			}
		}
	}
}
