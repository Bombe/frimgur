package net.pterodactylus.frimgur.insert

import kotlin.test.Test
import org.hamcrest.MatcherAssert.assertThat
import org.hamcrest.Matchers.equalTo

class InsertOptionServiceTest {

	private val insertOptionService = DefaultInsertOptionService()

	@Test
	fun `default insert destination is CHK@`() {
		val insertOptionService = insertOptionService
		assertThat(insertOptionService.defaultInsertDestination, equalTo("CHK@"))
	}

	@Test
	fun `default insert destination retains value`() {
		insertOptionService.defaultInsertDestination = "SSK@"
		assertThat(insertOptionService.defaultInsertDestination, equalTo("SSK@"))
	}

}
