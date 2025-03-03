package net.pterodactylus.frimgur.inject

import com.google.inject.Guice.createInjector
import kotlin.test.Test
import net.pterodactylus.frimgur.image.DefaultImageService
import net.pterodactylus.frimgur.image.ImageService
import org.hamcrest.MatcherAssert.assertThat
import org.hamcrest.Matchers.instanceOf
import org.hamcrest.Matchers.sameInstance

/**
 * Unit test for [ImageModule].
 */
class ImageModuleTest {

	@Test
	fun `image module can create an image service`() {
		assertThat(injector.getInstance(ImageService::class.java), instanceOf(DefaultImageService::class.java))
	}

	@Test
	fun `image service is provided as singleton`() {
		val firstInstance = injector.getInstance(ImageService::class.java)
		val secondInstance = injector.getInstance(ImageService::class.java)
		assertThat(firstInstance, sameInstance(secondInstance))
	}

	private val imageModule = ImageModule()
	private val injector = createInjector(imageModule)

}
