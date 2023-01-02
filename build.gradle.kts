plugins {
	id("org.jetbrains.kotlin.jvm") version "1.7.10"
}

repositories {
	mavenCentral()
	maven {
		setUrl("https://mvn.freenetproject.org/")
	}
}

dependencies {
	compileOnly(group = "org.freenetproject", name = "fred", version = "build01491")
	implementation(group = "com.google.inject", name = "guice", version = "5.1.0")

	testImplementation(group = "org.freenetproject", name = "fred", version = "build01491")
	testImplementation(group = "org.jetbrains.kotlin", name = "kotlin-test-junit5")
	testImplementation(group = "org.hamcrest", name = "hamcrest", version = "2.2")
	testImplementation(group = "org.mockito", name = "mockito-junit-jupiter", version = "4.11.0")
	testImplementation(group = "org.mockito.kotlin", name = "mockito-kotlin", version = "4.1.0")
}

tasks.named<Test>("test") {
	useJUnitPlatform()
}

tasks.create("fatJar", Jar::class) {
	archiveFileName.set(project.name.toLowerCase() + "-jar-with-dependencies.jar")
	from((configurations.runtimeClasspath.get()).map { if (it.isDirectory) it else zipTree(it) })
	duplicatesStrategy = DuplicatesStrategy.INCLUDE
	manifest {
		attributes("Plugin-Main-Class" to "net.pterodactylus.frimgur.plugin.Frimgur")
	}
	with(tasks.named<Jar>("jar").get())
}
