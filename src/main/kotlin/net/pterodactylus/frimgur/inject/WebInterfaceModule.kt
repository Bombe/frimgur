package net.pterodactylus.frimgur.inject

import com.google.inject.AbstractModule
import com.google.inject.Provides
import freenet.client.HighLevelSimpleClient
import freenet.clients.http.PageMaker
import freenet.clients.http.Toadlet
import freenet.clients.http.ToadletContainer
import freenet.pluginmanager.FredPluginL10n
import net.pterodactylus.frimgur.web.ClasspathFileToadlet
import net.pterodactylus.frimgur.web.DefaultToadletRegistry
import net.pterodactylus.frimgur.web.DefaultWebInterface
import net.pterodactylus.frimgur.web.FreenetToadletFactory
import net.pterodactylus.frimgur.web.MainPageProcessor
import net.pterodactylus.frimgur.web.ToadletRegistry
import net.pterodactylus.frimgur.web.ToadletSpec
import net.pterodactylus.frimgur.web.WebInterface
import javax.inject.Named

/**
 * Guice [module][com.google.inject.Module] for creating [WebInterface] instances.
 */
class WebInterfaceModule(private val prefix: String) : AbstractModule() {

	@Provides
	fun getFreenetToadletFactory(highLevelSimpleClient: HighLevelSimpleClient): FreenetToadletFactory =
		FreenetToadletFactory(prefix, highLevelSimpleClient)

	@Provides
	fun getToadletRegistry(
		toadletContainer: ToadletContainer,
		pluginL10n: FredPluginL10n,
		@Named("Main") mainToadlet: Toadlet,
		@Named("StaticJavascript") staticJavascriptToadlet: Toadlet,
	): ToadletRegistry =
		DefaultToadletRegistry(toadletContainer, menuCategoryKey, pluginL10n).apply {
			addToadlet(ToadletSpec(staticJavascriptToadlet))
			addToadlet(ToadletSpec(mainToadlet, "Navigation.Main.Title", "Navigation.Main.Tooltip"))
		}

	@Provides
	fun getWebInterface(pageMaker: PageMaker, pluginL10n: FredPluginL10n, toadletRegistry: ToadletRegistry): WebInterface =
		DefaultWebInterface(menuCategoryKey, menuCategoryTooltipKey, pageMaker, pluginL10n, toadletRegistry)

	@Provides
	@Named("Main")
	fun getMainToadlet(freenetToadletFactory: FreenetToadletFactory): Toadlet =
		freenetToadletFactory.createFreenetToadlet(MainPageProcessor())

	@Provides
	@Named("StaticJavascript")
	fun getStaticJavascriptToadlet(highLevelSimpleClient: HighLevelSimpleClient): Toadlet =
		ClasspathFileToadlet(prefix + "static/js/", "/static/js/", highLevelSimpleClient).apply {
			addMimeType("application/javascript") { filename -> filename.endsWith(".js", ignoreCase = true) }
		}

}
