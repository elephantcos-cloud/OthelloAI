package com.othello.ai

object OthelloEngine {

    const val EMPTY = 0
    const val BLACK = 1  // মানুষ
    const val WHITE = 2  // AI

    private val DR = intArrayOf(-1,-1,-1, 0, 0, 1, 1, 1)
    private val DC = intArrayOf(-1, 0, 1,-1, 1,-1, 0, 1)

    private val WEIGHT = arrayOf(
        intArrayOf(100,-20, 10,  5,  5, 10,-20,100),
        intArrayOf(-20,-50, -2, -2, -2, -2,-50,-20),
        intArrayOf( 10, -2,  5,  1,  1,  5, -2, 10),
        intArrayOf(  5, -2,  1,  0,  0,  1, -2,  5),
        intArrayOf(  5, -2,  1,  0,  0,  1, -2,  5),
        intArrayOf( 10, -2,  5,  1,  1,  5, -2, 10),
        intArrayOf(-20,-50, -2, -2, -2, -2,-50,-20),
        intArrayOf(100,-20, 10,  5,  5, 10,-20,100)
    )

    fun newBoard(): Array<IntArray> {
        val b = Array(8) { IntArray(8) }
        b[3][3] = WHITE; b[3][4] = BLACK
        b[4][3] = BLACK; b[4][4] = WHITE
        return b
    }

    fun canPlace(board: Array<IntArray>, r: Int, c: Int, player: Int): Boolean {
        if (board[r][c] != EMPTY) return false
        val opp = if (player == BLACK) WHITE else BLACK
        for (d in 0..7) {
            var nr = r + DR[d]; var nc = c + DC[d]
            var foundOpp = false
            while (nr in 0..7 && nc in 0..7 && board[nr][nc] == opp) {
                foundOpp = true; nr += DR[d]; nc += DC[d]
            }
            if (foundOpp && nr in 0..7 && nc in 0..7 && board[nr][nc] == player) return true
        }
        return false
    }

    fun getValidMoves(board: Array<IntArray>, player: Int): List<Pair<Int,Int>> {
        val moves = mutableListOf<Pair<Int,Int>>()
        for (r in 0..7) for (c in 0..7)
            if (canPlace(board, r, c, player)) moves.add(Pair(r, c))
        return moves
    }

    fun applyMove(board: Array<IntArray>, r: Int, c: Int, player: Int): Array<IntArray> {
        val newBoard = board.map { it.clone() }.toTypedArray()
        val opp = if (player == BLACK) WHITE else BLACK
        newBoard[r][c] = player
        for (d in 0..7) {
            var nr = r + DR[d]; var nc = c + DC[d]; var cnt = 0
            while (nr in 0..7 && nc in 0..7 && newBoard[nr][nc] == opp) {
                cnt++; nr += DR[d]; nc += DC[d]
            }
            if (cnt > 0 && nr in 0..7 && nc in 0..7 && newBoard[nr][nc] == player) {
                var fr = r + DR[d]; var fc = c + DC[d]
                repeat(cnt) { newBoard[fr][fc] = player; fr += DR[d]; fc += DC[d] }
            }
        }
        return newBoard
    }

    fun countPieces(board: Array<IntArray>, player: Int) =
        board.sumOf { row -> row.count { it == player } }

    private fun evaluate(board: Array<IntArray>, player: Int): Int {
        val opp = if (player == BLACK) WHITE else BLACK
        var score = 0
        for (r in 0..7) for (c in 0..7) {
            when (board[r][c]) {
                player -> score += WEIGHT[r][c]
                opp    -> score -= WEIGHT[r][c]
            }
        }
        val myMoves = getValidMoves(board, player).size
        val oppMoves = getValidMoves(board, opp).size
        score += 5 * (myMoves - oppMoves)
        return score
    }

    private fun minimax(
        board: Array<IntArray>, depth: Int,
        alpha: Int, beta: Int, player: Int, maximizing: Boolean, aiPlayer: Int
    ): Int {
        val opp = if (player == BLACK) WHITE else BLACK
        val moves = getValidMoves(board, player)
        if (depth == 0 || moves.isEmpty()) {
            val oppMoves = getValidMoves(board, opp)
            if (moves.isEmpty() && oppMoves.isEmpty()) {
                val b = countPieces(board, BLACK); val w = countPieces(board, WHITE)
                return when {
                    b > w -> if (aiPlayer == BLACK)  5000 else -5000
                    w > b -> if (aiPlayer == WHITE)  5000 else -5000
                    else  -> 0
                }
            }
            if (moves.isEmpty()) return minimax(board, depth, alpha, beta, opp, !maximizing, aiPlayer)
            return evaluate(board, aiPlayer)
        }
        var a = alpha; var b2 = beta
        return if (maximizing) {
            var best = Int.MIN_VALUE
            for ((r, c) in moves) {
                val nb = applyMove(board, r, c, player)
                val v = minimax(nb, depth-1, a, b2, opp, false, aiPlayer)
                if (v > best) best = v
                if (v > a) a = v
                if (b2 <= a) break
            }
            best
        } else {
            var best = Int.MAX_VALUE
            for ((r, c) in moves) {
                val nb = applyMove(board, r, c, player)
                val v = minimax(nb, depth-1, a, b2, opp, true, aiPlayer)
                if (v < best) best = v
                if (v < b2) b2 = v
                if (b2 <= a) break
            }
            best
        }
    }

    fun getBestMove(board: Array<IntArray>, player: Int, depth: Int): Pair<Int,Int>? {
        val moves = getValidMoves(board, player)
        if (moves.isEmpty()) return null
        val opp = if (player == BLACK) WHITE else BLACK
        var bestVal = Int.MIN_VALUE
        var bestMove = moves[0]
        for ((r, c) in moves) {
            val nb = applyMove(board, r, c, player)
            val v = minimax(nb, depth-1, Int.MIN_VALUE, Int.MAX_VALUE, opp, false, player)
            if (v > bestVal) { bestVal = v; bestMove = Pair(r, c) }
        }
        return bestMove
    }
}
