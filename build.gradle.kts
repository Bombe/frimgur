plugins {
	id("org.jetbrains.kotlin.jvm") version "1.7.10"
}

repositories {
	mavenCentral()
	maven {
		setUrl("https://maven.pterodactylus.net/")
	}
}

dependencies {
	implementation(group = "org.freenetproject", name = "fred", version = "0.7.5.1475")

	testImplementation(group = "org.jetbrains.kotlin", name = "kotlin-test-junit5")
	testImplementation(group = "org.hamcrest", name = "hamcrest", version = "2.2")
	testImplementation(group = "org.mockito", name = "mockito-junit-jupiter", version = "4.11.0")
	testImplementation(group = "org.mockito.kotlin", name = "mockito-kotlin", version = "4.1.0")
}

tasks.named<Test>("test") {
	useJUnitPlatform()
}
