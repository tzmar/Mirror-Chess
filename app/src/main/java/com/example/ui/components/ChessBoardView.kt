package com.example.ui.components

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.gestures.awaitEachGesture
import androidx.compose.foundation.gestures.awaitFirstDown
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxWithConstraints
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.draw.clip
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.IntOffset
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.zIndex
import com.example.ui.theme.AmberAccent
import com.example.ui.theme.BoardCaptureRing
import com.example.ui.theme.BoardCheckSquare
import com.example.ui.theme.BoardDarkSquare
import com.example.ui.theme.BoardLastMoveSquare
import com.example.ui.theme.BoardLegalMoveDot
import com.example.ui.theme.BoardLightSquare
import com.example.ui.theme.BoardSelectedSquare
import com.example.ui.theme.CyanAccent
import com.example.ui.theme.SurfaceElevated
import com.github.bhlangonijr.chesslib.Board
import com.github.bhlangonijr.chesslib.Piece
import com.github.bhlangonijr.chesslib.PieceType
import com.github.bhlangonijr.chesslib.Side
import com.github.bhlangonijr.chesslib.Square
import com.github.bhlangonijr.chesslib.move.Move
import kotlin.math.roundToInt

@Composable
fun ChessBoardView(
    board: Board,
    playerColor: Side,
    selectedSquare: Square?,
    legalMovesFromSelected: List<Move>,
    lastMove: Move?,
    isCheck: Boolean,
    isInteractive: Boolean = true,
    onSquareClick: (Square) -> Unit,
    onPieceDragged: (Square, Square) -> Unit,
    onPromotionChosen: (PieceType) -> Unit,
    pendingPromotionMove: Move?,
    modifier: Modifier = Modifier
) {
    // Orientation: if player is White, rank 8 is top (7 down to 0).
    // If player is Black, rank 1 is top (0 up to 7).
    val rankIndices = if (playerColor == Side.WHITE) (7 downTo 0).toList() else (0..7).toList()
    val fileIndices = if (playerColor == Side.WHITE) (0..7).toList() else (7 downTo 0).toList()

    val fileLabels = listOf("a", "b", "c", "d", "e", "f", "g", "h")
    val rankLabels = listOf("1", "2", "3", "4", "5", "6", "7", "8")

    // Drag-and-drop state
    var draggingSquare by remember { mutableStateOf<Square?>(null) }
    var dragCurrentOffset by remember { mutableStateOf<Offset?>(null) }
    var hoverSquare by remember { mutableStateOf<Square?>(null) }

    Card(
        modifier = modifier
            .fillMaxWidth()
            .aspectRatio(1f)
            .testTag("chess_board_card"),
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(containerColor = SurfaceElevated),
        elevation = CardDefaults.cardElevation(defaultElevation = 8.dp)
    ) {
        BoxWithConstraints(
            modifier = Modifier
                .fillMaxSize()
                .padding(6.dp)
                .pointerInput(board, playerColor, isInteractive) {
                    if (!isInteractive) return@pointerInput
                    awaitEachGesture {
                        val down = awaitFirstDown(requireUnconsumed = false)
                        val startOffset = down.position
                        val boardWidth = size.width.toFloat()
                        val boardHeight = size.height.toFloat()

                        val startSquare = getSquareFromOffset(
                            offset = startOffset,
                            boardWidth = boardWidth,
                            boardHeight = boardHeight,
                            rankIndices = rankIndices,
                            fileIndices = fileIndices,
                            fileLabels = fileLabels,
                            rankLabels = rankLabels
                        )

                        val pieceAtStart = if (startSquare != Square.NONE) board.getPiece(startSquare) else Piece.NONE
                        val canDragPiece = pieceAtStart != Piece.NONE && pieceAtStart.pieceSide == playerColor
                        var isDragStarted = false
                        val touchSlop = viewConfiguration.touchSlop

                        while (true) {
                            val event = awaitPointerEvent()
                            val change = event.changes.firstOrNull() ?: break

                            if (change.pressed) {
                                val distance = (change.position - startOffset).getDistance()
                                if (distance > touchSlop && canDragPiece && !isDragStarted) {
                                    isDragStarted = true
                                    draggingSquare = startSquare
                                    dragCurrentOffset = change.position
                                    onSquareClick(startSquare)
                                }

                                if (isDragStarted) {
                                    change.consume()
                                    dragCurrentOffset = change.position
                                    hoverSquare = getSquareFromOffset(
                                        offset = change.position,
                                        boardWidth = boardWidth,
                                        boardHeight = boardHeight,
                                        rankIndices = rankIndices,
                                        fileIndices = fileIndices,
                                        fileLabels = fileLabels,
                                        rankLabels = rankLabels
                                    )
                                }
                            } else {
                                // Pointer lifted
                                if (isDragStarted && draggingSquare != null) {
                                    change.consume()
                                    val targetSquare = getSquareFromOffset(
                                        offset = change.position,
                                        boardWidth = boardWidth,
                                        boardHeight = boardHeight,
                                        rankIndices = rankIndices,
                                        fileIndices = fileIndices,
                                        fileLabels = fileLabels,
                                        rankLabels = rankLabels
                                    )
                                    onPieceDragged(draggingSquare!!, targetSquare)
                                } else {
                                    // Clean tap
                                    if (startSquare != Square.NONE) {
                                        onSquareClick(startSquare)
                                    }
                                }
                                draggingSquare = null
                                dragCurrentOffset = null
                                hoverSquare = null
                                break
                            }
                        }
                    }
                }
        ) {
            val squareSize = maxWidth / 8f

            // 8x8 Grid of Squares
            Column(modifier = Modifier.fillMaxSize()) {
                for (r in rankIndices) {
                    Row(modifier = Modifier.fillMaxWidth().weight(1f)) {
                        for (f in fileIndices) {
                            val squareName = "${fileLabels[f].uppercase()}${rankLabels[r]}"
                            val square = try {
                                Square.valueOf(squareName)
                            } catch (_: Exception) {
                                Square.NONE
                            }

                            val isLight = (r + f) % 2 != 0
                            val piece = if (square != Square.NONE) board.getPiece(square) else Piece.NONE

                            val isSelected = square == selectedSquare
                            val isLastMoveSquare = lastMove != null && (square == lastMove.from || square == lastMove.to)
                            val isKingInCheck = isCheck && piece.pieceType == PieceType.KING && piece.pieceSide == board.sideToMove
                            val matchingLegalMove = legalMovesFromSelected.firstOrNull { it.to == square }
                            val isLegalDestination = matchingLegalMove != null
                            val isCaptureDestination = isLegalDestination && piece != Piece.NONE
                            val isHoverTarget = square == hoverSquare && isLegalDestination
                            val isBeingDragged = square == draggingSquare

                            Box(
                                modifier = Modifier
                                    .weight(1f)
                                    .aspectRatio(1f)
                                    .background(
                                        when {
                                            isKingInCheck -> BoardCheckSquare
                                            isSelected -> BoardSelectedSquare
                                            isLastMoveSquare -> BoardLastMoveSquare
                                            isLight -> BoardLightSquare
                                            else -> BoardDarkSquare
                                        }
                                    )
                                    .then(
                                        if (isHoverTarget) {
                                            Modifier.border(2.5.dp, CyanAccent, RoundedCornerShape(2.dp))
                                        } else {
                                            Modifier
                                        }
                                    )
                                    .testTag("square_$squareName"),
                                contentAlignment = Alignment.Center
                            ) {
                                // Coordinate labels along the board outer borders
                                if (f == fileIndices.first()) {
                                    Text(
                                        text = rankLabels[r],
                                        modifier = Modifier
                                            .align(Alignment.TopStart)
                                            .padding(2.dp),
                                        fontSize = 9.sp,
                                        fontWeight = FontWeight.Bold,
                                        color = if (isLight) BoardDarkSquare else BoardLightSquare
                                    )
                                }
                                if (r == rankIndices.last()) {
                                    Text(
                                        text = fileLabels[f],
                                        modifier = Modifier
                                            .align(Alignment.BottomEnd)
                                            .padding(2.dp),
                                        fontSize = 9.sp,
                                        fontWeight = FontWeight.Bold,
                                        color = if (isLight) BoardDarkSquare else BoardLightSquare
                                    )
                                }

                                // Chess piece (dimmed if being dragged from this square)
                                if (piece != Piece.NONE) {
                                    Box(
                                        modifier = Modifier
                                            .fillMaxSize()
                                            .alpha(if (isBeingDragged) 0.25f else 1f)
                                    ) {
                                        ChessPieceView(
                                            piece = piece,
                                            modifier = Modifier
                                                .fillMaxSize()
                                                .padding(3.dp)
                                        )
                                    }
                                }

                                // Legal move indicators
                                if (isLegalDestination) {
                                    if (isCaptureDestination) {
                                        // Capture target ring
                                        Box(
                                            modifier = Modifier
                                                .fillMaxSize()
                                                .padding(2.dp)
                                                .border(3.dp, BoardCaptureRing, CircleShape)
                                        )
                                    } else {
                                        // Quiet move center dot
                                        Box(
                                            modifier = Modifier
                                                .size(squareSize * 0.28f)
                                                .clip(CircleShape)
                                                .background(BoardLegalMoveDot)
                                        )
                                    }
                                }
                            }
                        }
                    }
                }
            }

            // Floating dragged piece directly following user's pointer
            if (draggingSquare != null && dragCurrentOffset != null) {
                val piece = board.getPiece(draggingSquare!!)
                if (piece != Piece.NONE) {
                    val floatingPieceSize = squareSize * 1.25f
                    Box(
                        modifier = Modifier
                            .offset {
                                IntOffset(
                                    x = (dragCurrentOffset!!.x - (floatingPieceSize.toPx() / 2f)).roundToInt(),
                                    y = (dragCurrentOffset!!.y - (floatingPieceSize.toPx() * 0.82f)).roundToInt() // slightly above thumb
                                )
                            }
                            .size(floatingPieceSize)
                            .zIndex(100f),
                        contentAlignment = Alignment.Center
                    ) {
                        ChessPieceView(
                            piece = piece,
                            modifier = Modifier.fillMaxSize()
                        )
                    }
                }
            }
        }
    }

    // Pawn Promotion Dialog
    if (pendingPromotionMove != null) {
        PromotionDialog(
            side = board.sideToMove,
            onSelect = onPromotionChosen
        )
    }
}

