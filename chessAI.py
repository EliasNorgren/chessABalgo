import chess
import chess.svg
import random
import math
from PyQt5.QtSvg import QSvgWidget
from PyQt5.QtWidgets import QApplication, QWidget


class MainWindow(QWidget):
    def __init__(self):
        super().__init__()

        self.setGeometry(2000, 100, 600, 600)

        self.widgetSvg = QSvgWidget(parent=self)
        self.widgetSvg.setGeometry(10, 10, 500, 500)
        self.chessboard = chess.Board()
        self.chessboardSvg = chess.svg.board(self.chessboard).encode("UTF-8")
        self.widgetSvg.load(self.chessboardSvg)


def makeMove(move: chess.Move, window: MainWindow):
    window.chessboard.push(chess.Move(
        from_square=move.from_square, to_square=move.to_square))
    window.chessboardSvg = chess.svg.board(window.chessboard).encode("UTF-8")
    window.widgetSvg.load(window.chessboardSvg)
    window.update()


def evaluationFunction(board: chess.Board):

    if len(list(board.legal_moves)) == 0 :
        if board.turn == chess.BLACK :
            return -1000
        elif board.turn == chess.WHITE :
            return 1000

    pawnDiff = calcPieceDiff(chess.PAWN, board)
    knightDiff = calcPieceDiff(chess.KNIGHT, board) * 3
    bishopDiff = calcPieceDiff(chess.BISHOP, board) * 3
    rookDiff = calcPieceDiff(chess.ROOK, board) * 5
    queenDiff = calcPieceDiff(chess.QUEEN, board) * 9
    return pawnDiff + knightDiff + bishopDiff + rookDiff + queenDiff


def calcPieceDiff(piece: chess.PieceType, board: chess.Board) -> int:
    return len(chess.Board.pieces(board, piece, chess.BLACK)) - len(chess.Board.pieces(board, piece, chess.WHITE))
                      


def minMax(board: chess.Board, depth: int, prevMove: chess.Move, alpha: int, beta: int):

    if depth == 0 or len(list(board.legal_moves)) == 0:
        return (prevMove, evaluationFunction(board=board))

    # MAX
    if board.turn == chess.BLACK :
       value = -1000
       bestMov = prevMove
       for mov in board.legal_moves:
            # mov_con = chess.Move.from_uci(str(mov))
            # makeMove(mov_con, window=window)
            board.push(mov)
            currentVal = minMax(board=board,depth=depth-1, prevMove=mov, alpha=alpha, beta=beta)[1]
            board.pop()
            if currentVal > value :
                value = currentVal
                bestMov = mov
            alpha = max(alpha, value)
            if value >= beta :
                break
       return (bestMov, value)
    # MIN
    elif board.turn == chess.WHITE :
        value = 1000
        bestMov = prevMove
        for mov in board.legal_moves:
            # mov_con = chess.Move.from_uci(str(mov))
            # makeMove(mov_con, window=window)
            board.push(mov)
            currentVal = minMax(board=board,depth=depth-1, prevMove=mov, alpha=alpha, beta=beta)[1]
            board.pop()
            if currentVal < value :
                value = currentVal
                bestMov = mov
            beta = min(beta, value)
            if currentVal <= alpha :
                break
            
        return (bestMov, value)

def legalMove(board:chess.Board, move: str):
    found = False
    for m in list(board.legal_moves) :
        mov_con = str(chess.Move.from_uci(str(m)))
        if move == mov_con :
            found = True
            break
    return found

if __name__ == "__main__":
    app = QApplication([])
    window = MainWindow()
    window.show()
    while(True):
    
        if (window.chessboard.turn):
            move = input()

            if not legalMove(window.chessboard, move) :
                print("Invalid move",move)
                continue
        else:
            result = minMax(window.chessboard, 5, None, alpha=-1000, beta=1000)
            move = result[0]
            print("AB_pred:",move,result[1])
            print("Current value:",evaluationFunction(window.chessboard))

        makeMove(chess.Move.from_uci(str(move)), window)
        
    app.exec()




    