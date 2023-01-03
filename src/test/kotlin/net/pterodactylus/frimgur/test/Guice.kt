package net.pterodactylus.frimgur.test

import com.google.inject.Module
import org.mockito.Mockito.mock

class Binding<T>(private val c: Class<T>) {
	fun toInstance(instance: T) = Module { binder -> binder.bind(c).toInstance(instance) }
	fun toMock() = Module { binder -> binder.bind(c).toInstance(mock(c))}
}

inline fun <reified T> bind() = Binding(T::class.java)
