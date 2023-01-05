package net.pterodactylus.frimgur.web

import net.pterodactylus.frimgur.web.annotations.toadletName
import org.hamcrest.MatcherAssert.assertThat
import org.hamcrest.Matchers.containsString
import org.hamcrest.Matchers.equalTo
import kotlin.test.Test

/**
 * Unit test for [MainPageProcessor].
 */
class MainPageProcessorTest {

	@Test
	fun `main page has correct template name`() {
		assertThat(mainPageProcessor.toadletName, equalTo("index.html"))
	}

	@Test
	fun `main page has title 'Frimgur Main Page'`() {
		assertThat(mainPageProcessor.processPage(PageRequest()).title, equalTo("Frimgur Main Page"))
	}

	@Test
	fun `main page content contains the word 'Frimgur'`() {
		assertThat(mainPageProcessor.processPage(PageRequest()).content, containsString("Frimgur"))
	}

	private val mainPageProcessor = MainPageProcessor()

}
