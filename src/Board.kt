import sun.misc.GC
import kotlin.math.max
import kotlin.math.min

enum class Pieces {
    BLACK, WHITE, Void
}

class TypeChess {
    var horizon: String = ""
    var vertical: String = ""
    var leanFromLeftToRight: String = ""
    var leanFromRightToLeft: String = ""
}

class Position constructor(yRow: Int, xColumn: Int) {
    var row = yRow
    var column = xColumn

    var globalYield = 0
    var pointYield = 0
}

class Board {
    val gridBoard: Array<Pieces?> = arrayOfNulls<Pieces?>(144)

    var nextAgency = Pieces.BLACK

    var lastPointRow = 0
    var lastPointColumn = 0

    var firstPlayer = Pieces.BLACK

    var nextStep = Position(0, 0)

    init {
    }

    fun toPointPiece(row: Int, column: Int, pieces: Pieces): Boolean {
        val position = (row - 1) * 12 + (column - 1)
        if (pieces == Pieces.Void) {
            gridBoard[position] = Pieces.Void
            agencyChange()
            return true
        }
        if (checkPointPiece(row, column) == Pieces.Void) {
            if (lastPointRow == 0) firstPlayer = pieces
            lastPointRow = row
            lastPointColumn = column
            gridBoard[position] = pieces
            agencyChange()
            return true
        }
        return false
    }

    fun unmakePointPiece(p: Position) {
        toPointPiece(p.row, p.column, Pieces.Void)
    }

    fun checkPointPiece(row: Int, column: Int): Pieces {
        val position = (row - 1) * 12 + (column - 1)
        if (gridBoard[position] == null) return Pieces.Void else return gridBoard[position] as Pieces
    }

    private fun agencyChange() {
        if (nextAgency == Pieces.BLACK) {
            nextAgency = Pieces.WHITE
        } else {
            nextAgency = Pieces.BLACK
        }
    }

    private fun printProbablyPosition() {
        val a = probablyPosition()
        println("length:${a.size}")
        for ((i, value) in a.withIndex()) {
            print("P$i:{${value.row},${value.column}}")
            if (i > 0 && i % 5 == 0) println("")
        }
        println("")
    }

    fun probablyPosition(): ArrayList<Position> {
        val a = arrayListOf<Position>()
        val flagA = arrayOfNulls<Boolean>(144)
        val basisGridPositon =
            arrayOf(
                -26,
                -25,
                -24,
                -23,
                -22,
                -14,
                -13,
                -12,
                -11,
                -10,
                -2,
                -1,
                0,
                1,
                2,
                10,
                11,
                12,
                13,
                14,
                22,
                23,
                24,
                25,
                26
            )
        for (i in 0..143) {
            if (gridBoard[i] != null) {
                for ((j, value) in basisGridPositon.withIndex()) {
                    val p = i + value
                    if (0 <= p && p < 144) {
                        if ((gridBoard[p] == null || gridBoard[p] == Pieces.Void) && flagA[p] == null) {
                            if ((p / 12).toInt() == (i / 12).toInt() + j / 5 - 2) {
                                flagA[p] = true
                                val x = (p / 12).toInt() + 1
                                val y = (p % 12) + 1
                                val pn = Position(x, y)
                                a.add(pn)
                            }
                        }
                    }
                }
            }
        }

        return a
    }

    fun isOver(): Boolean {
        return predictYieldForPoint(lastPointRow, lastPointColumn) >= FiveLine
    }

    fun evaluator(): Int {
        var countAllBlack = 0
        var countAllWhite = 0

        for (i in 1..12)
            for (j in 1..12) {
                val p = checkPointPiece(i, j)
                if (p == Pieces.WHITE) {
                    countAllWhite += predictYieldForPoint(i, j)
                } else if (p == Pieces.BLACK) {
                    countAllBlack = predictYieldForPoint(i, j)
                }
            }
        return countAllWhite - countAllBlack
    }

