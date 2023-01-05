package net.pterodactylus.frimgur.web

import freenet.clients.http.PageMaker
import freenet.pluginmanager.FredPluginL10n
import org.hamcrest.MatcherAssert.assertThat
import org.hamcrest.Matchers.equalTo
import org.mockito.kotlin.mock
import org.mockito.kotlin.verify
import java.util.concurrent.atomic.AtomicBoolean
import kotlin.test.Test

/**
 * Unit test for [DefaultWebInterface].
 */
class WebInterfaceTest {

	@Test
	fun `web interface registers menu category on page maker`() {
		webInterface.start()
		verify(pageMaker).addNavigationCategory("/frimgur/", "Menu.Test.Title", "Menu.Test.Tooltip", pluginL10n);
	}

	@Test
	fun `starting the web interface starts the toadlet registry`() {
		val startCalled = AtomicBoolean(false)
		val toadletRegistry = object : ToadletRegistry by toadletRegistry {
			override fun start() = startCalled.set(true)
		}
		val webInterface = DefaultWebInterface("Title", "Tooltip", pageMaker, pluginL10n, toadletRegistry)
		webInterface.start()
		assertThat(startCalled.get(), equalTo(true))
	}

	@Test
	fun `stopping the web interface stops the toadlet registry`() {
		val stopCalled = AtomicBoolean(false)
		val toadletRegistry = object : ToadletRegistry by toadletRegistry {
			override fun stop() = stopCalled.set(true)
		}
		val webInterface = DefaultWebInterface("Title", "Tooltip", pageMaker, pluginL10n, toadletRegistry)
		webInterface.stop()
		assertThat(stopCalled.get(), equalTo(true))
	}

	@Test
	fun `stopping the web interface removes the navigation category`() {
		webInterface.stop()
		verify(pageMaker).removeNavigationCategory("Menu.Test.Title");
	}

	private val pageMaker = mock<PageMaker>()
	private val pluginL10n = mock<FredPluginL10n>()
	private val toadletRegistry = mock<ToadletRegistry>()
	private val webInterface = DefaultWebInterface("Menu.Test.Title", "Menu.Test.Tooltip", pageMaker, pluginL10n, toadletRegistry)

}

