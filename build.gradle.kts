plugins {
	id("com.android.library")
	id("kotlin-android")
	id("org.jetbrains.dokka") version "1.9.10"
//	id("maven-publish")
}

android {
	namespace = "dev.frozenmilk.dairy.sinister"
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
	}
}

dependencies {
	//noinspection GradleDependency
	implementation("androidx.appcompat:appcompat:1.2.0")
	testImplementation("org.testng:testng:6.9.6")

	api(project(":Util"))

	compileOnly("org.firstinspires.ftc:RobotCore:9.0.1")
	compileOnly("org.firstinspires.ftc:FtcCommon:9.0.1")
}

//publishing {
//	publications {
//		register<MavenPublication>("release") {
//			groupId = "dev.frozenmilk.dairy"
//			artifactId = "Sinister"
//			version = "v0.0.0"
//
//			afterEvaluate {
//				from(components["release"])
//			}
//		}
//	}
//	repositories {
//		maven {
//			name = "Core"
//			url = uri("${project.buildDir}/release")
//		}
//	}
//}
