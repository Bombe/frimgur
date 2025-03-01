package net.pterodactylus.frimgur.web

import freenet.client.HighLevelSimpleClient
import freenet.clients.http.ToadletContext
import java.net.URI
import kotlin.test.Test
import org.mockito.AdditionalMatchers.or
import org.mockito.kotlin.any
import org.mockito.kotlin.eq
import org.mockito.kotlin.isNull
import org.mockito.kotlin.mock
import org.mockito.kotlin.verify

/**
 * Unit test for [ClasspathFileToadlet].
 */
class ClasspathFileToadletTest {

	@Test
	fun `toadlet cannot serve files that are not there`() {
		val toadlet = ClasspathFileToadlet("/prefix/", "/server/", highLevelSimpleClient)
		toadlet.handleMethodGET(URI("/prefix/does-not-exist.dat"), mock(), toadletContext)
		verify(toadletContext).sendReplyHeaders(eq(404), any(), any(), or(any(), isNull()), any())
	}

	@Test
	fun `toadlet can serve file from classpath`() {
		val toadlet = ClasspathFileToadlet("/prefix/", "/server/", highLevelSimpleClient)
		toadlet.handleMethodGET(URI("/prefix/test.dat"), mock(), toadletContext)
		verify(toadletContext).sendReplyHeaders(eq(200), any(), any(), or(any(), isNull()), any())
	}

	@Test
	fun `toadlet can serve mime types for specified file extensions`() {
		val toadlet = ClasspathFileToadlet("/prefix/", "/server/", highLevelSimpleClient)
		toadlet.addMimeType("application/x-test-data") { filename: String -> filename.endsWith(".dat", ignoreCase = true) }
		toadlet.handleMethodGET(URI("/prefix/test.dat"), mock(), toadletContext)
		verify(toadletContext).sendReplyHeaders(eq(200), any(), any(), eq("application/x-test-data"), any())
	}

	private val highLevelSimpleClient = mock<HighLevelSimpleClient>()
	private val toadletContext = mock<ToadletContext>()

}
