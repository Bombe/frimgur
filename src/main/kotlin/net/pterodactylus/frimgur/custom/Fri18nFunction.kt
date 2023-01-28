package net.pterodactylus.frimgur.custom

import com.mitchellbosecke.pebble.extension.AbstractExtension
import com.mitchellbosecke.pebble.extension.Extension
import com.mitchellbosecke.pebble.extension.Function
import com.mitchellbosecke.pebble.template.EvaluationContext
import com.mitchellbosecke.pebble.template.PebbleTemplate
import java.text.MessageFormat
import java.util.ResourceBundle

/**
 * Custom Pebble [Function] to make sure the correct class loader is used to
 * load [ResourceBundle]s.
 */
class Fri18nFunction(private var classLoader: ClassLoader) : Function {

	override fun getArgumentNames() = cachedArgumentNames

	override fun execute(args: Map<String, Any>, self: PebbleTemplate, context: EvaluationContext, lineNumber: Int): Any? {
		val bundle = args["bundle"] as? String ?: return null
		val key = args["key"] as? String ?: return null
		val params = args["params"]

		val resourceBundle = ResourceBundle.getBundle(bundle, context.locale, classLoader)
		val message = resourceBundle.getObject(key).toString()
		if (params is List<Any?>) {
			return MessageFormat.format(message, *params.toTypedArray())
		} else if (params != null) {
			return MessageFormat.format(message, params)
		}
		return message
	}

}

/**
 * Returns a Pebble [Extension] that will install [Fri18nFunction] under the name `fri18n`.
 */
fun fri18nExtension(classLoader: ClassLoader = Fri18nFunction::class.java.classLoader) = object : AbstractExtension() {
	override fun getFunctions(): Map<String, Function> = mapOf("i18n" to Fri18nFunction(classLoader))
}

private val cachedArgumentNames = listOf("bundle", "key", "params")
