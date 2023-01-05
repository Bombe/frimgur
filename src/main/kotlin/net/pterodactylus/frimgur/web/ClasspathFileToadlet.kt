package net.pterodactylus.frimgur.web

import freenet.client.HighLevelSimpleClient

/**
 * [File toadlet][FileToadlet] implementation that can deliver files from a location on the classpath.
 */
class ClasspathFileToadlet(stripPrefix: String, private val addPrefix: String, highLevelSimpleClient: HighLevelSimpleClient) : FileToadlet(stripPrefix, highLevelSimpleClient) {

	override fun locateFileData(file: String): FileData? =
		javaClass.getResourceAsStream(addPrefix + file)
			?.let { FileData(-1, null, it) }
			?.detectMimeType(file)

	private fun FileData.detectMimeType(filename: String) =
		mimeTypeFilters
			.firstOrNull { (filter) -> filter(filename) }
			?.let { this.copy(mimeType = it.second) }
			?: this

	fun addMimeType(mimeType: String, filenameFilter: (filename: String) -> Boolean) {
		mimeTypeFilters += (filenameFilter to mimeType)
	}

	private val mimeTypeFilters = mutableListOf<Pair<(String) -> Boolean, String>>()

}
