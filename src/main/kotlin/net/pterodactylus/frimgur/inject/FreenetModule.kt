package net.pterodactylus.frimgur.inject

import com.google.inject.AbstractModule
import com.google.inject.Provides
import freenet.clients.http.PageMaker
import freenet.pluginmanager.FredPluginL10n
import freenet.pluginmanager.PluginRespirator
import net.pterodactylus.frimgur.plugin.Frimgur

/**
 * Guice [module][com.google.inject.Module] that can provide Freenet-related objects.
 */
class FreenetModule(frimgur: Frimgur, pluginRespirator: PluginRespirator) : AbstractModule() {

	@get:Provides
	val pageMaker: PageMaker = pluginRespirator.pageMaker

	@get:Provides
	val pluginL10n: FredPluginL10n = frimgur

}
