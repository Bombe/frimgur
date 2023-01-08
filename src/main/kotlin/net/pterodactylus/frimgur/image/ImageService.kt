package net.pterodactylus.frimgur.image

import java.io.ByteArrayInputStream
import java.util.UUID
import javax.imageio.ImageIO

/**
 * Service for image-related functionality.
 */
interface ImageService {

	/**
	 * Decodes the given image, stores it, and returns its metadata.
	 *
	 * @param type The MIME type of the image
	 * @param data The encoded image
	 * @return The metadata parsed from the given image data
	 */
	fun addImage(type: String, data: ByteArray): ImageMetadata

	/**
	 * Returns metadata for the image with the given ID.
	 *
	 * @param id The ID of the image to get the metadata for
	 * @return The metadata for the image with the given ID,
	 * or `null` if no image with the given ID exists
	 */
	fun getImage(id: String): ImageMetadata?

}

class DefaultImageService : ImageService {

	override fun addImage(type: String, data: ByteArray): ImageMetadata {
		val bufferedImage = ImageIO.read(ByteArrayInputStream(data))
		return ImageMetadata(UUID.randomUUID().toString(), bufferedImage.width, bufferedImage.height, data.size)
	}

	override fun getImage(id: String): ImageMetadata? {
		TODO("Not yet implemented")
	}

}

/**
 * Metadata of an image.
 */
data class ImageMetadata(

	/** The ID of the image. */
	val id: String,

	/** The width of the image in pixels. */
	val width: Int,

	/** The height of the image in pixels. */
	val height: Int,

	/** The size of the encoded image in bytes. */
	val size: Int

)
