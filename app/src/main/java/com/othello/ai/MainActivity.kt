package com.othello.ai

import android.graphics.Color
import android.os.Bundle
import android.os.Handler
import android.os.Looper
import android.view.Gravity
import android.view.View
import android.widget.*
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import java.util.concurrent.Executors

class MainActivity : AppCompatActivity() {

    private lateinit var boardView: BoardView
    private lateinit var tvBlack: TextView
    private lateinit var tvWhite: TextView
    private lateinit var tvStatus: TextView
    private lateinit var btnNew: Button
    private lateinit var spinner: ProgressBar

    private var board = OthelloEngine.newBoard()
    private var currentPlayer = OthelloEngine.BLACK
    private var difficulty = 4  // depth
    private var gameOver = false

    private val executor = Executors.newSingleThreadExecutor()
    private val handler  = Handler(Looper.getMainLooper())

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        buildUI()
        startGame()
    }

    private fun buildUI() {
        val root = LinearLayout(this).apply {
            orientation = LinearLayout.VERTICAL
            setBackgroundColor(Color.parseColor("#1A1A2E"))
            setPadding(16, 48, 16, 16)
        }

        // Title
        val tvTitle = TextView(this).apply {
            text = "Othello AI"
            textSize = 26f
            setTextColor(Color.WHITE)
            gravity = Gravity.CENTER
            setPadding(0, 0, 0, 12)
            setTypeface(null, android.graphics.Typeface.BOLD)
        }
        root.addView(tvTitle)

        // Difficulty row
        val diffRow = LinearLayout(this).apply {
            orientation = LinearLayout.HORIZONTAL
            gravity = Gravity.CENTER
            setPadding(0, 0, 0, 12)
        }
        val tvDiff = TextView(this).apply { text = "কঠিনতা: "; setTextColor(Color.LTGRAY); textSize = 14f }
        diffRow.addView(tvDiff)
        for ((label, depth) in listOf("সহজ" to 2, "মাঝারি" to 4, "কঠিন" to 6)) {
            val btn = Button(this).apply {
                text = label; textSize = 12f
                setPadding(16, 4, 16, 4)
                setBackgroundColor(if (depth == difficulty) Color.parseColor("#7C4DFF") else Color.parseColor("#333355"))
                setTextColor(Color.WHITE)
                setOnClickListener {
                    difficulty = depth
                    buildUI()
                    startGame()
                }
            }
            val lp = LinearLayout.LayoutParams(
                LinearLayout.LayoutParams.WRAP_CONTENT,
                LinearLayout.LayoutParams.WRAP_CONTENT
            ).apply { setMargins(6, 0, 6, 0) }
            diffRow.addView(btn, lp)
        }
        root.addView(diffRow)

        // Score row
        val scoreRow = LinearLayout(this).apply {
            orientation = LinearLayout.HORIZONTAL
            gravity = Gravity.CENTER
            setPadding(0, 0, 0, 8)
        }
        tvBlack = makeScoreBox("⚫ তুমি", "2")
        tvWhite = makeScoreBox("⚪ AI", "2")
        scoreRow.addView(tvBlack)
        val spacer = View(this)
        scoreRow.addView(spacer, LinearLayout.LayoutParams(40, 1))
        scoreRow.addView(tvWhite)
        root.addView(scoreRow)

        // Status
        tvStatus = TextView(this).apply {
            text = "তোমার পালা ⚫"
            textSize = 15f
            setTextColor(Color.parseColor("#FFD700"))
            gravity = Gravity.CENTER
            setPadding(0, 4, 0, 8)
        }
        root.addView(tvStatus)

        // Board
        boardView = BoardView(this)
        val bp = LinearLayout.LayoutParams(
            LinearLayout.LayoutParams.MATCH_PARENT,
            LinearLayout.LayoutParams.WRAP_CONTENT
        ).apply { setMargins(8, 0, 8, 12) }
        root.addView(boardView, bp)

        // Spinner
        spinner = ProgressBar(this).apply {
            visibility = View.GONE
        }
        val sp = LinearLayout.LayoutParams(60, 60).apply { gravity = Gravity.CENTER_HORIZONTAL }
        root.addView(spinner, sp)

        // New game button
        btnNew = Button(this).apply {
            text = "নতুন খেলা"
            setBackgroundColor(Color.parseColor("#7C4DFF"))
            setTextColor(Color.WHITE)
            textSize = 15f
            setOnClickListener { startGame() }
        }
        val np = LinearLayout.LayoutParams(
            LinearLayout.LayoutParams.WRAP_CONTENT,
            LinearLayout.LayoutParams.WRAP_CONTENT
        ).apply { gravity = Gravity.CENTER_HORIZONTAL; topMargin = 8 }
        root.addView(btnNew, np)

        val scroll = ScrollView(this)
        scroll.addView(root)
        setContentView(scroll)
    }

    private fun makeScoreBox(label: String, score: String): TextView {
        return TextView(this).apply {
            text = "$label\n$score"
            textSize = 16f
            setTextColor(Color.WHITE)
            gravity = Gravity.CENTER
            setBackgroundColor(Color.parseColor("#16213E"))
            setPadding(24, 12, 24, 12)
        }
    }

    private fun startGame() {
        board = OthelloEngine.newBoard()
        currentPlayer = OthelloEngine.BLACK
        gameOver = false
        updateUI()
        boardView.onCellClick = { r, c -> onHumanMove(r, c) }
    }

    private fun onHumanMove(r: Int, c: Int) {
        if (gameOver || currentPlayer != OthelloEngine.BLACK) return
        val valid = OthelloEngine.getValidMoves(board, OthelloEngine.BLACK)
        if (Pair(r, c) !in valid) {
            Toast.makeText(this, "ওখানে রাখা যাবে না!", Toast.LENGTH_SHORT).show()
            return
        }
        board = OthelloEngine.applyMove(board, r, c, OthelloEngine.BLACK)
        currentPlayer = OthelloEngine.WHITE
        updateUI()
        checkAndContinue()
    }

    private fun checkAndContinue() {
        val whiteMoves = OthelloEngine.getValidMoves(board, OthelloEngine.WHITE)
        val blackMoves = OthelloEngine.getValidMoves(board, OthelloEngine.BLACK)

        if (whiteMoves.isEmpty() && blackMoves.isEmpty()) {
            endGame(); return
        }
        if (currentPlayer == OthelloEngine.WHITE) {
            if (whiteMoves.isEmpty()) {
                tvStatus.text = "AI এর চাল নেই, তোমার পালা ⚫"
                currentPlayer = OthelloEngine.BLACK
                updateUI(); return
            }
            aiMove()
        } else {
            if (blackMoves.isEmpty()) {
                tvStatus.text = "তোমার চাল নেই, AI এর পালা ⚪"
                currentPlayer = OthelloEngine.WHITE
                updateUI()
                checkAndContinue()
            }
        }
    }

    private fun aiMove() {
        tvStatus.text = "AI ভাবছে... ⚪"
        spinner.visibility = View.VISIBLE
        boardView.onCellClick = null

        executor.submit {
            val move = OthelloEngine.getBestMove(board, OthelloEngine.WHITE, difficulty)
            handler.post {
                spinner.visibility = View.GONE
                if (move != null) {
                    board = OthelloEngine.applyMove(board, move.first, move.second, OthelloEngine.WHITE)
                }
                currentPlayer = OthelloEngine.BLACK
                updateUI()
                boardView.onCellClick = { r, c -> onHumanMove(r, c) }
                checkAndContinue()
            }
        }
    }

    private fun updateUI() {
        boardView.board = board
        boardView.validMoves = if (currentPlayer == OthelloEngine.BLACK && !gameOver)
            OthelloEngine.getValidMoves(board, OthelloEngine.BLACK) else emptyList()
        boardView.invalidate()

        val b = OthelloEngine.countPieces(board, OthelloEngine.BLACK)
        val w = OthelloEngine.countPieces(board, OthelloEngine.WHITE)
        tvBlack.text = "⚫ তুমি\n$b"
        tvWhite.text = "⚪ AI\n$w"

        if (!gameOver)
            tvStatus.text = if (currentPlayer == OthelloEngine.BLACK) "তোমার পালা ⚫" else "AI এর পালা ⚪"
    }

    private fun endGame() {
        gameOver = true
        boardView.validMoves = emptyList()
        boardView.invalidate()
        val b = OthelloEngine.countPieces(board, OthelloEngine.BLACK)
        val w = OthelloEngine.countPieces(board, OthelloEngine.WHITE)
        val msg = when {
            b > w -> "🎉 তুমি জিতেছ! ($b - $w)"
            w > b -> "🤖 AI জিতেছে! ($w - $b)"
            else  -> "🤝 ড্র! ($b - $b)"
        }
        tvStatus.text = msg
        AlertDialog.Builder(this)
            .setTitle("খেলা শেষ!")
            .setMessage(msg)
            .setPositiveButton("আবার খেলো") { _, _ -> startGame() }
            .setNegativeButton("বন্ধ", null)
            .show()
    }

    override fun onDestroy() {
        super.onDestroy()
        executor.shutdown()
    }
}
