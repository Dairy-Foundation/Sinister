plugins {
	id("dev.frozenmilk.jvm-library") version "11.2.1-1.2.0"
	id("dev.frozenmilk.publish") version "0.1.0"
	id("dev.frozenmilk.doc") version "0.1.0"
}

dependencies {
	api("dev.frozenmilk.dairy:Util:1.2.2")
	testImplementation("junit:junit:4.13.2")
}

ftc {
	kotlin()
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
