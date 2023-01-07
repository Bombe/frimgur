package net.pterodactylus.frimgur.inject

import com.google.inject.Guice.createInjector
import freenet.pluginmanager.PluginRespirator
import net.pterodactylus.frimgur.web.WebInterface
import org.hamcrest.MatcherAssert.assertThat
import org.hamcrest.Matchers.notNullValue
import org.mockito.Mockito
import org.mockito.kotlin.mock
import org.mockito.kotlin.whenever
import kotlin.test.Test

/**
 * Unit test for cross-[module][com.google.inject.Module] functionality.
 */
class InjectionTest {

	@Test
	fun `web interface can be created`() {
		val injector = createInjector(
			FreenetModule(mock(), pluginRespirator),
			WebInterfaceModule("/prefix/", "Menu.Test", "Menu.Tooltip")
		)
		assertThat(injector.getInstance(WebInterface::class.java), notNullValue())
	}

	private val pluginRespirator = mock<PluginRespirator>(defaultAnswer = Mockito.RETURNS_DEEP_STUBS).apply {
		whenever(toadletContainer.formPassword).thenReturn("passwort")
	}

}
