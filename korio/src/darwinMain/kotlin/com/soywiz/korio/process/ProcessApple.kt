package com.soywiz.korio.process

import com.soywiz.korio.file.VfsProcessHandler
import kotlinx.cinterop.*
import platform.posix.*

actual suspend fun posixExec(
    path: String, cmdAndArgs: List<String>, env: Map<String, String>, handler: VfsProcessHandler
): Int {
    TODO()
}
