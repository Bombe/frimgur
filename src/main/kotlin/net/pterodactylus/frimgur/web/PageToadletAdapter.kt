package net.pterodactylus.frimgur.web

import freenet.client.HighLevelSimpleClient
import freenet.clients.http.Toadlet
import freenet.clients.http.ToadletContext
import freenet.support.api.HTTPRequest
import java.net.URI

/**
 * Adapter from a [Page] to a [Toadlet][freenet.clients.http.Toadlet].
 */
class PageToadletAdapter(private val highLevelSimpleClient: HighLevelSimpleClient) {

	fun adapt(path: String, page: Page) = object : Toadlet(highLevelSimpleClient) {

		override fun handleMethodGET(path: URI, httpRequest: HTTPRequest, toadletContext: ToadletContext) {
			page.handleGet()
		}

		override fun path() = path

	}

}
