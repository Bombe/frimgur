package net.pterodactylus.frimgur.inject

import com.google.inject.Guice.createInjector
import freenet.client.HighLevelSimpleClient
import net.pterodactylus.frimgur.test.bind
import org.hamcrest.MatcherAssert.assertThat
import org.hamcrest.Matchers.notNullValue
import kotlin.test.Test

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
		assertThat(injector.getInstance(InsertModule::class.java), notNullValue())
	}

	private val injector = createInjector(
		InsertModule(),
		bind<HighLevelSimpleClient>().toMock()
	)

}
