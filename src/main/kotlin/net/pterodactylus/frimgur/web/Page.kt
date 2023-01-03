package net.pterodactylus.frimgur.web

/**
 * Interface for components that want to handle HTTP requests.
 */
interface Page {

	/**
	 * Handles a GET request to the given path.
	 */
	fun handleGet()

}
