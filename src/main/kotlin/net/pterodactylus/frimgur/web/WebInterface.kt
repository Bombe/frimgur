package net.pterodactylus.frimgur.web

import freenet.clients.http.PageMaker
import freenet.clients.http.Toadlet
import freenet.clients.http.ToadletContainer
import freenet.pluginmanager.FredPluginL10n

/**
 * Frimgur web interface.
 */
interface WebInterface {

	fun start()
	fun stop()

}

/**
 * Fred-based [WebInterface] implementation.
 */
class DefaultWebInterface(private val pageMaker: PageMaker, private val pluginL10n: FredPluginL10n, private val toadletContainer: ToadletContainer, private val freenetToadletFactory: FreenetToadletFactory) : WebInterface {

	override fun start() {
		pageMaker.addNavigationCategory("/frimgur/", "Navigation.Menu.Title", "Navigation.Menu.Tooltip", pluginL10n)

		val mainPageToadlet = freenetToadletFactory.createFreenetToadlet(MainPageProcessor()).also(toadlets::add)
		toadletContainer.register(mainPageToadlet, "Navigation.Menu.Title", "/frimgur/", true, "Navigation.Main.Title", "Navigation.Main.Tooltip", false, null, pluginL10n)
	}

	override fun stop() {
		pageMaker.removeNavigationCategory("Navigation.Menu.Title")
		toadlets.forEach(toadletContainer::unregister)
	}

	private val toadlets = mutableListOf<Toadlet>()

}
