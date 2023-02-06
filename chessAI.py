import chess
import chess.svg
import random
import math
from PyQt5.QtSvg import QSvgWidget
from PyQt5.QtWidgets import QApplication, QWidget


class MainWindow(QWidget):
    def __init__(self):
        super().__init__()

        self.setGeometry(2000, 100, 800, 800)

        self.widgetSvg = QSvgWidget(parent=self)
        self.widgetSvg.setGeometry(0, 0, 800, 800)
        self.chessboard = chess.Board()
        self.chessboardSvg = chess.svg.board(self.chessboard).encode("UTF-8")
        self.widgetSvg.load(self.chessboardSvg)


def makeMove(move: chess.Move, window: MainWindow):
    window.chessboard.push(chess.Move(
        from_square=move.from_square, to_square=move.to_square))
    window.chessboardSvg = chess.svg.board(window.chessboard).encode("UTF-8")
    window.widgetSvg.load(window.chessboardSvg)
    # window.update()
    window.repaint()


def evaluationFunction(board: chess.Board):

    if chess.Board.is_fifty_moves(board) or chess.Board.is_repetition(board) :
        return -500

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

# Could add checks              
def sortMoveList(moves: chess.LegalMoveGenerator, board: chess.Board) :
    attackers = []
    nonAttackers = []
    for move in moves:
        valBefore = evaluationFunction(board=board)
        board.push(move)
        valafter = evaluationFunction(board=board)
        board.pop()
        if valBefore != valafter :
            attackers.append(move)
        else:
            nonAttackers.append(move)           
    return (attackers, nonAttackers)



def minMax(board: chess.Board, depth: int, prevMove: chess.Move, alpha: int, beta: int):

    if prevMove != None and str(chess.Move.from_uci(str(prevMove))) == "b1c3" :
        print("IN",str(chess.Move.from_uci(str(prevMove))))

    result = sortMoveList(board.legal_moves, board)
    attackMoves = result[0]
    noAttackMoves = result[1]

    sortedMoves = []
    if depth == -1 :
        return (prevMove, evaluationFunction(board=board))
    if depth == 0 and len(attackMoves) + len(noAttackMoves) == 0:
        return (prevMove, evaluationFunction(board=board))
    elif depth == 0 and len(attackMoves) != 0:
        sortedMoves = attackMoves
    elif depth != 0 and len(attackMoves) + len(noAttackMoves) != 0:
        # Makes the algorithm pick attacks first 
        sortedMoves = attackMoves + noAttackMoves

    # MAX
    if board.turn == chess.BLACK :
       value = -1000
       bestMov = prevMove
       for mov in sortedMoves:
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
        for mov in sortedMoves:
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
            move = input("Enter move: ")

            if not legalMove(window.chessboard, move) :
                print("Invalid move",move)
                continue
        else:
            result = minMax(window.chessboard, 3, None, alpha=-1000, beta=1000)
            move = result[0]
            print("AB end prediciton:",result[1])
            print("AI move", move)
            print("Current value:",evaluationFunction(window.chessboard))
            print("-"*20)

        makeMove(chess.Move.from_uci(str(move)), window)
        
    app.exec()




    