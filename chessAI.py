import chess
import chess.svg
import random
import math
from PyQt5.QtSvg import QSvgWidget
from PyQt5.QtWidgets import QApplication, QWidget, QPushButton
import os
import sys
import time
import chess.engine
import chess.polyglot


class MainWindow(QWidget):
    def __init__(self, AIStart):
        super().__init__()
        self.boardSize = 800
        self.setGeometry(0, 0, self.boardSize, self.boardSize)
        self.AIStart = AIStart
        self.widgetSvg = QSvgWidget(parent=self)
        self.widgetSvg.setGeometry(0, 0, self.boardSize, self.boardSize)
        self.chessboard = chess.Board()
        # self.chessboard = chess.Board(fen="1k6/8/8/7R/3K4/8/8/8 w - - 0 1")

        self.chessboardSvg = chess.svg.board(self.chessboard).encode("UTF-8")
        self.widgetSvg.load(self.chessboardSvg)
        self.chessMove = ""
        self.running = False
        if AIStart:
            self.running = True
            AIMove(self.chessboard, self)
            self.running = False

        # self.revertMoveButton = QPushButton(text="Revert")
        # self.revertMoveButton.move(900, 100)

    def mousePressEvent(self, event):
        if self.running:
            return
        if not self.AIStart:
            self.running = True
            if humanMove(self, event):
                AIMove(self.chessboard, self)

            self.AIsTurn = False
            self.running = False
        else:
            self.running = True
            if (humanMove(self, event)):
                AIMove(self.chessboard, self)
            self.running = False


def AIMove(chessboard: chess.Board, window: MainWindow):

    # max_weight = -math.inf
    # with chess.polyglot.open_reader("performance.bin") as reader:
    #     for entry in reader.find_all(window.chessboard):
    #         print(entry.move, entry.weight, entry.learn)
    #         if entry.weight > max_weight:
    #             max_weight = entry.weight
    #             move = entry.move
    # if max_weight > -math.inf:
    #     makeMove(chess.Move.from_uci(str(move)), window)
    #     return

    print("Thinking")

    start = time.perf_counter()
    result = None
    transPositionTable = dict()
    i = 1
    while True:
        result = minMax(chessboard, depth=i, prevMove=None, alpha=-
                        1000, beta=1000, transPositionTable=transPositionTable)
        # if (result[0] == None and (chess.Board.is_fifty_moves(chessboard) or chess.Board.is_repetition(chessboard) or chess.Board.is_stalemate(chessboard) or chess.Board.is_fivefold_repetition(chessboard))):
        if (result[0] == None) :
            print("AI did not find any non-losing move", chessboard.move_stack)
            makeMove(chess.Move.from_uci(
                str(list(chessboard.legal_moves)[0])), window)
            return

        end = time.perf_counter()
        ms = (end-start)
        print(i, int(ms), result[1], result[2])
        if ms >= 1 or len(list(chessboard.legal_moves)) == 0 or result[1] == -math.inf or result[1] == math.inf:
            break
        i = i + 1

    makeMove(chess.Move.from_uci(str(result[0])), window)

    print("AB end prediciton:", result[1], "at depth", result[2])
    print("AI move", result[0])
    print("Current value:", evaluationFunction(chessboard))
    # print("Time taken", int(ms))
    print("-"*30)


def humanMove(self, event):
    if len(list(self.chessboard.legal_moves)) == 0:
        print("Human lost")
        return False
    if self.chessMove == "":
        self.chessMove = getSquare(
            event, (self.boardSize*0.075)/2, self.boardSize - (self.boardSize*0.075)/2)
        return False
    elif len(self.chessMove) == 2:
        self.chessMove = self.chessMove + \
            getSquare(event, (self.boardSize*0.075)/2,
                      self.boardSize - (self.boardSize*0.075)/2)

    move = legalMove(window.chessboard, self.chessMove)
    if move == None:
        print("Invalid move", self.chessMove)
        self.chessMove = ""
        return False

    # Human move
    print("Human move :", self.chessMove)

    makeMove(move, window)
    self.chessMove = ""
    return True


def getSquare(event, lowest, highest):
    separation = (highest - lowest) / 8
    x = event.pos().x()
    y = event.pos().y()
    y_line = get_column_letter(x, lowest, highest, separation)
    x_line = get_row_number(y, lowest, highest, separation)
    return y_line + str(x_line)


