package net.pterodactylus.frimgur.image

import kotlin.test.Test
import net.pterodactylus.frimgur.image.ImageStatus.Failed
import net.pterodactylus.frimgur.image.ImageStatus.Inserted
import net.pterodactylus.frimgur.image.ImageStatus.Inserting
import net.pterodactylus.frimgur.image.ImageStatus.Waiting
import net.pterodactylus.frimgur.test.isMetadataWith
import org.hamcrest.MatcherAssert.assertThat
import org.hamcrest.Matchers.contains
import org.hamcrest.Matchers.emptyIterable
import org.hamcrest.Matchers.equalTo
import org.hamcrest.Matchers.not
import org.hamcrest.Matchers.nullValue
import java.util.concurrent.atomic.AtomicBoolean
import java.util.concurrent.atomic.AtomicReference

/**
 * Unit test for [DefaultImageService].
 */
class ImageServiceTest {

	@Test
	fun `image service can parse PNG files`() {
		val metadata = imageService.addImage(get1x1Png())
		assertThat(metadata, isMetadataWith(equalTo(1), equalTo(1), equalTo(67), equalTo("image/png"), equalTo("image.png")))
	}

	@Test
	fun `image service can parse JPEG files`() {
		val metadata = imageService.addImage(get1x1Jpeg())
		assertThat(metadata, isMetadataWith(equalTo(1), equalTo(1), equalTo(333), equalTo("image/jpeg"), equalTo("image.jpg")))
	}

	@Test
	fun `image service can parse GIF files`() {
		val metadata = imageService.addImage(get1x1Gif())
		assertThat(metadata, isMetadataWith(equalTo(1), equalTo(1), equalTo(35), equalTo("image/gif"), equalTo("image.gif")))
	}

	@Test
	fun `image service can parse BMP files`() {
		val metadata = imageService.addImage(get1x1Bmp())
		assertThat(metadata, isMetadataWith(equalTo(1), equalTo(1), equalTo(66), equalTo("image/bmp"), equalTo("image.bmp")))
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
	fun `image service can list added images`() {
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
	fun `added image has status 'waiting'`() {
		val id = imageService.addImage(get1x1Png())!!.id
		assertThat(imageService.getImage(id)!!.status, equalTo(Waiting))
	}

	@Test
	fun `image status can be set 'inserted'`() {
		val id = imageService.addImage(get1x1Png())!!.id
		imageService.setImageStatus(id, Inserted)
		assertThat(imageService.getImage(id)!!.status, equalTo(Inserted))
	}

	@Test
	fun `image status can be set 'failed'`() {
		val id = imageService.addImage(get1x1Png())!!.id
		imageService.setImageStatus(id, Failed)
		assertThat(imageService.getImage(id)!!.status, equalTo(Failed))
	}

	@Test
	fun `added image does not have a generated key`() {
		val id = imageService.addImage(get1x1Png())!!.id
		assertThat(imageService.getImage(id)!!.key, nullValue())
	}

	@Test
	fun `image key can be set`() {
		val id = imageService.addImage(get1x1Png())!!.id
		imageService.setImageKey(id, "CHK@Test")
		assertThat(imageService.getImage(id)!!.key, equalTo("CHK@Test"))
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

	@Test
	fun `image service notifies listener if image status has been changed to inserting`() {
		val insertingImage = AtomicReference<ImageData>()
		imageService.onImageInserting(insertingImage::set)
		val imageId = imageService.addImage(get1x1Png())!!.id
		imageService.setImageStatus(imageId, Inserting)
		assertThat(insertingImage.get().metadata.id, equalTo(imageId))
	}

	@Test
	fun `image service can change filename of an image in status waiting`() {
		val imageId = imageService.addImage(get1x1Png())!!.id
		imageService.setImageFilename(imageId, "new-filename.png");
		assertThat(imageService.getImageData(imageId)!!.metadata.filename, equalTo("new-filename.png"))
	}

	@Test
	fun `image service does not change filename of an image in status inserting`() {
		verifyThatFilenameCannotBeChangedForStatus(Inserting)
	}

	@Test
	fun `image service does not change filename of an image in status inserted`() {
		verifyThatFilenameCannotBeChangedForStatus(Inserted)
	}

	@Test
	fun `image service does not change filename of an image in status failed`() {
		verifyThatFilenameCannotBeChangedForStatus(Failed)
	}

	private fun verifyThatFilenameCannotBeChangedForStatus(status: ImageStatus) {
		val imageId = imageService.addImage(get1x1Png())!!.id
		imageService.setImageStatus(imageId, status)
		imageService.setImageFilename(imageId, "new-filename.png");
		assertThat(imageService.getImageData(imageId)!!.metadata.filename, not(equalTo("new-filename.png")))
	}

	private val imageService = DefaultImageService()

}
