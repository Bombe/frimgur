package net.pterodactylus.frimgur.plugin

import freenet.l10n.BaseL10n.LANGUAGE
import freenet.pluginmanager.FredPlugin
import freenet.pluginmanager.FredPluginL10n
import freenet.pluginmanager.PluginRespirator
import java.util.Locale
import java.util.ResourceBundle

/**
 * Frimgur main plugin class.
 */
class Frimgur : FredPlugin, FredPluginL10n {

	override fun runPlugin(pluginRespirator: PluginRespirator) = Unit

	override fun terminate() = Unit

	override fun setLanguage(newLanguage: LANGUAGE) {
		this.language = newLanguage
	}

	override fun getString(key: String): String {
		return ResourceBundle.getBundle("i18n/frimgur", Locale.forLanguageTag(language.shortCode)).getString(key)
	}

	private var language = LANGUAGE.ENGLISH

}
