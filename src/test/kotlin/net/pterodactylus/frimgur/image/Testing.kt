package net.pterodactylus.frimgur.image

/** Provides a PNG-encoded 1x1 image. */
fun get1x1Png() = ImageService::class.java.getResourceAsStream("1x1.png").readBytes()

/** Provides a JPEG-encoded 1x1 image. */
fun get1x1Jpeg() = ImageService::class.java.getResourceAsStream("1x1.jpg").readBytes()

/** Provides a BMP-encoded 1x1 image. */
fun get1x1Bmp() = ImageService::class.java.getResourceAsStream("1x1.bmp").readBytes()

/** Provides a GIF-encoded 1x1 image. */
fun get1x1Gif() = ImageService::class.java.getResourceAsStream("1x1.gif").readBytes()
