package net.pterodactylus.frimgur.web

import com.spotify.hamcrest.jackson.IsJsonObject.jsonObject
import com.spotify.hamcrest.jackson.JsonMatchers.isJsonStringMatching
import com.spotify.hamcrest.jackson.JsonMatchers.jsonInt
import com.spotify.hamcrest.jackson.JsonMatchers.jsonNull
import com.spotify.hamcrest.jackson.JsonMatchers.jsonText
import freenet.client.HighLevelSimpleClient
import freenet.clients.http.ToadletContext
import freenet.support.api.HTTPRequest
import freenet.support.io.ArrayBucket
import net.pterodactylus.frimgur.image.ImageMetadata
import net.pterodactylus.frimgur.image.ImageService
import net.pterodactylus.frimgur.image.ImageStatus.Inserted
import org.hamcrest.MatcherAssert.assertThat
import org.hamcrest.Matchers.equalTo
import org.mockito.ArgumentMatchers.anyBoolean
import org.mockito.ArgumentMatchers.anyInt
import org.mockito.ArgumentMatchers.anyLong
import org.mockito.kotlin.any
import org.mockito.kotlin.argThat
import org.mockito.kotlin.argumentCaptor
import org.mockito.kotlin.eq
import org.mockito.kotlin.isNull
import org.mockito.kotlin.mock
import org.mockito.kotlin.verify
import java.net.URI
import kotlin.test.Test
import net.pterodactylus.frimgur.image.ImageStatus
import net.pterodactylus.frimgur.image.ImageStatus.Inserting
import org.hamcrest.Matchers.allOf
import org.hamcrest.Matchers.greaterThanOrEqualTo
import org.hamcrest.Matchers.lessThan
import org.mockito.hamcrest.MockitoHamcrest.intThat
import org.mockito.kotlin.whenever
import java.util.concurrent.atomic.AtomicReference

/**
 * Unit test for [ImageToadlet].
 */
class ImageToadletTest {

	@Test
	fun `toadlet returns given path`() {
		assertThat(toadlet.path(), equalTo("/test/upload"))
	}

	@Test
	fun `GET request with image ID returns data about that image`() {
		val imageService = object : ImageService {
			override fun getImage(id: String) = ImageMetadata("123", 12, 23, 34, "image/test", "image.tst", Inserted, "CHK@Test").takeIf { id == "123" }
		}
		val toadlet = ImageToadlet("/path/", imageService, highLevelSimpleClient)
		toadlet.handleMethodGET(URI("/path/123"), httpRequest, toadletContext)
		verify(toadletContext).sendReplyHeaders(eq(200), any(), isNull(), any(), anyLong(), anyBoolean())
		val body = argumentCaptor<ByteArray>()
		verify(toadletContext).writeData(body.capture(), anyInt(), anyInt())
		assertThat(
			body.firstValue.decodeToString(), isJsonStringMatching(
				jsonObject()
					.where("id", jsonText("123"))
					.where(
						"metadata", jsonObject()
							.where("width", jsonInt(12))
							.where("height", jsonInt(23))
							.where("size", jsonInt(34))
							.where("status", jsonText("Inserted"))
							.where("key", jsonText("CHK@Test"))
							.where("filename", jsonText("image.tst"))
							.where("mimeType", jsonText("image/test"))
					)
			)
		)
	}

	@Test
	fun `unset key in image metadata is rendered as JSON null`() {
		val imageService = object : ImageService {
			override fun getImage(id: String) = ImageMetadata("123", 12, 23, 34, "image/test", "image", Inserted).takeIf { id == "123" }
		}
		val toadlet = ImageToadlet("/path/", imageService, highLevelSimpleClient)
		toadlet.handleMethodGET(URI("/path/123"), httpRequest, toadletContext)
		verify(toadletContext).sendReplyHeaders(eq(200), any(), isNull(), any(), anyLong(), anyBoolean())
		val body = argumentCaptor<ByteArray>()
		verify(toadletContext).writeData(body.capture(), anyInt(), anyInt())
		assertThat(
			body.firstValue.decodeToString(), isJsonStringMatching(
				jsonObject()
					.where(
						"metadata", jsonObject()
							.where("key", jsonNull())
					)
			)
		)
	}

