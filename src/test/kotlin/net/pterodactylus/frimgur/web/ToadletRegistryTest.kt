package net.pterodactylus.frimgur.web

import freenet.clients.http.Toadlet
import freenet.clients.http.ToadletContainer
import freenet.pluginmanager.FredPluginL10n
import org.mockito.ArgumentMatchers.anyBoolean
import org.mockito.kotlin.any
import org.mockito.kotlin.eq
import org.mockito.kotlin.inOrder
import org.mockito.kotlin.isNull
import org.mockito.kotlin.mock
import org.mockito.kotlin.never
import org.mockito.kotlin.verify
import org.mockito.kotlin.whenever
import kotlin.test.Test

/**
 * Unit test for [DefaultToadletRegistry].
 */
class ToadletRegistryTest {

	@Test
	fun `adding a toadlet does not register it`() {
		toadletRegistry.addToadlet(ToadletSpec(toadlet))
		verify(toadletContainer, never()).register(any(), any(), any(), anyBoolean(), any(), any(), anyBoolean(), any(), any())
	}

	@Test
	fun `adding a toadlet and then starting the registry does register the toadlet`() {
		toadletRegistry.addToadlet(ToadletSpec(toadlet))
		toadletRegistry.start()
		verify(toadletContainer).register(eq(toadlet), isNull(), eq("/test/path"), eq(true), isNull(), isNull(), anyBoolean(), isNull(), eq(pluginL10n))
	}

	@Test
	fun `toadlets are registered in reverse order`() {
		toadletRegistry.addToadlet(ToadletSpec(toadlet))
		val secondToadlet = createToadlet("/test/path2")
		toadletRegistry.addToadlet(ToadletSpec(secondToadlet))
		toadletRegistry.start()
		inOrder(toadletContainer) {
			verify(toadletContainer).register(eq(secondToadlet), isNull(), eq("/test/path2"), eq(true), isNull(), isNull(), anyBoolean(), isNull(), eq(pluginL10n))
			verify(toadletContainer).register(eq(toadlet), isNull(), eq("/test/path"), eq(true), isNull(), isNull(), anyBoolean(), isNull(), eq(pluginL10n))
		}
	}

	@Test
	fun `adding a toadlet and then stopping the registry will unregister the toadlet`() {
		toadletRegistry.addToadlet(ToadletSpec(toadlet))
		toadletRegistry.stop()
		verify(toadletContainer).unregister(eq(toadlet))
	}

	private val toadletContainer = mock<ToadletContainer>()
	private val pluginL10n = mock<FredPluginL10n>()
	private val toadletRegistry = DefaultToadletRegistry(toadletContainer, "Test.Menu", pluginL10n)
	private val toadlet = createToadlet("/test/path")

	private fun createToadlet(path: String) = mock<Toadlet>().also {
		whenever(it.path()).thenReturn(path)
	}

}
