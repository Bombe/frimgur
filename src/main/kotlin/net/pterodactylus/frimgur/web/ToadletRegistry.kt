package net.pterodactylus.frimgur.web

import freenet.clients.http.Toadlet
import freenet.clients.http.ToadletContainer
import freenet.pluginmanager.FredPluginL10n

/**
 * Registry for all toadlets that need to be registered by the [web interface][WebInterface].
 */
interface ToadletRegistry {

	/**
	 * Adds the given toadlet to the list of toadlets that will be registered once [start] is called.
	 *
	 * @param toadletSpec The specification of the toadlet to add
	 */
	fun addToadlet(toadletSpec: ToadletSpec)

	/**
	 * Registers all toadlets that have been added via [addToadlet].
	 */
	fun start()

	/**
	 * Unregisters all toadlets that have been added via [addToadlet].
	 */
	fun stop()

}

class DefaultToadletRegistry(private val toadletContainer: ToadletContainer, private val menuCategoryKey: String, private val pluginL10n: FredPluginL10n) : ToadletRegistry {

	override fun addToadlet(toadletSpec: ToadletSpec) {
		toadletSpecs.add(0, toadletSpec)
	}

	override fun start() {
		toadletSpecs
			.forEach { (toadlet, menuTitleKey, menuTooltipKey) ->
				toadletContainer.register(toadlet, menuCategoryKey.takeIf { menuTitleKey != null }, toadlet.path(), true, menuTitleKey, menuTooltipKey, false, null, pluginL10n)
			}
	}

	override fun stop() {
		toadletSpecs
			.map(ToadletSpec::toadlet)
			.forEach(toadletContainer::unregister)
	}

	private val toadletSpecs = mutableListOf<ToadletSpec>()

}

data class ToadletSpec(

	/** The toadlet to register. */
	val toadlet: Toadlet,

	/** The (optional) L10n key for the menu entry of this toadlet. May be `null`. */
	val menuTitleKey: String? = null,

	/** The (optional) L10n key for the menu entry tooltip of this toadlet. May be `null`. */
	val menuTooltipKey: String? = null

)
