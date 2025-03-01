package net.pterodactylus.frimgur.web

import freenet.client.HighLevelSimpleClient
import kotlin.test.Test
import net.pterodactylus.frimgur.web.annotations.ToadletName
import org.hamcrest.MatcherAssert.assertThat
import org.hamcrest.Matchers.equalTo
import org.mockito.kotlin.mock

/**
 * Unit test for [FreenetToadletFactory].
 */
class FreenetToadletFactoryTest {

	@Test
	fun `freenet toadlet can be created`() {
		val highLevelSimpleClient = mock<HighLevelSimpleClient>()
		val freenetToadletFactory = FreenetToadletFactory("/prefix/", highLevelSimpleClient)
		val pageProcessor = @ToadletName("test.html") object : PageProcessor {
			override fun processPage(pageRequest: PageRequest): PageResponse = TODO("not used")
		}
		val freenetToadlet = freenetToadletFactory.createFreenetToadlet(pageProcessor)
		assertThat(freenetToadlet.path(), equalTo("/prefix/test.html"))
	}

}
