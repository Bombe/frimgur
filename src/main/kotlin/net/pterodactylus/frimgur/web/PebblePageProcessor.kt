package net.pterodactylus.frimgur.web

import com.mitchellbosecke.pebble.PebbleEngine
import com.mitchellbosecke.pebble.template.PebbleTemplate
import java.io.StringWriter
import java.util.Locale

/**
 * Page processor that renders a Pebble template.
 */
open class PebblePageProcessor(templateName: String, private val formPassword: String, private val localeProvider: () -> Locale) : PageProcessor {

	override fun processPage(pageRequest: PageRequest) =
		PageResponse(getTitle(pageRequest), pebbleTemplate.render()).apply {
			addCssLink("static/css/frimgur.css")
			getScriptLinks(pageRequest).forEach(this::addJavascriptLink)
		}

	protected open fun getTitle(pageRequest: PageRequest) = ""
	protected open fun getScriptLinks(pageRequest: PageRequest): List<String> = emptyList()

	private fun PebbleTemplate.render() = StringWriter()
		.also { evaluate(it, mapOf("formPassword" to formPassword), localeProvider()) }
		.toString()

	private val pebbleTemplate = pebbleEngine.getTemplate(templateName)

}

private val pebbleEngine = PebbleEngine.Builder().build()
