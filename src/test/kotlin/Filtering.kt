import dev.frozenmilk.sinister.targeting.FullSearch
import org.junit.Assert
import org.junit.Test

class Filtering {
	@Test
	fun inclusions() {
		val search = FullSearch()
		Assert.assertEquals(false, search.determineInclusion("com.android.tools.r8"))
	}

	@Test
	fun inclusions2() {
		val search = FullSearch()
		Assert.assertEquals(false, search.determineInclusion("kotlin.io.path.PathsKt__PathRecursiveFunctionsKt\$copyToRecursively\$5"))
	}

	@Test
	fun inclusions3() {
		val search = FullSearch()
		Assert.assertEquals(false, search.determineInclusion("android.util.Log"))
	}
}