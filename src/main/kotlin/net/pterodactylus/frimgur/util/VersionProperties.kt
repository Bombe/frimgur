package net.pterodactylus.frimgur.util

import java.util.Properties

/**
 * Version information.
 */
data class VersionProperties(

	/** The version of the application. */
	val version: String,

	/** The hash of the commit that was built. */
	val hash: String

)

/**
 * Parses version properties from a file in the classpath.
 */
fun loadVersionProperties(versionResource: String) =
	VersionProperties::class.java.getResourceAsStream(versionResource)?.let { inputStream ->
		val properties = Properties()
		properties.load(inputStream)
		VersionProperties(properties.getProperty("version", "unknown"), properties.getProperty("hash", "unknown"))
	}
		?: VersionProperties("unknown", "unknown")

/**
 * The [version properties] of this application.
 */
val versionProperties = loadVersionProperties("/version.properties")
