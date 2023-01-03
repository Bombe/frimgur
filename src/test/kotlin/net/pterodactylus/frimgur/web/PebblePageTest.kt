package net.pterodactylus.frimgur.web

import org.hamcrest.MatcherAssert.assertThat
import org.hamcrest.Matchers.equalTo
import kotlin.test.Test

/**
 * Unit test for [PebblePage].
 */
class PebblePageTest {

	@Test
	fun `get request return text-html as content type`() {
		assertThat(response.content.contentType, equalTo("text/html"))
	}

	@Test
	fun `get request renders template`() {
		assertThat(response.content.toInputStream().readBytes(), equalTo("Test.\n".toByteArray()))
	}

	private val page = PebblePage("html/test.html")
	private val response = page.handleGet()

}
