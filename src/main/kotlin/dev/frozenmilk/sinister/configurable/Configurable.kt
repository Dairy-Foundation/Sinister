package dev.frozenmilk.sinister.configurable

interface Configurable {
	fun configure() {
		ConfigurableScanner.configure(this)
	}
}
