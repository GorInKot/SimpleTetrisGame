package com.example.simpletetrisgame.models

import android.graphics.Color
import android.util.Log
import com.example.simpletetrisgame.constants.FieldConstrants

enum class BlockColor(val rgbValue: Int, val byteValue: Byte) {
    PINK(Color.rgb(255, 105, 180), 2.toByte()),
    GREEN(Color.rgb(0, 128, 0), 3.toByte()),
    ORANGE(Color.rgb(255, 165, 0), 4.toByte()),
    YELLOW(Color.rgb(255, 215, 0), 5.toByte()),
    CYAN(Color.rgb(0, 255, 255), 6.toByte())
}

class Block(private val shapeIndex: Int, private val color: BlockColor) {
    var frameNumber = 0
    var position = Point(FieldConstrants.COLUMN_COUNT.value / 2, 0)

    companion object {
        fun createBlock(): Block {
            val shapeIndex = (0 until Shape.values().size).random()
            val blockColor = BlockColor.values().random()
            val block = Block(shapeIndex, blockColor)
            val startX = FieldConstrants.COLUMN_COUNT.value / 2 -
                    Shape.values()[shapeIndex].startPosition
            block.position.x = maxOf(0, startX)
            Log.d("TAGG", "Block: Created block with shapeIndex=$shapeIndex, frameCount=${Shape.values()[shapeIndex].frameCount}")
            return block
        }

        fun getColor(value: Byte): Int {
            return BlockColor.values().find { it.byteValue == value }?.rgbValue
                ?: Color.BLACK
        }
    }

    fun setState(frame: Int, position: Point) {
        this.frameNumber = frame
        this.position = position
    }

    fun getShape(frameNumber: Int): Array<ByteArray> {
        return Shape.values()[shapeIndex].getFrame(frameNumber).as2dByteArray()
    }

    fun getFrameCount(): Int = Shape.values()[shapeIndex].frameCount
    fun getColor(): Int = color.rgbValue
    fun getStaticValue(): Byte = color.byteValue
}