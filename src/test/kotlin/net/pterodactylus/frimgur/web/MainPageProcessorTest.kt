package net.pterodactylus.frimgur.web

import java.util.Locale.ENGLISH
import kotlin.test.Test
import net.pterodactylus.frimgur.insert.DefaultInsertOptionService
import net.pterodactylus.frimgur.web.annotations.toadletName
import org.hamcrest.MatcherAssert.assertThat
import org.hamcrest.Matchers.contains
import org.hamcrest.Matchers.containsString
import org.hamcrest.Matchers.equalTo
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

	@Test
	fun `processor include insert options in context if default destination is CHK@`() {
		insertOptionService.defaultInsertDestination = "CHK@"
		assertThat(mainPageProcessor.processPage(PageRequest()).content, containsString("value='insert-to-chk' checked"))
	}

	@Test
	fun `processor include insert options in context if default destination is SSK@`() {
		insertOptionService.defaultInsertDestination = "SSK@"
		assertThat(mainPageProcessor.processPage(PageRequest()).content, containsString("value='insert-to-ssk' checked"))
	}

	private var nodeRequiresConfigChange = false
	private var insertOptionService = DefaultInsertOptionService()
	private val mainPageProcessor = MainPageProcessor(insertOptionService, "", { ENGLISH }, { nodeRequiresConfigChange })

}
