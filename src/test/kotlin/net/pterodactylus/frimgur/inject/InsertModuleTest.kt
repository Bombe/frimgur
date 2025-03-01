package net.pterodactylus.frimgur.inject

import com.google.inject.Guice.createInjector
import freenet.client.HighLevelSimpleClient
import kotlin.test.Test
import net.pterodactylus.frimgur.insert.InsertService
import net.pterodactylus.frimgur.test.bind
import net.pterodactylus.frimgur.util.getInstance
import org.hamcrest.MatcherAssert.assertThat
import org.hamcrest.Matchers.notNullValue
import org.hamcrest.Matchers.sameInstance

/**
 * Unit test for [InsertModule].
 */
class InsertModuleTest {

	@Test
	fun `can create insert module`() {
		InsertModule()
	}

	@Test
	fun `guice module can create insert service`() {
		assertThat(injector.getInstance<InsertModule>(), notNullValue())
	}

	@Test
	fun `insert service is provided as singleton`() {
		val firstInstance: InsertService = injector.getInstance()
		val secondInstance: InsertService = injector.getInstance()
		assertThat(secondInstance, sameInstance(firstInstance))
	}

	private val injector = createInjector(
		InsertModule(),
		bind<HighLevelSimpleClient>().toMock()
	)

}
