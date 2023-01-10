package net.pterodactylus.frimgur.util

import com.google.inject.Injector

/**
 * Returns an instance of the given type.
 *
 * @param T The type of the instance to get
 * @return An instance of the given type
 * @see Injector.getInstance
 */
inline fun <reified T> Injector.getInstance(): T = getInstance(T::class.java)
