package net.pterodactylus.frimgur.web

import java.util.Locale
import net.pterodactylus.frimgur.insert.InsertOptionService
import net.pterodactylus.frimgur.web.annotations.ToadletName

/**
 * Frimgur main page.
 */
@ToadletName("index.html")
class MainPageProcessor(private val insertOptionService: InsertOptionService, formPassword: String, localeProvider: () -> Locale, private val nodeRequiresConfigChange: () -> Boolean) : PebblePageProcessor("static/html/main.html", formPassword, localeProvider) {

	override fun getTitle(pageRequest: PageRequest) = "Frimgur Main Page"
	override fun getScriptLinks(pageRequest: PageRequest) = listOf("static/js/frimgur.js")

	override fun getContextVariables(pageRequest: PageRequest) = mapOf(
		"nodeRequiresConfigChange" to nodeRequiresConfigChange(),
		"insertOptions" to createInsertOptions()
	)

	private fun createInsertOptions() = InsertOptions(insertOptionService.defaultInsertDestination == "CHK@", insertOptionService.defaultInsertDestination == "SSK@")

}

private data class InsertOptions(val insertToChk: Boolean, val insertToSsk: Boolean)
