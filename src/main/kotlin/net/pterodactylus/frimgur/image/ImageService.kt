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
	 * @param data The encoded image
	 * @return The metadata parsed from the given image data,
	 * or `null` if the image cannot be parsed
	 */
	fun addImage(data: ByteArray): ImageMetadata?

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

	override fun addImage(data: ByteArray): ImageMetadata? =
		ImageIO.read(ByteArrayInputStream(data))
			?.let { bufferedImage -> ImageMetadata(UUID.randomUUID().toString(), bufferedImage.width, bufferedImage.height, data.size) }
			?.also { imageMetadata -> imageData[imageMetadata.id] = ImageData(imageMetadata, data.clone()) }

	override fun getImage(id: String): ImageMetadata? = imageData[id]?.metadata

	private val imageData = mutableMapOf<String, ImageData>()

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

/**
 * Data of an image, consisting of both its [metadata][ImageMetadata] and
 * its content.
 */
data class ImageData(val metadata: ImageMetadata, val data: ByteArray)
