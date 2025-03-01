package net.pterodactylus.frimgur.web

import freenet.client.HighLevelSimpleClient
import freenet.clients.http.ToadletContext
import freenet.support.api.Bucket
import java.io.ByteArrayInputStream
import java.net.URI
import kotlin.test.Test
import org.hamcrest.MatcherAssert.assertThat
import org.hamcrest.Matchers.equalTo
import org.mockito.ArgumentMatchers.anyLong
import org.mockito.kotlin.any
import org.mockito.kotlin.argumentCaptor
import org.mockito.kotlin.eq
import org.mockito.kotlin.isNull
import org.mockito.kotlin.mock
import org.mockito.kotlin.verify

/**
 * Unit test for [FileToadlet].
 */
class FileToadletTest {

	@Test
	fun `file toadlet serves 404`() {
		val toadlet = createFileToadlet()
		toadlet.handleMethodGET(URI("test.dat"), mock(), toadletContext)
		verify(toadletContext).sendReplyHeaders(eq(404), eq("Not Found"), any(), isNull(), anyLong())
	}

	@Test
	fun `file is served correctly`() {
		val toadlet = createFileToadlet { testData }
		toadlet.handleMethodGET(URI("test.dat"), mock(), toadletContext)
		verify(toadletContext).sendReplyHeaders(eq(200), eq("OK"), any(), eq("application/x-test-data"), eq(10L))
		val bucket = argumentCaptor<Bucket>()
		verify(toadletContext).writeData(bucket.capture())
		verifyBucketContent(bucket.firstValue, ByteArray(10) { i -> i.toByte() })
	}

	@Test
	fun `path returns toadlet prefix`() {
		val toadlet = createFileToadlet()
		assertThat(toadlet.path(), equalTo("/test/"))
	}

	@Test
	fun `prefix is removed from requested URI`() {
		val toadlet = createFileToadlet { file -> testData.takeIf { file == "foo.dat" } }
		toadlet.handleMethodGET(URI("/test/foo.dat"), mock(), toadletContext)
		verify(toadletContext).sendReplyHeaders(eq(200), any(), any(), any(), anyLong())
	}

	@Test
	fun `file without content type can be served`() {
		val toadlet = createFileToadlet { testData.copy(mimeType = null) }
		toadlet.handleMethodGET(URI("/test/foo.dat"), mock(), toadletContext)
		verify(toadletContext).sendReplyHeaders(eq(200), any(), any(), isNull(), anyLong())
	}

	private fun createFileToadlet(fileData: (file: String) -> FileData? = { null }) =
		object : FileToadlet("/test/", highLevelSimpleClient) {
			override fun locateFileData(file: String) = fileData(file)
		}

	private fun verifyBucketContent(bucket: Bucket, content: ByteArray) {
		content.forEach { byte: Byte ->
			assertThat(bucket.inputStream.read(), equalTo(byte.toInt()))
		}
		assertThat(bucket.inputStream.read(), equalTo(-1))
	}

	private val highLevelSimpleClient = mock<HighLevelSimpleClient>()
	private val toadletContext = mock<ToadletContext>()

}

private val testData = FileData(10, "application/x-test-data", ByteArrayInputStream(ByteArray(10) { i -> i.toByte() }))