def get_column_letter(x, lowest, highest, separation):
    for i in range(0, 8):
        if x > lowest and x < highest and x > lowest and x < highest and x > lowest + separation * i and x < lowest + separation * (i + 1):
            return chr(97 + i)
    return ""


def get_row_number(y, lowest, highest, separation):
    for i in range(0, 8):
        if y > lowest and y < highest and y > lowest and y < highest and y > lowest + separation * i and y < lowest + separation * (i+1):
            return 8-i
    return ""


def makeMove(move: chess.Move, window: MainWindow):
    window.chessboard.promoted
    window.chessboard.push(chess.Move(
        from_square=move.from_square, to_square=move.to_square, promotion=move.promotion))

    if window.chessboard.is_check():
        window.chessboardSvg = chess.svg.board(
            window.chessboard,
            lastmove=move,
            check=window.chessboard.king(window.chessboard.turn)
        ).encode("UTF-8")
    else:
        window.chessboardSvg = chess.svg.board(
            window.chessboard,
            lastmove=move,
            check=None
        ).encode("UTF-8")
    window.widgetSvg.load(window.chessboardSvg)
    # window.update()
    window.repaint()


# def evaluationFunction2(board: chess.Board):

#     if chess.Board.is_fifty_moves(board) or chess.Board.is_repetition(board) or chess.Board.is_stalemate(board) or chess.Board.is_fivefold_repetition(board):
#         if board.turn == chess.BLACK:
#             return -99
#         elif board.turn == chess.WHITE:
#             return 99

#     if len(list(board.legal_moves)) == 0:
#         if board.turn == chess.BLACK:
#             return -math.inf
#         elif board.turn == chess.WHITE:
#             return math.inf

#     pawnDiff = calcPieceDiff(chess.PAWN, board)
#     knightDiff = calcPieceDiff(chess.KNIGHT, board) * 3
#     bishopDiff = calcPieceDiff(chess.BISHOP, board) * 3
#     rookDiff = calcPieceDiff(chess.ROOK, board) * 5
#     queenDiff = calcPieceDiff(chess.QUEEN, board) * 9
#     return pawnDiff + knightDiff + bishopDiff + rookDiff + queenDiff


pawn_tableWHITE = [
    [0,  0,  0,  0,  0,  0,  0,  0],
    [50, 50, 50, 50, 50, 50, 50, 50],
    [10, 10, 20, 30, 30, 20, 10, 10],
    [5,  5, 10, 25, 25, 10,  5,  5],
    [0,  0,  0, 20, 20,  0,  0,  0],
    [5, -5, -10,  0,  0, -10, -5,  5],
    [5, 10, 10, -20, -20, 10, 10,  5],
    [0,  0,  0,  0,  0,  0,  0,  0]
]

knight_tableWHITE = [
    [-50, -40, -30, -30, -30, -30, -40, -50],
    [-40, -20,  0,  0,  0,  0, -20, -40],
    [-30,  0, 10, 15, 15, 10,  0, -30],
    [-30,  5, 15, 20, 20, 15,  5, -30],
    [-30,  0, 15, 20, 20, 15,  0, -30],
    [-30,  5, 10, 15, 15, 10,  5, -30],
    [-40, -20,  0,  5,  5,  0, -20, -40],
    [-50, -40, -30, -30, -30, -30, -40, -50]
]

bishop_tableWHITE = [
    [-20, -10, -10, -10, -10, -10, -10, -20],
    [-10,  0,  0,  0,  0,  0,  0, -10],
    [-10,  0,  5, 10, 10,  5,  0, -10],
    [-10,  5,  5, 10, 10,  5,  5, -10],
    [-10,  0, 10, 10, 10, 10,  0, -10],
    [-10, 10, 10, 10, 10, 10, 10, -10],
    [-10,  5,  0,  0,  0,  0,  5, -10],
    [-20, -10, -10, -10, -10, -10, -10, -20]
]

rook_tableWHITE = [
    [0,  0,  0,  0,  0,  0,  0,  0],
    [5, 10, 10, 10, 10, 10, 10,  5],
    [-5,  0,  0,  0,  0,  0,  0, -5],
    [-5,  0,  0,  0,  0,  0,  0, -5],
    [-5,  0,  0,  0,  0,  0,  0, -5],
    [-5,  0,  0,  0,  0,  0,  0, -5],
    [-5,  0,  0,  0,  0,  0,  0, -5],
    [0,  0,  0,  5,  5,  0,  0,  0]
]

