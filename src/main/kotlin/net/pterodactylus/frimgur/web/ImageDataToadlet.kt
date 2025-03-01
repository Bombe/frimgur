package net.pterodactylus.frimgur.web

import freenet.client.HighLevelSimpleClient
import freenet.clients.http.Toadlet
import freenet.clients.http.ToadletContext
import freenet.support.api.HTTPRequest
import java.net.URI
import net.pterodactylus.frimgur.image.ImageService

/**
 * [Toadlet] that can deliver image data.
 */
class ImageDataToadlet(private val path: String, private val imageService: ImageService, highLevelSimpleClient: HighLevelSimpleClient) : Toadlet(highLevelSimpleClient) {

	override fun handleMethodGET(uri: URI, httpRequest: HTTPRequest, toadletContext: ToadletContext) {
		val imageId = uri.path.removePrefix(path)
		imageService.getImageData(imageId)
			?.also { (_, data) ->
				writeReply(toadletContext, 200, "image/png", "OK", data, 0, data.size)
			}
			?: writeReply(toadletContext, 404, null, "Not Found", "")
	}

	override fun path() = path

}
