plugins {
	id("dev.frozenmilk.library") version "10.1.1-0.0.0"
	id("org.jetbrains.dokka") version "1.9.10"
	id("maven-publish")
}

android {
	namespace = "dev.frozenmilk.sinister"

	publishing {
		singleVariant("release") {
			withSourcesJar()
			withJavadocJar()
		}
	}
}

ftc {
	kotlin

	sdk {
		appcompat
		RobotCore
		FtcCommon {
			configurationNames += "testImplementation"
		}
	}
}

dependencies {
	testImplementation("junit:junit:4.13.2")

	api("dev.frozenmilk.dairy:Util")
}

tasks.withType<Test>().configureEach {
	javaLauncher = javaToolchains.launcherFor {
		languageVersion = JavaLanguageVersion.of(17)
	}
}

publishing {
	repositories {
		maven {
			name = "Release"
			url = uri("https://repo.dairy.foundation/releases")
			credentials(PasswordCredentials::class)
			authentication {
				create<BasicAuthentication>("basic")
			}
		}
		maven {
			name = "Snapshot"
			url = uri("https://repo.dairy.foundation/snapshots")
			credentials(PasswordCredentials::class)
			authentication {
				create<BasicAuthentication>("basic")
			}
		}
	}
	publications {
		register<MavenPublication>("release") {
			groupId = "dev.frozenmilk"
			artifactId = "Sinister"
			version = "2.0.2"

			afterEvaluate {
				from(components["release"])
			}
		}
	}
}
