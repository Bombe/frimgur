package net.pterodactylus.frimgur.web

import freenet.client.HighLevelSimpleClient
import freenet.clients.http.ToadletContext
import freenet.support.MultiValueTable
import org.hamcrest.MatcherAssert
import org.hamcrest.MatcherAssert.assertThat
import org.hamcrest.Matchers.equalTo
import org.mockito.ArgumentMatcher
import org.mockito.ArgumentMatchers.anyLong
import org.mockito.ArgumentMatchers.argThat
import org.mockito.kotlin.eq
import org.mockito.kotlin.isNull
import org.mockito.kotlin.mock
import org.mockito.kotlin.verify
import java.net.URI
import kotlin.test.Test

/**
 * Unit test for [RedirectToadlet].
 */
class RedirectToadletTest {

	@Test
	fun `toadlet sends redirect to target URI`() {
		val redirectToadlet = RedirectToadlet("/test/", "/test/index.html", highLevelSimpleClient)
		val toadletContext = mock<ToadletContext>()
		redirectToadlet.handleMethodGET(URI("/test/"), mock(), toadletContext)
		verify(toadletContext).sendReplyHeaders(eq(303), eq("See Other"), argThat(containsHeader("location", "/test/index.html")), isNull(), anyLong())
	}

	@Test
	fun `toadlet returns given path`() {
		val redirectToadlet = RedirectToadlet("/test/", "/test/index.html", highLevelSimpleClient)
		assertThat(redirectToadlet.path(), equalTo("/test/"))
	}

	private val highLevelSimpleClient = mock<HighLevelSimpleClient>()

}

fun containsHeader(name: String, value: String) = ArgumentMatcher<MultiValueTable<String, String>> { headers ->
	headers.keys().asSequence().firstOrNull { it.equals(name, ignoreCase = true) }
		?.let { headers[it].equals(value) }
		?: false
}
