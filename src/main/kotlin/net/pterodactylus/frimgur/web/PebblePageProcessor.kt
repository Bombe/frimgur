package net.pterodactylus.frimgur.web

import com.mitchellbosecke.pebble.PebbleEngine
import com.mitchellbosecke.pebble.loader.ClasspathLoader
import com.mitchellbosecke.pebble.template.PebbleTemplate
import java.io.StringWriter
import java.util.Locale
import net.pterodactylus.frimgur.custom.fri18nExtension

/**
 * Page processor that renders a Pebble template.
 */
open class PebblePageProcessor(templateName: String, private val formPassword: String, private val localeProvider: () -> Locale) : PageProcessor {

	override fun processPage(pageRequest: PageRequest) =
		PageResponse(getTitle(pageRequest), pebbleTemplate.render(pageRequest)).apply {
			addCssLink("static/css/frimgur.css")
			getScriptLinks(pageRequest).forEach(this::addJavascriptLink)
		}

	protected open fun getTitle(pageRequest: PageRequest) = ""
	protected open fun getScriptLinks(pageRequest: PageRequest): List<String> = emptyList()
	protected open fun getContextVariables(pageRequest: PageRequest): Map<String, Any> = emptyMap()

	private fun PebbleTemplate.render(pageRequest: PageRequest) = StringWriter()
		.also { evaluate(it, getContextVariables(pageRequest) + ("formPassword" to formPassword), localeProvider()) }
		.toString()

	private val pebbleTemplate = pebbleEngine.getTemplate(templateName)

}

private val pebbleEngine = PebbleEngine.Builder()
	.loader(ClasspathLoader(PebblePageProcessor::class.java.classLoader))
	.extension(fri18nExtension())
	.build()
