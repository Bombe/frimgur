package net.pterodactylus.frimgur.web

/**
 * Interface for components that want to handle HTTP requests.
 */
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
	val code: Int = 500

)

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
