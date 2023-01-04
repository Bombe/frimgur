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
import java.io.InputStream
import java.net.URI

/**
 * [Toadlet] that can serve files.
 */
open class FileToadlet(private val prefix: String, highLevelSimpleClient: HighLevelSimpleClient) : Toadlet(highLevelSimpleClient) {

	override fun handleMethodGET(uri: URI, httpRequest: HTTPRequest, toadletContext: ToadletContext) {
		val fileData = locateFileData(uri.path.removePrefix(prefix))
		if (fileData == null) {
			toadletContext.sendReplyHeaders(404, "Not Found", MultiValueTable(), null, 0)
			return
		}
		toadletContext.sendReplyHeaders(200, "OK", MultiValueTable(), fileData.mimeType, fileData.length)
		toadletContext.writeData(fileData.toBucket())
	}

	/**
	 * Locates the requested file and supplies data about it.
	 *
	 * @param file The name of the file to locate
	 * @return Information about the located file, or `null` if the file could not be located
	 */
	protected open fun locateFileData(file: String): FileData? = null

	override fun path() = prefix

}

/**
 * Data about a file that should be served.
 */
data class FileData(val length: Long, val mimeType: String?, val inputStream: InputStream)

private fun FileData.toBucket(): Bucket = object : Bucket {

	override fun getOutputStream() = throw IOException("Bucket is read-only")
	override fun getOutputStreamUnbuffered() = outputStream
	override fun getInputStream() = this@toBucket.inputStream
	override fun getInputStreamUnbuffered() = this@toBucket.inputStream
	override fun getName() = toString()
	override fun size() = length.takeIf { it > -1 } ?: throw IOException("Unknown size")
	override fun isReadOnly() = true
	override fun setReadOnly() = Unit
	override fun free() = Unit
	override fun createShadow() = null
	override fun onResume(clientContext: ClientContext) = Unit
	override fun storeTo(dataOutputStream: DataOutputStream) = Unit

}
