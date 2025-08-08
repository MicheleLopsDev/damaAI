package io.github.luposolitario.damaai.screen

import android.app.Activity
import android.graphics.Bitmap
import android.os.Build
import android.os.Handler
import android.os.Looper
import android.view.PixelCopy
import android.view.View
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.gestures.detectTapGestures
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Rect
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.graphics.drawscope.clipPath
import androidx.compose.ui.graphics.drawscope.translate
import androidx.compose.ui.graphics.painter.Painter
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.res.painterResource
import io.github.luposolitario.damaai.data.BoardStyle
import io.github.luposolitario.damaai.data.GameState
import io.github.luposolitario.damaai.data.PlayerColor
import io.github.luposolitario.damaai.data.TeamStyle
import io.github.luposolitario.damaai.game_logic.Posizione
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.tween
import androidx.compose.runtime.getValue

@Composable
fun GameBoardArea(
    gameState: GameState,
    playerTeamStyle: TeamStyle,
    boardStyle: BoardStyle,
    selectedSquare: Posizione?,
    validMoveSquares: List<Posizione>,
    onSquareClick: (row: Int, col: Int) -> Unit,
    modifier: Modifier = Modifier
) {
    val playerPainter: Painter? = if (playerTeamStyle.id != "default") {
        painterResource(id = playerTeamStyle.flagResId)
    } else {
        null
    }

    // --- NUOVA LOGICA DI ANIMAZIONE ---
    val capturedPieceAlpha by animateFloatAsState(
        targetValue = if (gameState.capturedPiece != null) 0f else 1f,
        animationSpec = tween(durationMillis = 800), // Durata dell'animazione
        label = "CapturedPieceAlpha"
    )

    Canvas(
        modifier = modifier.pointerInput(Unit) {
            detectTapGestures { offset ->
                val squareSize = size.width / 8f
                val row = (offset.y / squareSize).toInt().coerceIn(0, 7)
                val col = (offset.x / squareSize).toInt().coerceIn(0, 7)
                onSquareClick(row, col)
            }
        }
    ) {
        val squareSize = size.width / 8f
        for (row in 0 until 8) {
            for (col in 0 until 8) {
                val isLightSquare = (row + col) % 2 == 0
                val squareColor =
                    if (isLightSquare) boardStyle.lightSquareColor else boardStyle.darkSquareColor
                drawRect(
                    color = squareColor,
                    topLeft = Offset(x = col * squareSize, y = row * squareSize),
                    size = Size(width = squareSize, height = squareSize)
                )

                if (selectedSquare != null && selectedSquare.riga == row && selectedSquare.colonna == col) {
                    drawRect(
                        color = Color.Yellow.copy(alpha = 0.5f),
                        topLeft = Offset(x = col * squareSize, y = row * squareSize),
                        size = Size(width = squareSize, height = squareSize)
                    )
                }
            }
        }

        validMoveSquares.forEach { pos ->
            drawCircle(
                color = Color.DarkGray.copy(alpha = 0.4f),
                radius = squareSize * 0.15f,
                center = Offset(
                    x = pos.colonna * squareSize + squareSize / 2,
                    y = pos.riga * squareSize + squareSize / 2
                )
            )
        }

        // --- DISEGNO DELLE PEDINE MODIFICATO ---
        val piecesToDraw = if (gameState.capturedPiece != null && capturedPieceAlpha > 0f) {
            // Se c'è una pedina da animare, la aggiungiamo alla lista dei pezzi normali
            gameState.pieces + gameState.capturedPiece!!
        } else {
            gameState.pieces
        }


        piecesToDraw.forEach { piece ->
            val isCapturedPiece = piece == gameState.capturedPiece
            val alpha = if(isCapturedPiece) capturedPieceAlpha else 1f

            val center = Offset(
                x = piece.col * squareSize + squareSize / 2,
                y = piece.row * squareSize + squareSize / 2
            )
            val pieceRadius = squareSize * 0.38f


            drawCircle(
                color = Color.Black.copy(alpha = 0.3f * alpha),
                radius = pieceRadius,
                center = center.copy(y = center.y + 4f)
            )

            if (piece.color == PlayerColor.WHITE) {
                if (playerPainter != null) {
                    drawCircle(color = Color.White.copy(alpha = alpha), radius = pieceRadius, center = center)
                    val clipPath =
                        Path().apply { addOval(Rect(center = center, radius = pieceRadius)) }
                    clipPath(path = clipPath) {
                        translate(left = center.x - pieceRadius, top = center.y - pieceRadius) {
                            with(playerPainter) {
                                draw(
                                    size = Size(
                                        pieceRadius * 2,
                                        pieceRadius * 2
                                    ),
                                    alpha = alpha
                                )
                            }
                        }
                    }
                    drawCircle(
                        color = Color(0xFFBBBBBB).copy(alpha = alpha),
                        radius = pieceRadius,
                        center = center,
                        style = Stroke(width = squareSize * 0.04f)
                    )
                } else {
                    drawCircle(color = Color.White.copy(alpha = alpha), radius = pieceRadius, center = center)
                    drawCircle(
                        color = Color(0xFFBBBBBB).copy(alpha = alpha),
                        radius = pieceRadius,
                        center = center,
                        style = Stroke(width = squareSize * 0.04f)
                    )
                }
            } else {
                drawCircle(color = Color(0xFF222222).copy(alpha = alpha), radius = pieceRadius, center = center)
                drawCircle(
                    color = Color.Black.copy(alpha = alpha),
                    radius = pieceRadius,
                    center = center,
                    style = Stroke(width = squareSize * 0.04f)
                )
            }
        }
    }
}