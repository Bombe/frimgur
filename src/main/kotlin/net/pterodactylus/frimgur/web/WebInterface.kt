package net.pterodactylus.frimgur.web

import freenet.clients.http.PageMaker
import freenet.pluginmanager.FredPluginL10n

/**
 * Frimgur web interface.
 */
interface WebInterface {

	fun start() = Unit
	fun stop() = Unit

}

/**
 * Fred-based [WebInterface] implementation.
 */
class DefaultWebInterface(private val categoryNameKey: String, private val categoryTooltipKey: String, private val pageMaker: PageMaker, private val pluginL10n: FredPluginL10n, private val toadletRegistry: ToadletRegistry) : WebInterface {

	override fun start() {
		pageMaker.addNavigationCategory("/frimgur/", categoryNameKey, categoryTooltipKey, pluginL10n)

		toadletRegistry.start()
	}

	override fun stop() {
		pageMaker.removeNavigationCategory(categoryNameKey)

		toadletRegistry.stop()
	}

}
