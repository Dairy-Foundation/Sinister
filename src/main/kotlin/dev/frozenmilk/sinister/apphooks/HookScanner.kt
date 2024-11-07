package dev.frozenmilk.sinister.apphooks

import java.lang.ref.WeakReference

abstract class HookScanner<T: Any>(clazz: Class<T>) : CollectImplementationsScanner<T>(clazz) {
	protected val registeredHooks = mutableSetOf<WeakReference<T>>()
	protected val allHooks: Set<T>
		get() = found + registeredHooks.mapNotNull { it.get() }
	fun register(hook: T) {
		registeredHooks.add(WeakReference(hook))
		registeredHooks.removeIf { it.get() == null }
	}
}
