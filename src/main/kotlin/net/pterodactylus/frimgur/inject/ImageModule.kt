package net.pterodactylus.frimgur.inject

import com.google.inject.AbstractModule
import com.google.inject.Provides
import net.pterodactylus.frimgur.image.ImageMetadata
import net.pterodactylus.frimgur.image.ImageService
import java.util.UUID.randomUUID

/**
 * Guice [com.google.inject.Module] implementation that provides image-related functionality.
 */
class ImageModule : AbstractModule() {

	@Provides
	fun getImageService(): ImageService =
		object : ImageService {
			override fun getImage(id: String) = null
			override fun addImage(data: ByteArray) = ImageMetadata(randomUUID().toString(), 1, 2, 3)
		}

}
