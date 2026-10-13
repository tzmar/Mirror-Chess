package com.example.ui

import android.app.Application
import android.net.Uri
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.example.bot.BotEngine
import com.example.data.AppDatabase
import com.example.data.EngineProfileEntity
import com.example.data.GameRecordEntity
import com.example.data.PlayerMoveEntity
import com.example.domain.ChessMoveRecord
import com.example.domain.ChessRepository
import com.example.domain.GameResult
import com.example.domain.PlayerModelRepository
import com.example.engine.EngineManager
import com.example.network.ConnectivityObserver
import com.github.bhlangonijr.chesslib.Board
import com.github.bhlangonijr.chesslib.Piece
import com.github.bhlangonijr.chesslib.PieceType
import com.github.bhlangonijr.chesslib.Side
import com.github.bhlangonijr.chesslib.Square
import com.github.bhlangonijr.chesslib.move.Move
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import java.util.UUID

enum class AppScreen {
    GAME,
    STATS,
    OPENINGS,
    ENGINES,
    SETTINGS
}

data class GameUiState(
    val board: Board = Board(),
    val playerColor: Side = Side.WHITE,
    val botColor: Side = Side.BLACK,
    val gameIndex: Int = 1,
    val selectedSquare: Square? = null,
    val legalMovesFromSelected: List<Move> = emptyList(),
    val lastMove: Move? = null,
    val isBotThinking: Boolean = false,
    val botExplanation: String? = null,
    val isLastMoveMirrorMatch: Boolean = false,
    val gameResult: GameResult = GameResult.IN_PROGRESS,
    val statusMessage: String = "Your turn",
    val pendingPromotionMove: Move? = null,
    val moveHistory: List<ChessMoveRecord> = emptyList(),
    val capturedByWhite: List<PieceType> = emptyList(),
    val capturedByBlack: List<PieceType> = emptyList(),
    val matchedMirrorCount: Int = 0
)

class MainViewModel(application: Application) : AndroidViewModel(application) {

    private val db = AppDatabase.getDatabase(application)
    val connectivityObserver = ConnectivityObserver(application)
    val playerModelRepository = PlayerModelRepository(db.playerMoveDao(), db.gameRecordDao())
    val engineManager = EngineManager(
        context = application,
        engineProfileDao = db.engineProfileDao(),
        connectivityObserver = connectivityObserver,
        coroutineScope = viewModelScope
    )
    val chessRepository = ChessRepository()
    val botEngine = BotEngine(chessRepository, playerModelRepository, engineManager)

    // Current navigation tab
    private val _currentScreen = MutableStateFlow(AppScreen.GAME)
    val currentScreen: StateFlow<AppScreen> = _currentScreen.asStateFlow()

    // Game state
    private val _gameUiState = MutableStateFlow(GameUiState())
    val gameUiState: StateFlow<GameUiState> = _gameUiState.asStateFlow()

    // Settings
    private val _botElo = MutableStateFlow(400)
    val botElo: StateFlow<Int> = _botElo.asStateFlow()

    private val _soundEnabled = MutableStateFlow(true)
    val soundEnabled: StateFlow<Int> = MutableStateFlow(1) // 1 = on

    // Engine Test Dialog Result
    private val _uciTestOutput = MutableStateFlow<List<String>?>(null)
    val uciTestOutput: StateFlow<List<String>?> = _uciTestOutput.asStateFlow()

