buildscript {
	repositories {
		mavenCentral()
		google()
	}
	dependencies {
		// Note for FTC Teams: Do not modify this yourself.
		//noinspection AndroidGradlePluginVersion
		classpath("com.android.tools.build:gradle:7.2.0")
		classpath("org.jetbrains.kotlin:kotlin-gradle-plugin:1.9.21")
	}
}

repositories {
	mavenCentral()
	google()
}

plugins {
	id("java-gradle-plugin")
	id("org.jetbrains.kotlin.jvm") version "1.9.21"
	id("maven-publish")
}

group = "dev.frozenmilk.sinister.sloth"
version = "0.0.2"

java {
	sourceCompatibility = JavaVersion.VERSION_1_8
	targetCompatibility = JavaVersion.VERSION_1_8
}

kotlin {
	compilerOptions {
		freeCompilerArgs.add("-Xjvm-default=all")
	}
}

dependencies {
	//noinspection AndroidGradlePluginVersion
	compileOnly("com.android.tools.build:gradle:7.2.0")
	testImplementation("junit:junit:4.13.2")
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
}

gradlePlugin {
	plugins {
		create("Load") {
			id = "dev.frozenmilk.sinister.sloth.Load"
			implementationClass = "dev.frozenmilk.sinister.sloth.Load"
		}
	}
}
