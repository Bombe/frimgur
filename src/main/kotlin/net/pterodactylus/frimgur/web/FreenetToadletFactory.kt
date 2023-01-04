package net.pterodactylus.frimgur.web

import freenet.client.HighLevelSimpleClient

/**
 * Factory for [FreenetToadlet] instances.
 */
class FreenetToadletFactory(private val highLevelSimpleClient: HighLevelSimpleClient) {

	/**
	 * Creates a new [FreenetToadlet] that uses the given [PageProcessor].
	 *
	 * @param pageProcessor The page processor to wrap
	 * @return A freenet toadlet that uses the given page processor
	 */
	fun createFreenetToadlet(pageProcessor: PageProcessor) = FreenetToadlet(highLevelSimpleClient, pageProcessor)

}
