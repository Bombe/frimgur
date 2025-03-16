package net.pterodactylus.frimgur.web

import freenet.client.HighLevelSimpleClient
import freenet.clients.http.Toadlet
import freenet.clients.http.ToadletContext
import freenet.support.MultiValueTable
import freenet.support.api.HTTPRequest
import java.net.URI
import net.pterodactylus.frimgur.image.ImageService
import net.pterodactylus.frimgur.image.ImageStatus
import net.pterodactylus.frimgur.image.ImageStatus.Inserting
import net.pterodactylus.frimgur.insert.InsertService

/**
 * [Toadlet] that takes an encoded image and stores it together with some metadata.
 */
class ImageToadlet(private val path: String, private val imageService: ImageService, private val insertService: InsertService, highLevelSimpleClient: HighLevelSimpleClient) : Toadlet(highLevelSimpleClient) {

	override fun handleMethodGET(uri: URI, httpRequest: HTTPRequest, toadletContext: ToadletContext) {
		val imageId = uri.path.removePrefix(path)
		val imageMetadata = imageService.getImage(imageId)
		if (imageMetadata == null) {
			toadletContext.sendReplyHeaders(404, "Not Found", null, null, 0)
			return
		}
		writeReply(toadletContext, 200, "application/json", "OK", imageMetadata.toJson().toString())
	}

	fun handleMethodPOST(@Suppress("UNUSED_PARAMETER") uri: URI, httpRequest: HTTPRequest, toadletContext: ToadletContext) {
		httpRequest.getPartAsBytesFailsafe("image-data", 20.millions())
			.takeIf { it.isNotEmpty() }
			?.let(imageService::addImage)
			?.also { imageMetadata ->
				toadletContext.sendReplyHeaders(201, "Created", MultiValueTable<String, String>().apply { put("Location", imageMetadata.id) }, null, 0)
			}
			?: toadletContext.sendReplyHeaders(400, "Bad Request", MultiValueTable(), null, 0)
	}

	fun handleMethodPATCH(uri: URI, httpRequest: HTTPRequest, toadletContext: ToadletContext) {
		val imageId = uri.path.removePrefix(path)
		val imageMetadata = imageService.getImage(imageId)
		if (imageMetadata == null) {
			toadletContext.sendReplyHeaders(404, "Not Found", null, null, 0)
			return
		}
		val changes = httpRequest.rawData.inputStream.use(objectMapper::readTree)
		if (changes.isEmpty) {
			toadletContext.sendReplyHeaders(204, "No Content", null, null, 0)
			return
		}
		if (changes.has("filename")) {
			val filename = changes.get("filename").asText()
			imageService.setImageFilename(imageId, filename)
		}
		if (changes.has("status")) {
			val newStatus = try {
				ImageStatus.valueOf(changes.get("status").asText())
			} catch (e: IllegalArgumentException) {
				null
			}
			if (newStatus == Inserting) {
				val (type, filename) = detectTypeFromFilename(imageService.getImage(imageId)!!.filename)
				insertService.insertImage(imageId, imageService.getImageData(imageId)!!.data, "image/${type}", filename)
			}
		}
		if (changes.has("width") || changes.has("height")) {
			val width = changes.get("width")?.asInt(-1)?.let { if (it <= 0) null else it }
			val height = changes.get("height")?.asInt(-1)?.let { if (it <= 0) null else it }
			val clonedMetadata = imageService.cloneImage(imageId, null, width, height)
			toadletContext.sendReplyHeaders(201, "Created", MultiValueTable<String, String>().apply { put("Location", clonedMetadata?.id) }, null, 0)
			return
		}
		toadletContext.sendReplyHeaders(200, "OK", null, null, 0)
	}

	fun handleMethodDELETE(uri: URI, @Suppress("UNUSED_PARAMETER") httpRequest: HTTPRequest, toadletContext: ToadletContext) {
		val imageId = uri.path.removePrefix(path)
		imageService.removeImage(imageId)
		toadletContext.sendReplyHeaders(204, "No Content", null, null, 0)
	}

	override fun path() = path

}

private fun Int.millions() = this * 1_000_000

private fun detectTypeFromFilename(filename: String) = when (filename.split(Regex("\\.")).last().lowercase()) {
	"jpg", "jpeg" -> TypeAndFilename("jpeg", filename)
	"png" -> TypeAndFilename("png", filename)
	else -> TypeAndFilename("png", "$filename.png")
}

typealias TypeAndFilename = Pair<String, String>
