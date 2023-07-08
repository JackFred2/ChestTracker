pluginManagement {
	repositories {
		maven {
			name = "Fabric"
			url = java.net.URI("https://maven.fabricmc.net/")
		}
		mavenCentral()
		gradlePluginPortal()
	}
}

rootProject.name = "chesttracker"