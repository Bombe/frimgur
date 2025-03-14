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
import java.net.URI
import kotlin.test.Test
import net.pterodactylus.frimgur.image.GeneratedImageMetadata
import net.pterodactylus.frimgur.image.ImageData
import net.pterodactylus.frimgur.image.ImageMetadata
import net.pterodactylus.frimgur.image.ImageService
import net.pterodactylus.frimgur.image.ImageStatus
import net.pterodactylus.frimgur.image.ImageStatus.Inserted
import net.pterodactylus.frimgur.insert.InsertService
import org.hamcrest.MatcherAssert.assertThat
import org.hamcrest.Matchers.allOf
import org.hamcrest.Matchers.contains
import org.hamcrest.Matchers.containsInRelativeOrder
import org.hamcrest.Matchers.equalTo
import org.hamcrest.Matchers.greaterThanOrEqualTo
import org.hamcrest.Matchers.lessThan
import org.mockito.ArgumentMatchers.anyBoolean
import org.mockito.ArgumentMatchers.anyInt
import org.mockito.ArgumentMatchers.anyLong
import org.mockito.hamcrest.MockitoHamcrest.intThat
import org.mockito.kotlin.any
import org.mockito.kotlin.argThat
import org.mockito.kotlin.argumentCaptor
import org.mockito.kotlin.eq
import org.mockito.kotlin.isNull
import org.mockito.kotlin.mock
import org.mockito.kotlin.verify
import org.mockito.kotlin.whenever

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
			override fun getImage(id: String) = ImageMetadata("123", 12, 23, "image.tst", Inserted, GeneratedImageMetadata("CHK@Test", "image.tst.jpg")).takeIf { id == "123" }
		}
		val toadlet = ImageToadlet("/path/", imageService, insertService, highLevelSimpleClient)
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
							.where("status", jsonText("Inserted"))
							.where("key", jsonText("CHK@Test"))
							.where("filename", jsonText("image.tst"))
							.where("insertFilename", jsonText("image.tst.jpg"))
					)
			)
		)
	}

	@Test
	fun `unset generated metadata in image metadata is rendered as JSON null`() {
		val imageService = object : ImageService {
			override fun getImage(id: String) = ImageMetadata("123", 12, 23, "image", Inserted).takeIf { id == "123" }
		}
		val toadlet = ImageToadlet("/path/", imageService, insertService, highLevelSimpleClient)
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
							.where("insertFilename", jsonNull())
					)
			)
		)
	}

	@Test
	fun `GET request with image ID returns json content-type`() {
		val imageService = object : ImageService {
			override fun getImage(id: String) = ImageMetadata("123", 12, 23).takeIf { id == "123" }
		}
		val toadlet = ImageToadlet("/path/", imageService, insertService, highLevelSimpleClient)
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
			override fun getImage(id: String) = ImageMetadata("123", 12, 23, "image", Inserted).takeIf { id == "123" }
		}
		val toadlet = ImageToadlet("/path/", imageService, insertService, highLevelSimpleClient)
		whenever(httpRequest.rawData).thenReturn(ArrayBucket("{}".toByteArray()))
		toadlet.handleMethodPATCH(URI("/path/123"), httpRequest, toadletContext)
		verify(toadletContext).sendReplyHeaders(eq(204), any(), isNull(), isNull(), anyLong())
	}

	@Test
	fun `PATCH request for valid image ID with new status as body returns 200`() {
		val imageService = object : ImageService {
			override fun getImage(id: String) = ImageMetadata("123", 12, 23, "image", Inserted).takeIf { id == "123" }
			override fun getImageData(id: String) = if (id == "123") ImageData(getImage(id)!!, ByteArray(5) { i -> i.toByte() }) else null
		}
		val toadlet = ImageToadlet("/path/", imageService, insertService, highLevelSimpleClient)
		whenever(httpRequest.rawData).thenReturn(ArrayBucket("{\"status\":\"Inserting\"}".toByteArray()))
		toadlet.handleMethodPATCH(URI("/path/123"), httpRequest, toadletContext)
		verify(toadletContext).sendReplyHeaders(eq(200), any(), isNull(), isNull(), anyLong())
	}

	@Test
	fun `PATCH request with status 'Inserting' and without type and filename without suffix starts image insert as PNG`() {
		handlePatchWithPreviouslySetFilename("134", "image", "image/png", "image.png")
	}

	@Test
	fun `PATCH request with status 'Inserting' and filename without suffix starts image insert as PNG`() {
		handlePatchWithExplicitFilename("130", "file.name", "image/png", "file.name.png")
	}

	@Test
	fun `PATCH request with status 'Inserting' and previously-set filename with png suffix starts image insert as PNG`() {
		handlePatchWithPreviouslySetFilename("133", "image.png", "image/png", "image.png")
	}

	@Test
	fun `PATCH request with status 'Inserting' and filename with png suffix starts image insert as PNG`() {
		handlePatchWithExplicitFilename("129", "image.png", "image/png", "image.png")
	}

	@Test
	fun `PATCH request with status 'Inserting' and previously-set filename with jpg suffix starts image insert as JPEG`() {
		handlePatchWithPreviouslySetFilename("132", "image.jpg", "image/jpeg", "image.jpg")
	}

	@Test
	fun `PATCH request with status 'Inserting' and filename with jpg suffix starts image insert as JPEG`() {
		handlePatchWithExplicitFilename("128", "image.jpg", "image/jpeg", "image.jpg")
	}

	@Test
	fun `PATCH request with status 'Inserting' and previously-set filename with jpeg suffix starts image insert as JPEG`() {
		handlePatchWithPreviouslySetFilename("131", "image.jpeg", "image/jpeg", "image.jpeg")
	}

	@Test
	fun `PATCH request with status 'Inserting' and filename with jpeg suffix starts image insert as JPEG`() {
		handlePatchWithExplicitFilename("127", "image.jpeg", "image/jpeg", "image.jpeg")
	}

	private fun handlePatchWithPreviouslySetFilename(id: String, givenFilename: String, expectedMimeType: String, expectedFilename: String) {
		handlePatchWithFilename(id, givenFilename, null, expectedMimeType, expectedFilename)
	}

	private fun handlePatchWithExplicitFilename(id: String, givenFilename: String, expectedMimeType: String, expectedFilename: String) {
		handlePatchWithFilename(id, null, givenFilename, expectedMimeType, expectedFilename)
	}

	private fun handlePatchWithFilename(id: String, previouslySetFilename: String?, explicitFilename: String?, expectedMimeType: String, expectedFilename: String) {
		val imageData = ByteArray(5) { i -> i.toByte() }
		val imageService = createImageServiceDeliveringImageAndStoringFilename(id, imageData, previouslySetFilename ?: "image")
		data class Arguments(val id: String, val data: ByteArray, val mimeType: String, val filename: String)
		val receivedArguments = mutableListOf<Arguments>()
		val insertService = createInsertServiceThatRecordsArguments(::Arguments, receivedArguments::add)
		val toadlet = ImageToadlet("/path/", imageService, insertService, highLevelSimpleClient)
		whenever(httpRequest.rawData).thenReturn(ArrayBucket("{\"status\":\"Inserting\"${explicitFilename?.let { ",\"filename\":\"$it\"" } ?: ""}}".toByteArray()))
		toadlet.handleMethodPATCH(URI("/path/$id"), httpRequest, toadletContext)
		assertThat(receivedArguments, contains(Arguments(id, imageData, expectedMimeType, expectedFilename)))
	}

	private fun createImageServiceDeliveringImageAndStoringFilename(imageId: String, data: ByteArray, filename: String = "image") = object : ImageService {
		private var filename: String = filename
		override fun getImage(id: String) = ImageMetadata(imageId, 12, 23, this.filename, Inserted).takeIf { id == imageId }
		override fun getImageData(id: String) = if (id == imageId) ImageData(getImage(id)!!, data) else null
		override fun setImageFilename(id: String, filename: String) = if (id == imageId) this.filename = filename else {}
	}

	private fun <A> createInsertServiceThatRecordsArguments(argumentCreator: (id: String, data: ByteArray, mimeType: String, filename: String) -> A, argumentRecorder: (argument: A) -> Unit) = object : InsertService {
		override fun insertImage(id: String, data: ByteArray, mimeType: String, filename: String) =
			argumentCreator(id, data, mimeType, filename).run(argumentRecorder)
	}

	@Test
	fun `PATCH request for valid image ID with new filename as body sets image filename`() {
		data class Arguments(val id: String, val filename: String)
		val imageFilenames = mutableListOf<Arguments>()
		val imageService = object : ImageService {
			override fun getImage(id: String) = ImageMetadata("123", 12, 23, "image", Inserted).takeIf { id == "123" }
			override fun setImageFilename(id: String, filename: String) = imageFilenames.add(Arguments(id, filename)).let { }
		}
		val toadlet = ImageToadlet("/path/", imageService, insertService, highLevelSimpleClient)
		whenever(httpRequest.rawData).thenReturn(ArrayBucket("{\"filename\":\"new-filename.png\"}".toByteArray()))
		toadlet.handleMethodPATCH(URI("/path/123"), httpRequest, toadletContext)
		assertThat(imageFilenames.single(), equalTo(Arguments("123", "new-filename.png")))
	}

	@Test
	fun `PATCH request with width will call image service with width only`() {
		data class Arguments(val id: String, val mimeType: String?, val width: Int?, val height: Int?)
		val receivedArguments = mutableListOf<Arguments>()
		val imageService = createCloningImageServiceThatRecordsArguments(125) { id, mimeType, width, height -> receivedArguments.add(Arguments(id, mimeType, width, height)) }
		val toadlet = ImageToadlet("/path/", imageService, insertService, highLevelSimpleClient)
		whenever(httpRequest.rawData).thenReturn(ArrayBucket("{\"width\":\"500\"}".toByteArray()))
		toadlet.handleMethodPATCH(URI("/path/125"), httpRequest, toadletContext)
		assertThat(receivedArguments.single(), equalTo(Arguments("125", null, 500, null)))
	}

	@Test
	fun `PATCH request with width and filename will set the filename before cloning`() {
		val imageService = recordOrderOfCalls(createCloningImageServiceThatRecordsArguments(125) { _, _, _, _ -> })
		val toadlet = ImageToadlet("/path/", imageService, insertService, highLevelSimpleClient)
		whenever(httpRequest.rawData).thenReturn(ArrayBucket("{\"width\":\"500\",\"filename\":\"file.name\"}".toByteArray()))
		toadlet.handleMethodPATCH(URI("/path/125"), httpRequest, toadletContext)
		assertThat(imageService.calledMethods, containsInRelativeOrder("setImageFilename", "cloneImage"))
	}

	@Test
	fun `PATCH request with width will respond with a success response code and the location of the new image`() {
		val imageService = createCloningImageServiceReturningImageMetadataWidthId(126)
		val toadlet = ImageToadlet("/path/", imageService, insertService, highLevelSimpleClient)
		whenever(httpRequest.rawData).thenReturn(ArrayBucket("{\"width\":\"500\"}".toByteArray()))
		toadlet.handleMethodPATCH(URI("/path/126"), httpRequest, toadletContext)
		verify(toadletContext).sendReplyHeaders(intThat(allOf(greaterThanOrEqualTo(200), lessThan(300))), any(), argThat(containsHeader("location", "126-2")), isNull(), anyLong())
	}

	@Test
	fun `PATCH request with height will call image service with height only`() {
		data class Arguments(val id: String, val mimeType: String?, val width: Int?, val height: Int?)
		val receivedArguments = mutableListOf<Arguments>()
		val imageService = createCloningImageServiceThatRecordsArguments(127) { id, mimeType, width, height -> receivedArguments.add(Arguments(id, mimeType, width, height)) }
		val toadlet = ImageToadlet("/path/", imageService, insertService, highLevelSimpleClient)
		whenever(httpRequest.rawData).thenReturn(ArrayBucket("{\"height\":\"500\"}".toByteArray()))
		toadlet.handleMethodPATCH(URI("/path/127"), httpRequest, toadletContext)
		assertThat(receivedArguments.single(), equalTo(Arguments("127", null, null, 500)))
	}

	@Test
	fun `PATCH request with height and filename will set the filename before cloning`() {
		val imageService = recordOrderOfCalls(createCloningImageServiceThatRecordsArguments(125) { _, _, _, _ -> })
		val toadlet = ImageToadlet("/path/", imageService, insertService, highLevelSimpleClient)
		whenever(httpRequest.rawData).thenReturn(ArrayBucket("{\"height\":\"600\",\"filename\":\"file.name\"}".toByteArray()))
		toadlet.handleMethodPATCH(URI("/path/125"), httpRequest, toadletContext)
		assertThat(imageService.calledMethods, containsInRelativeOrder("setImageFilename", "cloneImage"))
	}

	@Test
	fun `PATCH request with height will respond with a success response code and the location of the new image`() {
		val imageService = createCloningImageServiceReturningImageMetadataWidthId(128)
		val toadlet = ImageToadlet("/path/", imageService, insertService, highLevelSimpleClient)
		whenever(httpRequest.rawData).thenReturn(ArrayBucket("{\"height\":\"500\"}".toByteArray()))
		toadlet.handleMethodPATCH(URI("/path/128"), httpRequest, toadletContext)
		verify(toadletContext).sendReplyHeaders(intThat(allOf(greaterThanOrEqualTo(200), lessThan(300))), any(), argThat(containsHeader("location", "128-2")), isNull(), anyLong())
	}

	@Test
	fun `PATCH request with width and height will call image service with width and height`() {
		data class Arguments(val id: String, val mimeType: String?, val width: Int?, val height: Int?)
		val receivedArguments = mutableListOf<Arguments>()
		val imageService = createCloningImageServiceThatRecordsArguments(129) { id, mimeType, width, height -> receivedArguments.add(Arguments(id, mimeType, width, height)) }
		val toadlet = ImageToadlet("/path/", imageService, insertService, highLevelSimpleClient)
		whenever(httpRequest.rawData).thenReturn(ArrayBucket("{\"width\":\"400\",\"height\":\"600\"}".toByteArray()))
		toadlet.handleMethodPATCH(URI("/path/129"), httpRequest, toadletContext)
		assertThat(receivedArguments.single(), equalTo(Arguments("129", null, 400, 600)))
	}

	@Test
	fun `PATCH request with width and height and filename will set the filename before cloning`() {
		val imageService = recordOrderOfCalls(createCloningImageServiceThatRecordsArguments(125) { _, _, _, _ -> })
		val toadlet = ImageToadlet("/path/", imageService, insertService, highLevelSimpleClient)
		whenever(httpRequest.rawData).thenReturn(ArrayBucket("{\"width\":\"400\",\"height\":\"600\",\"filename\":\"file.name\"}".toByteArray()))
		toadlet.handleMethodPATCH(URI("/path/125"), httpRequest, toadletContext)
		assertThat(imageService.calledMethods, containsInRelativeOrder("setImageFilename", "cloneImage"))
	}

	@Test
	fun `PATCH request with width and height will respond with a success response code and the location of the new image`() {
		val imageService = createCloningImageServiceReturningImageMetadataWidthId(130)
		val toadlet = ImageToadlet("/path/", imageService, insertService, highLevelSimpleClient)
		whenever(httpRequest.rawData).thenReturn(ArrayBucket("{\"width\":\"400\",\"height\":\"600\"}".toByteArray()))
		toadlet.handleMethodPATCH(URI("/path/130"), httpRequest, toadletContext)
		verify(toadletContext).sendReplyHeaders(intThat(allOf(greaterThanOrEqualTo(200), lessThan(300))), any(), argThat(containsHeader("location", "130-2")), isNull(), anyLong())
	}

	private fun createCloningImageServiceThatRecordsArguments(imageId: Int, argumentsReceived: (id: String, mimeType: String?, width: Int?, height: Int?) -> Unit) = object : ImageService {
		override fun getImage(id: String) = ImageMetadata("$imageId", 12, 23, "image", Inserted).takeIf { id == "$imageId" }
		override fun cloneImage(id: String, mimeType: String?, width: Int?, height: Int?) =
			argumentsReceived(id, mimeType, width, height).let { null }
	}

	private fun createCloningImageServiceReturningImageMetadataWidthId(imageId: Int) = object : ImageService {
		override fun getImage(id: String) = ImageMetadata("$imageId", 12, 23, "image", Inserted).takeIf { id == "$imageId" }
		override fun cloneImage(id: String, mimeType: String?, width: Int?, height: Int?) =
			ImageMetadata("${imageId}-2", 1, 1).takeIf { id == "$imageId" }
	}

	private fun recordOrderOfCalls(imageService: ImageService) = object : ImageService {
		val calledMethods = mutableListOf<String>()
		override fun addImage(data: ByteArray): ImageMetadata? = imageService.addImage(data).also { calledMethods.add("addImage") }
		override fun cloneImage(id: String, mimeType: String?, width: Int?, height: Int?) = imageService.cloneImage(id, mimeType, width, height).also { calledMethods.add("cloneImage") }
		override fun getImage(id: String): ImageMetadata? = imageService.getImage(id).also { calledMethods.add("getImage") }
		override fun getImageData(id: String): ImageData? = imageService.getImageData(id).also { calledMethods.add("getImageData") }
		override fun setImageStatus(id: String, status: ImageStatus) = imageService.setImageStatus(id, status).also { calledMethods.add("setImageStatus") }
		override fun setImageFilename(id: String, filename: String) = imageService.setImageFilename(id, filename).also { calledMethods.add("setImageFilename") }
		override fun setImageKey(id: String, key: String) = imageService.setImageKey(id, key).also { calledMethods.add("setImageKey") }
		override fun getImageIds(): List<String> = imageService.getImageIds().also { calledMethods.add("getImageIds") }
		override fun removeImage(id: String) = imageService.removeImage(id).also { calledMethods.add("removeImage") }
	}

	@Test
	fun `DELETE request calls image service`() {
		val removedImages = mutableListOf<String>()
		val imageService = object : ImageService {
			override fun removeImage(id: String) = removedImages.add(id).let {}
		}
		val toadlet = ImageToadlet("/path/", imageService, insertService, highLevelSimpleClient)
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
			ImageMetadata("id-1", 720, 576)
				.takeIf { data.contentEquals(byteArrayOf(0, 1, 2, 3)) }
	}
	private val insertService = object : InsertService {}
	private val highLevelSimpleClient = mock<HighLevelSimpleClient>()
	private val toadlet = ImageToadlet("/test/upload", imageService, insertService, highLevelSimpleClient)

}
