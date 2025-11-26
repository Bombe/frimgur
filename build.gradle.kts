import kotlin.text.Charsets.UTF_8

plugins {
	id("org.jetbrains.kotlin.jvm") version "2.2.21"
	id("jacoco")
}

repositories {
	mavenCentral()
	maven {
		setUrl("https://maven.pterodactylus.net/")
	}
}

dependencies {
	compileOnly(group = "org.freenetproject", name = "fred", version = "0.7.5.1503")
	implementation(kotlin("reflect"))
	implementation(group = "com.google.inject", name = "guice", version = "7.0.0")
	implementation(group = "io.pebbletemplates", name = "pebble", version = "3.1.6")
	implementation(group = "com.fasterxml.jackson.module", name = "jackson-module-kotlin", version = "2.20.1")

	testImplementation(group = "org.freenetproject", name = "fred", version = "0.7.5.1503")
	testImplementation(platform("org.junit:junit-bom:5.14.1"))
	testRuntimeOnly(group = "org.junit.platform", name = "junit-platform-launcher")
	testImplementation(group = "org.jetbrains.kotlin", name = "kotlin-test")
	testImplementation(group = "org.hamcrest", name = "hamcrest", version = "3.0")
	testImplementation(group = "org.mockito", name = "mockito-inline", version = "4.11.0")
	testImplementation(group = "org.mockito.kotlin", name = "mockito-kotlin", version = "4.1.0")
	testImplementation(group = "com.spotify", name = "hamcrest-jackson", version = "1.3.2")
}

tasks.named<Test>("test") {
	useJUnitPlatform()
}

val createVersionProperties by tasks.registering {
	val versionFile = file("$buildDir/resources/generated-resources/version.properties")
	outputs.file(versionFile)
	doLast {
		versionFile.delete()
		val gitVersion = Runtime.getRuntime().exec(arrayOf("git", "describe", "--tags", "--dirty")).inputStream.readBytes().toString(UTF_8).trim()
		val gitFullHash = Runtime.getRuntime().exec(arrayOf("git", "rev-parse", "HEAD")).inputStream.readBytes().toString(UTF_8).trim()
		versionFile.appendText("version: ${gitVersion}\n")
		versionFile.appendText("hash: ${gitFullHash}\n")
	}
}

tasks["processResources"].dependsOn(createVersionProperties)

tasks.create("fatJar", Jar::class) {
	archiveFileName.set(project.name.toLowerCase() + "-jar-with-dependencies.jar")
	from((configurations.runtimeClasspath.get()).map { if (it.isDirectory) it else zipTree(it) })
	from(createVersionProperties.get().outputs)
	duplicatesStrategy = DuplicatesStrategy.INCLUDE
	manifest {
		attributes("Plugin-Main-Class" to "net.pterodactylus.frimgur.plugin.Frimgur")
	}
	with(tasks.named<Jar>("jar").get())
}

tasks.jacocoTestReport {
	reports {
		xml.required.set(true)
	}
}
