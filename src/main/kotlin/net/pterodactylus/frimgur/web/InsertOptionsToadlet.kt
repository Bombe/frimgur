package net.pterodactylus.frimgur.web

import freenet.client.HighLevelSimpleClient
import freenet.clients.http.Toadlet
import freenet.clients.http.ToadletContext
import freenet.support.api.HTTPRequest
import java.net.URI
import net.pterodactylus.frimgur.insert.InsertOptionService

/**
 * [Toadlet] that delivers the current insert options on a `GET` requests
 * and allows changing the options using `PATCH`.
 */
class InsertOptionsToadlet(private val path: String, private val insertOptionService: InsertOptionService, highLevelSimpleClient: HighLevelSimpleClient) : Toadlet(highLevelSimpleClient) {

	override fun handleMethodGET(uri: URI, request: HTTPRequest, toadletContext: ToadletContext) =
		writeReply(toadletContext, 200, "application/json", "OK", objectMapper.writeValueAsString(createInsertOptions()))

	@Suppress("UNUSED_PARAMETER")
	fun handleMethodPATCH(uri: URI, httpRequest: HTTPRequest, toadletContext: ToadletContext) {
		val changes = httpRequest.rawData.inputStream.use(objectMapper::readTree)
		if (changes.has("defaultDestination")) {
			insertOptionService.defaultInsertDestination = changes.get("defaultDestination").asText()
		}
		if (changes.has("defaultImageFormat")) {
			insertOptionService.defaultImageFormat = changes.get("defaultImageFormat").asText()
		}
		toadletContext.sendReplyHeaders(200, "OK", null, null, 0)
	}

	override fun path() = path

	private fun createInsertOptions() = InsertOptions(
		insertOptionService.defaultInsertDestination
	)

	private data class InsertOptions(val defaultDestination: String)

}
