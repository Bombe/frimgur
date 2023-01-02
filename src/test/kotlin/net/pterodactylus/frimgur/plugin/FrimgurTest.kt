package net.pterodactylus.frimgur.plugin

import freenet.l10n.BaseL10n
import freenet.pluginmanager.FredPlugin
import freenet.pluginmanager.FredPluginL10n
import freenet.pluginmanager.FredPluginThreadless
import freenet.pluginmanager.PluginRespirator
import org.hamcrest.MatcherAssert.assertThat
import org.hamcrest.Matchers.equalTo
import org.hamcrest.Matchers.instanceOf
import org.mockito.kotlin.mock
import kotlin.test.Test

/**
 * Unit test for [Frimgur].
 */
class FrimgurTest {

	@Test
	fun `Frimgur instance can be run`() {
		val pluginRespirator = mock<PluginRespirator>()
		frimgur.runPlugin(pluginRespirator)
	}

	@Test
	fun `Frimgur instance can be terminated`() {
		frimgur.terminate()
	}

	@Test
	fun `Frimgur implements FredPlugin`() {
		assertThat(frimgur, instanceOf(FredPlugin::class.java))
	}

	@Test
	fun `Frimgur can deliver translated keys for English language`() {
		frimgur.setLanguage(BaseL10n.LANGUAGE.ENGLISH);
		assertThat(frimgur.getString("Test.Message"), equalTo("Test Message"))
	}

	@Test
	fun `Frimgur can deliver translated keys for German language`() {
		frimgur.setLanguage(BaseL10n.LANGUAGE.GERMAN);
		assertThat(frimgur.getString("Test.Message"), equalTo("Testnachricht"))
	}

	@Test
	fun `Frimgur implements FredPluginL10n`() {
		assertThat(frimgur, instanceOf(FredPluginL10n::class.java))
	}

	@Test
	fun `Frimgur implements FredPluginThreadless`() {
		assertThat(frimgur, instanceOf(FredPluginThreadless::class.java))
	}

	private val frimgur = Frimgur()

}
