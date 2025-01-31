package com.soywiz.korim.format

import com.soywiz.kds.IntMap
import com.soywiz.klogger.Console
import com.soywiz.korio.experimental.KorioExperimentalApi
import com.soywiz.korio.lang.LATIN1
import com.soywiz.korio.stream.AsyncStream
import com.soywiz.korio.stream.ByteArrayBitReader
import com.soywiz.korio.stream.readAllAsFastStream
import com.soywiz.korio.stream.readBytesExact
import com.soywiz.korio.stream.readS32BE
import com.soywiz.korio.stream.readS32LE
import com.soywiz.korio.stream.readS64BE
import com.soywiz.korio.stream.readStream
import com.soywiz.korio.stream.readString
import com.soywiz.korio.stream.readStringz
import com.soywiz.korio.stream.readU16BE
import com.soywiz.korio.stream.readU24BE
import com.soywiz.korio.stream.readU8
import com.soywiz.korio.stream.skip
import com.soywiz.korio.stream.slice
import com.soywiz.korio.stream.sliceHere
import com.soywiz.korio.stream.sliceWithSize
import com.soywiz.korio.stream.toAsyncStream

// AVIF & HEIC metadata extractor
object AVIFInfo : ISOBMFF("avif")
object HEICInfo : ISOBMFF("heic")

// ISOBMFF
// https://gpac.github.io/mp4box.js/test/filereader.html
// https://en.wikipedia.org/wiki/ISO/IEC_base_media_file_format
// https://www.w3.org/TR/mse-byte-stream-format-isobmff/
open class ISOBMFF(vararg exts: String) : ImageFormatSuspend(*exts) {
    override suspend fun decodeHeaderSuspend(s: AsyncStream, props: ImageDecodingProps): ImageInfo? {
        val ss = s.slice(4 until 8).readString(4, LATIN1)
        if (ss != "ftyp") return null
        return StreamParser(props).also { it.decode(s) }.info
    }

    data class ItemExtent(val offset: Long, val size: Long)
    data class ItemInfo(val id: Int) {
        var type: String = ""
        var extents: ArrayList<ItemExtent> = arrayListOf()
        override fun toString(): String = "ItemInfo($id, $type, $extents)"
    }

    @OptIn(KorioExperimentalApi::class)
    class StreamParser(val props: ImageDecodingProps) {
        val debug get() = props.debug
        var info = ImageInfo()
        val items = IntMap<ItemInfo>()

        suspend fun decode(s: AsyncStream) {
            decodeLevel(s.sliceHere(), 0)
            if (debug) Console.error("ITEMS")
            items.fastValueForEach {
                if (it.type == "Exif") {
                    val extent = it.extents.first()
                    val range = s.sliceWithSize(extent.offset + 4, extent.size - 4).readAllAsFastStream()
                    EXIF.readExif(range.toAsyncStream(), info, debug = debug)
                }
            }
        }