	@Test
	fun `GET request with image ID returns json content-type`() {
		val imageService = object : ImageService {
			override fun getImage(id: String) = ImageMetadata("123", 12, 23, 34).takeIf { id == "123" }
		}
		val toadlet = ImageToadlet("/path/", imageService, highLevelSimpleClient)
		toadlet.handleMethodGET(URI("/path/123"), httpRequest, toadletContext)
		verify(toadletContext).sendReplyHeaders(any(), any(), isNull(), eq("application/json"), anyLong(), anyBoolean())
	}

	@Test
	fun `GET request with invalid image ID results in 404`() {
		toadlet.handleMethodGET(URI("/path/123"), httpRequest, toadletContext)
		verify(toadletContext).sendReplyHeaders(eq(404), any(), isNull(), isNull(), anyLong())
	}

	@Test
	fun `toadlet returns 400 if image data is missing in POST request`() {
		val httpRequest = object : HTTPRequest by httpRequest {
			override fun getPartAsBytesFailsafe(name: String, maxLength: Int) = byteArrayOf()
		}
		toadlet.handleMethodPOST(URI(""), httpRequest, toadletContext)
		verify(toadletContext).sendReplyHeaders(eq(400), any(), any(), isNull(), anyLong())
	}

	@Test
	fun `toadlet returns 400 if image data cannot be parsed in POST request`() {
		val httpRequest = object : HTTPRequest by httpRequest {
			override fun getPartAsBytesFailsafe(name: String, maxLength: Int) = byteArrayOf(0, 1, 2, 4)
		}
		toadlet.handleMethodPOST(URI(""), httpRequest, toadletContext)
		verify(toadletContext).sendReplyHeaders(eq(400), any(), any(), isNull(), anyLong())
	}

	@Test
	fun `toadlet returns redirect if all data is present in POST request`() {
		val httpRequest = createCompleteHttpRequest()
		toadlet.handleMethodPOST(URI(""), httpRequest, toadletContext)
		verify(toadletContext).sendReplyHeaders(eq(201), any(), argThat(containsHeader("location", "id-1")), isNull(), anyLong())
	}

	private fun createCompleteHttpRequest() = object : HTTPRequest by httpRequest {
		override fun getPartAsBytesFailsafe(name: String, maxLength: Int) = when (name) {
			"image-data" -> byteArrayOf(0, 1, 2, 3)
			else -> byteArrayOf()
		}
	}

	@Test
	fun `PATCH request for invalid image ID returns 404`() {
		toadlet.handleMethodPATCH(URI("/path/123"), httpRequest, toadletContext)
		verify(toadletContext).sendReplyHeaders(eq(404), any(), isNull(), isNull(), anyLong())
	}

	@Test
	fun `PATCH request for valid image ID with empty JSON object as body returns 204`() {
		val imageService = object : ImageService {
			override fun getImage(id: String) = ImageMetadata("123", 12, 23, 34, "image/test", "image", Inserted).takeIf { id == "123" }
		}
		val toadlet = ImageToadlet("/path/", imageService, highLevelSimpleClient)
		whenever(httpRequest.rawData).thenReturn(ArrayBucket("{}".toByteArray()))
		toadlet.handleMethodPATCH(URI("/path/123"), httpRequest, toadletContext)
		verify(toadletContext).sendReplyHeaders(eq(204), any(), isNull(), isNull(), anyLong())
	}

	@Test
	fun `PATCH request for valid image ID with new status as body returns 200`() {
		val imageService = object : ImageService {
			override fun getImage(id: String) = ImageMetadata("123", 12, 23, 34, "image/test", "image", Inserted).takeIf { id == "123" }
		}
		val toadlet = ImageToadlet("/path/", imageService, highLevelSimpleClient)
		whenever(httpRequest.rawData).thenReturn(ArrayBucket("{\"status\":\"Inserting\"}".toByteArray()))
		toadlet.handleMethodPATCH(URI("/path/123"), httpRequest, toadletContext)
		verify(toadletContext).sendReplyHeaders(eq(200), any(), isNull(), isNull(), anyLong())
	}

