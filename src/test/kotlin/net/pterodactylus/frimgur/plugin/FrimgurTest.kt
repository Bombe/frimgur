package net.pterodactylus.frimgur.plugin

import com.google.inject.Injector
import com.google.inject.util.Modules.override
import freenet.l10n.BaseL10n
import freenet.pluginmanager.FredPlugin
import freenet.pluginmanager.FredPluginL10n
import freenet.pluginmanager.FredPluginThreadless
import freenet.pluginmanager.PluginRespirator
import net.pterodactylus.frimgur.image.ImageService
import net.pterodactylus.frimgur.image.get1x1Png
import net.pterodactylus.frimgur.insert.InsertService
import net.pterodactylus.frimgur.test.bind
import net.pterodactylus.frimgur.util.getInstance
import net.pterodactylus.frimgur.web.WebInterface
import org.hamcrest.MatcherAssert.assertThat
import org.hamcrest.Matchers.equalTo
import org.hamcrest.Matchers.instanceOf
import org.mockito.Mockito.RETURNS_DEEP_STUBS
import org.mockito.kotlin.eq
import org.mockito.kotlin.mock
import org.mockito.kotlin.verify
import org.mockito.kotlin.whenever
import java.util.concurrent.atomic.AtomicBoolean
import java.util.concurrent.atomic.AtomicReference
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
			override fun start() = started.set(true)
		}
		val frimgur = object : Frimgur() {
			override fun getModules() = listOf(
				override(super.getModules()).with(
					bind<WebInterface>().toInstance(webInterface)
				)
			)
		}
		frimgur.runPlugin(pluginRespirator)
		assertThat(started.get(), equalTo(true))
	}

	@Test
	fun `terminating Frimgur stops the web interface`() {
		val stopped = AtomicBoolean(false)
		val webInterface = object : WebInterface {
			override fun stop() = stopped.set(true)
		}
		val frimgur = object : Frimgur() {
			override fun getModules() = listOf(
				override(super.getModules()).with(
					bind<WebInterface>().toInstance(webInterface)
				)
			)
		}
		frimgur.runPlugin(pluginRespirator)
		frimgur.terminate()
		assertThat(stopped.get(), equalTo(true))
	}

	@Test
	fun `insert service is wired up as event listener for new images`() {
		val insertService = mock<InsertService>()
		val injector = AtomicReference<Injector>()
		val frimgur = object : Frimgur() {
			override fun createInjector() = super.createInjector().also(injector::set)
			override fun getModules() = listOf(
				override(super.getModules()).with(
					bind<InsertService>().toInstance(insertService)
				)
			)
		}
		frimgur.runPlugin(pluginRespirator)
		val imageService = injector.get().getInstance<ImageService>()
		val metadata = imageService.addImage(testImage)
		verify(insertService).insertImage(eq(metadata!!.id), eq(testImage), eq("image/png"))
	}

	private val frimgur = Frimgur()
	private val pluginRespirator = mock<PluginRespirator>(defaultAnswer = RETURNS_DEEP_STUBS).apply {
		whenever(toadletContainer.formPassword).thenReturn("123")
	}

}

private val testImage = get1x1Png()
