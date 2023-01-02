package net.pterodactylus.frimgur.inject

import com.google.inject.AbstractModule
import com.google.inject.Provides
import freenet.clients.http.PageMaker
import freenet.pluginmanager.FredPluginL10n
import net.pterodactylus.frimgur.web.WebInterface

/**
 * Guice [module][com.google.inject.Module] for creating [WebInterface] instances.
 */
class WebInterfaceModule : AbstractModule() {

	@Provides
	fun getWebInterface(pageMaker: PageMaker, pluginL10n: FredPluginL10n): WebInterface =
		WebInterface(pageMaker, pluginL10n)

}
