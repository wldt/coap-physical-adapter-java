group = "io.github.wldt"
version = "0.1.1"
description = "Physical adapter to connect with the CoAP protocol"
java.sourceCompatibility = JavaVersion.VERSION_1_8


plugins {
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
    testImplementation("junit:junit:4.13.2")
}

java {
    withJavadocJar()
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
