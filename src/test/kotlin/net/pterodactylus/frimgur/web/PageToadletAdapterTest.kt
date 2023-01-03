package net.pterodactylus.frimgur.web

import freenet.client.HighLevelSimpleClient
import org.hamcrest.MatcherAssert.assertThat
import org.hamcrest.Matchers.equalTo
import org.mockito.kotlin.mock
import java.net.URI
import java.util.concurrent.atomic.AtomicReference
import kotlin.test.Test

/**
 * Unit test for [PageToadletAdapter].
 */
class PageToadletAdapterTest {

	@Test
	fun `returned toadlet returns given path`() {
		val toadlet = pageToadletAdapter.adapt("test.html", object : Page {
			override fun handleGet() = Unit
		})
		assertThat(toadlet.path(), equalTo("test.html"))
	}

	@Test
	fun `returned toadlet forwards request to page`() {
		val handleGetCalled = AtomicReference(false)
		val toadlet = pageToadletAdapter.adapt("test.html", object : Page {
			override fun handleGet() = handleGetCalled.set(true)
		})
		toadlet.handleMethodGET(URI("test.html"), mock(), mock())
		assertThat(handleGetCalled.get(), equalTo(true))
	}

	private val highLevelSimpleClient = mock<HighLevelSimpleClient>()
	private val pageToadletAdapter = PageToadletAdapter(highLevelSimpleClient)

}
