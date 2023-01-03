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
import org.mockito.kotlin.isNull
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
		val toadlet = createToadletWithResponse(::Response)
		assertThat(toadlet.path(), equalTo("test.html"))
	}

	@Test
	fun `returned toadlet forwards request to page`() {
		val handleGetCalled = AtomicReference(false)
		val toadlet = createToadletWithResponse { Response().also { handleGetCalled.set(true) } }
		toadlet.handleMethodGET(URI("test.html"), mock(), mock())
		assertThat(handleGetCalled.get(), equalTo(true))
	}

	@Test
	fun `returned response is handed back to toadlet container`() {
		val toadlet = createToadletWithResponse { Response(123) }
		toadlet.handleMethodGET(URI("test.html"), mock(), toadletContext)
		verify(toadletContext).sendReplyHeaders(eq(123), any(), any(), isNull(), anyLong())
	}

	@Test
	fun `return code 500 has correct reason phrase`() {
		val toadlet = createToadletWithResponse { Response(500) }
		toadlet.handleMethodGET(URI("test.html"), mock(), toadletContext)
		verify(toadletContext).sendReplyHeaders(eq(500), eq("Internal Server Error"), any(), isNull(), anyLong())
	}

	@Test
	fun `reason phrase can be overwritten`() {
		val toadlet = createToadletWithResponse { Response(123, reason = "Test Reason") }
		toadlet.handleMethodGET(URI("test.html"), mock(), toadletContext)
		verify(toadletContext).sendReplyHeaders(eq(123), eq("Test Reason"), any(), isNull(), anyLong())
	}

	@Test
	fun `no content is written correctly`() {
		val toadlet = createToadletWithResponse { Response(200) }
		toadlet.handleMethodGET(URI("test.html"), mock(), toadletContext)
		verify(toadletContext).sendReplyHeaders(eq(200), eq("OK"), any(), isNull(), eq(0L))
		verify(toadletContext).writeData(bucket.capture())
		verifyBucketContent(bucket.firstValue, byteArrayOf())
	}

	@Test
	fun `content can be sent as byte array`() {
		val toadlet = createToadletWithResponse { Response(200, content = from(byteArrayOf(0, 1, 2, 3, 4))) }
		toadlet.handleMethodGET(URI("test.html"), mock(), toadletContext)
		verify(toadletContext).sendReplyHeaders(eq(200), eq("OK"), any(), isNull(), eq(5L))
		verify(toadletContext).writeData(bucket.capture())
		verifyBucketContent(bucket.firstValue, byteArrayOf(0, 1, 2, 3, 4))
	}

	@Test
	fun `content can be sent as part of byte array`() {
		val toadlet = createToadletWithResponse { Response(200, content = from(byteArrayOf(0, 1, 2, 3, 4), 1, 3)) }
		toadlet.handleMethodGET(URI("test.html"), mock(), toadletContext)
		verify(toadletContext).sendReplyHeaders(eq(200), eq("OK"), any(), isNull(), eq(3L))
		verify(toadletContext).writeData(bucket.capture())
		verifyBucketContent(bucket.firstValue, byteArrayOf(1, 2, 3))
	}

	@Test
	fun `content can be sent as input stream`() {
		val toadlet = createToadletWithResponse { Response(200, content = from(ByteArrayInputStream(byteArrayOf(0, 1, 2, 3, 4)), 5)) }
		toadlet.handleMethodGET(URI("test.html"), mock(), toadletContext)
		verify(toadletContext).sendReplyHeaders(eq(200), eq("OK"), any(), isNull(), eq(5L))
		verify(toadletContext).writeData(bucket.capture())
		verifyBucketContent(bucket.firstValue, byteArrayOf(0, 1, 2, 3, 4))
	}

	@Test
	fun `content without content type is sent without content type`() {
		val toadlet = createToadletWithResponse { Response(200) }
		toadlet.handleMethodGET(URI("test.html"), mock(), toadletContext)
		verify(toadletContext).sendReplyHeaders(eq(200), eq("OK"), any(), isNull(), eq(0L))
	}

	@Test
	fun `content with content type is sent as the given content type`() {
		val toadlet = createToadletWithResponse { Response(200, content = from(ByteArrayInputStream(byteArrayOf(0, 1, 2, 3, 4)), 5).typed("application/octet-stream")) }
		toadlet.handleMethodGET(URI("test.html"), mock(), toadletContext)
		verify(toadletContext).sendReplyHeaders(eq(200), eq("OK"), any(), eq("application/octet-stream"), eq(5L))
	}

	private fun createToadletWithResponse(response: () -> Response) =
		pageToadletAdapter.adapt("test.html", object : Page {
			override fun handleGet() = response()
		})

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
