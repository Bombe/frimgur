package net.pterodactylus.frimgur.inject

import com.google.inject.AbstractModule
import com.google.inject.Provides
import freenet.client.HighLevelSimpleClient
import freenet.clients.http.PageMaker
import freenet.clients.http.Toadlet
import freenet.clients.http.ToadletContainer
import freenet.pluginmanager.FredPluginL10n
import net.pterodactylus.frimgur.image.ImageService
import net.pterodactylus.frimgur.web.ClasspathFileToadlet
import net.pterodactylus.frimgur.web.DefaultToadletRegistry
import net.pterodactylus.frimgur.web.DefaultWebInterface
import net.pterodactylus.frimgur.web.FreenetToadletFactory
import net.pterodactylus.frimgur.web.ImageDataToadlet
import net.pterodactylus.frimgur.web.ImageToadlet
import net.pterodactylus.frimgur.web.ListImagesToadlet
import net.pterodactylus.frimgur.web.MainPageProcessor
import net.pterodactylus.frimgur.web.RedirectToadlet
import net.pterodactylus.frimgur.web.ToadletRegistry
import net.pterodactylus.frimgur.web.ToadletSpec
import net.pterodactylus.frimgur.web.WebInterface
import java.util.Locale
import javax.inject.Named
import javax.inject.Provider

/**
 * Guice [module][com.google.inject.Module] for creating [WebInterface] instances.
 */
class WebInterfaceModule(private val prefix: String, private val menuCategoryKey: String, private val menuCategoryTooltipKey: String) : AbstractModule() {

	@Provides
	fun getFreenetToadletFactory(highLevelSimpleClient: HighLevelSimpleClient): FreenetToadletFactory =
		FreenetToadletFactory(prefix, highLevelSimpleClient)

	@Provides
	fun getToadletRegistry(
		toadletContainer: ToadletContainer,
		pluginL10n: FredPluginL10n,
		@Named("Main") mainToadlet: Toadlet,
		@Named("StaticJavascript") staticJavascriptToadlet: Toadlet,
		@Named("StaticCss") staticCssToadlet: Toadlet,
		@Named("ListImages") listImagesToadlet: Toadlet,
		@Named("ImageData") imageDataToadlet: Toadlet,
		@Named("Upload") uploadToadlet: Toadlet,
		@Named("Redirect") redirectToadlet: Toadlet,
	): ToadletRegistry =
		DefaultToadletRegistry(toadletContainer, menuCategoryKey, pluginL10n).apply {
			addToadlet(ToadletSpec(staticJavascriptToadlet))
			addToadlet(ToadletSpec(staticCssToadlet))
			addToadlet(ToadletSpec(listImagesToadlet))
			addToadlet(ToadletSpec(imageDataToadlet))
			addToadlet(ToadletSpec(uploadToadlet))
			addToadlet(ToadletSpec(mainToadlet, "Navigation.Main.Title", "Navigation.Main.Tooltip"))
			addToadlet(ToadletSpec(redirectToadlet))
		}

	@Provides
	fun getWebInterface(pageMaker: PageMaker, pluginL10n: FredPluginL10n, toadletRegistry: ToadletRegistry): WebInterface =
		DefaultWebInterface(menuCategoryKey, menuCategoryTooltipKey, pageMaker, pluginL10n, toadletRegistry)

	@Provides
	@Named("Main")
	fun getMainToadlet(freenetToadletFactory: FreenetToadletFactory, @Named("FormPassword") formPassword: String, localeProvider: Provider<Locale>): Toadlet =
		freenetToadletFactory.createFreenetToadlet(MainPageProcessor(formPassword, localeProvider::get))

	@Provides
	@Named("StaticJavascript")
	fun getStaticJavascriptToadlet(highLevelSimpleClient: HighLevelSimpleClient): Toadlet =
		ClasspathFileToadlet(prefix + "static/js/", "/static/js/", highLevelSimpleClient).apply {
			addMimeType("application/javascript") { filename -> filename.endsWith(".js", ignoreCase = true) }
		}

	@Provides
	@Named("StaticCss")
	fun getStaticCssToadlet(highLevelSimpleClient: HighLevelSimpleClient): Toadlet =
		ClasspathFileToadlet(prefix + "static/css/", "/static/css/", highLevelSimpleClient).apply {
			addMimeType("text/css") { filename -> filename.endsWith(".css", ignoreCase = true) }
		}

	@Provides
	@Named("Redirect")
	fun getRedirectToadlet(highLevelSimpleClient: HighLevelSimpleClient): Toadlet =
		RedirectToadlet(prefix, prefix + "index.html", highLevelSimpleClient)

	@Provides
	@Named("ListImages")
	fun getListImagesToadlet(imageService: ImageService, highLevelSimpleClient: HighLevelSimpleClient): Toadlet =
		ListImagesToadlet(prefix + "images/", imageService, highLevelSimpleClient)

	@Provides
	@Named("ImageData")
	fun getImageDataToadlet(imageService: ImageService, highLevelSimpleClient: HighLevelSimpleClient): Toadlet =
		ImageDataToadlet(prefix + "image-data/", imageService, highLevelSimpleClient)

	@Provides
	@Named("Upload")
	fun getUploadToadlet(imageService: ImageService, highLevelSimpleClient: HighLevelSimpleClient): Toadlet =
		ImageToadlet(prefix + "image/", imageService, highLevelSimpleClient)

}
