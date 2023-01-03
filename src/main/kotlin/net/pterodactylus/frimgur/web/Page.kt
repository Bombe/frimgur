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
