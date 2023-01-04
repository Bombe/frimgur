package net.pterodactylus.frimgur.web

import org.hamcrest.MatcherAssert.assertThat
import org.hamcrest.Matchers.equalTo
import kotlin.test.Test

/**
 * Unit test for [PebblePageProcessor].
 */
class PebblePageProcessorTest {

	@Test
	fun `page processor can render a pebble template`() {
		assertThat(pebblePageProcessor.processPage(PageRequest()).content, equalTo("Test.\n"))
	}

	@Test
	fun `default title is empty string`() {
		assertThat(PebblePageProcessor("").processPage(PageRequest()).title, equalTo(""))
	}

	@Test
	fun `page processor uses title from object instance`() {
		assertThat(pebblePageProcessor.processPage(PageRequest()).title, equalTo("Test Title"))
	}

	private val pebblePageProcessor = object : PebblePageProcessor("html/test.html") {
		override fun getTitle(pageRequest: PageRequest) = "Test Title"
	}

}