    fun printChessGrid() {
        for (i in 1..12) {
            for (j in 1..12) {
                when (checkPointPiece(i, j)) {
                    Pieces.WHITE -> print("1")
                    Pieces.BLACK -> print("2")
                    Pieces.Void -> print("0")
                }
                print("-")
            }
            println("")
        }
    }

    fun getOneStep(): Position {
        if (lastPointRow == 0) {
            return Position(6, 6)
        } else {
            dicision2(maxSearchDeep, Int.MAX_VALUE, Int.MIN_VALUE)
            return nextStep
        }
    }

    fun dicision2(deep: Int, alpha: Int, beta: Int): Int {
        if (deep == 0) return alpha
        val nextSteps = probablyPosition()
        var tAlpha = alpha
        var tBeta = beta
        var maxminV = if (deep % 2 == 1) Int.MIN_VALUE else Int.MAX_VALUE
        var maxminI = 0
        for ((index, value) in nextSteps.withIndex()) {
            toPointPiece(value.row, value.column, nextAgency)
            val ev = evaluator()
            if (deep % 2 == 1) {//max
                if (ev < beta) {
                    unmakePointPiece(value)
                    return ev
                }
                if (ev < tAlpha) tAlpha = ev
            } else {//min
                if (ev > alpha) {
                    unmakePointPiece(value)
                    return ev
                }
                if (ev > tBeta) tBeta = ev
            }
            val dv = dicision2(deep - 1, tAlpha, tBeta)
            if (deep % 2 == 1) {
                if (dv > maxminV) {
                    maxminV = dv
                    maxminI = index
                }
            } else {
                if (dv < maxminV) {
                    maxminV = dv
                    maxminI = index
                }
            }
            unmakePointPiece(value)
        }
        nextStep = nextSteps[maxminI]
        return maxminV
    }

    fun dicision1(deep: Int, newPoint: Position?, alpha: Int, beta: Int): Int {

        if (newPoint != null) {
            toPointPiece(newPoint.row, newPoint.column, nextAgency)
        }
        val ev = evaluator()
        if (deep == 1) return ev

        val nextSteps = probablyPosition()
        nextSteps.sortBy { predictYieldForPoint(it.row, it.column) }
        var maxMinEl = if (deep % 2 == 1) Int.MIN_VALUE else Int.MAX_VALUE
        var maxMinIndex = 0

        for ((index, value) in nextSteps.withIndex()) {
            val evd = dicision1(deep - 1, value, alpha, beta)
            unmakePointPiece(value)
            if (deep % 2 == 1) {//max
                if (maxMinEl < evd) {
                    maxMinEl = evd
                    maxMinIndex = index
                }
            } else {//min
                if (maxMinEl > evd) {
                    maxMinEl = evd
                    maxMinIndex = index
                }
            }

        }
        nextStep = nextSteps[maxMinIndex]
        return maxMinEl
    }

    fun predictYieldForPoint(row: Int, column: Int): Int {
//        whatever type of this piece all have to check out four rag around it
        val p = checkPointPiece(row, column)
        if (p == Pieces.Void) return 0

        val t = typeText(row, column)
        return scoresByOrientation(t.horizon, p) +
                scoresByOrientation(t.vertical, p) +
                scoresByOrientation(t.leanFromLeftToRight, p) +
                scoresByOrientation(t.leanFromRightToLeft, p)
    }

