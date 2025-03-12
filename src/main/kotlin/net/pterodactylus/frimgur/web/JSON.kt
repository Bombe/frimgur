package net.pterodactylus.frimgur.web

import com.fasterxml.jackson.databind.ObjectMapper
import net.pterodactylus.frimgur.image.ImageMetadata

val objectMapper = ObjectMapper()

fun ImageMetadata.toJson() = objectMapper.createObjectNode()!!.apply {
	put("id", id)
	putObject("metadata").apply {
		put("width", width)
		put("height", height)
		put("status", status.name)
		put("key", generatedImageMetadata.key)
		put("filename", filename)
	}
}
