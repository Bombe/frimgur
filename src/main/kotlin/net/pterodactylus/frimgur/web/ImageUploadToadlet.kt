package net.pterodactylus.frimgur.web

import freenet.client.HighLevelSimpleClient
import freenet.clients.http.Toadlet
import freenet.clients.http.ToadletContext
import freenet.support.MultiValueTable
import freenet.support.api.HTTPRequest
import java.net.URI

/**
 * [Toadlet] that takes an encoded image and stores it together with some metadata.
 */
class ImageUploadToadlet(private val path: String, private val imageService: ImageService, highLevelSimpleClient: HighLevelSimpleClient) : Toadlet(highLevelSimpleClient) {

	override fun handleMethodGET(uri: URI, httpRequest: HTTPRequest, toadletContext: ToadletContext) {
		TODO("Not yet implemented")
	}

	fun handleMethodPOST(uri: URI, httpRequest: HTTPRequest, toadletContext: ToadletContext) {
		val imageType = httpRequest.getPartAsStringFailsafe("image-type", 50)
		val imageData = httpRequest.getPartAsBytesFailsafe("image-data", 20.millions())
		if (imageData.isEmpty()) {
			toadletContext.sendReplyHeaders(400, "Bad Request", MultiValueTable(), null, 0)
			return
		}
		val imageMetadata = imageService.addImage(imageType, imageData)
		toadletContext.sendReplyHeaders(201, "Created", MultiValueTable<String, String>().apply { put("Location", imageMetadata.id) }, null, 0)
	}

	override fun path() = path

}

private fun Int.millions() = this * 1_000_000

/**
 * Service for image-related functionality.
 */
interface ImageService {

	/**
	 * Decodes the given image, stores it, and returns its metadata.
	 */
	fun addImage(type: String, data: ByteArray): ImageMetadata

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
