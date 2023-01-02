package net.pterodactylus.frimgur.test

import com.google.inject.Module

class Binding<T>(private val c: Class<T>) {
	fun toInstance(instance: T) = Module { binder -> binder.bind(c).toInstance(instance) }
}

inline fun <reified T> bind() = Binding(T::class.java)
