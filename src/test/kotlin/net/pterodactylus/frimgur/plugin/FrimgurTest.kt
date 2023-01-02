package net.pterodactylus.frimgur.plugin

import freenet.pluginmanager.FredPlugin
import freenet.pluginmanager.PluginRespirator
import org.hamcrest.MatcherAssert.assertThat
import org.hamcrest.Matchers.instanceOf
import org.mockito.kotlin.mock
import kotlin.test.Test

/**
 * Unit test for [Frimgur].
 */
class FrimgurTest {

	@Test
	fun `can create Frimgur instance`() {
		Frimgur()
	}

	@Test
	fun `Frimgur instance can be run`() {
		val pluginRespirator = mock<PluginRespirator>()
		val frimgur = Frimgur()
		frimgur.runPlugin(pluginRespirator)
	}

	@Test
	fun `Frimgur instance can be terminated`() {
		val frimgur = Frimgur()
		frimgur.terminate()
	}

	@Test
	fun `Frimgur implements FredPlugin`() {
		assertThat(Frimgur(), instanceOf(FredPlugin::class.java))
	}

}
