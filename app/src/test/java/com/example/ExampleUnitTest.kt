package com.example

import org.junit.Assert.*
import org.junit.Test

/**
 * Example local unit test, which will execute on the development machine (host).
 *
 * See [testing documentation](http://d.android.com/tools/testing).
 */
class ExampleUnitTest {
  @Test
  fun testChesslib() {
    val board = com.github.bhlangonijr.chesslib.Board()
    assertEquals("rnbqkbnr/pppppppp/8/8/8/8/PPPPPPPP/RNBQKBNR w KQkq - 0 1", board.fen)
    val legalMoves = board.legalMoves()
    assertTrue(legalMoves.isNotEmpty())
    val move = com.github.bhlangonijr.chesslib.move.Move(
      com.github.bhlangonijr.chesslib.Square.E2,
      com.github.bhlangonijr.chesslib.Square.E4
    )
    assertTrue(board.isMoveLegal(move, true))
    board.doMove(move)
    assertEquals(com.github.bhlangonijr.chesslib.Side.BLACK, board.sideToMove)
  }
}
