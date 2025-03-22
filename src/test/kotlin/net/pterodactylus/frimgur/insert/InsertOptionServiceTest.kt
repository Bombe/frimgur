package net.pterodactylus.frimgur.insert

import kotlin.test.Test
import org.hamcrest.MatcherAssert.assertThat
import org.hamcrest.Matchers.equalTo

class InsertOptionServiceTest {

	@Test
	fun `default insert destination is CHK@`() {
		assertThat(insertOptionService.defaultInsertDestination, equalTo("CHK@"))
	}

	@Test
	fun `default insert destination retains value`() {
		insertOptionService.defaultInsertDestination = "SSK@"
		assertThat(insertOptionService.defaultInsertDestination, equalTo("SSK@"))
	}

	@Test
	fun `default image format is PNG`() {
		assertThat(insertOptionService.defaultImageFormat, equalTo("png"))
	}

	@Test
	fun `default image format can be changed`() {
		insertOptionService.defaultImageFormat = "jpeg"
		assertThat(insertOptionService.defaultImageFormat, equalTo("jpeg"))
	}

	private val insertOptionService = DefaultInsertOptionService()

}
