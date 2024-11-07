import dev.frozenmilk.sinister.NoPreloadException
import dev.frozenmilk.sinister.loading.Preload
import dev.frozenmilk.sinister.apphooks.CollectImplementationsScanner
import dev.frozenmilk.sinister.apphooks.HookScanner
import dev.frozenmilk.sinister.apphooks.OnCreateEventLoopScanner
import dev.frozenmilk.sinister.preload
import org.junit.Assert
import org.junit.Test

class Preloading {
	@Test(expected = NoPreloadException::class)
	fun preloadFails() {
		NoPreloadObject::class.java.preload()
		Assert.fail()
	}

	@Test
	fun preloadObject() {
		PreLoadObject::class.java.preload()
	}

	@Test
	fun preloadImplementation() {
		PreloadImplementation::class.java.preload()
	}

	@Test
	fun preloadComplex() {
		CollectImplementationsScanner::class.java.preload()
		HookScanner::class.java.preload()
		OnCreateEventLoopScanner::class.java.preload()
	}
}

private object NoPreloadObject

@Preload
private object PreLoadObject

@Preload
private interface PreloadInterface

private object PreloadImplementation : PreloadInterface