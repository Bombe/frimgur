package net.pterodactylus.frimgur.inject

import com.google.inject.AbstractModule
import com.google.inject.Provides
import net.pterodactylus.frimgur.web.ImageMetadata
import net.pterodactylus.frimgur.web.ImageService
import java.util.UUID.randomUUID

/**
 * Guice [com.google.inject.Module] implementation that provides image-related functionality.
 */
class ImageModule : AbstractModule() {

	@Provides
	fun getImageService(): ImageService =
		object : ImageService {
			override fun addImage(type: String, data: ByteArray) = ImageMetadata(randomUUID().toString(), 1, 2, 3)
		}

}
