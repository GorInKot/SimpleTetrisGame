package com.example.simpletetrisgame.models

import android.graphics.Point
import com.example.simpletetrisgame.constants.CellConstants
import com.example.simpletetrisgame.constants.FieldConstrants
import com.example.simpletetrisgame.helper.array2dOfByte
import com.example.simpletetrisgame.storage.AppPreferences

class AppModel {
    var score: Int = 0
    private var preferences: AppPreferences? = null
    var currentBlock: Block? = null
    var currentState: String = Statuses.AWAITING_START.name

    private var field: Array<ByteArray> = array2dOfByte(
        FieldConstrants.ROW_COUNT.value, FieldConstrants.COLUMN_COUNT.value
    )

    fun setPreferences(preferences: AppPreferences?) {
        this.preferences = preferences
    }

    fun getCellStatus(row: Int, column: Int): Byte? {
        // Проверка границ
        if (row < 0 || row >= FieldConstrants.ROW_COUNT.value || column < 0 || column >= FieldConstrants.COLUMN_COUNT.value) {
            return CellConstants.EMPTY.value
        }
        return field[row][column]
    }

    private fun setCellStatus(row: Int, column: Int, status: Byte?) {
        if (row < 0 || row >= FieldConstrants.ROW_COUNT.value || column < 0 || column >= FieldConstrants.COLUMN_COUNT.value) {
            return
        }
        if (status != null) {
            field[row][column] = status
        }
    }

    fun isGameOver(): Boolean {
        return currentState == Statuses.OVER.name
    }

    fun isGameActive(): Boolean {
        return currentState == Statuses.ACTIVE.name
    }

    fun isGameAwaitingStart(): Boolean {
        return currentState == Statuses.AWAITING_START.name
    }

    private fun boostScore() {
        score += 10
        preferences?.getHighScore()?.let { highScore ->
            if (score > highScore) {
                preferences?.saveHighScore(score)
            }
        }
    }

    private fun generateNextBlock() {
        currentBlock = Block.createBlock()
    }

    private fun validTranslation(position: Point, shape: Array<ByteArray>): Boolean {
        return if (position.y < 0 || position.x < 0) {
            false
        } else if (position.y + shape.size > FieldConstrants.ROW_COUNT.value) {
            false
        } else if (position.x + shape[0].size > FieldConstrants.COLUMN_COUNT.value) {
            false
        } else {
            for (i in 0 until shape.size) {
                for (j in 0 until shape[i].size) {
                    val y = position.y + i
                    val x = position.x + j
                    if (y >= FieldConstrants.ROW_COUNT.value || x >= FieldConstrants.COLUMN_COUNT.value) {
                        return false
                    }
                    if (CellConstants.EMPTY.value != shape[i][j] && CellConstants.EMPTY.value != field[y][x]) {
                        return false
                    }
                }
            }
            true
        }
    }

    private fun moveValid(position: Point, frameNumber: Int?): Boolean {
        return frameNumber?.let { fn ->
            currentBlock?.getShape(fn)?.let { shape ->
                validTranslation(position, shape)
            }
        } ?: false
    }

