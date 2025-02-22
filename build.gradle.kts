plugins {
	id("org.jetbrains.kotlin.jvm") version "1.7.22"

	// 0.13.0 is the latest version still working with Java 8
	id("com.palantir.git-version") version "0.13.0"
	id("jacoco")
}

repositories {
	mavenCentral()
	maven {
		setUrl("https://mvn.freenetproject.org/")
	}
}

dependencies {
	compileOnly(group = "org.freenetproject", name = "fred", version = "build01491")
	implementation(kotlin("reflect"))
	implementation(group = "com.google.inject", name = "guice", version = "5.1.0")
	implementation(group = "io.pebbletemplates", name = "pebble", version = "3.1.6")
	implementation(group = "com.fasterxml.jackson.module", name = "jackson-module-kotlin", version = "2.14.1")

	testImplementation(group = "org.freenetproject", name = "fred", version = "build01491")
	testImplementation(platform("org.junit:junit-bom:5.12.0"))
	testRuntimeOnly(group = "org.junit.platform", name = "junit-platform-launcher", version = "1.12.0")
	testImplementation(group = "org.jetbrains.kotlin", name = "kotlin-test")
	testImplementation(group = "org.hamcrest", name = "hamcrest", version = "2.2")
	testImplementation(group = "org.mockito", name = "mockito-junit-jupiter", version = "4.11.0")
	testImplementation(group = "org.mockito.kotlin", name = "mockito-kotlin", version = "4.1.0")
	testImplementation(group = "com.spotify", name = "hamcrest-jackson", version = "1.3.1")
}

tasks.named<Test>("test") {
	useJUnitPlatform()
}

val gitVersion: groovy.lang.Closure<String> by extra
val versionDetails: groovy.lang.Closure<com.palantir.gradle.gitversion.VersionDetails> by extra

val createVersionProperties by tasks.registering {
	val versionFile = file("$buildDir/resources/generated-resources/version.properties")
	outputs.file(versionFile)
	doLast {
		versionFile.delete()
		versionFile.appendText("version: ${gitVersion()}\n")
		versionFile.appendText("hash: ${versionDetails().gitHashFull}\n")
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
