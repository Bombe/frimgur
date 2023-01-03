package net.pterodactylus.frimgur.web

import org.hamcrest.MatcherAssert.assertThat
import org.hamcrest.Matchers.equalTo
import kotlin.test.Test

/**
 * Unit test for [page][Page]-related functionality.
 */
class PageTest {

	@Test
	fun `invalid code has reason 'unknown'`() {
		assertThat(getReasonForStatus(12345), equalTo("Unknown"))
	}

	@Test
	fun `status code 200 has reason 'ok'`() {
		assertThat(getReasonForStatus(200), equalTo("OK"))
	}

	@Test
	fun `status code 500 has reason 'internal server error`() {
		assertThat(getReasonForStatus(500), equalTo("Internal Server Error"))
	}

}
