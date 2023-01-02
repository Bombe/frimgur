package net.pterodactylus.frimgur.web

import freenet.clients.http.PageMaker
import freenet.pluginmanager.FredPluginL10n
import org.mockito.kotlin.mock
import org.mockito.kotlin.verify
import kotlin.test.Test

/**
 * Unit test for [WebInterface].
 */
class WebInterfaceTest {

	@Test
	fun `web interface registers menu category on page maker`() {
		val pageMaker = mock<PageMaker>()
		val pluginL10n = mock<FredPluginL10n>()
		val webInterface = WebInterface(pageMaker, pluginL10n)
		webInterface.start()
		verify(pageMaker).addNavigationCategory("/frimgur/", "Navigation.Menu.Title", "Navigation.Menu.Title.Tooltip", pluginL10n);
	}

}
