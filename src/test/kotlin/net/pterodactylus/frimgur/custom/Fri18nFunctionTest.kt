package net.pterodactylus.frimgur.custom

import com.mitchellbosecke.pebble.PebbleEngine
import org.hamcrest.MatcherAssert.assertThat
import org.hamcrest.Matchers.contains
import org.hamcrest.Matchers.equalTo
import org.hamcrest.Matchers.sameInstance
import java.io.StringWriter
import java.util.Locale.ENGLISH
import kotlin.test.Test

/**
 * Unit test for [Fri18nFunction].
 */
class Fri18nFunctionTest {

	@Test
	fun `fri18n function exposes argument names`() {
		assertThat(fri18nFunction.argumentNames, contains("bundle", "key", "params"));
	}

	@Test
	fun `argument names list is cached`() {
		val firstArgumentNames = fri18nFunction.argumentNames
		val secondArgumentNames = fri18nFunction.argumentNames
		assertThat(firstArgumentNames, sameInstance(secondArgumentNames))
	}

	@Test
	fun `function returns value from bundle`() {
		val template = pebbleEngine.getLiteralTemplate("{{ i18n('i18n', 'Test.Message') }}")
		val output = StringWriter().apply { template.evaluate(this, ENGLISH) }.toString()
		assertThat(output, equalTo("Test Message"))
	}

	@Test
	fun `function can format message with one parameter`() {
		val template = pebbleEngine.getLiteralTemplate("{{ i18n('i18n', 'Test.WithOneParam', 'a') }}")
		val output = StringWriter().apply { template.evaluate(this, ENGLISH) }.toString()
		assertThat(output, equalTo("Parameter a"))
	}

	@Test
	fun `function can format message with two parameters`() {
		val template = pebbleEngine.getLiteralTemplate("{{ i18n('i18n', 'Test.WithTwoParams', ['a', 'b']) }}")
		val output = StringWriter().apply { template.evaluate(this, ENGLISH) }.toString()
		assertThat(output, equalTo("Parameters a and b"))
	}

	private val fri18nFunction = Fri18nFunction(testClassLoader)

}

private val testClassLoader = object : ClassLoader(Fri18nFunctionTest::class.java.classLoader) {
	override fun getResource(name: String?) = super.getResource("net/pterodactylus/frimgur/custom/test_$name")
}

private val pebbleEngine = PebbleEngine.Builder()
	.extension(fri18nExtension(testClassLoader))
	.build()
