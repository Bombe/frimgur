package net.pterodactylus.frimgur.plugin

import com.google.inject.Guice
import com.google.inject.Injector
import com.google.inject.Module
import freenet.l10n.BaseL10n.LANGUAGE
import freenet.pluginmanager.FredPlugin
import freenet.pluginmanager.FredPluginL10n
import freenet.pluginmanager.FredPluginThreadless
import freenet.pluginmanager.PluginRespirator
import net.pterodactylus.frimgur.image.ImageService
import net.pterodactylus.frimgur.inject.FreenetModule
import net.pterodactylus.frimgur.inject.ImageModule
import net.pterodactylus.frimgur.inject.InsertModule
import net.pterodactylus.frimgur.inject.WebInterfaceModule
import net.pterodactylus.frimgur.insert.InsertService
import net.pterodactylus.frimgur.util.getInstance
import net.pterodactylus.frimgur.web.WebInterface
import java.util.Locale
import java.util.ResourceBundle

/**
 * Frimgur main plugin class.
 */
open class Frimgur : FredPlugin, FredPluginL10n, FredPluginThreadless {

	override fun runPlugin(pluginRespirator: PluginRespirator) {
		this.pluginRespirator = pluginRespirator
		val injector = createInjector()
		wireUpListeners(injector)
		webInterface = injector.getInstance(WebInterface::class.java)
			.apply { start() }
	}

	private fun wireUpListeners(injector: Injector) {
		val imageService: ImageService = injector.getInstance()
		val insertService: InsertService = injector.getInstance()
		imageService.onNewImage { imageData -> insertService.insertImage(imageData.metadata.id, imageData.data, imageData.metadata.mimeType) }
	}

	protected open fun createInjector(): Injector = Guice.createInjector(getModules())

	protected open fun getModules(): List<Module> = listOf(
		FreenetModule(this, pluginRespirator),
		ImageModule(),
		InsertModule(),
		WebInterfaceModule("/frimgur/", "Navigation.Menu.Title", "Navigation.Menu.Tooltip")
	)

	override fun terminate() {
		webInterface?.stop()
	}

	override fun setLanguage(newLanguage: LANGUAGE) {
		this.language = newLanguage
	}

	override fun getString(key: String): String {
		return ResourceBundle.getBundle("i18n/frimgur", Locale.forLanguageTag(language.shortCode)).getString(key)
	}

	private var language = LANGUAGE.ENGLISH
	private lateinit var pluginRespirator: PluginRespirator

	private var webInterface: WebInterface? = null

}
