package net.pterodactylus.frimgur.image

import java.awt.image.DataBufferByte
import javax.imageio.ImageIO
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

/**
 * Unit test for [DefaultImageService].
 */
class ImageServiceTest {

	@Test
	fun `image service can parse PNG files`() {
		val metadata = imageService.addImage(get1x1Png())
		assertThat(metadata, isMetadataWith(equalTo(1), equalTo(1), equalTo("image")))
	}

	@Test
	fun `image service can parse JPEG files`() {
		val metadata = imageService.addImage(get1x1Jpeg())
		assertThat(metadata, isMetadataWith(equalTo(1), equalTo(1), equalTo("image")))
	}

	@Test
	fun `image service can parse GIF files`() {
		val metadata = imageService.addImage(get1x1Gif())
		assertThat(metadata, isMetadataWith(equalTo(1), equalTo(1), equalTo("image")))
	}

	@Test
	fun `image service can parse BMP files`() {
		val metadata = imageService.addImage(get1x1Bmp())
		assertThat(metadata, isMetadataWith(equalTo(1), equalTo(1), equalTo("image")))
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
	fun `added image does not have generated metadata`() {
		val id = imageService.addImage(get1x1Png())!!.id
		assertThat(imageService.getImage(id)!!.generatedImageMetadata, equalTo(GeneratedImageMetadata()))
	}

	@Test
	fun `image key can be set`() {
		val id = imageService.addImage(get1x1Png())!!.id
		imageService.setImageKey(id, "CHK@Test")
		assertThat(imageService.getImage(id)!!.generatedImageMetadata.key, equalTo("CHK@Test"))
	}

	@Test
	fun `generated filename of image can be set`() {
		val id = imageService.addImage(get1x1Png())!!.id
		imageService.setImageInsertFilename(id, "image.sfx")
		assertThat(imageService.getImage(id)!!.generatedImageMetadata.insertFilename, equalTo("image.sfx"))
	}

	@Test
	fun `image service can return image data for valid ID`() {
		val id1 = imageService.addImage(get1x1Png())!!.id
		val imageData = imageService.getImageData(id1)!!
		assertThat(imageData.metadata, isMetadataWith(equalTo(1), equalTo(1)))
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

	@Test
	fun `cloning an image without changing anything results in an identical image with a new ID`() {
		val imageMetadata = imageService.addImage(get1x1Png())!!
		val clonedData = imageService.cloneImage(imageMetadata.id)!!
		assertThat(clonedData.id, not(equalTo(imageMetadata.id)))
		assertThat(clonedData.copy(id = imageMetadata.id), equalTo(imageMetadata))
		assertThat((imageService.getImageData(clonedData.id)!!.data.decodeImage().raster.dataBuffer as DataBufferByte).data, equalTo((imageService.getImageData(imageMetadata.id)!!.data.decodeImage().raster.dataBuffer as DataBufferByte).data))
	}

	@Test
	fun `cloning an image that is inserting will result in an image that is waiting`() {
		val imageMetadata = imageService.addImage(get1x1Png())!!
		imageService.setImageStatus(imageMetadata.id, Inserting)
		val clonedData = imageService.cloneImage(imageMetadata.id)!!
		assertThat(clonedData.status, equalTo(Waiting))
	}

	@Test
	fun `cloning an image that is inserted will result in an image that is waiting`() {
		val imageMetadata = imageService.addImage(get1x1Png())!!
		imageService.setImageStatus(imageMetadata.id, Inserted)
		val clonedData = imageService.cloneImage(imageMetadata.id)!!
		assertThat(clonedData.status, equalTo(Waiting))
	}

	@Test
	fun `cloning an image that is failed will result in an image that is waiting`() {
		val imageMetadata = imageService.addImage(get1x1Png())!!
		imageService.setImageStatus(imageMetadata.id, Failed)
		val clonedData = imageService.cloneImage(imageMetadata.id)!!
		assertThat(clonedData.status, equalTo(Waiting))
	}

	@Test
	fun `cloning an image with a key will remove all generated metadata`() {
		val imageMetadata = imageService.addImage(get1x1Png())!!
		imageService.setImageKey(imageMetadata.id, "some-key")
		val clonedData = imageService.cloneImage(imageMetadata.id)!!
		assertThat(clonedData.generatedImageMetadata, equalTo(GeneratedImageMetadata()))
	}

	@Test
	fun `cloning an image and scaling it to 8x8 returns an appropriately-sized image`() {
		val imageMetadata = imageService.addImage(get1x1Png())!!
		val clonedData = imageService.cloneImage(imageMetadata.id, width = 8, height = 16)!!
		assertThat(clonedData.width, equalTo(8))
		assertThat(clonedData.height, equalTo(16))
	}

	@Test
	fun `cloning an image and scaling its width to 9 returns an image 9 pixels high`() {
		val imageMetadata = imageService.addImage(get1x1Png())!!
		val clonedData = imageService.cloneImage(imageMetadata.id, width = 9)!!
		assertThat(clonedData.width, equalTo(9))
		assertThat(clonedData.height, equalTo(9))
	}

	@Test
	fun `cloning an image and scaling its height to 11 returns an image 11 pixels wide`() {
		val imageMetadata = imageService.addImage(get1x1Png())!!
		val clonedData = imageService.cloneImage(imageMetadata.id, height = 11)!!
		assertThat(clonedData.width, equalTo(11))
		assertThat(clonedData.height, equalTo(11))
	}

	private val imageService = DefaultImageService()

}

private fun ByteArray.decodeImage() = inputStream().use(ImageIO::read)
