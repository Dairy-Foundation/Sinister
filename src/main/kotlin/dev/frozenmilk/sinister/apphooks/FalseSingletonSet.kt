package dev.frozenmilk.sinister.apphooks

import java.util.Collections

class FalseSingletonSet<T>(member: T) : MutableSet<T> by Collections.singleton(member) {
    override fun add(element: T) = false
    override fun remove(element: T) = false
}