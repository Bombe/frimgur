package net.pterodactylus.frimgur.inject

import com.google.inject.Guice.createInjector
import net.pterodactylus.frimgur.image.DefaultImageService
import net.pterodactylus.frimgur.image.ImageService
import org.hamcrest.MatcherAssert.assertThat
import org.hamcrest.Matchers.instanceOf
import kotlin.test.Test

/**
 * Unit test for [ImageModule].
 */
class ImageModuleTest {

	@Test
	fun `image module can create an image service`() {
		assertThat(injector.getInstance(ImageService::class.java), instanceOf(DefaultImageService::class.java))
	}

	private val imageModule = ImageModule()
	private val injector = createInjector(imageModule)

}
