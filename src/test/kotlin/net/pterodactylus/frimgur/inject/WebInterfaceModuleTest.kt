package net.pterodactylus.frimgur.inject

import com.google.inject.Binder
import com.google.inject.Guice.createInjector
import com.google.inject.Module
import freenet.clients.http.PageMaker
import freenet.pluginmanager.FredPluginL10n
import net.pterodactylus.frimgur.web.WebInterface
import org.hamcrest.MatcherAssert.assertThat
import org.hamcrest.Matchers.notNullValue
import org.mockito.kotlin.mock
import kotlin.test.Test

/**
 * Unit test for [WebInterfaceModule].
 */
class WebInterfaceModuleTest {

	@Test
	fun `module can create a web interface`() {
		assertThat(injector.getInstance(WebInterface::class.java), notNullValue())
	}

	private val injector = createInjector(WebInterfaceModule(), object : Module {
		override fun configure(binder: Binder) {
			binder.bind(PageMaker::class.java).toInstance(mock())
			binder.bind(FredPluginL10n::class.java).toInstance(mock())
		}
	})

}
