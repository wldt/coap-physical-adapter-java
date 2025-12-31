group = "io.github.wldt"
version = "0.1.2"
description = "Physical adapter to connect with the CoAP protocol"
java.sourceCompatibility = JavaVersion.VERSION_1_8


plugins {
    id("com.vanniktech.maven.publish") version "0.35.0"
    `java-library`
    `maven-publish`
    signing
}

repositories {
    mavenCentral()
    mavenLocal()
}

dependencies {
    api("org.eclipse.californium:californium-core:3.8.0")
    api("ch.qos.logback:logback-classic:1.5.13")
    api("com.google.code.gson:gson:2.10.1")
    api("io.github.wldt:wldt-core:0.4.0")
    api("com.fasterxml.jackson.dataformat:jackson-dataformat-yaml:2.19.0")
    testImplementation("junit:junit:4.13.2")
}

java {
    //withJavadocJar()
    withSourcesJar()
}

tasks.withType<JavaCompile>() {
    options.encoding = "UTF-8"
}

tasks.withType<Javadoc>() {
    options.encoding = "UTF-8"
    if (JavaVersion.current().isJava9Compatible) {
        (options as StandardJavadocDocletOptions).addBooleanOption("html5", true)
    }
}

// ✅ FIX ESATTO PER GRADLE 9.2.1 + vanniktech.maven.publish 0.35.0
afterEvaluate {
    val plainJavadocJarTask = tasks.findByName("plainJavadocJar")
    val metadataTask = tasks.findByName("generateMetadataFileForMavenPublication")

    if (plainJavadocJarTask != null && metadataTask != null) {
        metadataTask.dependsOn(plainJavadocJarTask)
    }
}

mavenPublishing {
    coordinates(group.toString(), name.toString(), version.toString())

    publishToMavenCentral()
    signAllPublications()

    pom {
        name.set("WLDT CoAP Physical Adapter")
        description.set("The CoAP Physical Adapter for a WLDT Digital Twin")
        inceptionYear.set("2025")
        url.set("https://github.com/wldt/coap-physical-adapter-java")
        licenses {
            license {
                name.set("The Apache License, Version 2.0")
                url.set("http://www.apache.org/licenses/LICENSE-2.0.txt")
                distribution.set("http://www.apache.org/licenses/LICENSE-2.0.txt")
            }
        }
        developers {
            developer {
                id.set("msandonini")
                name.set("Mirco Sandonini")
                url.set("https://github.com/msandonini")
            }
            developer {
                id.set("piconem")
                name.set("Marco Picone")
                url.set("https://github.com/piconem")
            }
        }
        scm {
            url.set("https://github.com/wldt/coap-physical-adapter-java")
            connection.set("scm:git:git://github.com/wldt/coap-physical-adapter-java.git")
            developerConnection.set("scm:git:ssh://git@github.com/wldt/coap-physical-adapter-java.git")
        }
    }
}
