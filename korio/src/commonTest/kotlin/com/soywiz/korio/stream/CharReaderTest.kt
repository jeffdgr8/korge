package com.soywiz.korio.stream

import com.soywiz.korio.lang.*
import kotlin.test.*

class CharReaderTest {
    @Test
    fun test() {
        val reader = "áéíóúñ".toByteArray(UTF8).toCharReader(UTF8)
        assertEquals("á,éí,óúñ", listOf(reader.read(1), reader.read(2), reader.read(10)).joinToString(","))
    }
}
