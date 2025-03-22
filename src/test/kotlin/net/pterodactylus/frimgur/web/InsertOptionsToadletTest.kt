package net.pterodactylus.frimgur.web

import com.fasterxml.jackson.databind.JsonNode
import com.spotify.hamcrest.jackson.JsonMatchers.isJsonStringMatching
import com.spotify.hamcrest.jackson.JsonMatchers.jsonObject
import com.spotify.hamcrest.jackson.JsonMatchers.jsonText
import freenet.clients.http.ToadletContext
import freenet.support.api.HTTPRequest
import freenet.support.io.ArrayBucket
import java.net.URI
import kotlin.test.Test
import net.pterodactylus.frimgur.insert.DefaultInsertOptionService
import org.hamcrest.Matcher
import org.hamcrest.MatcherAssert.assertThat
import org.hamcrest.Matchers.equalTo
import org.mockito.ArgumentMatchers.anyBoolean
import org.mockito.ArgumentMatchers.anyInt
import org.mockito.ArgumentMatchers.anyLong
import org.mockito.Mockito.mock
import org.mockito.kotlin.any
import org.mockito.kotlin.anyOrNull
import org.mockito.kotlin.argumentCaptor
import org.mockito.kotlin.eq
import org.mockito.kotlin.isNull
import org.mockito.kotlin.verify
import org.mockito.kotlin.whenever

class InsertOptionsToadletTest {

	@Test
	fun `given path is returned`() {
		assertThat(insertOptionsToadlet.path(), equalTo("/test/path"))
	}

	@Test
	fun `GET request returns correct value if default destination is CHK`() {
		insertOptionService.defaultInsertDestination = "CHK@"
		verifyJsonResponse {
			verifySingleAttribute("defaultDestination", jsonText("CHK@"))
		}
	}

	@Test
	fun `GET request returns correct value if default destination is SSK`() {
		insertOptionService.defaultInsertDestination = "SSK@"
		verifyJsonResponse {
			verifySingleAttribute("defaultDestination", jsonText("SSK@"))
		}
	}

	@Test
	fun `GET request returns correct value if default image format is PNG`() {
		insertOptionService.defaultImageFormat = "png"
		verifyJsonResponse {
			verifySingleAttribute("defaultImageFormat", jsonText("png"))
		}
	}

	@Test
	fun `GET request returns correct value if default image format is JPEG`() {
		insertOptionService.defaultImageFormat = "jpeg"
		verifyJsonResponse {
			verifySingleAttribute("defaultImageFormat", jsonText("jpeg"))
		}
	}

	@Test
	fun `PATCH request with empty object sends HTTP 200`() {
		whenever(httpRequest.rawData).thenReturn(ArrayBucket("{}".toByteArray()))
		insertOptionsToadlet.handleMethodPATCH(URI(""), httpRequest, toadletContext)
		verify(toadletContext).sendReplyHeaders(eq(200), anyOrNull(), anyOrNull(), anyOrNull(), anyLong())
	}

	@Test
	fun `PATCH request with new default destination returns HTTP 200`() {
		whenever(httpRequest.rawData).thenReturn(ArrayBucket("{\"defaultDestination\":\"USK@\"}".toByteArray()))
		insertOptionsToadlet.handleMethodPATCH(URI(""), httpRequest, toadletContext)
		verify(toadletContext).sendReplyHeaders(eq(200), anyOrNull(), anyOrNull(), anyOrNull(), anyLong())
	}

	@Test
	fun `PATCH request with new default destination sets value on insert option service`() {
		whenever(httpRequest.rawData).thenReturn(ArrayBucket("{\"defaultDestination\":\"USK@\"}".toByteArray()))
		insertOptionsToadlet.handleMethodPATCH(URI(""), httpRequest, toadletContext)
		assertThat(insertOptionService.defaultInsertDestination, equalTo("USK@"))
	}

	@Test
	fun `PATCH request with new default image format returns HTTP 200`() {
		whenever(httpRequest.rawData).thenReturn(ArrayBucket("{\"defaultImageFormat\":\"jpeg\"}".toByteArray()))
		insertOptionsToadlet.handleMethodPATCH(URI(""), httpRequest, toadletContext)
		verify(toadletContext).sendReplyHeaders(eq(200), anyOrNull(), anyOrNull(), anyOrNull(), anyLong())
	}

	@Test
	fun `PATCH request with new default image format sets value on insert option service`() {
		whenever(httpRequest.rawData).thenReturn(ArrayBucket("{\"defaultImageFormat\":\"jpeg\"}".toByteArray()))
		insertOptionsToadlet.handleMethodPATCH(URI(""), httpRequest, toadletContext)
		assertThat(insertOptionService.defaultImageFormat, equalTo("jpeg"))
	}

	private fun verifyJsonResponse(verifier: String.() -> Unit) {
		insertOptionsToadlet.handleMethodGET(URI(""), mock(), toadletContext)
		verify(toadletContext).sendReplyHeaders(eq(200), any(), isNull(), eq("application/json"), anyLong(), anyBoolean())
		val data = argumentCaptor<ByteArray>()
		verify(toadletContext).writeData(data.capture(), anyInt(), anyInt())
		verifier(data.firstValue.decodeToString())
	}

	private fun String.verifySingleAttribute(attribute: String, matcher: Matcher<in JsonNode>) =
		assertThat(this, isJsonStringMatching(jsonObject().where(attribute, matcher)))

	private val insertOptionService = DefaultInsertOptionService()
	private val insertOptionsToadlet = InsertOptionsToadlet("/test/path", insertOptionService, mock())
	private val toadletContext = mock<ToadletContext>()
	private val httpRequest = mock<HTTPRequest>()

}
