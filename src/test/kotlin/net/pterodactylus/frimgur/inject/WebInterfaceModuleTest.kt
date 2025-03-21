package net.pterodactylus.frimgur.inject

import com.google.inject.Guice.createInjector
import freenet.client.HighLevelSimpleClient
import freenet.clients.http.PageMaker
import freenet.clients.http.ToadletContainer
import freenet.pluginmanager.FredPluginL10n
import java.util.Locale
import java.util.Locale.ENGLISH
import kotlin.test.Test
import net.pterodactylus.frimgur.image.ImageService
import net.pterodactylus.frimgur.insert.InsertService
import net.pterodactylus.frimgur.test.bind
import net.pterodactylus.frimgur.web.FreenetToadletFactory
import net.pterodactylus.frimgur.web.ToadletRegistry
import net.pterodactylus.frimgur.web.WebInterface
import org.hamcrest.MatcherAssert.assertThat
import org.hamcrest.Matchers.notNullValue

/**
 * Unit test for [WebInterfaceModule].
 */
class WebInterfaceModuleTest {

	@Test
	fun `module can create a web interface`() {
		assertThat(injector.getInstance(WebInterface::class.java), notNullValue())
	}

	@Test
	fun `module can create a toadlet registry`() {
		assertThat(injector.getInstance(ToadletRegistry::class.java), notNullValue())
	}

	@Test
	fun `module can create a freenet toadlet factory`() {
		assertThat(injector.getInstance(FreenetToadletFactory::class.java), notNullValue())
	}

	private val injector = createInjector(
		WebInterfaceModule("/prefix", "Menu.Test", "Menu.Tooltip"),
		bind<PageMaker>().toMock(),
		bind<FredPluginL10n>().toMock(),
		bind<ToadletContainer>().toMock(),
		bind<HighLevelSimpleClient>().toMock(),
		bind<ImageService>().toMock(),
		bind<InsertService>().toMock(),
		bind<String>().toNamedInstance("FormPassword", "secret"),
		bind<Locale>().toProvider { ENGLISH },
		bind<Boolean>().toNamedProvider("NodeRequiresConfigChange") { false }
	)

}
