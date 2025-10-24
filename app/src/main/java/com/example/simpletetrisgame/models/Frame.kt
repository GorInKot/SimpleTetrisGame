package com.example.simpletetrisgame.models

import android.util.Log
import com.example.simpletetrisgame.helper.array2dOfByte

class Frame(private val width: Int) {
    val data: ArrayList<ByteArray> = ArrayList()

    fun addRow(byteStr: String): Frame {
        Log.d("TAGG", "Frame: Adding row '$byteStr' for width $width")
        require(byteStr.length == width) { "Row length ${byteStr.length} does not match frame width $width" }
        val row = ByteArray(width)
        for (index in byteStr.indices) {
            row[index] = when (byteStr[index]) {
                '0' -> 0
                '1' -> 1
                else -> throw IllegalArgumentException("Invalid character '${byteStr[index]}' in row string")
            }
        }
        data.add(row)
        return this
    }

    fun as2dByteArray(): Array<ByteArray> {
        val bytes = array2dOfByte(data.size, width)
        return data.toArray(bytes)
    }
}