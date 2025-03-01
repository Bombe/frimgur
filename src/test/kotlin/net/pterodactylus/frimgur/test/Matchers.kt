package net.pterodactylus.frimgur.test

import freenet.support.HTMLNode
import net.pterodactylus.frimgur.image.ImageMetadata
import org.hamcrest.Description
import org.hamcrest.Matcher
import org.hamcrest.Matchers.any
import org.hamcrest.TypeSafeDiagnosingMatcher

/**
 * Creates a matcher that matches an [ImageMetadata] objects. All parameters
 * have default values that will match any value.
 *
 * @param width A matcher for the width
 * @param height A matcher for the height
 * @param mimeType A matcher for the MIME type
 * @return A matcher for use with Hamcrest’s `assertThat`
 */
fun isMetadataWith(width: Matcher<Int> = any(Int::class.java), height: Matcher<Int> = any(Int::class.java), filename: Matcher<String> = any<String>()): Matcher<ImageMetadata?> = object : TypeSafeDiagnosingMatcher<ImageMetadata>() {

	override fun matchesSafely(imageMetadata: ImageMetadata, mismatchDescription: Description): Boolean {
		if (!width.matches(imageMetadata.width)) {
			mismatchDescription.appendText("width is ").appendValue(imageMetadata.width)
			return false
		}
		if (!height.matches(imageMetadata.height)) {
			mismatchDescription.appendText("height is ").appendValue(imageMetadata.height)
			return false
		}
		if (!filename.matches(imageMetadata.filename)) {
			mismatchDescription.appendText("Filename is ").appendValue(imageMetadata.filename)
			return false
		}
		return true
	}

	override fun describeTo(description: Description) {
		description.appendText("image metadata with width ").appendDescriptionOf(width)
			.appendText(", height ").appendDescriptionOf(height)
			.appendText(", and filename ").appendDescriptionOf(filename)
	}

}

/**
 * Creates a matcher for a [Triple].
 *
 * @param first A matcher for the first element
 * @param second A matcher for the second element
 * @param third A matcher for the third element
 * @return A matcher for use with Hamcrest’s `assertThat`
 */
fun <F, S, T> isTriple(first: Matcher<F>, second: Matcher<S>, third: Matcher<T>) = object : TypeSafeDiagnosingMatcher<Triple<F, S, T>>() {

	override fun matchesSafely(triple: Triple<F, S, T>, mismatchDescription: Description): Boolean {
		if (!first.matches(triple.first)) {
			mismatchDescription.appendText("first was ").appendValue(triple.first)
			return false
		}
		if (!second.matches(triple.second)) {
			mismatchDescription.appendText("second was ").appendValue(triple.second)
			return false
		}
		if (!third.matches(triple.third)) {
			mismatchDescription.appendText("third was ").appendValue(triple.third)
			return false
		}
		return true
	}

	override fun describeTo(description: Description) {
		description.appendText("triple of ").appendDescriptionOf(first)
			.appendText(", ").appendDescriptionOf(second)
			.appendText(", and ").appendDescriptionOf(third)
	}

}

/**
 * Returns a matcher for an [HTMLNode]. Only the name and the attributes
 * can be matched.
 *
 * @param name A matcher for the name
 * @param attributes A matcher for the attributes
 * @return A matcher for use with Hamcrest’s `assertThat`
 */
fun isHtmlNode(name: Matcher<String>, attributes: Matcher<Iterable<Pair<String, String>>>) = object : TypeSafeDiagnosingMatcher<HTMLNode>() {
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

inline fun <reified T> any(): Matcher<T> = any(T::class.java)