    fun typeText(row: Int, column: Int): TypeChess {
        val t = TypeChess()

        var rangeVT = 0
        var rangeVB = 4
        if (row - 4 < 1) rangeVT = 1 else rangeVT = row - 4
        if (row + 4 > 12) rangeVB = 12 else rangeVB = row + 4
        for (i in rangeVT..rangeVB) {
            when (checkPointPiece(i, column)) {
                Pieces.Void -> t.vertical += "0"
                Pieces.WHITE -> t.vertical += "1"
                Pieces.BLACK -> t.vertical += "2"
            }
        }

        var rangeHL = 0
        var rangeHR = 4
        if (column - 4 < 1) rangeHL = 1 else rangeHL = column - 4
        if (column + 4 > 12) rangeHR = 12 else rangeHR = column + 4
        for (i in rangeHL..rangeHR) {
            when (checkPointPiece(row, i)) {
                Pieces.Void -> t.horizon += "0"
                Pieces.WHITE -> t.horizon += "1"
                Pieces.BLACK -> t.horizon += "2"
            }
        }

        if (row > column) {
            val basis = row - column
            val startX = rangeHL
            val endX = 12 - basis

            for (i in startX..endX) {
                when (checkPointPiece(i + basis, i)) {
                    Pieces.Void -> t.leanFromLeftToRight += "0"
                    Pieces.WHITE -> t.leanFromLeftToRight += "1"
                    Pieces.BLACK -> t.leanFromLeftToRight += "2"
                }
            }

        } else if (column > row) {
            val basis = column - row
            val startY = rangeVT
            val endY = 12 - basis

            for (i in startY..endY) {
                when (checkPointPiece(i, i + basis)) {
                    Pieces.Void -> t.leanFromLeftToRight += "0"
                    Pieces.WHITE -> t.leanFromLeftToRight += "1"
                    Pieces.BLACK -> t.leanFromLeftToRight += "2"
                }
            }

        } else {
            for (i in min(rangeHL, rangeVT)..min(rangeHR, rangeVB)) {
                when (checkPointPiece(i, i)) {
                    Pieces.Void -> t.leanFromLeftToRight += "0"
                    Pieces.WHITE -> t.leanFromLeftToRight += "1"
                    Pieces.BLACK -> t.leanFromLeftToRight += "2"
                }
            }
        }

        val startX = if (column < 5) {
            if (row <= 9) 1 else column - 12 + row
        } else {
            if (row > 8) {
                column - 12 + row
            } else {
                column - 4
            }
        }
        val endX = column + min(row - 1, 12 - column)
        for (i in startX..endX) {
            when (checkPointPiece(row + column - i, i)) {
                Pieces.Void -> t.leanFromRightToLeft += "0"
                Pieces.WHITE -> t.leanFromRightToLeft += "1"
                Pieces.BLACK -> t.leanFromRightToLeft += "2"
            }
        }

        return t
    }

    fun scoresByOrientation(typeChess: String, piecesType: Pieces): Int {
        var countAll = 0
        if (piecesType == Pieces.WHITE) {
            if (typeChess.contains("11111")) countAll += FiveLine
            else if (typeChess.contains("011110")) countAll += FourDouble
            else if (typeChess.contains("01111") || typeChess.contains("11110")) countAll += FourSingle
            else if (typeChess.contains("01110")) countAll += ThreeDouble
            else if (typeChess.contains("11101") || typeChess.contains("10111")) countAll + ThreeWithoutOne
            else if (typeChess.contains("0111") || typeChess.contains("1110")) countAll += ThreeSingle
            else if (typeChess.contains("1101") || typeChess.contains("1011")) countAll + ThreeWithoutOne
            else if (typeChess.contains("0110")) countAll += TwoDouble
            else if (typeChess.contains("011") || typeChess.contains("110")) countAll += TwoSingle
            else countAll += OneDouble
        } else {
            if (typeChess.contains("22222")) countAll += FiveLine
            else if (typeChess.contains("022220")) countAll += FourDouble
            else if (typeChess.contains("02222") || typeChess.contains("22220")) countAll += FourSingle
            else if (typeChess.contains("02220")) countAll += ThreeDouble
            else if (typeChess.contains("22202") || typeChess.contains("20222")) countAll + FiveWithoutOne
            else if (typeChess.contains("0222") || typeChess.contains("2220")) countAll += ThreeSingle
            else if (typeChess.contains("2202") || typeChess.contains("2022")) countAll + ThreeWithoutOne
            else if (typeChess.contains("0220")) countAll += TwoDouble
            else if (typeChess.contains("022") || typeChess.contains("220")) countAll += TwoSingle
            else countAll += OneDouble
        }

        return countAll
    }
}
