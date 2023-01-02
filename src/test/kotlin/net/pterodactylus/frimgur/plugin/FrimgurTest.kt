package net.pterodactylus.frimgur.plugin

import com.google.inject.Guice
import freenet.l10n.BaseL10n
import freenet.pluginmanager.FredPlugin
import freenet.pluginmanager.FredPluginL10n
import freenet.pluginmanager.FredPluginThreadless
import freenet.pluginmanager.PluginRespirator
import net.pterodactylus.frimgur.test.bind
import net.pterodactylus.frimgur.web.WebInterface
import org.hamcrest.MatcherAssert.assertThat
import org.hamcrest.Matchers.equalTo
import org.hamcrest.Matchers.instanceOf
import org.mockito.Mockito.RETURNS_DEEP_STUBS
import org.mockito.kotlin.mock
import java.util.concurrent.atomic.AtomicBoolean
import kotlin.test.Test

/**
 * Unit test for [Frimgur].
 */
class FrimgurTest {

	@Test
	fun `Frimgur instance can be run`() {
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

	@Test
	fun `Frimgur starts the web interface`() {
		val started = AtomicBoolean(false)
		val webInterface = object : WebInterface {
			override fun start() =
				started.set(true)
		}
		val frimgur = object : Frimgur() {
			override fun createInjector() = Guice.createInjector(
				bind<WebInterface>().toInstance(webInterface)
			)
		}
		frimgur.runPlugin(pluginRespirator)
		assertThat(started.get(), equalTo(true))
	}

	private val frimgur = Frimgur()
	private val pluginRespirator = mock<PluginRespirator>(defaultAnswer = RETURNS_DEEP_STUBS)

}
