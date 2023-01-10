package net.pterodactylus.frimgur.image

import org.hamcrest.Description
import org.hamcrest.Matcher
import org.hamcrest.MatcherAssert.assertThat
import org.hamcrest.Matchers.any
import org.hamcrest.Matchers.contains
import org.hamcrest.Matchers.emptyIterable
import org.hamcrest.Matchers.equalTo
import org.hamcrest.Matchers.nullValue
import org.hamcrest.TypeSafeDiagnosingMatcher
import java.util.concurrent.atomic.AtomicBoolean
import kotlin.test.Test

/**
 * Unit test for [DefaultImageService].
 */
class ImageServiceTest {

	@Test
	fun `image service can parse PNG files`() {
		val metadata = imageService.addImage(get1x1Png())
		assertThat(metadata, isMetadataWith(equalTo(1), equalTo(1), equalTo(67), equalTo("image/png")))
	}

	@Test
	fun `image service can parse JPEG files`() {
		val metadata = imageService.addImage(get1x1Jpeg())
		assertThat(metadata, isMetadataWith(equalTo(1), equalTo(1), equalTo(333), equalTo("image/jpeg")))
	}

	@Test
	fun `image service can parse GIF files`() {
		val metadata = imageService.addImage(get1x1Gif())
		assertThat(metadata, isMetadataWith(equalTo(1), equalTo(1), equalTo(35), equalTo("image/gif")))
	}

	@Test
	fun `image service can parse BMP files`() {
		val metadata = imageService.addImage(get1x1Bmp())
		assertThat(metadata, isMetadataWith(equalTo(1), equalTo(1), equalTo(66), equalTo("image/bmp")))
	}

	@Test
	fun `invalid image data returns null`() {
		val metadata = imageService.addImage(byteArrayOf())
		assertThat(metadata, nullValue())
	}

	@Test
	fun `adding an image makes it available`() {
		val originalMetadata = imageService.addImage(get1x1Png())
		val storedMetadata = imageService.getImage(originalMetadata!!.id)
		assertThat(storedMetadata, equalTo(originalMetadata))
	}

	@Test
	fun `adding an image notifies listener`() {
		val listenerCalled = AtomicBoolean(false)
		imageService.onNewImage { (metadata, data) ->
			if ((metadata.width == 1) && (metadata.height == 1) && (metadata.size == 67) && (metadata.mimeType == "image/png") && (data.contentEquals(get1x1Png()))) {
				listenerCalled.set(true)
			}
		}
		imageService.addImage(get1x1Png())
		assertThat(listenerCalled.get(), equalTo(true))
	}

	@Test
	fun `adding an invalid image does not notify listener`() {
		val listenerCalled = AtomicBoolean(false)
		imageService.onNewImage { _ -> listenerCalled.set(true) }
		imageService.addImage(byteArrayOf(0, 1, 2, 3))
		assertThat(listenerCalled.get(), equalTo(false))
	}

	@Test
	fun `new image service does not have any images`() {
		assertThat(imageService.getImageIds(), emptyIterable())
	}

	@Test
	fun `image servce can list added images`() {
		val id1 = imageService.addImage(get1x1Png())!!.id
		val id2 = imageService.addImage(get1x1Gif())!!.id
		val id3 = imageService.addImage(get1x1Png())!!.id
		assertThat(imageService.getImageIds(), contains(id1, id2, id3))
	}

	@Test
	fun `images are listed in insert-order`() {
		val insertedIds = (1..1000).map {
			imageService.addImage(get1x1Png())!!.id
		}
		val idsInService = imageService.getImageIds()
		assertThat(idsInService, contains(insertedIds.map(::equalTo)))
	}

	@Test
	fun `image service can return image data for valid ID`() {
		val id1 = imageService.addImage(get1x1Png())!!.id
		val imageData = imageService.getImageData(id1)!!
		assertThat(imageData.metadata, isMetadataWith(equalTo(1), equalTo(1), equalTo(67), equalTo("image/png")))
		assertThat(imageData.data, equalTo(get1x1Png()))
	}

	@Test
	fun `image service returns null data for invalid ID`() {
		val imageData = imageService.getImageData("123")
		assertThat(imageData, nullValue())
	}

	@Test
	fun `image can be removed`() {
		val id1 = imageService.addImage(get1x1Png())!!.id
		val id2 = imageService.addImage(get1x1Gif())!!.id
		imageService.removeImage(id1)
		assertThat(imageService.getImageIds(), contains(id2))
	}

	private val imageService = DefaultImageService()

}

private fun isMetadataWith(width: Matcher<Int> = any(Int::class.java), height: Matcher<Int> = any(Int::class.java), size: Matcher<Int> = any(Int::class.java), mimeType: Matcher<String> = any(String::class.java)): Matcher<ImageMetadata?> = object : TypeSafeDiagnosingMatcher<ImageMetadata>() {

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
		if (!mimeType.matches(imageMetadata.mimeType)) {
			mismatchDescription.appendText("MIME type is ").appendValue(imageMetadata.mimeType)
			return false
		}
		return true
	}

	override fun describeTo(description: Description) {
		description.appendText("image metadata with width ").appendDescriptionOf(width)
			.appendText(", height ").appendDescriptionOf(height)
			.appendText(", size ").appendDescriptionOf(size)
			.appendText(", and MIME type ").appendDescriptionOf(mimeType)
	}

}
