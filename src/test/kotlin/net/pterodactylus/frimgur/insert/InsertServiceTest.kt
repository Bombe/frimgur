package net.pterodactylus.frimgur.insert

import freenet.client.HighLevelSimpleClient
import freenet.client.InsertBlock
import freenet.client.InsertContext
import freenet.client.async.ClientPutCallback
import freenet.keys.FreenetURI
import freenet.keys.FreenetURI.EMPTY_CHK_URI
import freenet.node.RequestStarter.MAXIMUM_PRIORITY_CLASS
import freenet.support.api.Bucket
import freenet.support.io.BucketTools
import org.hamcrest.MatcherAssert.assertThat
import org.hamcrest.Matchers.contains
import org.hamcrest.Matchers.equalTo
import org.mockito.AdditionalMatchers.or
import org.mockito.ArgumentMatchers.anyBoolean
import org.mockito.ArgumentMatchers.anyShort
import org.mockito.ArgumentMatchers.isNull
import org.mockito.kotlin.any
import org.mockito.kotlin.argumentCaptor
import org.mockito.kotlin.eq
import org.mockito.kotlin.mock
import org.mockito.kotlin.verify
import org.mockito.kotlin.whenever
import kotlin.test.Test

/**
 * Unit test for [InsertService].
 */
class InsertServiceTest {

	@Test
	fun `insert service can insert image`() {
		insertService.insertImage("id1", byteArrayOf(0, 1, 2, 3), "image/test")
		verify(highLevelSimpleClient).insert(anyOrNull(), anyOrNull(), anyBoolean(), anyOrNull(), anyOrNull(), anyShort())
	}

	@Test
	fun `insert service sets correct data for insert`() {
		insertService.insertImage("id1", byteArrayOf(0, 1, 2, 3), "image/test")
		val insertBlock = argumentCaptor<InsertBlock>()
		verify(highLevelSimpleClient).insert(insertBlock.capture(), anyOrNull(), anyBoolean(), anyOrNull(), anyOrNull(), anyShort())
		assertThat(insertBlock.firstValue.data.toByteArray(), equalTo(byteArrayOf(0, 1, 2, 3)))
	}

	@Test
	fun `insert service sets correct mime type for insert`() {
		insertService.insertImage("id1", byteArrayOf(0, 1, 2, 3), "image/test")
		val insertBlock = argumentCaptor<InsertBlock>()
		verify(highLevelSimpleClient).insert(insertBlock.capture(), anyOrNull(), anyBoolean(), anyOrNull(), anyOrNull(), anyShort())
		assertThat(insertBlock.firstValue.clientMetadata.mimeType, equalTo("image/test"))
	}

	@Test
	fun `insert service sets correct target URI for insert`() {
		insertService.insertImage("id1", byteArrayOf(0, 1, 2, 3), "image/test")
		val insertBlock = argumentCaptor<InsertBlock>()
		verify(highLevelSimpleClient).insert(insertBlock.capture(), anyOrNull(), anyBoolean(), anyOrNull(), anyOrNull(), anyShort())
		assertThat(insertBlock.firstValue.desiredURI, equalTo(EMPTY_CHK_URI))
	}

	@Test
	fun `insert service sets correct target filename for png insert`() {
		verifyFilenameHint("image/png", "image.png")
	}

	@Test
	fun `insert service sets correct target filename for jpeg insert`() {
		verifyFilenameHint("image/jpeg", "image.jpg")
	}

	@Test
	fun `insert service sets correct target filename for bmp insert`() {
		verifyFilenameHint("image/bmp", "image.bmp")
	}

	@Test
	fun `insert service sets correct target filename for gif insert`() {
		verifyFilenameHint("image/gif", "image.gif")
	}

	@Test
	fun `insert service sets default target filename for unknown mime type`() {
		verifyFilenameHint("image/test", "image")
	}

