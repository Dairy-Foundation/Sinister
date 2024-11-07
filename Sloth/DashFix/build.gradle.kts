plugins {
	id("com.android.library")
	id("kotlin-android")
	id("org.jetbrains.dokka") version "1.9.10"
	id("maven-publish")
}

android {
	namespace = "dev.frozenmilk.sinister.sloth.dash"
	compileSdk = 29

	defaultConfig {
		minSdk = 24
		//noinspection ExpiredTargetSdkVersion
		targetSdk = 28

		testInstrumentationRunner = "androidx.test.runner.AndroidJUnitRunner"
		consumerProguardFiles("consumer-rules.pro")
	}

	buildTypes {
		release {
			isMinifyEnabled = false
			proguardFiles(getDefaultProguardFile("proguard-android-optimize.txt"), "proguard-rules.pro")
		}
	}
	compileOptions {
		sourceCompatibility = JavaVersion.VERSION_1_8
		targetCompatibility = JavaVersion.VERSION_1_8

		kotlin {
			compilerOptions {
				freeCompilerArgs.add("-Xjvm-default=all")
			}
		}
	}
}

repositories {
	maven {
		url = uri("https://maven.brott.dev/")
	}
}

dependencies {
	//noinspection GradleDependency
	implementation("androidx.appcompat:appcompat:1.2.0")
	testImplementation("junit:junit:4.13.2")

	compileOnly(project(":Sinister"))
	compileOnly("com.acmerobotics.dashboard:dashboard:0.4.16") {
		exclude("org.firstinspires.ftc")
	}

	compileOnly("org.firstinspires.ftc:RobotCore:10.1.0")
	compileOnly("org.firstinspires.ftc:FtcCommon:10.1.0")

	testImplementation("org.firstinspires.ftc:FtcCommon:10.1.0")
}

publishing {
	repositories {
		maven {
			name = "Dairy"
			url = uri("https://repo.dairy.foundation/releases")
			credentials(PasswordCredentials::class)
			authentication {
				create<BasicAuthentication>("basic")
			}
		}
		maven {
			name = "DairySNAPSHOT"
			url = uri("https://repo.dairy.foundation/snapshots")
			credentials(PasswordCredentials::class)
			authentication {
				create<BasicAuthentication>("basic")
			}
		}
	}
	publications {
		register<MavenPublication>("release") {
			groupId = "dev.frozenmilk.sinister.sloth"
			artifactId = "DashFix"
			version = "0.0.0"

			afterEvaluate {
				from(components["release"])
			}
		}
	}
}
