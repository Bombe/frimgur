package net.pterodactylus.frimgur.web

import com.fasterxml.jackson.module.kotlin.jacksonObjectMapper
import freenet.client.HighLevelSimpleClient
import freenet.clients.http.Toadlet
import freenet.clients.http.ToadletContext
import freenet.support.MultiValueTable
import freenet.support.api.HTTPRequest
import net.pterodactylus.frimgur.image.ImageMetadata
import net.pterodactylus.frimgur.image.ImageService
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
		httpRequest.getPartAsBytesFailsafe("image-data", 20.millions())
			.takeIf { it.isNotEmpty() }
			?.let(imageService::addImage)
			?.also { imageMetadata ->
				toadletContext.sendReplyHeaders(201, "Created", MultiValueTable<String, String>().apply { put("Location", imageMetadata.id) }, null, 0)
			}
			?: toadletContext.sendReplyHeaders(400, "Bad Request", MultiValueTable(), null, 0)
	}

	override fun path() = path

}

private fun Int.millions() = this * 1_000_000

private val objectMapper = jacksonObjectMapper()

private fun ImageMetadata.toJson() = objectMapper.createObjectNode()!!.apply {
	put("id", id)
	putObject("metadata").apply {
		put("width", width)
		put("height", height)
		put("size", size)
	}
}
