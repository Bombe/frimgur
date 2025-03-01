package net.pterodactylus.frimgur.util

import kotlin.test.Test
import org.hamcrest.MatcherAssert.assertThat
import org.hamcrest.Matchers.equalTo

/**
 * Unit test for [VersionProperties].
 */
class VersionPropertiesTest {

	@Test
	fun `version properties are parsed from resources`() {
		val versionProperties = loadVersionProperties("version.properties")
		assertThat(versionProperties.version, equalTo("1.2.3-45-g67890a.dirty"))
		assertThat(versionProperties.hash, equalTo("0123456789abcdef0123456789abcdef01234567"))
	}

	@Test
	fun `version properties from missing source are unknown`() {
		val versionProperties = loadVersionProperties("missing.properties")
		assertThat(versionProperties.version, equalTo("unknown"))
		assertThat(versionProperties.hash, equalTo("unknown"))
	}

}
