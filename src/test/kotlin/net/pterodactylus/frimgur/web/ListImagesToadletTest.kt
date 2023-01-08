package net.pterodactylus.frimgur.web

import com.spotify.hamcrest.jackson.JsonMatchers.isJsonStringMatching
import com.spotify.hamcrest.jackson.JsonMatchers.jsonArray
import com.spotify.hamcrest.jackson.JsonMatchers.jsonInt
import com.spotify.hamcrest.jackson.JsonMatchers.jsonObject
import com.spotify.hamcrest.jackson.JsonMatchers.jsonText
import freenet.client.HighLevelSimpleClient
import freenet.clients.http.ToadletContext
import net.pterodactylus.frimgur.image.ImageMetadata
import net.pterodactylus.frimgur.image.ImageService
import org.hamcrest.MatcherAssert.assertThat
import org.hamcrest.Matchers.containsInAnyOrder
import org.hamcrest.Matchers.equalTo
import org.mockito.ArgumentMatchers.anyBoolean
import org.mockito.ArgumentMatchers.anyInt
import org.mockito.ArgumentMatchers.anyLong
import org.mockito.kotlin.any
import org.mockito.kotlin.argumentCaptor
import org.mockito.kotlin.eq
import org.mockito.kotlin.isNull
import org.mockito.kotlin.mock
import org.mockito.kotlin.verify
import java.net.URI
import kotlin.test.Test

/**
 * Unit test for [ListImagesToadlet].
 */
class ListImagesToadletTest {

	@Test
	fun `path of the toadlet is correct`() {
		assertThat(toadlet.path(), equalTo("/path/images"))
	}

	@Test
	fun `empty list of images is transferred correctly`() {
		toadlet.handleMethodGET(URI(""), mock(), toadletContext)
		verify(toadletContext).sendReplyHeaders(eq(200), any(), isNull(), eq("application/json"), anyLong(), anyBoolean())
		val data = argumentCaptor<ByteArray>()
		verify(toadletContext).writeData(data.capture(), anyInt(), anyInt())
		assertThat(data.firstValue.decodeToString(), isJsonStringMatching(jsonArray()))
	}

	@Test
	fun `list with images is transferred correctly`() {
		val imageService = object : ImageService {
			override fun getImage(id: String) = when (id) {
				"id1" -> ImageMetadata("id1", 11, 12, 13)
				"id2" -> ImageMetadata("id2", 21, 22, 23)
				else -> null
			}

			override fun getImageIds() = listOf("id1", "id2")
		}
		val toadlet = ListImagesToadlet("/path/images", imageService, highLevelSimpleClient)
		toadlet.handleMethodGET(URI(""), mock(), toadletContext)
		verify(toadletContext).sendReplyHeaders(eq(200), any(), isNull(), eq("application/json"), anyLong(), anyBoolean())
		val data = argumentCaptor<ByteArray>()
		verify(toadletContext).writeData(data.capture(), anyInt(), anyInt())
		assertThat(
			data.firstValue.decodeToString(), isJsonStringMatching(
				jsonArray(
					containsInAnyOrder(
						jsonObject()
							.where("id", jsonText("id1"))
							.where(
								"metadata", jsonObject()
									.where("width", jsonInt(11))
									.where("height", jsonInt(12))
									.where("size", jsonInt(13))
							),
						jsonObject()
							.where("id", jsonText("id2"))
							.where(
								"metadata", jsonObject()
									.where("width", jsonInt(21))
									.where("height", jsonInt(22))
									.where("size", jsonInt(23))
							)
					)
				)
			)
		)
	}

	private val imageService = object : ImageService {}
	private val highLevelSimpleClient = mock<HighLevelSimpleClient>()
	private val toadlet = ListImagesToadlet("/path/images", imageService, highLevelSimpleClient)
	private val toadletContext = mock<ToadletContext>()

}
