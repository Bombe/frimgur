package net.pterodactylus.frimgur.web

import java.io.ByteArrayInputStream
import java.io.InputStream

/**
 * Interface for components that want to handle HTTP requests.
 */
@Deprecated("superseded by PageProcessor", replaceWith = ReplaceWith("PageProcessor"))
interface Page {

	/**
	 * Handles a GET request to the given path.
	 */
	fun handleGet(): Response

}

/**
 * Container for HTTP responses.
 */
data class Response(

	/**
	 * The code of the HTTP response, as defined in
	 * <a href="https://www.rfc-editor.org/rfc/rfc2616#section-6.1.1">6.1.1 of RFC 2616</a>.
	 */
	val code: Int = 500,

	/**
	 * The reason phrase of the HTTP response. If unset, defaults to using [getReasonForStatus].
	 */
	val reason: String? = null,

	/**
	 * The content to stream back to the client.
	 */
	val content: Content = NullContent

)

/**
 * The content that should be delivered to the client.
 */
interface Content {

	/**
	 * The MIME type of the content. Can be `null` if the content type is unknown.
	 */
	val contentType: String?
		get() = null

	/**
	 * The length of the content. May be `null` to signify unknown length.
	 *
	 * @return The length of the content, or `null` if the length is unknown
	 */
	fun length(): Long?

	/**
	 * An input stream delivering the data.
	 *
	 * @return The input stream containing the data that should be delivered to the client
	 */
	fun toInputStream(): InputStream

	/**
	 * Returns a [Content] implementation that returns the same values as this `Content` for
	 * [length] and [toInputStream] but returns the given content type for [contentType].
	 *
	 * @param contentType The new content type
	 * @return A [Content] implementation with a changed content type
	 */
	fun typed(contentType: String): Content = object : Content by this {
		override val contentType = contentType
	}

}

/**
 * [Content] implementation that delivers 0 bytes of content back to the client.
 */
object NullContent : Content {
	override fun length() = 0L
	override fun toInputStream() = object : InputStream() {
		override fun read() = -1
	}
}

/**
 * Returns a [Content] implementation that serves (a part of) the given byte array to the client.
 *
 * @param buffer The byte array containing the data to deliver to the client
 * @param offset The offset of the content to deliver
 * @param length The length of the content to deliver
 * @return A [Content] implementation that delivers (a part of) the given byte array
 */
fun from(buffer: ByteArray, offset: Int = 0, length: Int = buffer.size) = object : Content {
	private val inputStream = ByteArrayInputStream(buffer, offset, length)
	override fun length() = length.toLong()
	override fun toInputStream() = inputStream
}

/**
 * Returns a [Content] implementation that delivers the given input stream to the client.
 *
 * @param inputStream The input stream containing the data to deliver to the client
 * @param length The length of the data to deliver, or `null` if the length is unknown
 * @return A [Content] implementation that delivers the input stream to the client
 */
fun from(inputStream: InputStream, length: Long? = null) = object : Content {
	override fun length() = length
	override fun toInputStream() = inputStream
}

/**
 * Returns the reason phrase for the given status code, or “Unknown” if no mapping exists.
 *
 * @param code The HTTP status code
 * @return The reason phrase, or “Unknown”
 */
fun getReasonForStatus(code: Int): String = when (code) {
	200 -> "OK"
	500 -> "Internal Server Error"
	else -> "Unknown"
}
