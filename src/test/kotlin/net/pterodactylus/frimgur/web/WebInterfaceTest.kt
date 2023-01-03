package net.pterodactylus.frimgur.web

import freenet.clients.http.PageMaker
import freenet.clients.http.Toadlet
import freenet.clients.http.ToadletContainer
import freenet.pluginmanager.FredPluginL10n
import org.mockito.Mockito.isNull
import org.mockito.kotlin.eq
import org.mockito.kotlin.mock
import org.mockito.kotlin.verify
import kotlin.test.Test

/**
 * Unit test for [DefaultWebInterface].
 */
class WebInterfaceTest {

	@Test
	fun `web interface registers menu category on page maker`() {
		webInterface.start()
		verify(pageMaker).addNavigationCategory("/frimgur/", "Navigation.Menu.Title", "Navigation.Menu.Tooltip", pluginL10n);
	}

	@Test
	fun `web interface registers toadlet on toadlet container`() {
		val wantedToadlet = mock<Toadlet>()
		val pageToadletAdapter = object : PageToadletAdapter {
			override fun adapt(path: String, page: Page) = if ((path == "") && (page is MainPage)) wantedToadlet else mock()
		}
		val webInterface = DefaultWebInterface(pageMaker, pluginL10n, toadletContainer, pageToadletAdapter)
		webInterface.start()
		verify(toadletContainer).register(eq(wantedToadlet), eq("Navigation.Menu.Title"), eq("/frimgur/"), eq(true), eq("Navigation.Main.Title"), eq("Navigation.Main.Tooltip"), eq(false), isNull(), eq(pluginL10n))
	}

	@Test
	fun `stopping the web interface removes the navigation category`() {
		webInterface.stop()
		verify(pageMaker).removeNavigationCategory("Navigation.Menu.Title");
	}

	@Test
	fun `stopping the web interface unregisters the toadlets`() {
		val toadlet = mock<Toadlet>()
		val pageToadletAdapter = object : PageToadletAdapter {
			override fun adapt(path: String, page: Page) = toadlet
		}
		val webInterface = DefaultWebInterface(pageMaker, pluginL10n, toadletContainer, pageToadletAdapter)
		webInterface.start()
		webInterface.stop()
		verify(toadletContainer).unregister(eq(toadlet))
	}

	private val pageMaker = mock<PageMaker>()
	private val pluginL10n = mock<FredPluginL10n>()
	private val toadletContainer = mock<ToadletContainer>()
	private val pageToadletAdapter = mock<PageToadletAdapter>()
	private val webInterface = DefaultWebInterface(pageMaker, pluginL10n, toadletContainer, pageToadletAdapter)

}
