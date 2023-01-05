package net.pterodactylus.frimgur.web

/**
 * Interface for processing requests from the web interface.
 */
interface PageProcessor {

	fun processPage(pageRequest: PageRequest): PageResponse

}

/**
 * A request by the web interface for the [page processor][PageProcessor].
 */
class PageRequest

/**
 * The response of a [page processor][PageProcessor].
 */
class PageResponse(val title: String, val content: String) {

	val javascriptLinks: List<String> = mutableListOf()

	/**
	 * Adds a `<script>` element to the page’s `<head>` element.
	 */
	fun addJavascriptLink(url: String) {
		(javascriptLinks as MutableList<String>).add(url)
	}

}
