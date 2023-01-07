package net.pterodactylus.frimgur.inject

import com.google.inject.Guice.createInjector
import freenet.client.HighLevelSimpleClient
import freenet.clients.http.PageMaker
import freenet.clients.http.ToadletContainer
import freenet.pluginmanager.FredPluginL10n
import freenet.pluginmanager.PluginRespirator
import org.hamcrest.MatcherAssert.assertThat
import org.hamcrest.Matchers.notNullValue
import org.mockito.Mockito.RETURNS_DEEP_STUBS
import org.mockito.kotlin.mock
import org.mockito.kotlin.whenever
import kotlin.test.Test

/**
 * Unit test for [FreenetModule].
 */
class FreenetModuleTest {

	@Test
	fun `freenet module can provide page maker`() {
		assertThat(injector.getInstance(PageMaker::class.java), notNullValue())
	}

	@Test
	fun `freenet module can provide plugin L10n`() {
		assertThat(injector.getInstance(FredPluginL10n::class.java), notNullValue())
	}

	@Test
	fun `freenet module can provide toadlet container`() {
		assertThat(injector.getInstance(ToadletContainer::class.java), notNullValue())
	}

	@Test
	fun `freenet module can provide high-level simple client`() {
		assertThat(injector.getInstance(HighLevelSimpleClient::class.java), notNullValue())
	}

	private val pluginRespirator = mock<PluginRespirator>(defaultAnswer = RETURNS_DEEP_STUBS).apply {
		whenever(toadletContainer.formPassword).thenReturn("passwort")
	}
	private val module = FreenetModule(mock(), pluginRespirator)
	private val injector = createInjector(module)

}
