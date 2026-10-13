package com.example.ui.components

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.drawscope.DrawScope
import androidx.compose.ui.graphics.drawscope.Fill
import androidx.compose.ui.graphics.drawscope.Stroke
import com.github.bhlangonijr.chesslib.Piece
import com.github.bhlangonijr.chesslib.PieceType
import com.github.bhlangonijr.chesslib.Side

private val WhiteFill = Color(0xFFF8FAFC)
private val WhiteStroke = Color(0xFF1E293B)
private val BlackFill = Color(0xFF0F172A)
private val BlackStroke = Color(0xFF94A3B8)

@Composable
fun ChessPieceView(
    piece: Piece,
    modifier: Modifier = Modifier
) {
    if (piece == Piece.NONE) return

    val isWhite = piece.pieceSide == Side.WHITE
    val fillColor = if (isWhite) WhiteFill else BlackFill
    val strokeColor = if (isWhite) WhiteStroke else BlackStroke

    Canvas(modifier = modifier.fillMaxSize()) {
        val w = size.width
        val h = size.height

        when (piece.pieceType) {
            PieceType.PAWN -> drawPawn(w, h, fillColor, strokeColor)
            PieceType.KNIGHT -> drawKnight(w, h, fillColor, strokeColor, isWhite)
            PieceType.BISHOP -> drawBishop(w, h, fillColor, strokeColor)
            PieceType.ROOK -> drawRook(w, h, fillColor, strokeColor)
            PieceType.QUEEN -> drawQueen(w, h, fillColor, strokeColor)
            PieceType.KING -> drawKing(w, h, fillColor, strokeColor)
            else -> {}
        }
    }
}

private fun DrawScope.drawPawn(w: Float, h: Float, fill: Color, stroke: Color) {
    val strokeW = w * 0.045f

    // Head
    drawCircle(color = fill, radius = w * 0.16f, center = Offset(w * 0.5f, h * 0.32f), style = Fill)
    drawCircle(color = stroke, radius = w * 0.16f, center = Offset(w * 0.5f, h * 0.32f), style = Stroke(strokeW))

    // Body
    val bodyPath = Path().apply {
        moveTo(w * 0.42f, h * 0.45f)
        lineTo(w * 0.58f, h * 0.45f)
        lineTo(w * 0.70f, h * 0.78f)
        lineTo(w * 0.30f, h * 0.78f)
        close()
    }
    drawPath(bodyPath, color = fill, style = Fill)
    drawPath(bodyPath, color = stroke, style = Stroke(strokeW))

    // Base
    val basePath = Path().apply {
        moveTo(w * 0.22f, h * 0.85f)
        lineTo(w * 0.78f, h * 0.85f)
        lineTo(w * 0.72f, h * 0.78f)
        lineTo(w * 0.28f, h * 0.78f)
        close()
    }
    drawPath(basePath, color = fill, style = Fill)
    drawPath(basePath, color = stroke, style = Stroke(strokeW))
}

private fun DrawScope.drawKnight(w: Float, h: Float, fill: Color, stroke: Color, isWhite: Boolean) {
    val strokeW = w * 0.045f
    val path = Path().apply {
        moveTo(w * 0.28f, h * 0.85f)
        lineTo(w * 0.74f, h * 0.85f)
        lineTo(w * 0.70f, h * 0.75f)
        // Back of head/mane
        cubicTo(w * 0.72f, h * 0.55f, w * 0.65f, h * 0.30f, w * 0.52f, h * 0.20f)
        // Ears
        lineTo(w * 0.45f, h * 0.26f)
        // Snout
        lineTo(w * 0.30f, h * 0.38f)
        lineTo(w * 0.33f, h * 0.48f)
        // Jaw/throat
        lineTo(w * 0.44f, h * 0.50f)
        // Chest
        cubicTo(w * 0.42f, h * 0.65f, w * 0.35f, h * 0.75f, w * 0.28f, h * 0.85f)
        close()
    }
    drawPath(path, color = fill, style = Fill)
    drawPath(path, color = stroke, style = Stroke(strokeW))

    // Eye
    val eyeColor = if (isWhite) stroke else fill
    drawCircle(color = eyeColor, radius = w * 0.035f, center = Offset(w * 0.44f, h * 0.35f))
}

