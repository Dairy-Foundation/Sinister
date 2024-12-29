package dev.frozenmilk.sinister.apphooks

import dev.frozenmilk.sinister.loading.Preload
import org.firstinspires.ftc.ftccommon.internal.AnnotatedHooksClassFilter
import org.firstinspires.ftc.robotcore.internal.opmode.ClassFilter
import org.firstinspires.ftc.robotcore.internal.opmode.ClassManager

@Suppress("unused")
@Preload
object SDKFilterRemover {
    init {
        @Suppress("unchecked_cast")
        val filters = ClassManager::class.java.getDeclaredField("filters").apply {
            isAccessible = true
        }.get(ClassManager.getInstance()) as MutableSet<ClassFilter>
        filters.remove(AnnotatedHooksClassFilter.getInstance())
    }
}