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


class MainWindow(QWidget):
    def __init__(self, AIStart, depth):
        super().__init__()
        self.boardSize = 800
        self.setGeometry(0, 0, self.boardSize, self.boardSize)
        self.AIStart = AIStart
        self.deph = depth
        self.widgetSvg = QSvgWidget(parent=self)
        self.widgetSvg.setGeometry(0, 0, self.boardSize, self.boardSize)
        # self.chessboard = chess.Board()
        self.chessboard = chess.Board(fen="k7/5P2/8/8/8/8/7p/K7 w - - 0 1")

        self.chessboardSvg = chess.svg.board(self.chessboard).encode("UTF-8")
        self.widgetSvg.load(self.chessboardSvg)
        self.chessMove = ""
        self.running = False
        if AIStart:
            self.running = True
            AIMove(self.chessboard, depth, self)
            self.running = False

        # self.revertMoveButton = QPushButton(text="Revert")
        # self.revertMoveButton.move(900, 100)

    def mousePressEvent(self, event):
        if self.running:
            return
        if not self.AIStart:
            self.running = True
            if humanMove(self, event):
                AIMove(self.chessboard, self.deph, self)

            self.AIsTurn = False
            self.running = False
        else:
            self.running = True
            if (humanMove(self, event)):
                AIMove(self.chessboard, self.deph, self)
            self.running = False


def AIMove(chessboard: chess.Board, depth, window: MainWindow):
    print("Thinking")

    start = time.perf_counter()
    result = None
    startingDepth = int(depth)
    while True:
        result = minMax(chessboard, depth=1, prevMove=None, alpha=-1000, beta=1000)
        if (result[0] == None):
            print("AI did not find move ", chessboard.move_stack)
            return
        end = time.perf_counter()
        ms = (end-start)
        if ms > 6 :
            break
        startingDepth = startingDepth + 1
            
    makeMove(chess.Move.from_uci(str(result[0])), window)
    
    print("AB end prediciton:", result[1], "at depth", result[2], ",", startingDepth)
    print("AI move", result[0])
    print("Current value:", evaluationFunction(chessboard))
    print("Time taken", int(ms))
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
    if  move == None:
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


def evaluationFunction(board: chess.Board):

    if chess.Board.is_fifty_moves(board) or chess.Board.is_repetition(board):
        if board.turn == chess.WHITE:
            return 500
        else:
            return -500

    if len(list(board.legal_moves)) == 0:
        if board.turn == chess.BLACK:
            return -1000
        elif board.turn == chess.WHITE:
            return 1000

    pawnDiff = calcPieceDiff(chess.PAWN, board)
    knightDiff = calcPieceDiff(chess.KNIGHT, board) * 3
    bishopDiff = calcPieceDiff(chess.BISHOP, board) * 3
    rookDiff = calcPieceDiff(chess.ROOK, board) * 5
    queenDiff = calcPieceDiff(chess.QUEEN, board) * 9
    return pawnDiff + knightDiff + bishopDiff + rookDiff + queenDiff


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


def minMax(board: chess.Board, depth: int, prevMove: chess.Move, alpha: int, beta: int):

    result = sortMoveList(board.legal_moves, board)
    attackMoves = result[0]
    noAttackMoves = result[1]

    sortedMoves : chess.Move = []
    # Quiescence base case
    if depth == -1:
        value = (prevMove, evaluationFunction(board=board), -1)
        return value
    # Regular base case
    elif depth == 0 or len(attackMoves) + len(noAttackMoves) == 0 or chess.Board.is_fifty_moves(board) or chess.Board.is_repetition(board):
        value = (prevMove, evaluationFunction(board=board), depth)
        return value
    # Quiescence continuation
    elif depth == 0 and len(attackMoves) != 0:
        sortedMoves = attackMoves
    # Regular continuation
    elif depth != 0 and len(attackMoves) + len(noAttackMoves) != 0:
        # Makes the algorithm pick attacks first
        sortedMoves = attackMoves + noAttackMoves
    else:
        print("WTF")
        exit()

    # MAX
    if board.turn == chess.BLACK:
        value = -998
        bestMov = prevMove
        maxDepth = -math.inf
        
        for mov in sortedMoves:
            mov : chess.Move
            board.push(mov)
            ret = minMax(board=board, depth=depth-1, prevMove=mov, alpha=alpha, beta=beta)
            currentVal = ret[1]
            board.pop()
            if currentVal > value:
                value = currentVal
                bestMov = mov
                maxDepth = ret[2]
            alpha = max(alpha, value)
            if value >= beta:
                break
        return (bestMov, value, maxDepth)
    # MIN
    elif board.turn == chess.WHITE:
        value = 999
        bestMov = prevMove
        maxDepth = -math.inf
        for mov in sortedMoves:
            mov : chess.Move
            board.push(mov)
            ret = minMax(board=board, depth=depth-1, prevMove=mov, alpha=alpha, beta=beta)
            currentVal = ret[1]
            board.pop()
            if currentVal < value:
                value = currentVal
                bestMov = mov
                maxDepth = ret[2]
            beta = min(beta, value)
            if currentVal <= alpha:
                break

        return (bestMov, value, maxDepth)


def legalMove(board: chess.Board, move: str):
    for m in list(board.legal_moves):
        mov_con = str(chess.Move.from_uci(str(m)))
        if move in mov_con:
            return m
    return None


if __name__ == "__main__":
    app = QApplication([])
    AIstart = False
    if sys.argv[1] == "white":
        AIstart = True
    window = MainWindow(AIStart=AIstart, depth=sys.argv[2])
    # window = MainWindow(AIStart=AIstart, depth=1)

    # window.AIsTurn= sys.argv[1]
    window.show()
    app.exec()
