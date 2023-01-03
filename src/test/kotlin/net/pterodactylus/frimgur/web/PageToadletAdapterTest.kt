package net.pterodactylus.frimgur.web

import freenet.client.HighLevelSimpleClient
import freenet.clients.http.ToadletContext
import freenet.support.api.Bucket
import org.hamcrest.MatcherAssert.assertThat
import org.hamcrest.Matchers.equalTo
import org.mockito.ArgumentMatchers.anyLong
import org.mockito.kotlin.any
import org.mockito.kotlin.argumentCaptor
import org.mockito.kotlin.eq
import org.mockito.kotlin.mock
import org.mockito.kotlin.verify
import java.io.ByteArrayInputStream
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
		toadlet.handleMethodGET(URI("test.html"), mock(), toadletContext)
		verify(toadletContext).sendReplyHeaders(eq(123), any(), any(), any(), anyLong())
	}

	@Test
	fun `return code 500 has correct reason phrase`() {
		val toadlet = pageToadletAdapter.adapt("test.html", object : Page {
			override fun handleGet() = Response(500)
		})
		toadlet.handleMethodGET(URI("test.html"), mock(), toadletContext)
		verify(toadletContext).sendReplyHeaders(eq(500), eq("Internal Server Error"), any(), any(), anyLong())
	}

	@Test
	fun `reason phrase can be overwritten`() {
		val toadlet = pageToadletAdapter.adapt("test.html", object : Page {
			override fun handleGet() = Response(123, reason = "Test Reason")
		})
		toadlet.handleMethodGET(URI("test.html"), mock(), toadletContext)
		verify(toadletContext).sendReplyHeaders(eq(123), eq("Test Reason"), any(), any(), anyLong())
	}

	@Test
	fun `no content is written correctly`() {
		val toadlet = pageToadletAdapter.adapt("test.html", object : Page {
			override fun handleGet() = Response(200)
		})
		toadlet.handleMethodGET(URI("test.html"), mock(), toadletContext)
		verify(toadletContext).sendReplyHeaders(eq(200), eq("OK"), any(), any(), eq(0L))
		verify(toadletContext).writeData(bucket.capture())
		verifyBucketContent(bucket.firstValue, byteArrayOf())
	}

	@Test
	fun `content can be sent as byte array`() {
		val toadlet = pageToadletAdapter.adapt("test.html", object : Page {
			override fun handleGet() = Response(200, content = from(byteArrayOf(0, 1, 2, 3, 4)))
		})
		toadlet.handleMethodGET(URI("test.html"), mock(), toadletContext)
		verify(toadletContext).sendReplyHeaders(eq(200), eq("OK"), any(), any(), eq(5L))
		verify(toadletContext).writeData(bucket.capture())
		verifyBucketContent(bucket.firstValue, byteArrayOf(0, 1, 2, 3, 4))
	}

	@Test
	fun `content can be sent as part of byte array`() {
		val toadlet = pageToadletAdapter.adapt("test.html", object : Page {
			override fun handleGet() = Response(200, content = from(byteArrayOf(0, 1, 2, 3, 4), 1, 3))
		})
		toadlet.handleMethodGET(URI("test.html"), mock(), toadletContext)
		verify(toadletContext).sendReplyHeaders(eq(200), eq("OK"), any(), any(), eq(3L))
		verify(toadletContext).writeData(bucket.capture())
		verifyBucketContent(bucket.firstValue, byteArrayOf(1, 2, 3))
	}

	@Test
	fun `content can be sent as input stream`() {
		val toadlet = pageToadletAdapter.adapt("test.html", object : Page {
			override fun handleGet() = Response(200, content = from(ByteArrayInputStream(byteArrayOf(0, 1, 2, 3, 4)), 5))
		})
		toadlet.handleMethodGET(URI("test.html"), mock(), toadletContext)
		verify(toadletContext).sendReplyHeaders(eq(200), eq("OK"), any(), any(), eq(5L))
		verify(toadletContext).writeData(bucket.capture())
		verifyBucketContent(bucket.firstValue, byteArrayOf(0, 1, 2, 3, 4))
	}

	private fun verifyBucketContent(bucket: Bucket, content: ByteArray) {
		content.forEach { byte: Byte ->
			assertThat(bucket.inputStream.read(), equalTo(byte.toInt()))
		}
		assertThat(bucket.inputStream.read(), equalTo(-1))
	}

	private val highLevelSimpleClient = mock<HighLevelSimpleClient>()
	private val pageToadletAdapter = PageToadletAdapter(highLevelSimpleClient)
	private val toadletContext = mock<ToadletContext>()
	private val bucket = argumentCaptor<Bucket>()

}
