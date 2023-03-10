package net.pterodactylus.frimgur.web

import com.spotify.hamcrest.jackson.IsJsonObject.jsonObject
import com.spotify.hamcrest.jackson.JsonMatchers.isJsonStringMatching
import com.spotify.hamcrest.jackson.JsonMatchers.jsonInt
import com.spotify.hamcrest.jackson.JsonMatchers.jsonNull
import com.spotify.hamcrest.jackson.JsonMatchers.jsonText
import freenet.client.HighLevelSimpleClient
import freenet.clients.http.ToadletContext
import freenet.support.api.HTTPRequest
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
			override fun getImage(id: String) = ImageMetadata("123", 12, 23, 34, "image/test", Inserted, "CHK@Test").takeIf { id == "123" }
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
					)
			)
		)
	}

	@Test
	fun `unset key in image metadata is rendered as JSON null`() {
		val imageService = object : ImageService {
			override fun getImage(id: String) = ImageMetadata("123", 12, 23, 34, "image/test", Inserted).takeIf { id == "123" }
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