queen_tableWHITE = [
    [-20, -10, -10, -5, -5, -10, -10, -20],
    [-10,  0,  0,  0,  0,  0,  0, -10],
    [-10,  0,  5,  5,  5,  5,  0, -10],
    [-5,  0,  5,  5,  5,  5,  0, -5],
    [0,  0,  5,  5,  5,  5,  0, -5],
    [-10,  5,  5,  5,  5,  5,  0, -10],
    [-10,  0,  5,  0,  0,  0,  0, -10],
    [-20, -10, -10, -5, -5, -10, -10, -20]
]

king_tableWHITE = [
    [-30, -40, -40, -50, -50, -40, -40, -30],
    [-30, -40, -40, -50, -50, -40, -40, -30],
    [-30, -40, -40, -50, -50, -40, -40, -30],
    [-30, -40, -40, -50, -50, -40, -40, -30],
    [-20, -30, -30, -40, -40, -30, -30, -20],
    [-10, -20, -20, -20, -20, -20, -20, -10],
    [20, 20,  0,  0,  0,  0, 20, 20],
    [20, 30, 10,  0,  0, 10, 30, 20]
]

pawn_tableBLACK = []
knight_tableBLACK = []
bishop_tableBLACK = []
rook_tableBLACK = []
queen_tableBLACK = []
king_tableBLACK = []


def initPieceSquareTables(whiteTable, blackTable):
    row = 7
    for i in range(8):
        newRow = []
        for col in range(8):
            newRow.append(whiteTable[row][col])
        blackTable.append(newRow)
        row -= 1


def printPieceSqTable(table):
    print(20*"-")
    for row in table:
        for col in row:
            print(f"{'{:3}'.format(col)},", end="")
        print()
    print(20*"-")


def evaluationFunction(board: chess.Board):

    if chess.Board.is_fifty_moves(board) or chess.Board.is_repetition(board) or chess.Board.is_stalemate(board) or chess.Board.is_fivefold_repetition(board):
        if board.turn == chess.BLACK:
            return -math.inf
        elif board.turn == chess.WHITE:
            return math.inf

    if len(list(board.legal_moves)) == 0:
        if board.turn == chess.BLACK:
            return -math.inf
        elif board.turn == chess.WHITE:
            return math.inf

    whiteSum = 0
    blackSum = 0

    for row in range(8):
        for col in range(8):
            numerical = 8 * row + col
            piece = board.piece_at(numerical)
            if piece == None:
                continue
            if piece.piece_type == chess.PAWN:

                if piece.color == chess.WHITE:

                    whiteSum += 100
                    whiteSum += pawn_tableWHITE[7-row][col]

                else:
                    blackSum += 100
                    blackSum += pawn_tableBLACK[7-row][col]

            elif piece.piece_type == chess.KNIGHT:
                if piece.color == chess.WHITE:
                    whiteSum += 320
                    whiteSum += knight_tableWHITE[7-row][col]
                else:
                    blackSum += 320
                    blackSum += knight_tableBLACK[7-row][col]
            elif piece.piece_type == chess.BISHOP:
                if piece.color == chess.WHITE:
                    whiteSum += 330
                    whiteSum += bishop_tableWHITE[7-row][col]
                else:
                    blackSum += 330
                    blackSum += bishop_tableBLACK[7-row][col]
            elif piece.piece_type == chess.ROOK:
                if piece.color == chess.WHITE:
                    whiteSum += 500
                    whiteSum += rook_tableWHITE[7-row][col]
                else:
                    blackSum += 500
                    blackSum += rook_tableBLACK[7-row][col]
            elif piece.piece_type == chess.QUEEN:
                if piece.color == chess.WHITE:
                    whiteSum += 900
                    whiteSum += queen_tableWHITE[7-row][col]
                else:
                    blackSum += 900
                    blackSum += queen_tableBLACK[7-row][col]
            elif piece.piece_type == chess.KING:
                if piece.color == chess.WHITE:
                    whiteSum += 20000
                    whiteSum += king_tableWHITE[7-row][col]
                else:
                    blackSum += 20000
                    blackSum += king_tableBLACK[7-row][col]
    # print(f"whiteSum = {whiteSum} blackSum = {blackSum}")
    return blackSum - whiteSum


def calcPieceDiff(piece: chess.PieceType, board: chess.Board) -> int:
    return len(chess.Board.pieces(board, piece, chess.BLACK)) - len(chess.Board.pieces(board, piece, chess.WHITE))

# Could add checks


