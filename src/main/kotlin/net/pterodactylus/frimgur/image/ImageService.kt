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
	fun addImage(data: ByteArray): ImageMetadata? = null

	/**
	 * Returns metadata for the image with the given ID.
	 *
	 * @param id The ID of the image to get the metadata for
	 * @return The metadata for the image with the given ID,
	 * or `null` if no image with the given ID exists
	 */
	fun getImage(id: String): ImageMetadata? = null

	fun getImageData(id: String): ImageData? = null

	/**
	 * Returns the IDs of all images that are currently stored in this image service.
	 *
	 * @return The IDs of all stored images
	 */
	fun getImageIds(): List<String> = emptyList()

	/**
	 * Removes the image with the given ID from this image service.
	 *
	 * @param id The ID of the image to remove
	 */
	fun removeImage(id: String) = Unit

}

class DefaultImageService : ImageService {

	override fun addImage(data: ByteArray): ImageMetadata? =
		detectImageType(data)
			?.let { imageType ->
				ByteArrayInputStream(data).use { byteArrayInputStream ->
					ImageIO.read(byteArrayInputStream)
						?.let { bufferedImage -> ImageMetadata(UUID.randomUUID().toString(), bufferedImage.width, bufferedImage.height, data.size, imageType) }
						?.also { imageMetadata -> imageData[imageMetadata.id] = ImageData(imageMetadata, data.clone()) }
				}
			}

	private fun detectImageType(data: ByteArray): String? =
		ByteArrayInputStream(data).use { inputStream ->
			ImageIO.createImageInputStream(inputStream).use { imageInputStream ->
				ImageIO.getImageReaders(imageInputStream).nextOrNull()?.formatName.let {
					when (it) {
						"gif" -> "image/gif"
						"png" -> "image/png"
						"JPEG" -> "image/jpeg"
						"bmp" -> "image/bmp"
						else -> null
					}
				}
			}
		}

	override fun getImage(id: String): ImageMetadata? = imageData[id]?.metadata

	override fun getImageData(id: String): ImageData? = imageData[id]

	override fun getImageIds() = imageData.keys.toList()

	override fun removeImage(id: String) {
		imageData.remove(id)
	}

	private val imageData = mutableMapOf<String, ImageData>()

}

private fun <T> Iterator<T>.nextOrNull() = if (hasNext()) next() else null

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
	val size: Int,

	/** The MIME type of the image. */
	val mimeType: String = ""

)

/**
 * Data of an image, consisting of both its [metadata][ImageMetadata] and
 * its content.
 */
data class ImageData(val metadata: ImageMetadata, val data: ByteArray)
