package net.pterodactylus.frimgur.inject

import com.google.inject.AbstractModule
import com.google.inject.Provides
import freenet.client.HighLevelSimpleClient
import freenet.clients.http.PageMaker
import freenet.clients.http.ToadletContainer
import freenet.pluginmanager.FredPluginL10n
import net.pterodactylus.frimgur.web.DefaultWebInterface
import net.pterodactylus.frimgur.web.FreenetToadletFactory
import net.pterodactylus.frimgur.web.WebInterface

/**
 * Guice [module][com.google.inject.Module] for creating [WebInterface] instances.
 */
class WebInterfaceModule(private val prefix: String) : AbstractModule() {

	@Provides
	fun getFreenetToadletFactory(highLevelSimpleClient: HighLevelSimpleClient): FreenetToadletFactory =
		FreenetToadletFactory(prefix, highLevelSimpleClient)

	@Provides
	fun getWebInterface(pageMaker: PageMaker, pluginL10n: FredPluginL10n, toadletContainer: ToadletContainer, freenetToadletFactory: FreenetToadletFactory): WebInterface =
		DefaultWebInterface(pageMaker, pluginL10n, toadletContainer, freenetToadletFactory)

}
