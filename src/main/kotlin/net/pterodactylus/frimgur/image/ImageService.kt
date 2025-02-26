package net.pterodactylus.frimgur.image

import net.pterodactylus.frimgur.image.ImageStatus.Inserting
import java.io.ByteArrayInputStream
import java.util.UUID
import javax.imageio.ImageIO
import net.pterodactylus.frimgur.image.ImageStatus.Inserted
import net.pterodactylus.frimgur.image.ImageStatus.Waiting
import java.awt.Graphics
import java.awt.image.BufferedImage
import java.io.ByteArrayOutputStream

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
	 * Creates a new image from the image with the given ID, optionally changing its encoding type or dimensions.
	 * The dimensions are handled as follows:
	 *
	 * - If both [width] and [height] are non-`null`, they are used as is, ignoring the resulting aspect ratio.
	 * - If both [width] and [height] are `null`, the dimensions of the image are not changed.
	 * - Otherwise, the dimension that is `null` will be calculated from the given dimension and the aspect ratio of the original image.
	 * E.g. when cloning an image that is 1920x1080 pixels giving a [width] of 640 and a [height] of `null`  will result in an image that is 640x360 pixels.
	 *
	 * @param id The ID of the image to clone
	 * @param mimeType The new MIME type of the image (or `null` to not change the type)
	 * @param width The new width of the image (or `null`)
	 * @param height The new height of the image (or `null`)
	 * @return The [ImageMetadata] of the newly created image, or `null` if no image was created
	 */
	fun cloneImage(id: String, mimeType: String? = null, width: Int? = null, height: Int? = null): ImageMetadata? = null;

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
	 * Sets the status of the image with the given ID.
	 *
	 * @param id The ID of the image to set the status of
	 * @param status The new status of the image
	 */
	fun setImageStatus(id: String, status: ImageStatus) = Unit

	/**
	 * Sets the filename that will be associated with an image on insert.
	 * The filename can only be changed while the image’s status is [Waiting].
	 *
	 * @param id The ID of the image
	 * @param filename The new filename for the image
	 */
	fun setImageFilename(id: String, filename: String) = Unit

	/**
	 * Sets the key of the image with the given ID.
	 *
	 * @param id The ID of the image to set the key of
	 * @param key The new key of the image
	 */
	fun setImageKey(id: String, key: String) = Unit

	/**
	 * Returns the IDs of all images that are currently stored in this image service.
	 * The returned list is sorted by time of [image addition][addImage], oldest
	 * image ID first.
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

	/**
	 * Adds a listener that gets notified when an image has been [added][addImage].
	 *
	 * @param listener The listener to notifies on new images
	 */
	fun onNewImage(listener: (imageMetadata: ImageData) -> Unit) = Unit

	/**
	 * Adds a listener that gets notified when an image’s status has been set to [Inserting].
	 *
	 * @param listener The listener to notifies on images changing status to [Inserting]
	 */
	fun onImageInserting(listener: (imageData: ImageData) -> Unit) = Unit

}

class DefaultImageService : ImageService {

