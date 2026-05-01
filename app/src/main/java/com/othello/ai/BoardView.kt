package com.othello.ai

import android.content.Context
import android.graphics.*
import android.util.AttributeSet
import android.view.MotionEvent
import android.view.View

class BoardView @JvmOverloads constructor(
    context: Context, attrs: AttributeSet? = null
) : View(context, attrs) {

    var board: Array<IntArray> = OthelloEngine.newBoard()
    var validMoves: List<Pair<Int,Int>> = emptyList()
    var onCellClick: ((Int, Int) -> Unit)? = null

    private val paintBoard = Paint().apply { color = Color.parseColor("#2E7D32") }
    private val paintLine  = Paint().apply { color = Color.parseColor("#1B5E20"); strokeWidth = 2f; style = Paint.Style.STROKE }
    private val paintBlack = Paint().apply { color = Color.parseColor("#212121"); isAntiAlias = true }
    private val paintWhite = Paint().apply { color = Color.parseColor("#F5F5F5"); isAntiAlias = true }
    private val paintWhiteBorder = Paint().apply { color = Color.parseColor("#BDBDBD"); isAntiAlias = true; style = Paint.Style.STROKE; strokeWidth = 2f }
    private val paintHint  = Paint().apply { color = Color.parseColor("#80C8E6"); isAntiAlias = true }
    private val paintStar  = Paint().apply { color = Color.parseColor("#1B5E20"); isAntiAlias = true }

    private var cellSize = 0f

    override fun onMeasure(widthMeasureSpec: Int, heightMeasureSpec: Int) {
        val size = minOf(MeasureSpec.getSize(widthMeasureSpec), MeasureSpec.getSize(heightMeasureSpec))
        setMeasuredDimension(size, size)
    }

    override fun onDraw(canvas: Canvas) {
        cellSize = width / 8f
        canvas.drawRect(0f, 0f, width.toFloat(), height.toFloat(), paintBoard)
        for (i in 0..8) {
            canvas.drawLine(i * cellSize, 0f, i * cellSize, height.toFloat(), paintLine)
            canvas.drawLine(0f, i * cellSize, width.toFloat(), i * cellSize, paintLine)
        }
        // Star points
        for (r in intArrayOf(2, 6)) for (c in intArrayOf(2, 6))
            canvas.drawCircle(c * cellSize + cellSize/2, r * cellSize + cellSize/2, 5f, paintStar)

        // Hint dots
        for ((r, c) in validMoves)
            canvas.drawCircle(c * cellSize + cellSize/2, r * cellSize + cellSize/2, cellSize * 0.15f, paintHint)

        // Pieces
        val pad = cellSize * 0.1f
        for (r in 0..7) for (c in 0..7) {
            val cx = c * cellSize + cellSize/2
            val cy = r * cellSize + cellSize/2
            val rad = cellSize/2 - pad
            when (board[r][c]) {
                OthelloEngine.BLACK -> canvas.drawCircle(cx, cy, rad, paintBlack)
                OthelloEngine.WHITE -> {
                    canvas.drawCircle(cx, cy, rad, paintWhite)
                    canvas.drawCircle(cx, cy, rad, paintWhiteBorder)
                }
            }
        }
    }

    override fun onTouchEvent(event: MotionEvent): Boolean {
        if (event.action == MotionEvent.ACTION_UP) {
            val c = (event.x / cellSize).toInt()
            val r = (event.y / cellSize).toInt()
            if (r in 0..7 && c in 0..7) onCellClick?.invoke(r, c)
        }
        return true
    }
}