    // Persisted flows
    val allGames = playerModelRepository.allGames.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())
    val gamesCount = playerModelRepository.gamesCount.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), 0)
    val playerWins = playerModelRepository.playerWins.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), 0)
    val botWins = playerModelRepository.botWins.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), 0)
    val draws = playerModelRepository.draws.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), 0)
    val mirrorAccuracy = playerModelRepository.mirrorAccuracy.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), 0)
    val openingStats = playerModelRepository.openingStats.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())
    val piecePreferences = playerModelRepository.piecePreferences.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())
    val totalMovesCount = playerModelRepository.totalPlayerMovesCount.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), 0)

    val engineProfiles = engineManager.engineProfiles.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())
    val activeEngineId = engineManager.activeEngineId
    val downloadProgressMap = engineManager.downloadProgressMap
    val isConnected = connectivityObserver.isConnected

    // Transient recorded moves for current game session to commit upon finish
    private val playerMovesThisGame = mutableListOf<PlayerMoveEntity>()
    private var currentGameId: String = UUID.randomUUID().toString()

    init {
        // Initialize game index and colors from historical data
        viewModelScope.launch {
            val lastGame = playerModelRepository.getLastGame()
            if (lastGame == null) {
                // Game 1: Player plays White against baseline 400 Elo bot
                initNewGame(gameIndex = 1, playerColor = Side.WHITE)
            } else {
                // Game 2 onward: Bot takes the color the player played in previous game, player takes other
                val nextIndex = lastGame.gameIndex + 1
                val lastPlayerColor = if (lastGame.playerColor == "WHITE") Side.WHITE else Side.BLACK
                val nextPlayerColor = if (lastPlayerColor == Side.WHITE) Side.BLACK else Side.WHITE
                initNewGame(gameIndex = nextIndex, playerColor = nextPlayerColor)
            }
        }
    }

    fun navigateTo(screen: AppScreen) {
        _currentScreen.value = screen
    }

    fun setElo(elo: Int) {
        _botElo.value = elo.coerceIn(400, 2600)
    }

    private fun initNewGame(gameIndex: Int, playerColor: Side) {
        currentGameId = UUID.randomUUID().toString()
        playerMovesThisGame.clear()
        chessRepository.resetBoard()

        val botColor = if (playerColor == Side.WHITE) Side.BLACK else Side.WHITE
        val isPlayerTurn = chessRepository.sideToMove == playerColor

        _gameUiState.value = GameUiState(
            board = chessRepository.currentBoard,
            playerColor = playerColor,
            botColor = botColor,
            gameIndex = gameIndex,
            selectedSquare = null,
            legalMovesFromSelected = emptyList(),
            lastMove = null,
            isBotThinking = false,
            botExplanation = if (gameIndex == 1) "Game 1: Baseline Bot (400 Elo)" else "Mirror Bot initialized with your play style",
            isLastMoveMirrorMatch = false,
            gameResult = GameResult.IN_PROGRESS,
            statusMessage = if (isPlayerTurn) "Your turn (${playerColor.name.lowercase().replaceFirstChar { it.uppercase() }})" else "Mirror Bot thinking...",
            pendingPromotionMove = null,
            moveHistory = emptyList(),
            capturedByWhite = emptyList(),
            capturedByBlack = emptyList(),
            matchedMirrorCount = 0
        )

        // If bot plays first (player is Black), launch bot move
        if (!isPlayerTurn) {
            triggerBotMove()
        }
    }

    fun onSquareClicked(square: Square) {
        val currentState = _gameUiState.value
        if (currentState.gameResult != GameResult.IN_PROGRESS || currentState.isBotThinking) return
        if (chessRepository.sideToMove != currentState.playerColor) return

        val piece = chessRepository.getPiece(square)

        // If square already selected, or selecting own piece
        if (currentState.selectedSquare == null) {
            if (piece != Piece.NONE && piece.pieceSide == currentState.playerColor) {
                val legalMoves = chessRepository.getLegalMovesForSquare(square)
                _gameUiState.value = currentState.copy(
                    selectedSquare = square,
                    legalMovesFromSelected = legalMoves
                )
            }
        } else {
            // Check if tapped square is a legal destination
            val move = currentState.legalMovesFromSelected.firstOrNull { it.to == square }
            if (move != null) {
                // Check if this move requires pawn promotion
                val movingPiece = chessRepository.getPiece(currentState.selectedSquare)
                val isPawnPromotion = movingPiece.pieceType == PieceType.PAWN &&
                        (square.rank.ordinal == 7 || square.rank.ordinal == 0)

                if (isPawnPromotion) {
                    _gameUiState.value = currentState.copy(
                        pendingPromotionMove = move,
                        selectedSquare = null,
                        legalMovesFromSelected = emptyList()
                    )
                } else {
                    executePlayerMove(move)
                }
            } else if (piece != Piece.NONE && piece.pieceSide == currentState.playerColor) {
                // Switch selected piece
                val legalMoves = chessRepository.getLegalMovesForSquare(square)
                _gameUiState.value = currentState.copy(
                    selectedSquare = square,
                    legalMovesFromSelected = legalMoves
                )
            } else {
                // Deselect
                _gameUiState.value = currentState.copy(
                    selectedSquare = null,
                    legalMovesFromSelected = emptyList()
                )
            }
        }
    }

    fun onPieceDragged(fromSquare: Square, toSquare: Square) {
        val currentState = _gameUiState.value
        if (currentState.gameResult != GameResult.IN_PROGRESS || currentState.isBotThinking) return
        if (chessRepository.sideToMove != currentState.playerColor) return

        if (fromSquare == toSquare) {
            onSquareClicked(fromSquare)
            return
        }

        val movingPiece = chessRepository.getPiece(fromSquare)
        if (movingPiece == Piece.NONE || movingPiece.pieceSide != currentState.playerColor) return

        val legalMoves = chessRepository.getLegalMovesForSquare(fromSquare)
        val move = legalMoves.firstOrNull { it.to == toSquare }

        if (move != null) {
            val isPawnPromotion = movingPiece.pieceType == PieceType.PAWN &&
                    (toSquare.rank.ordinal == 7 || toSquare.rank.ordinal == 0)

            if (isPawnPromotion) {
                _gameUiState.value = currentState.copy(
                    pendingPromotionMove = move,
                    selectedSquare = null,
                    legalMovesFromSelected = emptyList()
                )
            } else {
                executePlayerMove(move)
            }
        } else {
            val targetPiece = chessRepository.getPiece(toSquare)
            if (targetPiece != Piece.NONE && targetPiece.pieceSide == currentState.playerColor) {
                _gameUiState.value = currentState.copy(
                    selectedSquare = toSquare,
                    legalMovesFromSelected = chessRepository.getLegalMovesForSquare(toSquare)
                )
            } else {
                _gameUiState.value = currentState.copy(
                    selectedSquare = fromSquare,
                    legalMovesFromSelected = legalMoves
                )
            }
        }
    }

    fun onPromotionChosen(pieceType: PieceType) {
        val pending = _gameUiState.value.pendingPromotionMove ?: return
        val side = _gameUiState.value.playerColor
        val promoPiece = Piece.make(side, pieceType)
        val fullMove = Move(pending.from, pending.to, promoPiece)

        _gameUiState.value = _gameUiState.value.copy(pendingPromotionMove = null)
        executePlayerMove(fullMove)
    }

    private fun executePlayerMove(move: Move) {
        val currentState = _gameUiState.value
        val fenBefore = chessRepository.normalizeFen(chessRepository.currentFen)
        val piece = chessRepository.getPiece(move.from)
        val capturedTarget = chessRepository.getPiece(move.to)

        val record = chessRepository.makeMove(move, isMirrorMatch = false) ?: return

        // Save move for player database
        playerMovesThisGame.add(
            PlayerMoveEntity(
                gameId = currentGameId,
                moveNumber = (chessRepository.getHistory().size + 1) / 2,
                color = currentState.playerColor.name,
                fenBefore = fenBefore,
                moveUci = record.moveUci,
                san = record.san,
                pieceType = record.pieceType.name,
                isCapture = record.isCapture,
                isCheck = record.isCheck
            )
        )

        // Update captured pieces
        val (capWhite, capBlack) = updateCapturedPieces()

        val status = chessRepository.checkGameStatus()
        if (status != GameResult.IN_PROGRESS) {
            handleGameOver(status)
            _gameUiState.value = _gameUiState.value.copy(
                board = chessRepository.currentBoard,
                lastMove = move,
                selectedSquare = null,
                legalMovesFromSelected = emptyList(),
                gameResult = status,
                statusMessage = formatStatusMessage(status),
                moveHistory = chessRepository.getHistory(),
                capturedByWhite = capWhite,
                capturedByBlack = capBlack
            )
        } else {
            _gameUiState.value = _gameUiState.value.copy(
                board = chessRepository.currentBoard,
                lastMove = move,
                selectedSquare = null,
                legalMovesFromSelected = emptyList(),
                isBotThinking = true,
                statusMessage = "Mirror Bot thinking...",
                moveHistory = chessRepository.getHistory(),
                capturedByWhite = capWhite,
                capturedByBlack = capBlack
            )
            triggerBotMove()
        }
    }

    private fun triggerBotMove() {
        viewModelScope.launch(Dispatchers.Default) {
            delay(500) // Realistic thoughtful pause

            val currentState = _gameUiState.value
            val isFirstGame = currentState.gameIndex == 1
            val botColor = currentState.botColor

            val decision = botEngine.getBotMove(
                isFirstGame = isFirstGame,
                botColor = botColor,
                currentElo = _botElo.value
            )

            if (decision != null) {
                val record = chessRepository.makeMove(decision.move, isMirrorMatch = decision.isMirrorMatch)
                val status = chessRepository.checkGameStatus()
                val (capWhite, capBlack) = updateCapturedPieces()

                val newMatchedCount = currentState.matchedMirrorCount + if (decision.isMirrorMatch) 1 else 0

                if (status != GameResult.IN_PROGRESS) {
                    handleGameOver(status)
                    _gameUiState.value = currentState.copy(
                        board = chessRepository.currentBoard,
                        lastMove = decision.move,
                        isBotThinking = false,
                        botExplanation = decision.explanation,
                        isLastMoveMirrorMatch = decision.isMirrorMatch,
                        gameResult = status,
                        statusMessage = formatStatusMessage(status),
                        moveHistory = chessRepository.getHistory(),
                        capturedByWhite = capWhite,
                        capturedByBlack = capBlack,
                        matchedMirrorCount = newMatchedCount
                    )
                } else {
                    _gameUiState.value = currentState.copy(
                        board = chessRepository.currentBoard,
                        lastMove = decision.move,
                        isBotThinking = false,
                        botExplanation = decision.explanation,
                        isLastMoveMirrorMatch = decision.isMirrorMatch,
                        statusMessage = if (chessRepository.isKingInCheck()) "Check! Your turn" else "Your turn",
                        moveHistory = chessRepository.getHistory(),
                        capturedByWhite = capWhite,
                        capturedByBlack = capBlack,
                        matchedMirrorCount = newMatchedCount
                    )
                }
            }
        }
    }

    fun resignGame() {
        val currentState = _gameUiState.value
        if (currentState.gameResult != GameResult.IN_PROGRESS) return

        val status = if (currentState.playerColor == Side.WHITE) {
            GameResult.RESIGNATION_WHITE
        } else {
            GameResult.RESIGNATION_BLACK
        }
        handleGameOver(status)
        _gameUiState.value = currentState.copy(
            gameResult = status,
            statusMessage = "You resigned"
        )
    }

    fun startNewGame() {
        val currentState = _gameUiState.value
        val nextIndex = currentState.gameIndex + 1
        // Game 2 onward: Bot takes the color the player played, player takes the other
        val nextPlayerColor = if (currentState.playerColor == Side.WHITE) Side.BLACK else Side.WHITE
        initNewGame(gameIndex = nextIndex, playerColor = nextPlayerColor)
    }

    private fun handleGameOver(result: GameResult) {
        viewModelScope.launch {
            // Save all player moves to Room
            playerModelRepository.savePlayerMoves(playerMovesThisGame)

            val currentState = _gameUiState.value
            val history = chessRepository.getHistory()
            val openingName = chessRepository.identifyOpening(history.map { it.moveUci })

            val gameOutcome = when (result) {
                GameResult.WHITE_WON_CHECKMATE -> if (currentState.playerColor == Side.WHITE) "PLAYER_WON" else "BOT_WON"
                GameResult.BLACK_WON_CHECKMATE -> if (currentState.playerColor == Side.BLACK) "PLAYER_WON" else "BOT_WON"
                GameResult.RESIGNATION_WHITE -> if (currentState.playerColor == Side.WHITE) "BOT_WON" else "PLAYER_WON"
                GameResult.RESIGNATION_BLACK -> if (currentState.playerColor == Side.BLACK) "BOT_WON" else "PLAYER_WON"
                else -> "DRAW"
            }

            val gameRecord = GameRecordEntity(
                id = currentGameId,
                gameIndex = currentState.gameIndex,
                playerColor = currentState.playerColor.name,
                botColor = currentState.botColor.name,
                result = gameOutcome,
                termination = result.name,
                movesCount = history.size,
                matchedMirrorMovesCount = currentState.matchedMirrorCount,
                openingName = openingName
            )
            playerModelRepository.saveGameRecord(gameRecord)
        }
    }

    private fun updateCapturedPieces(): Pair<List<PieceType>, List<PieceType>> {
        val startingPieces = mapOf(
            PieceType.PAWN to 8,
            PieceType.KNIGHT to 2,
            PieceType.BISHOP to 2,
            PieceType.ROOK to 2,
            PieceType.QUEEN to 1
        )
        val currentWhite = mutableMapOf<PieceType, Int>()
        val currentBlack = mutableMapOf<PieceType, Int>()

        for (square in Square.values()) {
            if (square == Square.NONE) continue
            val piece = chessRepository.getPiece(square)
            if (piece != Piece.NONE) {
                if (piece.pieceSide == Side.WHITE) {
                    currentWhite[piece.pieceType] = (currentWhite[piece.pieceType] ?: 0) + 1
                } else {
                    currentBlack[piece.pieceType] = (currentBlack[piece.pieceType] ?: 0) + 1
                }
            }
        }

        val capturedByWhite = mutableListOf<PieceType>()
        val capturedByBlack = mutableListOf<PieceType>()

        startingPieces.forEach { (type, count) ->
            val blackRemaining = currentBlack[type] ?: 0
            val whiteRemaining = currentWhite[type] ?: 0
            repeat(maxOf(0, count - blackRemaining)) { capturedByWhite.add(type) }
            repeat(maxOf(0, count - whiteRemaining)) { capturedByBlack.add(type) }
        }

        return Pair(capturedByWhite, capturedByBlack)
    }

    private fun formatStatusMessage(result: GameResult): String {
        return when (result) {
            GameResult.WHITE_WON_CHECKMATE -> "White wins by Checkmate!"
            GameResult.BLACK_WON_CHECKMATE -> "Black wins by Checkmate!"
            GameResult.DRAW_STALEMATE -> "Draw by Stalemate"
            GameResult.DRAW_INSUFFICIENT -> "Draw by Insufficient Material"
            GameResult.DRAW_FIFTY_MOVES -> "Draw by 50-Move Rule"
            GameResult.DRAW_REPETITION -> "Draw by Repetition"
            GameResult.RESIGNATION_WHITE -> "White resigned"
            GameResult.RESIGNATION_BLACK -> "Black resigned"
            GameResult.IN_PROGRESS -> "Game in progress"
        }
    }

    // Engine operations
    fun selectEngine(engineId: String) {
        viewModelScope.launch {
            engineManager.selectEngine(engineId)
        }
    }

    fun downloadEngine(profile: EngineProfileEntity) {
        engineManager.startDownload(profile)
    }

    fun cancelDownload(engineId: String) {
        engineManager.cancelDownload(engineId)
    }

    fun deleteEngine(engineId: String) {
        viewModelScope.launch {
            engineManager.deleteDownloadedEngine(engineId)
        }
    }

    fun importEngine(name: String, uri: Uri) {
        viewModelScope.launch {
            engineManager.importEngineFile(name, uri)
        }
    }

    fun testEngineUci(engineId: String) {
        viewModelScope.launch {
            val output = engineManager.testEngineUci(engineId)
            _uciTestOutput.value = output
        }
    }

    fun dismissTestDialog() {
        _uciTestOutput.value = null
    }

    fun resetPlayerStyleData() {
        viewModelScope.launch {
            playerModelRepository.resetAllStyleData()
            // Reset to Game 1
            initNewGame(gameIndex = 1, playerColor = Side.WHITE)
        }
    }
}
