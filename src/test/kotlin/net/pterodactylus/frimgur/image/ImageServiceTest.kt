package net.pterodactylus.frimgur.image

import org.hamcrest.Description
import org.hamcrest.Matcher
import org.hamcrest.MatcherAssert.assertThat
import org.hamcrest.Matchers.any
import org.hamcrest.Matchers.contains
import org.hamcrest.Matchers.containsInAnyOrder
import org.hamcrest.Matchers.emptyIterable
import org.hamcrest.Matchers.equalTo
import org.hamcrest.Matchers.nullValue
import org.hamcrest.TypeSafeDiagnosingMatcher
import kotlin.test.Test

/**
 * Unit test for [DefaultImageService].
 */
class ImageServiceTest {

	@Test
	fun `image service can parse PNG files`() {
		val metadata = imageService.addImage(getBytes("1x1.png"))
		assertThat(metadata, isMetadataWith(equalTo(1), equalTo(1), equalTo(67)))
	}

	@Test
	fun `image service can parse JPEG files`() {
		val metadata = imageService.addImage(getBytes("1x1.jpg"))
		assertThat(metadata, isMetadataWith(equalTo(1), equalTo(1), equalTo(333)))
	}

	@Test
	fun `image service can parse GIF files`() {
		val metadata = imageService.addImage(getBytes("1x1.gif"))
		assertThat(metadata, isMetadataWith(equalTo(1), equalTo(1), equalTo(35)))
	}

	@Test
	fun `image service can parse BMP files`() {
		val metadata = imageService.addImage(getBytes("1x1.bmp"))
		assertThat(metadata, isMetadataWith(equalTo(1), equalTo(1), equalTo(66)))
	}

	@Test
	fun `invalid image data returns null`() {
		val metadata = imageService.addImage(byteArrayOf())
		assertThat(metadata, nullValue())
	}

	@Test
	fun `adding an image makes it available`() {
		val originalMetadata = imageService.addImage(getBytes("1x1.png"))
		val storedMetadata = imageService.getImage(originalMetadata!!.id)
		assertThat(storedMetadata, equalTo(originalMetadata))
	}

	@Test
	fun `new image service does not have any images`() {
		assertThat(imageService.getImageIds(), emptyIterable())
	}

	@Test
	fun `image servce can list added images`() {
		val id1 = imageService.addImage(getBytes("1x1.png"))!!.id
		val id2 = imageService.addImage(getBytes("1x1.gif"))!!.id
		val id3 = imageService.addImage(getBytes("1x1.png"))!!.id
		assertThat(imageService.getImageIds(), containsInAnyOrder(id1, id2, id3))
	}

	@Test
	fun `image can be removed`() {
		val id1 = imageService.addImage(getBytes("1x1.png"))!!.id
		val id2 = imageService.addImage(getBytes("1x1.gif"))!!.id
		imageService.removeImage(id1)
		assertThat(imageService.getImageIds(), contains(id2))
	}

	private fun getBytes(path: String) = javaClass.getResourceAsStream(path)!!.readBytes()

	private val imageService = DefaultImageService()

}

private fun isMetadataWith(width: Matcher<Int> = any(Int::class.java), height: Matcher<Int> = any(Int::class.java), size: Matcher<Int> = any(Int::class.java)): Matcher<ImageMetadata?> = object : TypeSafeDiagnosingMatcher<ImageMetadata>() {

	override fun matchesSafely(imageMetadata: ImageMetadata, mismatchDescription: Description): Boolean {
		if (!width.matches(imageMetadata.width)) {
			mismatchDescription.appendText("width is ").appendValue(imageMetadata.width)
			return false
		}
		if (!height.matches(imageMetadata.height)) {
			mismatchDescription.appendText("height is ").appendValue(imageMetadata.height)
			return false
		}
		if (!size.matches(imageMetadata.size)) {
			mismatchDescription.appendText("size is ").appendValue(imageMetadata.size)
			return false
		}
		return true
	}

	override fun describeTo(description: Description) {
		description.appendText("image metadata with width ").appendDescriptionOf(width)
			.appendText(", height ").appendDescriptionOf(height)
			.appendText(", and size ").appendDescriptionOf(size)
	}

}