    fun generateField(action: String) {
        println("AppModel: Generating field with action: $action, currentBlock: $currentBlock, position: ${currentBlock?.position}, frameNumber: ${currentBlock?.frameNumber}, frameCount: ${currentBlock?.frameCount}")
        if (isGameActive()) {
            resetField()
            val frameNumber: Int? = currentBlock?.frameNumber
            val frameCount: Int? = currentBlock?.frameCount
            if (frameNumber == null || frameCount == null) {
                println("AppModel: Invalid frameNumber or frameCount, aborting")
                return
            }
            if (frameNumber < 0 || frameNumber >= frameCount) {
                println("AppModel: Invalid frameNumber $frameNumber for frameCount $frameCount, resetting to 0")
                currentBlock?.setState(0, currentBlock?.position ?: return)
                translateBlock(currentBlock?.position ?: return, 0)
                return
            }
            val coordinate: Point = currentBlock?.position?.let { Point(it.x, it.y) } ?: return

            when (action) {
                Motions.LEFT.name -> {
                    coordinate.x = currentBlock?.position?.x?.minus(1) ?: return
                }
                Motions.RIGHT.name -> {
                    coordinate.x = currentBlock?.position?.x?.plus(1) ?: return
                }
                Motions.DOWN.name -> {
                    coordinate.y = currentBlock?.position?.y?.plus(1) ?: return
                }
                Motions.ROTATE.name -> {
                    var newFrameNumber = frameNumber.plus(1)
                    if (newFrameNumber >= frameCount) {
                        newFrameNumber = 0
                    }
                    println("AppModel: Rotating block, frameNumber: $frameNumber, newFrameNumber: $newFrameNumber, frameCount: $frameCount")
                    if (!moveValid(coordinate, newFrameNumber)) {
                        translateBlock(currentBlock?.position ?: return, frameNumber)
                        return
                    }
                    translateBlock(coordinate, newFrameNumber)
                    currentBlock?.setState(newFrameNumber, coordinate)
                    return
                }
            }
            if (!moveValid(coordinate, frameNumber)) {
                translateBlock(currentBlock?.position ?: return, frameNumber)
                if (Motions.DOWN.name == action) {
                    boostScore()
                    persistCellData()
                    assessField()
                    generateNextBlock()
                    if (!blockAdditionPossible()) {
                        currentState = Statuses.OVER.name
                        currentBlock = null
                        resetField(false)
                    }
                }
            } else {
                translateBlock(coordinate, frameNumber)
                currentBlock?.setState(frameNumber, coordinate)
            }
        }
    }

    private fun resetField(ephemeralCellsOnly: Boolean = true) {
        for (i in 0 until FieldConstrants.ROW_COUNT.value) {
            (0 until FieldConstrants.COLUMN_COUNT.value)
                .filter { !ephemeralCellsOnly || field[i][it] == CellConstants.EPHEMERAL.value }
                .forEach { field[i][it] = CellConstants.EMPTY.value }
        }
    }

    private fun persistCellData() {
        for (i in 0 until field.size) {
            for (j in 0 until field[i].size) {
                var status = getCellStatus(i, j)
                if (status == CellConstants.EPHEMERAL.value) {
                    status = currentBlock?.staticValue
                    setCellStatus(i, j, status)
                }
            }
        }
    }

    private fun assessField() {
        for (i in 0 until field.size) {
            var emptyCells = 0
            for (j in 0 until field[i].size) {
                val status = getCellStatus(i, j)
                val isEmpty = CellConstants.EMPTY.value == status
                if (isEmpty) emptyCells++
            }
            if (emptyCells == 0) shiftRows(i)
        }
    }

    private fun translateBlock(position: Point, frameNumber: Int) {
        synchronized(field) {
            val shape: Array<ByteArray>? = currentBlock?.getShape(frameNumber)
            if (shape != null) {
                for (i in shape.indices) {
                    for (j in 0 until shape[i].size) {
                        val y = position.y + i
                        val x = position.x + j
                        if (y >= 0 && y < FieldConstrants.ROW_COUNT.value && x >= 0 && x < FieldConstrants.COLUMN_COUNT.value) {
                            if (CellConstants.EMPTY.value != shape[i][j]) {
                                field[y][x] = shape[i][j]
                            }
                        }
                    }
                }
            }
        }
    }

    private fun blockAdditionPossible(): Boolean {
        return currentBlock?.let {
            moveValid(it.position, it.frameNumber)
        } ?: false
    }

    private fun shiftRows(nToRow: Int) {
        if (nToRow > 0) {
            for (j in nToRow - 1 downTo 0) {
                for (m in 0 until field[j].size) {
                    setCellStatus(j + 1, m, getCellStatus(j, m))
                }
            }
        }
        for (j in 0 until field[0].size) {
            setCellStatus(0, j, CellConstants.EMPTY.value)
        }
    }

    fun startGame() {
        if (!isGameActive()) {
            currentState = Statuses.ACTIVE.name
            generateNextBlock()
        }
    }

    fun restartGame() {
        resetModel()
        startGame()
    }

    fun endGame() {
        score = 0
        currentState = Statuses.OVER.name
    }

    private fun resetModel() {
        resetField(false)
        currentState = Statuses.AWAITING_START.name
        score = 0
    }

    enum class Statuses {
        AWAITING_START, ACTIVE, INACTIVE, OVER
    }

    enum class Motions {
        LEFT, RIGHT, DOWN, ROTATE
    }
}