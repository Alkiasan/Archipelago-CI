plugins {
    id("java")
    id("dev.architectury.loom") version("1.11-SNAPSHOT")
    id("architectury-plugin") version("3.4-SNAPSHOT")
    kotlin("jvm") version("2.2.0")
}

group = "com.alkia.archipelago"
version = "1.8.2"

architectury {
    platformSetupLoomIde()
    fabric()
}

loom {
    silentMojangMappingsLicense()

    mixin {
        defaultRefmapName.set("mixins.${project.name}.refmap.json")
    }
}

repositories {
    mavenCentral()
    maven(url = "https://dl.cloudsmith.io/public/geckolib3/geckolib/maven/")
    maven("https://maven.impactdev.net/repository/development/")
    maven("https://oss.sonatype.org/content/repositories/snapshots")
    maven ("https://maven.shedaniel.me/")
    maven ("https://maven.terraformersmc.com/releases/")
    maven (url = "https://api.modrinth.com/maven")
}

dependencies {
    minecraft("net.minecraft:minecraft:1.21.1")
    mappings(loom.officialMojangMappings())
    modImplementation("net.fabricmc:fabric-loader:0.17.2")
    modImplementation("maven.modrinth:jade:15.10.0+fabric")

    modRuntimeOnly("net.fabricmc.fabric-api:fabric-api:0.116.6+1.21.1")
    modImplementation(fabricApi.module("fabric-command-api-v2", "0.116.6+1.21.1"))
    modImplementation(fabricApi.module("fabric-item-api-v1", "0.116.6+1.21.1"))
    modImplementation(fabricApi.module("fabric-rendering-v1", "0.116.6+1.21.1"))
    modImplementation(fabricApi.module("fabric-key-binding-api-v1", "0.116.6+1.21.1"))
    modImplementation(fabricApi.module("fabric-lifecycle-events-v1", "0.116.6+1.21.1"))

    modImplementation("net.fabricmc:fabric-language-kotlin:1.13.6+kotlin.2.2.20")
    modImplementation("com.cobblemon:fabric:1.8.0+1.21.1")

    modImplementation ("com.terraformersmc:modmenu:11.0.1")
    modApi("me.shedaniel.cloth:cloth-config-fabric:15.0.140") {
        exclude(group = "net.fabricmc.fabric-api")
    }
    testImplementation("org.junit.jupiter:junit-jupiter-api:5.10.0")
    testRuntimeOnly("org.junit.jupiter:junit-jupiter-engine:5.10.0")
}

tasks.getByName<Test>("test") {
    useJUnitPlatform()
}

tasks.processResources {
    inputs.property("version", project.version)

    filesMatching("fabric.mod.json") {
        expand(project.properties)
    }
}