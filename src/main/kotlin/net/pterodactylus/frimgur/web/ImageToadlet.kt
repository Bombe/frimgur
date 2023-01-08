package net.pterodactylus.frimgur.web

import com.fasterxml.jackson.annotation.JsonRootName
import com.fasterxml.jackson.databind.JsonNode
import com.fasterxml.jackson.databind.node.ObjectNode
import com.fasterxml.jackson.module.kotlin.jacksonObjectMapper
import freenet.client.HighLevelSimpleClient
import freenet.clients.http.Toadlet
import freenet.clients.http.ToadletContext
import freenet.support.MultiValueTable
import freenet.support.api.HTTPRequest
import java.net.URI

/**
 * [Toadlet] that takes an encoded image and stores it together with some metadata.
 */
class ImageToadlet(private val path: String, private val imageService: ImageService, highLevelSimpleClient: HighLevelSimpleClient) : Toadlet(highLevelSimpleClient) {

	override fun handleMethodGET(uri: URI, httpRequest: HTTPRequest, toadletContext: ToadletContext) {
		val imageId = uri.path.removePrefix(path)
		val imageMetadata = imageService.getImage(imageId)
		if (imageMetadata == null) {
			toadletContext.sendReplyHeaders(404, "Not Found", null, null, 0)
			return
		}
		writeReply(toadletContext, 200, "application/json", "OK", imageMetadata.toJson().toString())
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

private val objectMapper = jacksonObjectMapper()

private fun ImageMetadata.toJson() = objectMapper.createObjectNode()!!.apply {
	put("id", id)
	putObject("metadata").apply {
		put("width", width)
		put("height", height)
		put("size", size)
	}
}
