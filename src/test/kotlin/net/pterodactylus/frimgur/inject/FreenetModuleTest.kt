package net.pterodactylus.frimgur.inject

import com.google.inject.Guice.createInjector
import freenet.clients.http.PageMaker
import freenet.clients.http.ToadletContainer
import freenet.pluginmanager.FredPluginL10n
import org.hamcrest.MatcherAssert.assertThat
import org.hamcrest.Matchers.notNullValue
import org.mockito.Mockito.RETURNS_DEEP_STUBS
import org.mockito.kotlin.mock
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

	private val module = FreenetModule(mock(), mock(defaultAnswer = RETURNS_DEEP_STUBS))
	private val injector = createInjector(module)

}