private fun DrawScope.drawBishop(w: Float, h: Float, fill: Color, stroke: Color) {
    val strokeW = w * 0.045f

    // Top cross/orb
    drawCircle(color = fill, radius = w * 0.05f, center = Offset(w * 0.5f, h * 0.18f), style = Fill)
    drawCircle(color = stroke, radius = w * 0.05f, center = Offset(w * 0.5f, h * 0.18f), style = Stroke(strokeW))

    // Mitre body
    val mitre = Path().apply {
        moveTo(w * 0.5f, h * 0.23f)
        cubicTo(w * 0.72f, h * 0.35f, w * 0.70f, h * 0.60f, w * 0.62f, h * 0.75f)
        lineTo(w * 0.38f, h * 0.75f)
        cubicTo(w * 0.30f, h * 0.60f, w * 0.28f, h * 0.35f, w * 0.5f, h * 0.23f)
        close()
    }
    drawPath(mitre, color = fill, style = Fill)
    drawPath(mitre, color = stroke, style = Stroke(strokeW))

    // Slit
    drawLine(
        color = stroke,
        start = Offset(w * 0.52f, h * 0.34f),
        end = Offset(w * 0.62f, h * 0.44f),
        strokeWidth = strokeW,
        cap = StrokeCap.Round
    )

    // Base
    val basePath = Path().apply {
        moveTo(w * 0.24f, h * 0.85f)
        lineTo(w * 0.76f, h * 0.85f)
        lineTo(w * 0.68f, h * 0.75f)
        lineTo(w * 0.32f, h * 0.75f)
        close()
    }
    drawPath(basePath, color = fill, style = Fill)
    drawPath(basePath, color = stroke, style = Stroke(strokeW))
}

private fun DrawScope.drawRook(w: Float, h: Float, fill: Color, stroke: Color) {
    val strokeW = w * 0.045f

    // Battlements
    val castlePath = Path().apply {
        moveTo(w * 0.28f, h * 0.24f)
        lineTo(w * 0.38f, h * 0.24f)
        lineTo(w * 0.38f, h * 0.32f)
        lineTo(w * 0.44f, h * 0.32f)
        lineTo(w * 0.44f, h * 0.24f)
        lineTo(w * 0.56f, h * 0.24f)
        lineTo(w * 0.56f, h * 0.32f)
        lineTo(w * 0.62f, h * 0.32f)
        lineTo(w * 0.62f, h * 0.24f)
        lineTo(w * 0.72f, h * 0.24f)
        lineTo(w * 0.70f, h * 0.40f)
        lineTo(w * 0.30f, h * 0.40f)
        close()
    }
    drawPath(castlePath, color = fill, style = Fill)
    drawPath(castlePath, color = stroke, style = Stroke(strokeW))

    // Tower waist
    val towerPath = Path().apply {
        moveTo(w * 0.34f, h * 0.40f)
        lineTo(w * 0.66f, h * 0.40f)
        lineTo(w * 0.68f, h * 0.76f)
        lineTo(w * 0.32f, h * 0.76f)
        close()
    }
    drawPath(towerPath, color = fill, style = Fill)
    drawPath(towerPath, color = stroke, style = Stroke(strokeW))

    // Base
    val basePath = Path().apply {
        moveTo(w * 0.22f, h * 0.86f)
        lineTo(w * 0.78f, h * 0.86f)
        lineTo(w * 0.72f, h * 0.76f)
        lineTo(w * 0.28f, h * 0.76f)
        close()
    }
    drawPath(basePath, color = fill, style = Fill)
    drawPath(basePath, color = stroke, style = Stroke(strokeW))
}

