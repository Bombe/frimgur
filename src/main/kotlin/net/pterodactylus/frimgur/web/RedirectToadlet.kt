package net.pterodactylus.frimgur.web

import freenet.client.HighLevelSimpleClient
import freenet.clients.http.Toadlet
import freenet.clients.http.ToadletContext
import freenet.support.MultiValueTable
import freenet.support.api.HTTPRequest
import java.net.URI

/**
 * [Toadlet] that redirects every incoming request to a different URI.
 */
class RedirectToadlet(private val path: String, private val target: String, highLevelSimpleClient: HighLevelSimpleClient) : Toadlet(highLevelSimpleClient) {

	override fun handleMethodGET(uri: URI, httpRequest: HTTPRequest, toadletContext: ToadletContext) {
		toadletContext.sendReplyHeaders(303, "See Other", MultiValueTable<String, String>().apply { put("Location", target) }, null, -1)
	}

	override fun path() = path
}
