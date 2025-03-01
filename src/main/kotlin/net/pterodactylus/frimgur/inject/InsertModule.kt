package net.pterodactylus.frimgur.inject

import com.google.inject.AbstractModule
import com.google.inject.Provides
import freenet.client.HighLevelSimpleClient
import javax.inject.Singleton
import net.pterodactylus.frimgur.insert.DefaultInsertService
import net.pterodactylus.frimgur.insert.InsertService

/**
 * Guice [Module] that configures insert-related functionality.
 */
class InsertModule : AbstractModule() {

	@Provides
	@Singleton
	fun getInsertService(highLevelSimpleClient: HighLevelSimpleClient): InsertService =
		DefaultInsertService(highLevelSimpleClient)

}