private fun DrawScope.drawQueen(w: Float, h: Float, fill: Color, stroke: Color) {
    val strokeW = w * 0.045f

    // Crown orbs
    val orbs = listOf(
        Offset(w * 0.22f, h * 0.24f),
        Offset(w * 0.36f, h * 0.18f),
        Offset(w * 0.50f, h * 0.15f),
        Offset(w * 0.64f, h * 0.18f),
        Offset(w * 0.78f, h * 0.24f)
    )
    for (orb in orbs) {
        drawCircle(color = fill, radius = w * 0.04f, center = orb, style = Fill)
        drawCircle(color = stroke, radius = w * 0.04f, center = orb, style = Stroke(strokeW))
    }

    // Crown Body
    val crown = Path().apply {
        moveTo(w * 0.24f, h * 0.27f)
        lineTo(w * 0.32f, h * 0.42f)
        lineTo(w * 0.36f, h * 0.22f)
        lineTo(w * 0.44f, h * 0.42f)
        lineTo(w * 0.50f, h * 0.19f)
        lineTo(w * 0.56f, h * 0.42f)
        lineTo(w * 0.64f, h * 0.22f)
        lineTo(w * 0.68f, h * 0.42f)
        lineTo(w * 0.76f, h * 0.27f)
        lineTo(w * 0.68f, h * 0.75f)
        lineTo(w * 0.32f, h * 0.75f)
        close()
    }
    drawPath(crown, color = fill, style = Fill)
    drawPath(crown, color = stroke, style = Stroke(strokeW))

    // Base
    val basePath = Path().apply {
        moveTo(w * 0.22f, h * 0.86f)
        lineTo(w * 0.78f, h * 0.86f)
        lineTo(w * 0.70f, h * 0.75f)
        lineTo(w * 0.30f, h * 0.75f)
        close()
    }
    drawPath(basePath, color = fill, style = Fill)
    drawPath(basePath, color = stroke, style = Stroke(strokeW))
}

private fun DrawScope.drawKing(w: Float, h: Float, fill: Color, stroke: Color) {
    val strokeW = w * 0.045f

    // Cross
    drawLine(
        color = stroke,
        start = Offset(w * 0.5f, h * 0.12f),
        end = Offset(w * 0.5f, h * 0.26f),
        strokeWidth = strokeW * 1.2f,
        cap = StrokeCap.Round
    )
    drawLine(
        color = stroke,
        start = Offset(w * 0.42f, h * 0.18f),
        end = Offset(w * 0.58f, h * 0.18f),
        strokeWidth = strokeW * 1.2f,
        cap = StrokeCap.Round
    )

    // Crown
    val crown = Path().apply {
        moveTo(w * 0.30f, h * 0.27f)
        cubicTo(w * 0.40f, h * 0.24f, w * 0.60f, h * 0.24f, w * 0.70f, h * 0.27f)
        cubicTo(w * 0.75f, h * 0.42f, w * 0.72f, h * 0.65f, w * 0.65f, h * 0.75f)
        lineTo(w * 0.35f, h * 0.75f)
        cubicTo(w * 0.28f, h * 0.65f, w * 0.25f, h * 0.42f, w * 0.30f, h * 0.27f)
        close()
    }
    drawPath(crown, color = fill, style = Fill)
    drawPath(crown, color = stroke, style = Stroke(strokeW))

    // Base
    val basePath = Path().apply {
        moveTo(w * 0.20f, h * 0.86f)
        lineTo(w * 0.80f, h * 0.86f)
        lineTo(w * 0.72f, h * 0.75f)
        lineTo(w * 0.28f, h * 0.75f)
        close()
    }
    drawPath(basePath, color = fill, style = Fill)
    drawPath(basePath, color = stroke, style = Stroke(strokeW))
}
