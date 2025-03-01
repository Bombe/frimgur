package net.pterodactylus.frimgur.web

import freenet.client.HighLevelSimpleClient
import freenet.clients.http.Toadlet
import freenet.clients.http.ToadletContext
import freenet.support.api.HTTPRequest
import java.net.URI
import net.pterodactylus.frimgur.image.ImageMetadata
import net.pterodactylus.frimgur.image.ImageService

/**
 * [Toadlet] that can list all images of an [image service][ImageService].
 */
class ListImagesToadlet(private val path: String, private val imageService: ImageService, highLevelSimpleClient: HighLevelSimpleClient) : Toadlet(highLevelSimpleClient) {

	override fun handleMethodGET(uri: URI, httpRequest: HTTPRequest, toadletContext: ToadletContext) {
		val imageMetadatas = imageService.getImageIds()
			.mapNotNull(imageService::getImage)
			.map(ImageMetadata::toJson)
			.let { imageMetadatas ->
				objectMapper.createArrayNode()
					.also {
						imageMetadatas.forEach(it::add)
					}
			}
		writeReply(toadletContext, 200, "application/json", "OK", imageMetadatas.toString())
	}

	override fun path() = path

}
