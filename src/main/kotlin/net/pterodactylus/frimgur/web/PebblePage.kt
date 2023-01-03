package net.pterodactylus.frimgur.web

import com.mitchellbosecke.pebble.PebbleEngine
import java.io.StringWriter

/**
 * [Page] implementation based on a Pebble template.
 */
open class PebblePage(template: String) : Page {

	override fun handleGet(): Response {
		val output = StringWriter().also(compiledTemplate::evaluate).toString()
		return Response(200, content = from(output.toByteArray()).typed("text/html"))
	}

	private val compiledTemplate = pebbleEngine.getTemplate(template)

}

private val pebbleEngine = PebbleEngine.Builder().build()
