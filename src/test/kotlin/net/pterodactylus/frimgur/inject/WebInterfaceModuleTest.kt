package net.pterodactylus.frimgur.inject

import com.google.inject.Guice.createInjector
import freenet.client.HighLevelSimpleClient
import freenet.clients.http.PageMaker
import freenet.clients.http.Toadlet
import freenet.clients.http.ToadletContainer
import freenet.pluginmanager.FredPluginL10n
import java.util.Locale
import java.util.Locale.ENGLISH
import kotlin.test.Test
import net.pterodactylus.frimgur.image.ImageService
import net.pterodactylus.frimgur.insert.InsertOptionService
import net.pterodactylus.frimgur.insert.InsertService
import net.pterodactylus.frimgur.test.bind
import net.pterodactylus.frimgur.test.matches
import net.pterodactylus.frimgur.util.getInstance
import net.pterodactylus.frimgur.web.FreenetToadletFactory
import net.pterodactylus.frimgur.web.ToadletRegistry
import net.pterodactylus.frimgur.web.WebInterface
import org.hamcrest.MatcherAssert.assertThat
import org.hamcrest.Matchers.hasItem
import org.hamcrest.Matchers.notNullValue
import org.mockito.ArgumentMatchers.anyBoolean
import org.mockito.kotlin.any
import org.mockito.kotlin.anyOrNull
import org.mockito.kotlin.argumentCaptor
import org.mockito.kotlin.atLeastOnce
import org.mockito.kotlin.verify

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

	@Test
	fun `toadlet registry contains toadlet for 'insert-options' path`() {
		val toadletRegistry = injector.getInstance<ToadletRegistry>()
		val toadletContainer = injector.getInstance<ToadletContainer>()
		toadletRegistry.start()
		val registeredToadlets = argumentCaptor<Toadlet>()
		verify(toadletContainer, atLeastOnce()).register(registeredToadlets.capture(), anyOrNull(), any(), anyBoolean(), anyOrNull(), anyOrNull(), anyBoolean(), anyOrNull(), anyOrNull())
		assertThat(registeredToadlets.allValues, hasItem(matches<Toadlet> { it.path().startsWith("/prefix/insert-options/") }))
	}

	private val injector = createInjector(
		WebInterfaceModule("/prefix/", "Menu.Test", "Menu.Tooltip"),
		bind<PageMaker>().toMock(),
		bind<FredPluginL10n>().toMock(),
		bind<ToadletContainer>().toMock(),
		bind<HighLevelSimpleClient>().toMock(),
		bind<ImageService>().toMock(),
		bind<InsertService>().toMock(),
		bind<InsertOptionService>().toMock(),
		bind<String>().toNamedInstance("FormPassword", "secret"),
		bind<Locale>().toProvider { ENGLISH },
		bind<Boolean>().toNamedProvider("NodeRequiresConfigChange") { false }
	)

}
