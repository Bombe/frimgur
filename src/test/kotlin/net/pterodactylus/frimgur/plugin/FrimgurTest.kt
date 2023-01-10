package net.pterodactylus.frimgur.plugin

import com.google.inject.Injector
import com.google.inject.Module
import com.google.inject.util.Modules.override
import freenet.l10n.BaseL10n
import freenet.pluginmanager.FredPlugin
import freenet.pluginmanager.FredPluginL10n
import freenet.pluginmanager.FredPluginThreadless
import freenet.pluginmanager.PluginRespirator
import net.pterodactylus.frimgur.image.ImageService
import net.pterodactylus.frimgur.image.ImageStatus
import net.pterodactylus.frimgur.image.ImageStatus.Inserting
import net.pterodactylus.frimgur.image.get1x1Png
import net.pterodactylus.frimgur.insert.InsertService
import net.pterodactylus.frimgur.test.bind
import net.pterodactylus.frimgur.util.getInstance
import net.pterodactylus.frimgur.web.WebInterface
import org.hamcrest.Description
import org.hamcrest.Matcher
import org.hamcrest.MatcherAssert.assertThat
import org.hamcrest.Matchers.contains
import org.hamcrest.Matchers.equalTo
import org.hamcrest.Matchers.instanceOf
import org.hamcrest.TypeSafeDiagnosingMatcher
import org.mockito.Mockito.RETURNS_DEEP_STUBS
import org.mockito.kotlin.mock
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
		val insertImageArguments = mutableListOf<Triple<String, ByteArray, String>>()
		val insertService = object : InsertService {
			override fun insertImage(id: String, data: ByteArray, mimeType: String) {
				insertImageArguments += Triple(id, data, mimeType)
			}
		}
		runPlugin(bind<InsertService>().toInstance(insertService)) { injector ->
			val imageService = injector.getInstance<ImageService>()
			val metadata = imageService.addImage(testImage)!!
			assertThat(insertImageArguments, contains(isTriple(equalTo(metadata.id), equalTo(testImage), equalTo("image/png"))))
		}
	}

	@Test
	fun `image service is called to set status to inserting when insert starts`() {
		val imageStatusSet = mutableListOf<Pair<String, ImageStatus>>()
		val imageService = object : ImageService {
			override fun setImageStatus(id: String, status: ImageStatus) {
				imageStatusSet += id to status
			}
		}
		runPlugin(bind<ImageService>().toInstance(imageService)) { injector ->
			val insertService: InsertService = injector.getInstance()
			insertService.insertImage("id1", byteArrayOf(), "image/test")
			assertThat(imageStatusSet, contains("id1" to Inserting))
		}
	}

	private fun runPlugin(vararg overrideModules: Module, action: (Injector) -> Unit) {
		val injector = AtomicReference<Injector>()
		val frimgur = object : Frimgur() {
			override fun createInjector() = super.createInjector().also(injector::set)
			override fun getModules() = listOf(
				override(super.getModules()).with(*overrideModules)
			)
		}
		frimgur.runPlugin(pluginRespirator)
		action(injector.get())
	}

	private val frimgur = Frimgur()
	private val pluginRespirator = mock<PluginRespirator>(defaultAnswer = RETURNS_DEEP_STUBS).apply {
		whenever(toadletContainer.formPassword).thenReturn("123")
	}

}

private fun <F, S, T> isTriple(first: Matcher<F>, second: Matcher<S>, third: Matcher<T>) = object : TypeSafeDiagnosingMatcher<Triple<F, S, T>>() {

	override fun matchesSafely(triple: Triple<F, S, T>, mismatchDescription: Description): Boolean {
		if (!first.matches(triple.first)) {
			mismatchDescription.appendText("first was ").appendValue(triple.first)
			return false
		}
		if (!second.matches(triple.second)) {
			mismatchDescription.appendText("second was ").appendValue(triple.second)
			return false
		}
		if (!third.matches(triple.third)) {
			mismatchDescription.appendText("third was ").appendValue(triple.third)
			return false
		}
		return true
	}

	override fun describeTo(description: Description) {
		description.appendText("triple of ").appendDescriptionOf(first)
			.appendText(", ").appendDescriptionOf(second)
			.appendText(", and ").appendDescriptionOf(third)
	}

}

private val testImage = get1x1Png()
