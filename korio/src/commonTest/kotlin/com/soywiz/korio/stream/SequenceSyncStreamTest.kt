package com.soywiz.korio.stream

import com.soywiz.korio.lang.*
import kotlin.test.*

class SequenceSyncStreamTest {
    @Test
    fun test() {
        val stream = sequenceSyncStream {
            repeat(10) {
                yield("á".toByteArray(UTF8))
            }
        }
        assertEquals("áááááááááá", stream.readBytes(100).toString(UTF8))
        assertEquals("", stream.readBytes(100).toString(UTF8))
    }
}
