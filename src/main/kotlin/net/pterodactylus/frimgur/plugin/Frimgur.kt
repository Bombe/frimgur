package net.pterodactylus.frimgur.plugin

import com.google.inject.Guice
import com.google.inject.Injector
import com.google.inject.Module
import com.google.inject.name.Names
import freenet.l10n.BaseL10n.LANGUAGE
import freenet.pluginmanager.FredPlugin
import freenet.pluginmanager.FredPluginL10n
import freenet.pluginmanager.FredPluginThreadless
import freenet.pluginmanager.FredPluginVersioned
import freenet.pluginmanager.PluginRespirator
import java.util.Locale
import java.util.ResourceBundle
import net.pterodactylus.frimgur.image.ImageService
import net.pterodactylus.frimgur.image.ImageStatus.Failed
import net.pterodactylus.frimgur.image.ImageStatus.Inserted
import net.pterodactylus.frimgur.image.ImageStatus.Inserting
import net.pterodactylus.frimgur.inject.FreenetModule
import net.pterodactylus.frimgur.inject.ImageModule
import net.pterodactylus.frimgur.inject.InsertModule
import net.pterodactylus.frimgur.inject.WebInterfaceModule
import net.pterodactylus.frimgur.insert.InsertService
import net.pterodactylus.frimgur.util.getInstance
import net.pterodactylus.frimgur.util.versionProperties
import net.pterodactylus.frimgur.web.WebInterface

/**
 * Frimgur main plugin class.
 */
open class Frimgur : FredPlugin, FredPluginL10n, FredPluginThreadless, FredPluginVersioned {

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
		insertService.onInsertStarting { id -> imageService.setImageStatus(id, Inserting) }
		insertService.onInsertGeneratingUri(imageService::setImageKey)
		insertService.onInsertFailed { id -> imageService.setImageStatus(id, Failed) }
		insertService.onInsertFinished { id -> imageService.setImageStatus(id, Inserted) }
	}

	protected open fun createInjector(): Injector = Guice.createInjector(getModules())

	protected open fun getModules(): List<Module> = listOf(
		FreenetModule(this, pluginRespirator),
		ImageModule(),
		InsertModule(),
		WebInterfaceModule("/frimgur/", "Navigation.Menu.Title", "Navigation.Menu.Tooltip"),
		Module { binder -> binder.bind(Locale::class.java).toProvider { Locale.forLanguageTag(language.shortCode) } },
		Module { binder -> binder.bind(Boolean::class.java).annotatedWith(Names.named("NodeRequiresConfigChange")).toProvider { !pluginRespirator.node.getConfig().get("fproxy").getBoolean("enableExtendedMethodHandling") }}
	)

	override fun terminate() {
		webInterface?.stop()
	}

	override fun setLanguage(newLanguage: LANGUAGE) {
		this.language = newLanguage
	}

	override fun getString(key: String): String {
		return ResourceBundle.getBundle("i18n/frimgur" + getResourceBundleSuffix(), Locale.forLanguageTag(language.shortCode)).getString(key)
	}

	override fun getVersion() = versionProperties.version

	protected open fun getResourceBundleSuffix() = ""

	private var language = LANGUAGE.ENGLISH
	private lateinit var pluginRespirator: PluginRespirator

	private var webInterface: WebInterface? = null

}
