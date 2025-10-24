package com.example.simpletetrisgame

import android.content.Context
import android.graphics.Canvas
import android.graphics.Color
import android.graphics.Paint
import android.graphics.RectF
import android.os.Handler
import android.os.Looper
import android.os.Message
import android.util.AttributeSet
import android.util.Log
import android.view.View
import android.widget.Toast
import com.example.simpletetrisgame.constants.CellConstants
import com.example.simpletetrisgame.constants.FieldConstrants
import com.example.simpletetrisgame.models.AppModel
import com.example.simpletetrisgame.models.Block

class TetrisView: View {
    private val paint = Paint()
    private var lastMove: Long = 0
    var model: AppModel? = null
    var activity: GameActivity? = null
    private val viewHandler = ViewHandler(this)
    private var cellSize: Dimension = Dimension(0,0)
    private var frameOffset: Dimension = Dimension(0,0)

    constructor(context: Context, attrs: AttributeSet):
            super(context, attrs)

    constructor(context: Context, attrs: AttributeSet, defStyle: Int):
            super(context, attrs, defStyle)

    companion object {
        private val DELAY = 500
        private val BLOCK_OFFSET = 2
        private val FRAME_OFFSET_BASE = 10
    }

//    fun setModel(model: AppModel) {
//        this.model = model
//    }
//
//    fun setActivity(gameActivity: GameActivity) {
//        this.activity = gameActivity
//    }

    fun setGameCommand(move: AppModel.Motions) {
        model?.let { model ->
            if (model.currentState == AppModel.Statuses.ACTIVE.name) {
                Log.d("TAGG","TetrisView: Setting game command: $move")
                if (move == AppModel.Motions.DOWN) {
                    model.generateField(move.name)
                    invalidate()
                    return
                }
                setGameCommandWithDelay(move)
            }
        }
    }

    fun setGameCommandWithDelay(move: AppModel.Motions) {
        val now = System.currentTimeMillis()
        if (now - lastMove > DELAY) {
            Log.d("TAGG","TetrisView: Setting game command with delay: $move")
            model?.generateField(move.name)
            invalidate()
            lastMove = now
        }
        updateScores()
        viewHandler.sleep(DELAY.toLong())
    }

    private fun updateScores() {
        activity?.tvCurrentScore?.text = "${model?.score}"
        activity?.tvHighScore?.text = "${activity?.appPreferences?.getHighScore()}"
    }

    override fun onDraw(canvas: Canvas) {
        super.onDraw(canvas)
        drawFrame(canvas)
        if (model != null) {
            for (i in 0 until FieldConstrants.ROW_COUNT.value) {
                for (j in 0 until FieldConstrants.COLUMN_COUNT.value) {
                    drawCell(canvas, i, j)
                }
            }
        }
    }

    private fun drawFrame(canvas: Canvas) {
        paint.color = Color.LTGRAY
        canvas.drawRect(
            frameOffset.width.toFloat(),
            frameOffset.height.toFloat(),
            width - frameOffset.width.toFloat(),
            height - frameOffset.height.toFloat(),
            paint
        )
    }

    private fun drawCell(canvas: Canvas, row: Int, col: Int) {
        val cellStatus = model?.getCellStatus(row, col)
        if (cellStatus != CellConstants.EMPTY.value) {
            val color = if (cellStatus == CellConstants.EPHEMERAL.value) {
                model?.currentBlock?.getColor() ?: Color.BLACK
            } else {
                Block.getColor(cellStatus ?: CellConstants.EMPTY.value)
            }
            drawCell(canvas, col, row, color)
        }
    }

    private fun drawCell(canvas: Canvas, x: Int, y: Int, rgbColor: Int) {
        paint.color = rgbColor
        val top: Float = (frameOffset.height + y * cellSize.height + BLOCK_OFFSET).toFloat()
        val left: Float = (frameOffset.width + x * cellSize.width + BLOCK_OFFSET).toFloat()
        val bottom: Float = (frameOffset.height + (y + 1) * cellSize.height - BLOCK_OFFSET).toFloat()
        val right: Float = (frameOffset.width + (x + 1) * cellSize.width - BLOCK_OFFSET).toFloat()
        val rectangle = RectF(left, top, right, bottom)
        canvas.drawRoundRect(rectangle, 4f, 4f, paint)
    }

//    private fun drawCell(canvas: Canvas, row: Int, col: Int) {
//        val cellStatus = model?.getCellStatus(row, col)
//        if (cellStatus != CellConstants.EMPTY.value) {
//            val color = if (cellStatus == CellConstants.EPHEMERAL.value) {
//                model?.currentBlock?.color
//            } else {
//                Block.getColor(cellStatus as Byte)
//            }
//            color?.let { drawCell(canvas, col, row, it) } // Исправлен порядок: col, row
//        }
//    }
//
//    private fun drawCell(canvas: Canvas, x: Int, y: Int, rgbColor: Int) {
//        paint.color = rgbColor
//        val top: Float = (frameOffset.height + y * cellSize.height + BLOCK_OFFSET).toFloat()
//        val left: Float = (frameOffset.width + x * cellSize.width + BLOCK_OFFSET).toFloat()
//        val right: Float = (frameOffset.height + (x + 1) * cellSize.height + BLOCK_OFFSET).toFloat()
//        val bottom: Float = (frameOffset.width + (y + 1) * cellSize.width + BLOCK_OFFSET).toFloat()
//        val rectangle = RectF(left, top, right, bottom)
//        canvas.drawRoundRect(rectangle, 4F, 4F, paint)
//    }

    override fun onSizeChanged(width: Int, height: Int, previousWidth: Int, previousHeight: Int) {
        super.onSizeChanged(width, height, previousWidth, previousHeight)

        val cellWidth = (width - 2 * FRAME_OFFSET_BASE) / FieldConstrants.COLUMN_COUNT.value
        val cellHeight = (height - 2 * FRAME_OFFSET_BASE) / FieldConstrants.ROW_COUNT.value
        val n = Math.min(cellWidth, cellHeight)
        this.cellSize = Dimension(n,n)
        val offsetX = (width - FieldConstrants.COLUMN_COUNT.value * n) / 2
        val offsetY = (height - FieldConstrants.ROW_COUNT.value * n) / 2
        this.frameOffset = Dimension(offsetX, offsetY)

    }
}

private class ViewHandler(private val owner: TetrisView) : Handler(Looper.getMainLooper()) {
    override fun handleMessage(message: Message) {
        if (message.what == 0) {
            owner.model?.let { model ->
                if (model.isGameOver()) {
                    model.endGame()
                    Toast.makeText(owner.activity, "Game Over", Toast.LENGTH_LONG).show()
                }
                if (model.isGameActive()) {
                    owner.setGameCommandWithDelay(AppModel.Motions.DOWN)
                }
            }
        }
    }

    fun sleep(delay: Long) {
        removeMessages(0)
        sendMessageDelayed(obtainMessage(0), delay)
    }
}

private data class Dimension(val width: Int, val height: Int)
