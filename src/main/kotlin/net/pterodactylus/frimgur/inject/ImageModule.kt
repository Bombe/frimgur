package net.pterodactylus.frimgur.inject

import com.google.inject.AbstractModule
import com.google.inject.Provides
import net.pterodactylus.frimgur.image.DefaultImageService
import net.pterodactylus.frimgur.image.ImageService
import javax.inject.Singleton

/**
 * Guice [com.google.inject.Module] implementation that provides image-related functionality.
 */
class ImageModule : AbstractModule() {

	@Provides
	@Singleton
	fun getImageService(): ImageService =
		DefaultImageService()

}