	@Test
	fun `PATCH request for valid image ID with new status as body sets image status to Inserting`() {
		val insertingImages = AtomicReference<String>()
		val imageService = object : ImageService {
			override fun getImage(id: String) = ImageMetadata("123", 12, 23, 34, "image/test", "image", Inserted).takeIf { id == "123" }
			override fun setImageStatus(id: String, status: ImageStatus) = if (status == Inserting) { insertingImages.set(id) } else {}
		}
		val toadlet = ImageToadlet("/path/", imageService, highLevelSimpleClient)
		whenever(httpRequest.rawData).thenReturn(ArrayBucket("{\"status\":\"Inserting\"}".toByteArray()))
		toadlet.handleMethodPATCH(URI("/path/123"), httpRequest, toadletContext)
		assertThat(insertingImages.get(), equalTo("123"))
	}

	@Test
	fun `PATCH request for valid image ID with new filename as body sets image filename`() {
		data class Arguments(val id: String, val filename: String)
		val imageFilenames = mutableListOf<Arguments>()
		val imageService = object : ImageService {
			override fun getImage(id: String) = ImageMetadata("123", 12, 23, 34, "image/test", "image", Inserted).takeIf { id == "123" }
			override fun setImageFilename(id: String, filename: String) = imageFilenames.add(Arguments(id, filename)).let { }
		}
		val toadlet = ImageToadlet("/path/", imageService, highLevelSimpleClient)
		whenever(httpRequest.rawData).thenReturn(ArrayBucket("{\"filename\":\"new-filename.png\"}".toByteArray()))
		toadlet.handleMethodPATCH(URI("/path/123"), httpRequest, toadletContext)
		assertThat(imageFilenames.single(), equalTo(Arguments("123", "new-filename.png")))
	}

	@Test
	fun `PATCH request with width will call image service with width only`() {
		data class Arguments(val id: String, val mimeType: String?, val width: Int?, val height: Int?)
		val receivedArguments = mutableListOf<Arguments>()
		val imageService = createCloningImageServiceThatRecordsArguments(125) { id, mimeType, width, height -> receivedArguments.add(Arguments(id, mimeType, width, height)) }
		val toadlet = ImageToadlet("/path/", imageService, highLevelSimpleClient)
		whenever(httpRequest.rawData).thenReturn(ArrayBucket("{\"width\":\"500\"}".toByteArray()))
		toadlet.handleMethodPATCH(URI("/path/125"), httpRequest, toadletContext)
		assertThat(receivedArguments.single(), equalTo(Arguments("125", null, 500, null)))
	}

	@Test
	fun `PATCH request with width will respond with a success response code and the location of the new image`() {
		val imageService = createCloningImageServiceReturningImageMetadataWidthId(126)
		val toadlet = ImageToadlet("/path/", imageService, highLevelSimpleClient)
		whenever(httpRequest.rawData).thenReturn(ArrayBucket("{\"width\":\"500\"}".toByteArray()))
		toadlet.handleMethodPATCH(URI("/path/126"), httpRequest, toadletContext)
		verify(toadletContext).sendReplyHeaders(intThat(allOf(greaterThanOrEqualTo(200), lessThan(300))), any(), argThat(containsHeader("location", "126-2")), isNull(), anyLong())
	}

	@Test
	fun `PATCH request with height will call image service with height only`() {
		data class Arguments(val id: String, val mimeType: String?, val width: Int?, val height: Int?)
		val receivedArguments = mutableListOf<Arguments>()
		val imageService = createCloningImageServiceThatRecordsArguments(127) { id, mimeType, width, height -> receivedArguments.add(Arguments(id, mimeType, width, height)) }
		val toadlet = ImageToadlet("/path/", imageService, highLevelSimpleClient)
		whenever(httpRequest.rawData).thenReturn(ArrayBucket("{\"height\":\"500\"}".toByteArray()))
		toadlet.handleMethodPATCH(URI("/path/127"), httpRequest, toadletContext)
		assertThat(receivedArguments.single(), equalTo(Arguments("127", null, null, 500)))
	}