private fun getSquareFromOffset(
    offset: Offset,
    boardWidth: Float,
    boardHeight: Float,
    rankIndices: List<Int>,
    fileIndices: List<Int>,
    fileLabels: List<String>,
    rankLabels: List<String>
): Square {
    if (boardWidth <= 0f || boardHeight <= 0f) return Square.NONE
    val colIndex = ((offset.x / boardWidth) * 8).toInt().coerceIn(0, 7)
    val rowIndex = ((offset.y / boardHeight) * 8).toInt().coerceIn(0, 7)
    val r = rankIndices[rowIndex]
    val f = fileIndices[colIndex]
    val squareName = "${fileLabels[f].uppercase()}${rankLabels[r]}"
    return try {
        Square.valueOf(squareName)
    } catch (_: Exception) {
        Square.NONE
    }
}

@Composable
fun PromotionDialog(
    side: Side,
    onSelect: (PieceType) -> Unit
) {
    val choices = listOf(
        PieceType.QUEEN to "Queen",
        PieceType.KNIGHT to "Knight",
        PieceType.ROOK to "Rook",
        PieceType.BISHOP to "Bishop"
    )

    AlertDialog(
        onDismissRequest = { onSelect(PieceType.QUEEN) },
        title = {
            Text(
                text = "Promote Pawn",
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.Bold,
                color = CyanAccent
            )
        },
        text = {
            Column(
                modifier = Modifier.fillMaxWidth(),
                verticalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                Text(
                    text = "Choose promotion piece:",
                    style = MaterialTheme.typography.bodyMedium
                )
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceEvenly
                ) {
                    choices.forEach { (type, label) ->
                        val dummyPiece = Piece.make(side, type)
                        Column(
                            horizontalAlignment = Alignment.CenterHorizontally,
                            modifier = Modifier
                                .clip(RoundedCornerShape(8.dp))
                                .clickable { onSelect(type) }
                                .padding(8.dp)
                                .testTag("promote_$label")
                        ) {
                            Box(modifier = Modifier.size(44.dp)) {
                                ChessPieceView(piece = dummyPiece)
                            }
                            Text(
                                text = label,
                                style = MaterialTheme.typography.labelSmall,
                                fontWeight = FontWeight.SemiBold
                            )
                        }
                    }
                }
            }
        },
        confirmButton = {},
        dismissButton = {}
    )
}
