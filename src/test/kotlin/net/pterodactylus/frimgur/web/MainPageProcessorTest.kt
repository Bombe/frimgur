package net.pterodactylus.frimgur.web

import net.pterodactylus.frimgur.web.annotations.toadletName
import org.hamcrest.MatcherAssert.assertThat
import org.hamcrest.Matchers.contains
import org.hamcrest.Matchers.containsString
import org.hamcrest.Matchers.equalTo
import java.util.Locale.ENGLISH
import kotlin.test.Test
import org.hamcrest.Matchers.not

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

	@Test
	fun `main page adds a script link`() {
		assertThat(mainPageProcessor.processPage(PageRequest()).javascriptLinks, contains("static/js/frimgur.js"))
	}

	@Test
	fun `main page reacts on change in variable`() {
		assertThat(mainPageProcessor.processPage(PageRequest()).content, not(containsString("node-requires-config-change")))
		nodeRequiresConfigChange = true
		assertThat(mainPageProcessor.processPage(PageRequest()).content, containsString("node-requires-config-change"))
	}

	private var nodeRequiresConfigChange = false
	private val mainPageProcessor = MainPageProcessor("", { ENGLISH }, { nodeRequiresConfigChange })

}
