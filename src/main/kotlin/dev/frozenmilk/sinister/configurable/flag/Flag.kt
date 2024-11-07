package dev.frozenmilk.sinister.configurable.flag

import dev.frozenmilk.sinister.configurable.Configurable

interface Flag : Configurable {
	var flag: Boolean
}