        private suspend fun decodeLevel(s: AsyncStream, level: Int) {
            while (!s.eof()) {
                val blockSize = s.readS32BE()
                val blockType = s.readStringz(4, LATIN1)
                //val blockSubtype = s.readStringz(4, LATIN1)
                //val blockStream = s.readStream(blockSize - 12)
                val blockStream = if (blockSize < 8) {
                    s.readStream(s.getAvailable())
                } else {
                    s.readStream(blockSize - 8)
                }
                //if (blockSize)
                if (debug) {
                    Console.error("${"  ".repeat(level)}blockSize=$blockSize, blockType=$blockType")
                }
                when (blockType) {
                    "ftyp" -> Unit
                    "meta" -> {
                        blockStream.skip(4)
                        decodeLevel(blockStream, level + 1)
                    }
                    /// See ISO 14496-12:2015 § 8.11.3
                    // https://github.com/kornelski/avif-parse/blob/2174e0e15647918cbbcb965ba142c285a6ef457f/src/lib.rs#L1136
                    "iloc" -> {
                        val version = blockStream.readU8()
                        val flags = blockStream.readU24BE()
                        val sizePacked = ByteArrayBitReader(blockStream.readBytesExact(2))
                        val offsetSize = sizePacked.readIntBits(4)
                        val lengthSize = sizePacked.readIntBits(4)
                        val baseOffsetSize = sizePacked.readIntBits(4)
                        val indexSize = when (version) {
                            1, 2 -> sizePacked.readIntBits(4)
                            else -> 0
                        }
                        val count: Int = when (version) {
                            0, 1 -> blockStream.readU16BE()
                            2 -> blockStream.readS32BE()
                            else -> TODO()
                        }

                        suspend fun AsyncStream.readSize(size: Int): Long {
                            return when (size) {
                                0 -> 0L
                                4 -> blockStream.readS32BE().toLong()
                                8 -> blockStream.readS64BE()
                                else -> TODO("size=$size")
                            }
                        }

                        if (debug) {
                            Console.error("iloc version=$version, flags=$flags, count=$count, offsetSize=$offsetSize, lengthSize=$lengthSize, baseOffsetSize=$baseOffsetSize, indexSize=$indexSize")
                        }
                        for (n in 0 until count) {
                            val itemID = when (version) {
                                0, 1 -> blockStream.readU16BE()
                                2 -> blockStream.readS32LE()
                                else -> TODO()
                            }
                            val method = when (version) {
                                0 -> 0
                                1, 2 -> blockStream.readU16BE()
                                else -> TODO()
                            }
                            val dataRefIndex = blockStream.readU16BE()

                            val baseOffset: Long = blockStream.readSize(baseOffsetSize)
                            val extentCount = blockStream.readU16BE()
                            if (debug) {
                                Console.error(" - itemID=$itemID, method=$method, dataRefIndex=$dataRefIndex, baseOffset=$baseOffset, extentCount=$extentCount")
                            }
                            val itemInfo = items.getOrPut(itemID) { ItemInfo(itemID) }
                            for (m in 0 until extentCount) {
                                val extentIndex = blockStream.readSize(indexSize)
                                val extentOffset = blockStream.readSize(offsetSize)
                                val extentLength = blockStream.readSize(lengthSize)
                                val offset = baseOffset + extentOffset
                                if (debug) {
                                    Console.error("   - $extentIndex, $offset, $extentLength")
                                }
                                itemInfo.extents.add(ItemExtent(offset, extentLength))
                            }
                            //Console.error(" - $itemID, $unk2, $offset, $size")
                        }
                    }
                    "iinf" -> {
                        val version = blockStream.readU8()
                        val flags = blockStream.readU24BE()
                        val entryCount = when (version) {
                            0 -> blockStream.readU16BE()
                            1 -> blockStream.readS32BE()
                            else -> TODO()
                        }
                        for (n in 0 until entryCount) {
                            decodeLevel(blockStream, level + 1)
                        }
                    }
                    "infe" -> {
                        val version = blockStream.readU8()
                        val flags = blockStream.readU24BE()
                        val itemID: Int = when (version) {
                            2 -> blockStream.readU16BE()
                            3 -> blockStream.readS32BE()
                            else -> TODO()
                        }
                        val protectionIndex = blockStream.readU16BE()
                        val itemType = blockStream.readStringz(4, LATIN1)
                        val itemInfo = items.getOrPut(itemID) { ItemInfo(itemID) }
                        itemInfo.type = itemType
                        if (debug) Console.error("infe: itemID=$itemID, protectionIndex=$protectionIndex, itemType=${itemType}")
                    }
                    "iprp" -> decodeLevel(blockStream, level + 1)
                    "ipco" -> decodeLevel(blockStream, level + 1)
                    "ispe" -> {
                        blockStream.skip(4)
                        info.width = blockStream.readS32BE()
                        info.height = blockStream.readS32BE()
                    }
                    "mdat" -> {
                        blockStream.skip(4)
                    }
                }
            }
        }

        suspend fun checkExif(blockStream: AsyncStream): Boolean {
            if (blockStream.sliceHere().readStringz(4, LATIN1) == "Exif") {
                val exif = EXIF.readExif(blockStream.sliceHere())
                info.orientation = exif.orientation
                return true
            }
            return false
        }
    }
}
