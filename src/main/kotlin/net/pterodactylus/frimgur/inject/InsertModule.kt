package net.pterodactylus.frimgur.inject

import com.google.inject.AbstractModule
import com.google.inject.Provides
import freenet.client.HighLevelSimpleClient
import jakarta.inject.Singleton
import net.pterodactylus.frimgur.insert.DefaultInsertOptionService
import net.pterodactylus.frimgur.insert.DefaultInsertService
import net.pterodactylus.frimgur.insert.InsertOptionService
import net.pterodactylus.frimgur.insert.InsertService

/**
 * Guice [Module] that configures insert-related functionality.
 */
class InsertModule : AbstractModule() {

	@Provides
	@Singleton
	fun getInsertService(insertOptionService: InsertOptionService, highLevelSimpleClient: HighLevelSimpleClient): InsertService =
		DefaultInsertService(insertOptionService, highLevelSimpleClient)

	@Provides
	@Singleton
	fun getInsertOptionService(): InsertOptionService =
		DefaultInsertOptionService()

}
