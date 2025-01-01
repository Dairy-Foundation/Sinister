plugins {
	id("dev.frozenmilk.android-library")
	id("dev.frozenmilk.publish") version "0.0.4"
	id("dev.frozenmilk.doc") version "0.0.4"
}

android.namespace = "dev.frozenmilk.sinister"

ftc {
	kotlin

	sdk {
		RobotCore
		FtcCommon {
			configurationNames += "testImplementation"
		}
	}
}

dependencies {
	api("dev.frozenmilk.dairy:Util")
}

publishing {
	publications {
		register<MavenPublication>("release") {
			groupId = "dev.frozenmilk"
			artifactId = "Sinister"

			artifact(dairyDoc.dokkaHtmlJar)
			artifact(dairyDoc.dokkaJavadocJar)

			afterEvaluate {
				from(components["release"])
			}
		}
	}
}
