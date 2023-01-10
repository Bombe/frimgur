package net.pterodactylus.frimgur.insert

import freenet.client.HighLevelSimpleClient
import freenet.client.InsertBlock
import freenet.client.InsertContext
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
