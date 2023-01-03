package net.pterodactylus.frimgur.web

import freenet.client.HighLevelSimpleClient
import freenet.clients.http.ToadletContext
import org.hamcrest.MatcherAssert.assertThat
import org.hamcrest.Matchers.equalTo
import org.mockito.ArgumentMatchers
import org.mockito.ArgumentMatchers.anyLong
import org.mockito.kotlin.any
import org.mockito.kotlin.eq
import org.mockito.kotlin.mock
import org.mockito.kotlin.verify
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
			override fun handleGet() = Response()
		})
		assertThat(toadlet.path(), equalTo("test.html"))
	}

	@Test
	fun `returned toadlet forwards request to page`() {
		val handleGetCalled = AtomicReference(false)
		val toadlet = pageToadletAdapter.adapt("test.html", object : Page {
			override fun handleGet() = Response().also { handleGetCalled.set(true) }
		})
		toadlet.handleMethodGET(URI("test.html"), mock(), mock())
		assertThat(handleGetCalled.get(), equalTo(true))
	}

	@Test
	fun `returned response is handed back to toadlet container`() {
		val toadlet = pageToadletAdapter.adapt("test.html", object : Page {
			override fun handleGet() = Response(123)
		})
		val toadletContext = mock<ToadletContext>()
		toadlet.handleMethodGET(URI("test.html"), mock(), toadletContext)
		verify(toadletContext).sendReplyHeaders(eq(123), any(), any(), any(), anyLong())
	}

	@Test
	fun `return code 500 has correct reason phrase`() {
		val toadlet = pageToadletAdapter.adapt("test.html", object : Page {
			override fun handleGet() = Response(500)
		})
		val toadletContext = mock<ToadletContext>()
		toadlet.handleMethodGET(URI("test.html"), mock(), toadletContext)
		verify(toadletContext).sendReplyHeaders(eq(500), eq("Internal Server Error"), any(), any(), anyLong())
	}

	@Test
	fun `reason phrase can be overwritten`() {
		val toadlet = pageToadletAdapter.adapt("test.html", object : Page {
			override fun handleGet() = Response(123, reason = "Test Reason")
		})
		val toadletContext = mock<ToadletContext>()
		toadlet.handleMethodGET(URI("test.html"), mock(), toadletContext)
		verify(toadletContext).sendReplyHeaders(eq(123), eq("Test Reason"), any(), any(), anyLong())
	}

	private val highLevelSimpleClient = mock<HighLevelSimpleClient>()
	private val pageToadletAdapter = PageToadletAdapter(highLevelSimpleClient)

}
