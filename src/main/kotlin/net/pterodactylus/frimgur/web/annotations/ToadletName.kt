package net.pterodactylus.frimgur.web.annotations

import net.pterodactylus.frimgur.web.PageProcessor

/**
 * Annotation for [PageProcessor]s that is used by the
 * [FreenetToadlet][net.pterodactylus.frimgur.web.FreenetToadlet]
 * to communicate the path of the toadlet to the toadlet container.
 */
@Target(AnnotationTarget.CLASS)
annotation class ToadletName(val value: String)

/**
 * Returns the [toadlet name][ToadletName.value] of a [PageProcessor].
 */
val PageProcessor.toadletName: String?
	get() =
		javaClass.getAnnotation(ToadletName::class.java)?.value
