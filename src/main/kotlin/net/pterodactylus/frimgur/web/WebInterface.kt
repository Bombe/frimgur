package net.pterodactylus.frimgur.web

import freenet.clients.http.PageMaker
import freenet.pluginmanager.FredPluginL10n

/**
 * Frimgur web interface.
 */
class WebInterface(private val pageMaker: PageMaker, private val pluginL10n: FredPluginL10n) {

	fun start() {
		pageMaker.addNavigationCategory("/frimgur/", "Navigation.Menu.Title", "Navigation.Menu.Title.Tooltip", pluginL10n)
	}

}
