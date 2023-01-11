package net.pterodactylus.frimgur.web

import org.hamcrest.MatcherAssert.assertThat
import org.hamcrest.Matchers.contains
import org.hamcrest.Matchers.emptyIterable
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
		assertThat(PebblePageProcessor("", "").processPage(PageRequest()).title, equalTo(""))
	}

	@Test
	fun `page processor uses title from object instance`() {
		assertThat(pebblePageProcessor.processPage(PageRequest()).title, equalTo("Test Title"))
	}

	@Test
	fun `page processor includes no script links`() {
		assertThat(pebblePageProcessor.processPage(PageRequest()).javascriptLinks, emptyIterable())
	}

	@Test
	fun `page processor includes script links`() {
		scriptLinks += "static/test1.js"
		scriptLinks += "static/test2.js"
		assertThat(pebblePageProcessor.processPage(PageRequest()).javascriptLinks, contains("static/test1.js", "static/test2.js"))
	}

	private val scriptLinks = mutableListOf<String>()
	private val pebblePageProcessor = object : PebblePageProcessor("static/html/test.html", "") {
		override fun getTitle(pageRequest: PageRequest) = "Test Title"
		override fun getScriptLinks(pageRequest: PageRequest) = scriptLinks
	}

}
