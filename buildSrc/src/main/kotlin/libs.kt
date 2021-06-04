@file:Suppress("ObjectPropertyName")

import org.gradle.api.JavaVersion

object Versions {
    val java = JavaVersion.VERSION_1_8
    const val kotlin = "1.4.20"

    const val ktlint = "0.41.0"

    const val `junit-jupiter` = "5.7.2"
    const val assertk = "0.24"
}

object Libs {
    const val `kotlin-stdlib` = "org.jetbrains.kotlin:kotlin-stdlib:${Versions.kotlin}"
    const val `kotlin-stdlib-common` = "org.jetbrains.kotlin:kotlin-stdlib-common:${Versions.kotlin}"
    const val `kotlin-stdlib-jdk7` = "org.jetbrains.kotlin:kotlin-stdlib-jdk7:${Versions.kotlin}"
    const val `kotlin-stdlib-jdk8` = "org.jetbrains.kotlin:kotlin-stdlib-jdk8:${Versions.kotlin}"
    const val `kotlin-reflect` = "org.jetbrains.kotlin:kotlin-reflect:${Versions.kotlin}"
    const val `kotlin-test-junit5` = "org.jetbrains.kotlin:kotlin-test-junit5:${Versions.kotlin}"

    const val `junit-jupiter` = "org.junit.jupiter:junit-jupiter:${Versions.`junit-jupiter`}"
    const val `junit-jupiter-api` = "org.junit.jupiter:junit-jupiter-api:${Versions.`junit-jupiter`}"

    const val `assertk-jvm` = "com.willowtreeapps.assertk:assertk-jvm:${Versions.assertk}"
}
