import javafx.application.Application
import javafx.fxml.FXML
import javafx.fxml.FXMLLoader
import javafx.scene.Parent
import javafx.scene.Scene
import javafx.scene.canvas.Canvas
import javafx.scene.control.Alert
import javafx.scene.control.Button
import javafx.scene.control.Label
import javafx.scene.paint.Color
import javafx.stage.Stage

class ChessBoardUIC(val buttonListOfPieces: ArrayList<Button> = ArrayList()) : Application() {

    @FXML
    lateinit var canvas: javafx.scene.canvas.Canvas
    @FXML
    lateinit var piecesCanvas: Canvas
    @FXML
    lateinit var agencyStateLabel: Label
    @FXML
    lateinit var startBt: Button

    val chessBoardWidth: Double = 548.0
    val chessBoardHeight: Double = chessBoardWidth
    val chessBoardGridGap: Double = chessBoardWidth / 11

    val chessBoardGridHorizonPoint = arrayListOf<Double>()

    var board: Board = Board()

    var isGameOver = false

    override fun start(primaryStage: Stage?) {
        val parentScene = FXMLLoader.load<Any>(javaClass.getResource("uicm.fxml")) as Parent

        configFXMLVar(parentScene)
        initConfig(primaryStage!!, parentScene)

        primaryStage.show()
    }

    private fun initConfig(primaryStage: Stage, parent: Parent) {
        primaryStage.scene = Scene(parent, 800.0, 600.0)
        primaryStage.title = "五子棋AI对战"
        primaryStage.isResizable = false

        canvas.width = primaryStage.scene.height
        canvas.height = primaryStage.scene.height + 12

        chessBoardGridDraw()

        piecesCanvas.width = canvas.width
        piecesCanvas.height = canvas.height
        clearPieces()

        startBt.setOnMouseClicked { reStart() }
    }

    private fun chessBoardGridDraw() {
        val graphicsContext = canvas.graphicsContext2D

        graphicsContext.fill = Color.AQUA
        graphicsContext.fillRect(0.0, 0.0, canvas.width, canvas.height)

        graphicsContext.stroke = Color.ORANGE
        graphicsContext.lineWidth = 3.0
        graphicsContext.strokeRect(30.0, 30.0, chessBoardWidth, chessBoardHeight)

        chessBoardGridHorizonPoint.add(30.0)
        for (i in 1..10) {
            graphicsContext.strokeLine(
                30 + i * chessBoardGridGap,
                30.0,
                30 + i * chessBoardGridGap,
                chessBoardHeight + 30
            )
            chessBoardGridHorizonPoint.add(30 + i * chessBoardGridGap)
            graphicsContext.strokeLine(
                30.0,
                30 + i * chessBoardGridGap,
                chessBoardWidth + 30,
                30 + i * chessBoardGridGap
            )
        }
        chessBoardGridHorizonPoint.add(30 + 11 * chessBoardGridGap)

        val stringDraw = arrayListOf("1", "2", "3", "4", "5", "6", "7", "8", "9", "10", "11", "12")
        for ((index, value) in stringDraw.withIndex()) {
            graphicsContext.stroke = Color.BLACK
            graphicsContext.lineWidth = 1.2
            graphicsContext.strokeText(value, 30 + index * chessBoardGridGap - 7, chessBoardHeight + 30 + 20)
            graphicsContext.strokeText(value, 8.0, 30 + chessBoardGridGap * index + 6)
        }

        piecesCanvas.setOnMouseClicked {
            if (isGameOver) {
                return@setOnMouseClicked
            }

            val row = clickedPosition(it.y)
            val column = clickedPosition(it.x)

            if (row == 0 || column == 0) return@setOnMouseClicked

            if (board.toPointPiece(row, column, board.nextAgency)) {
                refreshPiecesPoint()
                stateChange()
                aroundAction()
                if (isGameOver == false && board.nextAgency == Pieces.WHITE) {
                    val p = board.getOneStep()
                    board.toPointPiece(p.row, p.column, board.nextAgency)
                    refreshPiecesPoint()
                    stateChange()
                    aroundAction()
                }
            } else {
//                TODO:do nothing if the action was failed
            }
        }

    }

    fun aroundAction() {
        isGameOver = board.isOver()
        if (isGameOver){
            startBt.text = "reStart"
            Alert(Alert.AlertType.INFORMATION, "Game is Over.The winner is ${board.firstPlayer}").show()
        }
    }

    private fun stateChange() {
        if (board.nextAgency == Pieces.WHITE) {
            agencyStateLabel.text = "NEXT->WHITE"
        } else {
            agencyStateLabel.text = "NEXT->BLACK"
        }
    }

    private fun clickedPosition(p: Double): Int {
        if (p < chessBoardGridHorizonPoint[0] - 15 || p > chessBoardGridHorizonPoint[11] + 15) {
            return 0
        }
        var i = 1
        for ((k, value) in chessBoardGridHorizonPoint.withIndex()) {
            if (p > value + 15) {
                i = k + 2
            } else {
                break
            }
        }
        return i
    }

    fun refreshPiecesPoint() {

        val gridBoard = board.gridBoard
        val graphicsContext = piecesCanvas.graphicsContext2D

        for ((index, value) in gridBoard.withIndex()) {
            val x = 30.0 + (index % 12) * chessBoardGridGap - 15
            val y = 30.0 + (index / 12) * chessBoardGridGap - 15
            when (value) {
                Pieces.BLACK -> {
                    graphicsContext.fill = Color.BLACK
                }
                Pieces.WHITE -> {
                    graphicsContext.fill = Color.WHITE
                }
                else -> {
                    graphicsContext.fill = Color.TRANSPARENT
                }
            }
            graphicsContext.fillOval(x, y, 30.0, 30.0)
        }
    }

    fun reStart() {
        isGameOver = false
        clearPieces()
    }

    private fun clearPieces() {
        board = Board()
        val graphicsContext = piecesCanvas.graphicsContext2D
        graphicsContext.clearRect(0.0, 0.0, piecesCanvas.width, piecesCanvas.height)
    }

    private fun configFXMLVar(parent: Parent) {
        canvas = parent.lookup("#canvas") as Canvas
        piecesCanvas = parent.lookup("#piecesCanvas") as Canvas
        agencyStateLabel = parent.lookup("#angecyState") as Label
        startBt = parent.lookup("#startBt") as Button

    }

    fun toLaunch() {
        Application.launch()
    }

}

fun main(args: Array<String>) {
    ChessBoardUIC().toLaunch()

}