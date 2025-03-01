package net.pterodactylus.frimgur.web.annotations

import kotlin.test.Test
import net.pterodactylus.frimgur.web.PageProcessor
import net.pterodactylus.frimgur.web.PageRequest
import net.pterodactylus.frimgur.web.PageResponse
import org.hamcrest.MatcherAssert.assertThat
import org.hamcrest.Matchers.equalTo

/**
 * Unit test for [ToadletName]-related functionality.
 */
class ToadletNameTest {

	@Test
	fun `toadlet name can be extracted`() {
		@ToadletName("test.html")
		class TestPageProcessor : PageProcessor {
			override fun processPage(pageRequest: PageRequest): PageResponse = TODO("Not important")
		}
		assertThat(TestPageProcessor().toadletName, equalTo("test.html"))
	}

}