	@Test
	fun `PATCH request with height will respond with a success response code and the location of the new image`() {
		val imageService = createCloningImageServiceReturningImageMetadataWidthId(128)
		val toadlet = ImageToadlet("/path/", imageService, highLevelSimpleClient)
		whenever(httpRequest.rawData).thenReturn(ArrayBucket("{\"height\":\"500\"}".toByteArray()))
		toadlet.handleMethodPATCH(URI("/path/128"), httpRequest, toadletContext)
		verify(toadletContext).sendReplyHeaders(intThat(allOf(greaterThanOrEqualTo(200), lessThan(300))), any(), argThat(containsHeader("location", "128-2")), isNull(), anyLong())
	}

	@Test
	fun `PATCH request with width and height will call image service with width and height`() {
		data class Arguments(val id: String, val mimeType: String?, val width: Int?, val height: Int?)
		val receivedArguments = mutableListOf<Arguments>()
		val imageService = createCloningImageServiceThatRecordsArguments(129) { id, mimeType, width, height -> receivedArguments.add(Arguments(id, mimeType, width, height)) }
		val toadlet = ImageToadlet("/path/", imageService, highLevelSimpleClient)
		whenever(httpRequest.rawData).thenReturn(ArrayBucket("{\"width\":\"400\",\"height\":\"600\"}".toByteArray()))
		toadlet.handleMethodPATCH(URI("/path/129"), httpRequest, toadletContext)
		assertThat(receivedArguments.single(), equalTo(Arguments("129", null, 400, 600)))
	}

	@Test
	fun `PATCH request with width and height will respond with a success response code and the location of the new image`() {
		val imageService = createCloningImageServiceReturningImageMetadataWidthId(130)
		val toadlet = ImageToadlet("/path/", imageService, highLevelSimpleClient)
		whenever(httpRequest.rawData).thenReturn(ArrayBucket("{\"width\":\"400\",\"height\":\"600\"}".toByteArray()))
		toadlet.handleMethodPATCH(URI("/path/130"), httpRequest, toadletContext)
		verify(toadletContext).sendReplyHeaders(intThat(allOf(greaterThanOrEqualTo(200), lessThan(300))), any(), argThat(containsHeader("location", "130-2")), isNull(), anyLong())
	}

	private fun createCloningImageServiceThatRecordsArguments(imageId: Int, argumentsReceived: (id: String, mimeType: String?, width: Int?, height: Int?) -> Unit) = object : ImageService {
		override fun getImage(id: String) = ImageMetadata("$imageId", 12, 23, 34, "image/test", "image", Inserted).takeIf { id == "$imageId" }
		override fun cloneImage(id: String, mimeType: String?, width: Int?, height: Int?) =
			argumentsReceived(id, mimeType, width, height).let { null }
	}

	private fun createCloningImageServiceReturningImageMetadataWidthId(imageId: Int) = object : ImageService {
		override fun getImage(id: String) = ImageMetadata("$imageId", 12, 23, 34, "image/test", "image", Inserted).takeIf { id == "$imageId" }
		override fun cloneImage(id: String, mimeType: String?, width: Int?, height: Int?) =
			ImageMetadata("${imageId}-2", 1, 1, 1).takeIf { id == "$imageId" }
	}

	@Test
	fun `DELETE request calls image service`() {
		val removedImages = mutableListOf<String>()
		val imageService = object : ImageService {
			override fun removeImage(id: String) = removedImages.add(id).let {}
		}
		val toadlet = ImageToadlet("/path/", imageService, highLevelSimpleClient)
		toadlet.handleMethodDELETE(URI("/path/124"), httpRequest, toadletContext)
		assertThat(removedImages.single(), equalTo("124"))
	}

	@Test
	fun `DELETE request returns 204`() {
		toadlet.handleMethodDELETE(URI("/path/123"), httpRequest, toadletContext)
		verify(toadletContext).sendReplyHeaders(eq(204), any(), isNull(), isNull(), anyLong())
	}

	private val httpRequest = mock<HTTPRequest>()
	private val toadletContext = mock<ToadletContext>()
	private val imageService = object : ImageService {
		override fun addImage(data: ByteArray) =
			ImageMetadata("id-1", 720, 576, 1234)
				.takeIf { data.contentEquals(byteArrayOf(0, 1, 2, 3)) }
	}
	private val highLevelSimpleClient = mock<HighLevelSimpleClient>()
	private val toadlet = ImageToadlet("/test/upload", imageService, highLevelSimpleClient)

}
