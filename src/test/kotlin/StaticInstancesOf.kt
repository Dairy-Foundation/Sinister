import dev.frozenmilk.sinister.loading.Preload
import dev.frozenmilk.sinister.preload
import dev.frozenmilk.sinister.staticInstancesOf
import org.junit.Assert
import org.junit.Test

class StaticInstancesOf {
	@Test
	fun objectInstance() {
		Implementation::class.java.preload()
		Assert.assertEquals(Implementation::class.java.staticInstancesOf(TestInterface::class.java), listOf(Implementation))
	}

	@Test
	fun nestedObjectInstance() {
		NestedImplementation::class.java.preload()
		Assert.assertEquals(NestedImplementation::class.java.staticInstancesOf(TestInterface::class.java), listOf(NestedImplementation))
	}
}

@Preload
private interface TestInterface

private object Implementation : TestInterface

private interface NestedInterface : TestInterface

private object NestedImplementation : NestedInterface
