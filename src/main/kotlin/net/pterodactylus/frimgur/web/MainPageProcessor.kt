package net.pterodactylus.frimgur.web

import net.pterodactylus.frimgur.web.annotations.ToadletName
import java.util.Locale

/**
 * Frimgur main page.
 */
@ToadletName("index.html")
class MainPageProcessor(formPassword: String, localeProvider: () -> Locale, private val nodeRequiresConfigChange: () -> Boolean) : PebblePageProcessor("static/html/main.html", formPassword, localeProvider) {

	override fun getTitle(pageRequest: PageRequest) = "Frimgur Main Page"
	override fun getScriptLinks(pageRequest: PageRequest) = listOf("static/js/frimgur.js")

	override fun getContextVariables(pageRequest: PageRequest) = mapOf("nodeRequiresConfigChange" to nodeRequiresConfigChange())

}