	override fun addImage(data: ByteArray): ImageMetadata? =
		detectImageType(data)
			?.let { imageType ->
				ByteArrayInputStream(data).use { byteArrayInputStream ->
					ImageIO.read(byteArrayInputStream)
						?.let { bufferedImage -> ImageMetadata(UUID.randomUUID().toString(), bufferedImage.width, bufferedImage.height, data.size, imageType, createFilenameForMimeType(imageType), Waiting) }
						?.let { imageMetadata -> ImageData(imageMetadata, data) }
						?.also { imageData -> this.imageData[imageData.metadata.id] = imageData }
						?.also { imageData -> newImageListeners.forEach { listener -> listener(imageData) } }
						?.metadata
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

	private fun createFilenameForMimeType(mimeType: String) = when (mimeType) {
		"image/png" -> "image.png"
		"image/jpeg" -> "image.jpg"
		"image/bmp" -> "image.bmp"
		"image/gif" -> "image.gif"
		else -> "image"
	}

	override fun cloneImage(id: String, mimeType: String?, width: Int?, height: Int?) = imageData[id]?.let {
		val newId = UUID.randomUUID().toString()
		val renderedImage = it.data.inputStream().use(ImageIO::read).let { sourceImage ->
			if ((width == null) && (height == null)) {
				sourceImage
			} else {
				val originalWidth = sourceImage.width
				val originalHeight = sourceImage.height
				val newWidth = width ?: (originalWidth * height!! / originalHeight)
				val newHeight = height ?: (originalHeight * width!! / originalWidth)
				BufferedImage(newWidth, newHeight, BufferedImage.TYPE_INT_RGB).also { image ->
					image.graphics.use { graphics ->
						graphics.drawImage(sourceImage, 0, 0, newWidth, newHeight, null)
					}
				}
			}
		}
		val encodedImage = ByteArrayOutputStream().use { byteArrayOutputStream ->
			ImageIO.write(renderedImage, (mimeType ?: it.metadata.mimeType).removePrefix("image/"), byteArrayOutputStream)
			byteArrayOutputStream
		}.toByteArray()
		ImageData(it.metadata.copy(id = newId, status = Waiting, width = renderedImage.width, height = renderedImage.height, mimeType = mimeType ?: it.metadata.mimeType), encodedImage).also { imageData[newId] = it }.metadata
	}

	override fun getImage(id: String): ImageMetadata? = imageData[id]?.metadata

	override fun getImageData(id: String): ImageData? = imageData[id]

	override fun setImageFilename(id: String, filename: String) {
		imageData[id]?.let { oldImageData ->
			if (oldImageData.metadata.status == Waiting) {
				imageData[id] = oldImageData.copy(metadata = oldImageData.metadata.copy(filename = filename))
			}
		}
	}

	override fun setImageStatus(id: String, status: ImageStatus) {
		imageData[id]?.let { oldImageData ->
			imageData[id] = oldImageData.copy(metadata = oldImageData.metadata.copy(status = status)).also { newImageData ->
				if (status == Inserting) {
					insertingImageListeners.forEach { it(newImageData) }
				}
			}
		}
	}

	override fun setImageKey(id: String, key: String) {
		imageData[id]?.let { oldImageData ->
			imageData[id] = oldImageData.copy(metadata = oldImageData.metadata.copy(key = key))
		}
	}

	override fun getImageIds() = imageData
		.map(Map.Entry<String, ImageData>::value)
		.map(ImageData::metadata)
		.sorted()
		.map(ImageMetadata::id)

	override fun removeImage(id: String) {
		imageData.remove(id)
	}

	override fun onNewImage(listener: (imageMetadata: ImageData) -> Unit) {
		newImageListeners += listener
	}

	override fun onImageInserting(listener: (imageData: ImageData) -> Unit) {
		insertingImageListeners += listener
	}

	private val newImageListeners = mutableListOf<(ImageData) -> Unit>()
	private val insertingImageListeners = mutableListOf<(ImageData) -> Unit>()
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
	val mimeType: String = "",

	/** The name of the file to insert. */
	val filename: String = "",

	/** The status of the image. */
	val status: ImageStatus = Inserting,

	/** The generated key of the image. */
	val key: String? = null,

	) : Comparable<ImageMetadata> {

	override fun compareTo(other: ImageMetadata) =
		compareBy(ImageMetadata::insertTime).compare(this, other)

	private val insertTime: Long = System.currentTimeMillis()

}

/**
 * The current state of an image. Some of the states are mutually exclusive
 * in the lifecycle of a single image, e.g. there is no possible transition
 * from [Failed] to [Inserted].
 */
enum class ImageStatus {
	Waiting, Inserting, Failed, Inserted
}

/**
 * Data of an image, consisting of both its [metadata][ImageMetadata] and
 * its content.
 */
data class ImageData(val metadata: ImageMetadata, val data: ByteArray)

fun <R> Graphics.use(action: (Graphics) -> R): R = try {
	action(this)
} finally {
	dispose()
}