	private fun verifyFilenameHint(mimeType: String, expectedFilename: String) {
		insertService.insertImage("id1", byteArrayOf(0, 1, 2, 3), mimeType)
		val filenameHint = argumentCaptor<String>()
		verify(highLevelSimpleClient).insert(anyOrNull(), filenameHint.capture(), anyBoolean(), anyOrNull(), anyOrNull(), anyShort())
		assertThat(filenameHint.firstValue, equalTo(expectedFilename))
	}

	@Test
	fun `insert service sets correct priority for insert`() {
		insertService.insertImage("id1", byteArrayOf(0, 1, 2, 3), "image/test")
		verify(highLevelSimpleClient).insert(anyOrNull(), anyOrNull(), anyBoolean(), anyOrNull(), anyOrNull(), eq(MAXIMUM_PRIORITY_CLASS))
	}

	@Test
	fun `insert service sets insert context for insert`() {
		insertService.insertImage("id1", byteArrayOf(0, 1, 2, 3), "image/test")
		verify(highLevelSimpleClient).insert(anyOrNull(), anyOrNull(), anyBoolean(), eq(insertContext), anyOrNull(), anyShort())
	}

	@Test
	fun `insert service specifies callback for insert`() {
		insertService.insertImage("id1", byteArrayOf(0, 1, 2, 3), "image/test")
		verify(highLevelSimpleClient).insert(anyOrNull(), anyOrNull(), anyBoolean(), anyOrNull(), any(), anyShort())
	}

	@Test
	fun `insert service notifies listener when insert is started`() {
		insertService.insertImage("id1", byteArrayOf(0, 1, 2, 3), "image/test")
		assertThat(insertStartedIds, contains("id1"))
	}

	@Test
	fun `insert service notifies listener when insert has generated URI`() {
		val generatedUris = mutableListOf<Pair<String, String>>()
		insertService.onInsertGeneratingUri { id, uri -> generatedUris += id to uri }
		insertService.insertImage("id1", byteArrayOf(0, 1, 2, 3), "image/test")
		val clientPutCallback = argumentCaptor<ClientPutCallback>()
		verify(highLevelSimpleClient).insert(anyOrNull(), anyOrNull(), anyBoolean(), anyOrNull(), clientPutCallback.capture(), anyShort())
		clientPutCallback.firstValue.onGeneratedURI(FreenetURI("KSK@Test"), mock())
		assertThat(generatedUris, contains("id1" to "KSK@Test"))
	}

	@Test
	fun `insert service notifies listener when insert has finished`() {
		val finishedIds = mutableListOf<String>()
		insertService.onInsertFinished { id -> finishedIds += id }
		insertService.insertImage("id1", byteArrayOf(0, 1, 2, 3), "image/test")
		val clientPutCallback = argumentCaptor<ClientPutCallback>()
		verify(highLevelSimpleClient).insert(anyOrNull(), anyOrNull(), anyBoolean(), anyOrNull(), clientPutCallback.capture(), anyShort())
		clientPutCallback.firstValue.onSuccess(mock())
		assertThat(finishedIds, contains("id1"))
	}

	@Test
	fun `insert service notifies listener when insert has failed`() {
		val failedIds = mutableListOf<String>()
		insertService.onInsertFailed { id -> failedIds += id }
		insertService.insertImage("id1", byteArrayOf(0, 1, 2, 3), "image/test")
		val clientPutCallback = argumentCaptor<ClientPutCallback>()
		verify(highLevelSimpleClient).insert(anyOrNull(), anyOrNull(), anyBoolean(), anyOrNull(), clientPutCallback.capture(), anyShort())
		clientPutCallback.firstValue.onFailure(mock(), mock())
		assertThat(failedIds, contains("id1"))
	}

	private val insertContext = mock<InsertContext>()
	private val highLevelSimpleClient = mock<HighLevelSimpleClient>().apply {
		whenever(getInsertContext(anyBoolean())).thenReturn(insertContext)
	}
	private val insertStartedIds = mutableListOf<String>()
	private val insertService = DefaultInsertService(highLevelSimpleClient).apply {
		onInsertStarting(insertStartedIds::add)
	}

}

private inline fun <reified T : Any> anyOrNull() = or(any<T>(), isNull())
private fun Bucket.toByteArray() = BucketTools.toByteArray(this)
