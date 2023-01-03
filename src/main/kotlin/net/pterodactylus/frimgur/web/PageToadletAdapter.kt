package net.pterodactylus.frimgur.web

import freenet.client.HighLevelSimpleClient
import freenet.client.async.ClientContext
import freenet.clients.http.Toadlet
import freenet.clients.http.ToadletContext
import freenet.support.MultiValueTable
import freenet.support.api.Bucket
import freenet.support.api.HTTPRequest
import java.io.DataOutputStream
import java.io.IOException
import java.net.URI

/**
 * Adapter from a [Page] to a [Toadlet][freenet.clients.http.Toadlet].
 */
class PageToadletAdapter(private val highLevelSimpleClient: HighLevelSimpleClient) {

	fun adapt(path: String, page: Page) = object : Toadlet(highLevelSimpleClient) {

		override fun handleMethodGET(path: URI, httpRequest: HTTPRequest, toadletContext: ToadletContext) {
			val response = page.handleGet()
			toadletContext.sendReplyHeaders(response.code, response.reason ?: getReasonForStatus(response.code), MultiValueTable(), response.content.contentType, response.content.length() ?: -1)
			toadletContext.writeData(response.content.toBucket())
		}

		override fun path() = path

	}

}

private fun Content.toBucket(): Bucket = object : Bucket {

	override fun getOutputStream() = throw IOException("Bucket is read-only")
	override fun getOutputStreamUnbuffered() = outputStream
	override fun getInputStream() = toInputStream()
	override fun getInputStreamUnbuffered() = toInputStream()
	override fun getName() = toString()
	override fun size() = length() ?: throw IOException("Unknown size")
	override fun isReadOnly() = true
	override fun setReadOnly() = Unit
	override fun free() = Unit
	override fun createShadow() = null
	override fun onResume(clientContext: ClientContext) = Unit
	override fun storeTo(dataOutputStream: DataOutputStream) = Unit

}
