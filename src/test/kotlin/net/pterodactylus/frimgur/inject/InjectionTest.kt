package net.pterodactylus.frimgur.inject

import com.google.inject.Guice.createInjector
import net.pterodactylus.frimgur.web.WebInterface
import org.hamcrest.MatcherAssert.assertThat
import org.hamcrest.Matchers.notNullValue
import org.mockito.Mockito
import org.mockito.kotlin.mock
import kotlin.test.Test

/**
 * Unit test for cross-[module][com.google.inject.Module] functionality.
 */
class InjectionTest {

	@Test
	fun `web interface can be created`() {
		val injector = createInjector(
			FreenetModule(mock(), mock(defaultAnswer = Mockito.RETURNS_DEEP_STUBS)),
			WebInterfaceModule("/prefix/")
		)
		assertThat(injector.getInstance(WebInterface::class.java), notNullValue())
	}

}
