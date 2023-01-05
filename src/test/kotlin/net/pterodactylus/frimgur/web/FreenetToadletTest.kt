package net.pterodactylus.frimgur.web

import freenet.client.HighLevelSimpleClient
import freenet.clients.http.PageMaker
import freenet.clients.http.PageNode
import freenet.clients.http.ToadletContext
import freenet.support.HTMLNode
import net.pterodactylus.frimgur.web.annotations.ToadletName
import org.hamcrest.Description
import org.hamcrest.Matcher
import org.hamcrest.MatcherAssert.assertThat
import org.hamcrest.Matchers.allOf
import org.hamcrest.Matchers.equalTo
import org.hamcrest.Matchers.hasItem
import org.hamcrest.TypeSafeDiagnosingMatcher
import org.mockito.ArgumentMatchers.anyLong
import org.mockito.kotlin.any
import org.mockito.kotlin.eq
import org.mockito.kotlin.mock
import org.mockito.kotlin.verify
import org.mockito.kotlin.whenever
import java.net.URI
import kotlin.test.Test

/**
 * Unit test for [FreenetToadlet].
 */
class FreenetToadletTest {

	@Test
	fun `toadlet sets page title from processor`() {
		toadlet.handleMethodGET(URI(""), mock(), toadletContext)
		verify(pageMaker).getPageNode(eq("Page Title"), eq(toadletContext))
	}

	@Test
	fun `toadlet returns HTTP status 200`() {
		toadlet.handleMethodGET(URI(""), mock(), toadletContext)
		verify(toadletContext).sendReplyHeaders(eq(200), eq("OK"), any(), any(), anyLong())
	}

	@Test
	fun `toadlet renders page node`() {
		toadlet.handleMethodGET(URI(""), mock(), toadletContext)

		verify(toadletContext).sendReplyHeaders(eq(200), eq("OK"), any(), any(), eq(73L))
		val expectedHtmlNode = HTMLNode("html").apply {
			addChild("head")
			addChild("body").addChild("%", "<Rendered.>")
		}
		verify(toadletContext).writeData(eq(expectedHtmlNode.generate().toByteArray()))
	}

	@Test
	fun `toadlet adds link to page header`() {
		val pageProcessor = object : PageProcessor {
			override fun processPage(pageRequest: PageRequest) = PageResponse("", "").apply {
				addJavascriptLink("static/test.js")
			}
		}
		val toadlet = FreenetToadlet(highLevelSimpleClient, "/prefix/", pageProcessor)
		toadlet.handleMethodGET(URI(""), mock(), toadletContext)
		assertThat(
			pageNode.headNode.children, hasItem(
				isHtmlNode(
					equalTo("script"),
					allOf(
						hasItem(equalTo("src" to "/prefix/static/test.js")),
						hasItem(equalTo("language" to "javascript")),
						hasItem(equalTo("type" to "application/javascript"))
					)
				)
			)
		)
	}

	@Test
	fun `toadlet returns the prefix plus the page processor's toadlet name`() {
		assertThat(toadlet.path(), equalTo("/prefix/test.html"))
	}

	private val highLevelSimpleClient = mock<HighLevelSimpleClient>()
	private val outer = HTMLNode("html")
	private val headNode = outer.addChild("head")
	private val contentNode = outer.addChild("body")
	private val pageNode = createObject(PageNode::class.java, arrayOf(HTMLNode::class.java, HTMLNode::class.java, HTMLNode::class.java), outer, headNode, contentNode)
	private val pageMaker = mock<PageMaker>()
	private val toadletContext = mock<ToadletContext>()
	private val pageProcessor = @ToadletName("test.html") object : PageProcessor {
		override fun processPage(pageRequest: PageRequest) = PageResponse("Page Title", "<Rendered.>")
	}
	private val toadlet = FreenetToadlet(highLevelSimpleClient, "/prefix/", pageProcessor)

	init {
		whenever(pageMaker.getPageNode(any(), eq(toadletContext))).thenReturn(pageNode)
		whenever(toadletContext.pageMaker).thenReturn(this@FreenetToadletTest.pageMaker)
	}

}

private fun <T> createObject(clazz: Class<T>, parameterTypes: Array<Class<*>>, vararg arguments: Any?): T {
	val constructor = clazz.getDeclaredConstructor(*parameterTypes)
	constructor.isAccessible = true
	return constructor.newInstance(*arguments)
}

private fun isHtmlNode(name: Matcher<String>, attributes: Matcher<Iterable<Pair<String, String>>>) = object : TypeSafeDiagnosingMatcher<HTMLNode>() {
	override fun matchesSafely(item: HTMLNode, mismatchDescription: Description): Boolean {
		if (!name.matches(item.firstTag)) {
			mismatchDescription.appendText("name is ").appendValue(item.firstTag)
			return false;
		}
		if (!attributes.matches(item.attributes.toList())) {
			mismatchDescription.appendText("attributes are ").appendValue(item.attributes)
			return false;
		}
		return true
	}

	override fun describeTo(description: Description) {
		description.appendText("HTML node with name ").appendDescriptionOf(name)
			.appendText(" and attributes ").appendDescriptionOf(attributes)
	}

}
