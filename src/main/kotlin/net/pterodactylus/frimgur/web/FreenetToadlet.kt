package net.pterodactylus.frimgur.web

import freenet.client.HighLevelSimpleClient
import freenet.clients.http.Toadlet
import freenet.clients.http.ToadletContext
import freenet.support.MultiValueTable
import freenet.support.api.HTTPRequest
import java.net.URI
import net.pterodactylus.frimgur.web.annotations.toadletName

/**
 * [Toadlet][freenet.clients.http.Toadlet] implementation that renders Freenet’s web interface around a [PageProcessor].
 */
class FreenetToadlet(highLevelSimpleClient: HighLevelSimpleClient, private val prefix: String, private val pageProcessor: PageProcessor) : Toadlet(highLevelSimpleClient){

	override fun handleMethodGET(uri: URI, httpRequest: HTTPRequest, toadletContext: ToadletContext) {
		val pageResponse = pageProcessor.processPage(PageRequest())
		val pageNode = toadletContext.pageMaker.getPageNode(pageResponse.title, toadletContext)
		pageResponse.javascriptLinks.forEach { url -> pageNode.headNode.addChild("script", arrayOf("src", "language", "type"), arrayOf(prefix + url, "javascript", "application/javascript")) }
		pageResponse.cssLinks.forEach { url -> pageNode.headNode.addChild("link", arrayOf("rel", "type", "href"), arrayOf("stylesheet", "text/css", prefix + url)) }
		pageNode.content.addChild("%", pageResponse.content)
		val renderedPage = pageNode.outer.generate().toByteArray()
		toadletContext.sendReplyHeaders(200, "OK", MultiValueTable(), "text/html", renderedPage.size.toLong())
		toadletContext.writeData(renderedPage)
	}

	override fun path() = prefix + pageProcessor.toadletName

}
