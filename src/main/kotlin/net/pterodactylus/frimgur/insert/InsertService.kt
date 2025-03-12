package net.pterodactylus.frimgur.insert

import freenet.client.ClientMetadata
import freenet.client.HighLevelSimpleClient
import freenet.client.InsertBlock
import freenet.client.InsertException
import freenet.client.async.BaseClientPutter
import freenet.client.async.ClientContext
import freenet.client.async.ClientPutCallback
import freenet.keys.FreenetURI
import freenet.node.RequestClient
import freenet.node.RequestClientBuilder
import freenet.node.RequestStarter.MAXIMUM_PRIORITY_CLASS
import freenet.support.api.Bucket
import freenet.support.io.ArrayBucket
import java.awt.image.BufferedImage
import java.awt.image.BufferedImage.TYPE_INT_RGB
import java.io.ByteArrayInputStream
import java.io.ByteArrayOutputStream
import javax.imageio.ImageIO
import net.pterodactylus.frimgur.image.use

/**
 * Service that inserts images into Freenet.
 */
interface InsertService {

	/**
	 * Inserts the given image into Freenet.
	 *
	 * @param id The ID of the image
	 * @param data The data of the image
	 * @param mimeType The MIME type of the image
	 * @param filename The name of the file
	 */
	fun insertImage(id: String, data: ByteArray, mimeType: String, filename: String) = Unit

	/**
	 * Adds a listener that will be notified when the insert of the image
	 * with the given ID is started, and what its filename is.
	 *
	 * @param listener Listener for image-insertion-started events
	 */
	fun onInsertStarting(listener: (id: String, filename: String) -> Unit) = Unit

	fun onInsertGeneratingUri(listener: (id: String, uri: String) -> Unit) = Unit

	fun onInsertFinished(listener: (id: String) -> Unit) = Unit

	fun onInsertFailed(listener: (id: String) -> Unit) = Unit

}

/**
 * Default [InsertService] implementation.
 */
class DefaultInsertService(private val highLevelSimpleClient: HighLevelSimpleClient) : InsertService {

	override fun insertImage(id: String, data: ByteArray, mimeType: String, filename: String) {
		val encodedImage = encodeImage(data, mimeType)
		val insertBlock = InsertBlock(encodedImage.toBucket(), ClientMetadata(mimeType), FreenetURI("CHK@"))
		val insertContext = highLevelSimpleClient.getInsertContext(false)
		val filenameWithExtension = filename.maybeAppendExtension(mimeType)
		insertStartingListeners.forEach { listener -> listener(id, filenameWithExtension) }
		highLevelSimpleClient.insert(insertBlock, filenameWithExtension, false, insertContext, object : ClientPutCallback by getEmptyClientPutCallback(requestClient) {
			override fun onGeneratedURI(freenetURI: FreenetURI, p1: BaseClientPutter) {
				insertGeneratingUriListeners.forEach { listener -> listener(id, freenetURI.toString()) }
			}

			override fun onSuccess(baseClientPutter: BaseClientPutter) {
				insertFinishedListeners.forEach { listener -> listener(id) }
			}

			override fun onFailure(p0: InsertException?, p1: BaseClientPutter?) {
				insertFailedListeners.forEach { listener -> listener(id) }
			}
		}, MAXIMUM_PRIORITY_CLASS)
	}

	private fun encodeImage(data: ByteArray, mimeType: String) = data.imageFormat?.let { originalImageType ->
		when (mimeType) {
			"image/png" ->
				if (originalImageType == "png")
					data
				else
					ByteArrayOutputStream().use { outputStream -> ImageIO.write(ImageIO.read(ByteArrayInputStream(data)), "PNG", outputStream); outputStream }.toByteArray()
			"image/jpeg" ->
				if (originalImageType == "JPEG")
					data
				else
					ByteArrayOutputStream().use { outputStream -> ImageIO.write(ImageIO.read(ByteArrayInputStream(data)).removeTransparency(), "JPEG", outputStream); outputStream }.toByteArray()
			else -> null
		}
	} ?: data

	private fun String.maybeAppendExtension(mimeType: String) = when (mimeType) {
		"image/png" -> if (!endsWith(".png")) "$this.png" else this
		"image/jpeg" -> if (!endsWith(".jpg") && !endsWith(".jpeg")) "$this.jpg" else this
		else -> this
	}

	override fun onInsertStarting(listener: (id: String, filename: String) -> Unit) {
		insertStartingListeners += listener
	}

	override fun onInsertGeneratingUri(listener: (id: String, uri: String) -> Unit) {
		insertGeneratingUriListeners += listener
	}

	override fun onInsertFinished(listener: (id: String) -> Unit) {
		insertFinishedListeners += listener
	}

	override fun onInsertFailed(listener: (id: String) -> Unit) {
		insertFailedListeners += listener
	}

	private val insertStartingListeners = mutableListOf<(String, String) -> Unit>()
	private val insertGeneratingUriListeners = mutableListOf<(String, String) -> Unit>()
	private val insertFinishedListeners = mutableListOf<(String) -> Unit>()
	private val insertFailedListeners = mutableListOf<(String) -> Unit>()
	private val requestClient = RequestClientBuilder().realTime().build()

}

private fun ByteArray.toBucket() = ArrayBucket(this)

val ByteArray.imageFormat get() = ByteArrayInputStream(this).use { inputStream ->
	ImageIO.createImageInputStream(inputStream).use { imageInputStream ->
		ImageIO.getImageReaders(imageInputStream).asSequence()
			.map { it.formatName }
			.firstOrNull()
	}
}

private fun BufferedImage.removeTransparency() = BufferedImage(width, height, TYPE_INT_RGB).apply {
	graphics.use {
		it.drawImage(this@removeTransparency, 0, 0, width, height, null)
	}
}

private fun getEmptyClientPutCallback(requestClient: RequestClient) = object : ClientPutCallback {
	override fun onResume(clientContext: ClientContext) = Unit
	override fun getRequestClient() = requestClient
	override fun onGeneratedURI(freenetURI: FreenetURI, baseClientPutter: BaseClientPutter) = Unit
	override fun onGeneratedMetadata(bucket: Bucket, baseClientPutter: BaseClientPutter) = Unit
	override fun onFetchable(baseClientPutter: BaseClientPutter) = Unit
	override fun onSuccess(baseClientPutter: BaseClientPutter) = Unit
	override fun onFailure(insertException: InsertException, baseClientPutter: BaseClientPutter) = Unit
}
