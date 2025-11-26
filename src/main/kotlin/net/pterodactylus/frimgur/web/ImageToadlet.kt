package net.pterodactylus.frimgur.web

import com.fasterxml.jackson.databind.JsonNode
import freenet.client.HighLevelSimpleClient
import freenet.clients.http.Toadlet
import freenet.clients.http.ToadletContext
import freenet.support.MultiValueTable
import freenet.support.api.HTTPRequest
import java.net.URI
import javax.swing.Spring.height
import net.pterodactylus.frimgur.image.ImageService
import net.pterodactylus.frimgur.image.ImageStatus
import net.pterodactylus.frimgur.image.ImageStatus.Inserting
import net.pterodactylus.frimgur.insert.InsertOptionService
import net.pterodactylus.frimgur.insert.InsertService

/**
 * [Toadlet] that takes an encoded image and stores it together with some metadata.
 */
class ImageToadlet(private val path: String, private val imageService: ImageService, private val insertService: InsertService, private val insertOptionService: InsertOptionService, highLevelSimpleClient: HighLevelSimpleClient) : Toadlet(highLevelSimpleClient) {

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
			val width = changes.getNewDimension("width", imageMetadata.width)?.let { if (it <= 0) null else it }
			val height = changes.getNewDimension("height", imageMetadata.height)?.let { if (it <= 0) null else it }
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

	private fun detectTypeFromFilename(filename: String) = when (filename.split(Regex("\\.")).last().lowercase()) {
		"jpg", "jpeg" -> TypeAndFilename("jpeg", filename)
		"png" -> TypeAndFilename("png", filename)
		else -> TypeAndFilename(insertOptionService.defaultImageFormat, "$filename.${insertOptionService.defaultImageFormat.replace("jpeg", "jpg")}")
	}

	override fun path() = path

}

private fun Int.millions() = this * 1_000_000

typealias TypeAndFilename = Pair<String, String>

private fun JsonNode?.getNewDimension(field: String, oldSize: Int) =
	this?.get(field)?.asText()?.let { dimension ->
		if (dimension.endsWith("%")) {
			(dimension.removeSuffix("%").toDouble() / 100.0 * oldSize).toInt()
		} else {
			dimension.toInt()
		}
	}
