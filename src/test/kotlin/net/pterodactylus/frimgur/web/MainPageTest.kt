package net.pterodactylus.frimgur.web

import org.hamcrest.MatcherAssert.assertThat
import org.hamcrest.Matchers.containsString
import org.hamcrest.Matchers.equalTo
import kotlin.test.Test

/**
 * Unit test for [MainPage].
 */
class MainPageTest {

	@Test
	fun `main page returns error code 200`() {
		val page = MainPage()
		val result = page.handleGet()
		assertThat(result.code, equalTo(200))
	}

	@Test
	fun `main page returns a page containing the word 'Frimgur'`() {
		val page = MainPage()
		val result = page.handleGet()
		assertThat(result.content.toInputStream().readBytes().decodeToString(), containsString("Frimgur"))
	}

}
