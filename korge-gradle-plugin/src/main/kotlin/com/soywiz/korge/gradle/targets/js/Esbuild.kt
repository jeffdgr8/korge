package com.soywiz.korge.gradle.targets.js

import com.soywiz.korge.gradle.*
import org.gradle.api.*
import org.gradle.api.tasks.*
import org.gradle.kotlin.dsl.*
import java.io.*

fun Project.configureEsbuild() {
    val wwwFolder = File(rootProject.buildDir, "www")
    val esbuildFolder = File(rootProject.buildDir, "esbuild")
    val isWindows = org.apache.tools.ant.taskdefs.condition.Os.isFamily(org.apache.tools.ant.taskdefs.condition.Os.FAMILY_WINDOWS)
    val esbuildCmd = if (isWindows) File(esbuildFolder, "esbuild.cmd") else File(esbuildFolder, "esbuild")
    val env by lazy { org.jetbrains.kotlin.gradle.targets.js.nodejs.NodeJsRootPlugin.apply(project.rootProject).requireConfigured() }
    val npmCmd by lazy { File(env.nodeDir, if (env.isWindows) "npm.cmd" else "npm") }

    val npmInstallEsbuild = "npmInstallEsbuild"
    if (rootProject.tasks.findByName(npmInstallEsbuild) == null) {
        rootProject.tasks.create(npmInstallEsbuild, Exec::class) {
            it.onlyIf { !esbuildCmd.exists() }
            val esbuildVersion = "0.12.20"
            it.commandLine(npmCmd, "-g", "install", "esbuild@$esbuildVersion", "--prefix", esbuildFolder)
        }
    }

    val browserEsbuildResources by tasks.creating(Copy::class) {
        from(project.tasks.getByName("jsProcessResources").outputs.files)
        //for (sourceSet in gkotlin.js().compilations.flatMap { it.kotlinSourceSets }) from(sourceSet.resources)
        into(wwwFolder)
    }

    val browserPrepareEsbuildPrepare by tasks.creating(Task::class) {
        dependsOn(browserEsbuildResources)
        dependsOn("::npmInstallEsbuild")
    }

    val browserPrepareEsbuildDebug by tasks.creating(Task::class) {
        dependsOn("compileDevelopmentExecutableKotlinJs")
        dependsOn(browserPrepareEsbuildPrepare)
    }

    val browserPrepareEsbuildRelease by tasks.creating(Task::class) {
        dependsOn("compileProductionExecutableKotlinJs")
        dependsOn(browserPrepareEsbuildPrepare)
    }

    for (debug in listOf(false, true)) {
        val debugPrefix = if (debug) "Debug" else "Release"
        val browserPrepareEsbuild = when {
            debug -> browserPrepareEsbuildDebug
            else -> browserPrepareEsbuildRelease
        }

        //for (run in listOf(false, true)) {
        for (run in listOf(false)) {
            val runSuffix = if (run) "Run" else ""

            // browserDebugEsbuild
            // browserReleaseEsbuild
            tasks.create("browser${debugPrefix}Esbuild${runSuffix}", Exec::class) {
                it.dependsOn(browserPrepareEsbuild)

                it.commandLine(ArrayList<Any>().apply {
                    add(esbuildCmd)
                    //add("--watch",)
                    add("--bundle")
                    add("--minify")
                    add("--sourcemap=external")
                    add(File(buildDir, "js/node_modules/${project.name}/kotlin/${project.name}.js"))
                    add("--outfile=${File(wwwFolder, "${project.name}.js")}")
                    // @TODO: Close this command on CTRL+C
                    //if (run) add("--servedir=$wwwFolder")
                })
            }
        }
    }


}