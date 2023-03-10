package net.pterodactylus.frimgur.insert

import freenet.client.ClientMetadata
import freenet.client.HighLevelSimpleClient
import freenet.client.InsertBlock
import freenet.client.InsertException
import freenet.client.async.BaseClientPutter
import freenet.client.async.ClientContext
import freenet.client.async.ClientPutCallback
import freenet.keys.FreenetURI
import freenet.node.RequestClient
import freenet.node.RequestClientBuilder
import freenet.node.RequestStarter.MAXIMUM_PRIORITY_CLASS
import freenet.support.api.Bucket
import freenet.support.io.ArrayBucket

/**
 * Service that inserts images into Freenet.
 */
interface InsertService {

	/**
	 * Inserts the given image into Freenet.
	 *
	 * @param id The ID of the image
	 * @param data The data of the image
	 * @param mimeType The MIME type of the image
	 */
	fun insertImage(id: String, data: ByteArray, mimeType: String) = Unit

	/**
	 * Adds a listener that will be notified when the insert of the image
	 * with the given ID is started.
	 *
	 * @param listener Listener for image-insertion-started events
	 */
	fun onInsertStarting(listener: (id: String) -> Unit) = Unit

	fun onInsertGeneratingUri(listener: (id: String, uri: String) -> Unit) = Unit

	fun onInsertFinished(listener: (id: String) -> Unit) = Unit

	fun onInsertFailed(listener: (id: String) -> Unit) = Unit

}

/**
 * Default [InsertService] implementation.
 */
class DefaultInsertService(private val highLevelSimpleClient: HighLevelSimpleClient) : InsertService {

	override fun insertImage(id: String, data: ByteArray, mimeType: String) {
		val insertBlock = InsertBlock(data.toBucket(), ClientMetadata(mimeType), FreenetURI("CHK@"))
		val insertContext = highLevelSimpleClient.getInsertContext(false)
		insertStartingListeners.forEach { listener -> listener(id) }
		highLevelSimpleClient.insert(insertBlock, createFilenameForMimeType(mimeType), false, insertContext, object : ClientPutCallback by getEmptyClientPutCallback(requestClient) {
			override fun onGeneratedURI(freenetURI: FreenetURI, p1: BaseClientPutter) {
				insertGeneratingUriListeners.forEach { listener -> listener(id, freenetURI.toString()) }
			}

			override fun onSuccess(baseClientPutter: BaseClientPutter) {
				insertFinishedListeners.forEach { listener -> listener(id) }
			}

			override fun onFailure(p0: InsertException?, p1: BaseClientPutter?) {
				insertFailedListeners.forEach { listener -> listener(id) }
			}
		}, MAXIMUM_PRIORITY_CLASS)
	}

	private fun createFilenameForMimeType(mimeType: String) = when (mimeType) {
		"image/png" -> "image.png"
		"image/jpeg" -> "image.jpg"
		"image/bmp" -> "image.bmp"
		"image/gif" -> "image.gif"
		else -> "image"
	}

	override fun onInsertStarting(listener: (id: String) -> Unit) {
		insertStartingListeners += listener
	}

	override fun onInsertGeneratingUri(listener: (id: String, uri: String) -> Unit) {
		insertGeneratingUriListeners += listener
	}

	override fun onInsertFinished(listener: (id: String) -> Unit) {
		insertFinishedListeners += listener
	}

	override fun onInsertFailed(listener: (id: String) -> Unit) {
		insertFailedListeners += listener
	}

	private val insertStartingListeners = mutableListOf<(String) -> Unit>()
	private val insertGeneratingUriListeners = mutableListOf<(String, String) -> Unit>()
	private val insertFinishedListeners = mutableListOf<(String) -> Unit>()
	private val insertFailedListeners = mutableListOf<(String) -> Unit>()
	private val requestClient = RequestClientBuilder().realTime().build()

}

private fun ByteArray.toBucket() = ArrayBucket(this)

private fun getEmptyClientPutCallback(requestClient: RequestClient) = object : ClientPutCallback {
	override fun onResume(clientContext: ClientContext) = Unit
	override fun getRequestClient() = requestClient
	override fun onGeneratedURI(freenetURI: FreenetURI, baseClientPutter: BaseClientPutter) = Unit
	override fun onGeneratedMetadata(bucket: Bucket, baseClientPutter: BaseClientPutter) = Unit
	override fun onFetchable(baseClientPutter: BaseClientPutter) = Unit
	override fun onSuccess(baseClientPutter: BaseClientPutter) = Unit
	override fun onFailure(insertException: InsertException, baseClientPutter: BaseClientPutter) = Unit
}
