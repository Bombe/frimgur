package net.pterodactylus.frimgur.inject

import com.google.inject.Guice.createInjector
import freenet.client.HighLevelSimpleClient
import freenet.clients.http.PageMaker
import freenet.clients.http.ToadletContainer
import freenet.pluginmanager.FredPluginL10n
import net.pterodactylus.frimgur.test.bind
import net.pterodactylus.frimgur.web.FreenetToadletFactory
import net.pterodactylus.frimgur.web.PageToadletAdapter
import net.pterodactylus.frimgur.web.WebInterface
import org.hamcrest.MatcherAssert.assertThat
import org.hamcrest.Matchers.notNullValue
import kotlin.test.Test

/**
 * Unit test for [WebInterfaceModule].
 */
class WebInterfaceModuleTest {

	@Test
	fun `module can create a web interface`() {
		assertThat(injector.getInstance(WebInterface::class.java), notNullValue())
	}

	@Test
	fun `module can create a page toadlet adapter`() {
		assertThat(injector.getInstance(PageToadletAdapter::class.java), notNullValue())
	}

	@Test
	fun `module can create a freenet toadlet factory`() {
		assertThat(injector.getInstance(FreenetToadletFactory::class.java), notNullValue())
	}

	private val injector = createInjector(
		WebInterfaceModule(),
		bind<PageMaker>().toMock(),
		bind<FredPluginL10n>().toMock(),
		bind<ToadletContainer>().toMock(),
		bind<HighLevelSimpleClient>().toMock()
	)

}
