package net.pterodactylus.frimgur.web

import net.pterodactylus.frimgur.web.annotations.ToadletName

/**
 * Frimgur main page.
 */
@ToadletName("/frimgur/")
class MainPageProcessor : PebblePageProcessor("html/main.html") {

	override fun getTitle(pageRequest: PageRequest) = "Frimgur Main Page"

}
