package net.pterodactylus.frimgur.web

import freenet.client.HighLevelSimpleClient
import freenet.clients.http.ToadletContext
import net.pterodactylus.frimgur.image.ImageData
import net.pterodactylus.frimgur.image.ImageMetadata
import net.pterodactylus.frimgur.image.ImageService
import org.hamcrest.MatcherAssert.assertThat
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
 * Unit test for [ImageDataToadlet].
 */
class ImageDataToadletTest {

	@Test
	fun `toadlet returns correct path`() {
		assertThat(toadlet.path(), equalTo("/path/test/"))
	}

	@Test
	fun `toadlet returns 404 if no image ID was given`() {
		toadlet.handleMethodGET(URI("/path/test/"), mock(), toadletContext)
		verify(toadletContext).sendReplyHeaders(eq(404), any(), isNull(), isNull(), anyLong(), anyBoolean())
	}

	@Test
	fun `toadlet returns 404 if invalid image ID was given`() {
		toadlet.handleMethodGET(URI("/path/test/1234"), mock(), toadletContext)
		verify(toadletContext).sendReplyHeaders(eq(404), any(), isNull(), isNull(), anyLong(), anyBoolean())
	}

	@Test
	fun `toadlet returns 200 and data if valid image ID was given`() {
		toadlet.handleMethodGET(URI("/path/test/123"), mock(), toadletContext)
		verify(toadletContext).sendReplyHeaders(eq(200), any(), isNull(), eq("image/test"), eq(4))
		val data = argumentCaptor<ByteArray>()
		verify(toadletContext).writeData(data.capture(), anyInt(), anyInt())
		assertThat(data.firstValue, equalTo(byteArrayOf(0, 1, 2, 3)))
	}

	private val highLevelSimpleClient = mock<HighLevelSimpleClient>()
	private val toadletContext = mock<ToadletContext>()
	private val imageService = object : ImageService {
		override fun getImageData(id: String) =
			ImageData(ImageMetadata("123", 12, 23, 34, "image/test"), byteArrayOf(0, 1, 2, 3))
				.takeIf { id == "123" }
	}
	private val toadlet = ImageDataToadlet("/path/test/", imageService, highLevelSimpleClient)

}