def sortMoveList(moves: chess.LegalMoveGenerator, board: chess.Board):
    attackers = []
    attackers_value = []
    nonAttackers = []
    for move in moves:
        valBefore = evaluationFunction(board=board)
        board.push(move)
        valafter = evaluationFunction(board=board)
        board.pop()
        if valBefore != valafter:
            # if len(attackers) > 1 :
            # diff = abs(valBefore - valafter)
            # for i in range(0, len(attackers)):
            #     if diff > attackers_value[i]:
            #         attackers_value.insert(i, diff)
            #         attackers.insert(i, move)
            #         break
            attackers.append(move)
            # attackers_value.append(diff)
        else:
            nonAttackers.append(move)

    return (attackers, nonAttackers)


def minMax(board: chess.Board, depth: int, prevMove: chess.Move, alpha: int, beta: int, transPositionTable: dict):

    result = sortMoveList(board.legal_moves, board)
    attackMoves = result[0]
    noAttackMoves = result[1]

    sortedMoves: chess.Move = []

    # Quiescence continuation
    if depth == 0 and len(attackMoves) != 0:
        sortedMoves = attackMoves + noAttackMoves
    # Quiescence base case
    elif depth == -1:
        value = (prevMove, evaluationFunction(board=board), -1)
        return value
    # Regular base case
    elif depth == 0 or len(attackMoves) + len(noAttackMoves) == 0 or chess.Board.is_fifty_moves(board) or chess.Board.is_repetition(board):
        value = (prevMove, evaluationFunction(board=board), depth)
        return value

    # Regular continuation
    elif depth != 0 and len(attackMoves) + len(noAttackMoves) != 0:
        # Makes the algorithm pick attacks first
        sortedMoves = attackMoves + noAttackMoves
    else:
        print("WTF")
        exit()

    # MAX
    if board.turn == chess.BLACK:
        value = -math.inf
        bestMov = prevMove
        maxDepth = -math.inf

        for mov in sortedMoves:
            mov: chess.Move
            board.push(mov)
            ret = minMax(board=board, depth=depth-1, prevMove=mov,
                         alpha=alpha, beta=beta, transPositionTable=transPositionTable)
            currentVal = ret[1]
            board.pop()
            if currentVal > value:
                value = currentVal
                bestMov = mov
                maxDepth = ret[2]
            alpha = max(alpha, value)
            if value >= beta:
                break
        transPositionTable[hash(str(board.fen) + str(depth))] = ret
        return (bestMov, value, maxDepth)
    # MIN
    elif board.turn == chess.WHITE:
        value = math.inf
        bestMov = prevMove
        maxDepth = -math.inf
        for mov in sortedMoves:
            mov: chess.Move
            board.push(mov)
            ret = minMax(board=board, depth=depth-1, prevMove=mov,
                         alpha=alpha, beta=beta, transPositionTable=transPositionTable)
            currentVal = ret[1]
            board.pop()
            if currentVal < value:
                value = currentVal
                bestMov = mov
                maxDepth = ret[2]
            beta = min(beta, value)
            if currentVal <= alpha:
                break
        transPositionTable[hash(str(board.fen) + str(depth))] = ret
        return (bestMov, value, maxDepth)


def legalMove(board: chess.Board, move: str):
    for m in list(board.legal_moves):
        mov_con = str(chess.Move.from_uci(str(m)))
        if move in mov_con:
            return m
    return None


if __name__ == "__main__":
    initPieceSquareTables(pawn_tableWHITE, pawn_tableBLACK)
    initPieceSquareTables(knight_tableWHITE, knight_tableBLACK)
    initPieceSquareTables(bishop_tableWHITE, bishop_tableBLACK)
    initPieceSquareTables(rook_tableWHITE, rook_tableBLACK)
    initPieceSquareTables(queen_tableWHITE, queen_tableBLACK)
    initPieceSquareTables(king_tableWHITE, king_tableBLACK)

    printPieceSqTable(pawn_tableWHITE)
    printPieceSqTable(pawn_tableBLACK)

    app = QApplication([])
    AIstart = False
    if sys.argv[1] == "white":
        AIstart = True
    window = MainWindow(AIStart=AIstart)
    # window = MainWindow(AIStart=AIstart, depth=1)

    # window.AIsTurn= sys.argv[1]
    window.show()
    app.exec()

    # # chessboard = chess.Board(fen="1P6/3P4/8/3n4/8/8/3p4/8 w - - 0 1")
    # chessboard = chess.Board()
    # print(evaluationFunction(chessboard))
