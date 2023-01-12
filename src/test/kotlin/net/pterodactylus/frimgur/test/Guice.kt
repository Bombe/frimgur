package net.pterodactylus.frimgur.test

import com.google.inject.Module
import org.mockito.Mockito.mock
import javax.inject.Named

class Binding<T>(private val c: Class<T>) {
	fun toNamedInstance(name: String, instance: T) = Module { binder -> binder.bind(c).annotatedWith(Named(name)).toInstance(instance) }
	fun toInstance(instance: T) = Module { binder -> binder.bind(c).toInstance(instance) }
	fun toMock() = Module { binder -> binder.bind(c).toInstance(mock(c)) }
	fun toProvider(provider: () -> T) = Module { binder -> binder.bind(c).toProvider(provider)}
}

inline fun <reified T> bind() = Binding(T::class.java)
