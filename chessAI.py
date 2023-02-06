import chess
import chess.svg
import random
import math
from PyQt5.QtSvg import QSvgWidget
from PyQt5.QtWidgets import QApplication, QWidget


class MainWindow(QWidget):
    def __init__(self):
        super().__init__()
        self.boardSize = 800
        self.setGeometry(0, 0, self.boardSize, self.boardSize)

        self.widgetSvg = QSvgWidget(parent=self)
        self.widgetSvg.setGeometry(0, 0, self.boardSize, self.boardSize)
        self.chessboard = chess.Board()
        self.chessboardSvg = chess.svg.board(self.chessboard).encode("UTF-8")
        self.widgetSvg.load(self.chessboardSvg)
        self.chessMove = ""

    def mousePressEvent(self, event):

        
        if self.chessMove == "" :
            self.chessMove = getSquare(event, (self.boardSize*0.075)/2, self.boardSize - (self.boardSize*0.075)/2)
            return
        elif len(self.chessMove) == 2 :
            self.chessMove = self.chessMove + getSquare(event, (self.boardSize*0.075)/2, self.boardSize - (self.boardSize*0.075)/2)
            
        if not legalMove(window.chessboard, self.chessMove) :
            print("Invalid move",self.chessMove)
            self.chessMove = ""
            return

        # Human move
        print("Human move :",self.chessMove)
        if len(list(self.chessboard.legal_moves)) == 0 :
            print("White lost")
            return
        makeMove(chess.Move.from_uci(str(self.chessMove)), window)
        self.chessMove = ""


        result = minMax(window.chessboard, 3, None, alpha=-1000, beta=1000)
        if(result[0] == None):
            print(chess.Board.move_stack)
            return
        makeMove(chess.Move.from_uci(str(result[0])),window)
        print("AB end prediciton:",result[1])
        print("AI move", result[0])
        print("Current value:",evaluationFunction(window.chessboard))    
        print("-"*30)

def getSquare(event, lowest, highest): 
    separation = (highest - lowest) / 8
    x = event.pos().x()
    y = event.pos().y()
    y_line = get_column_letter(x, lowest, highest, separation)
    x_line = get_row_number(y, lowest, highest, separation)
    return y_line + str(x_line)

def get_column_letter(x, lowest, highest, separation):
    for i in range(0,8):
        if x > lowest and x < highest and x > lowest and x < highest and x > lowest + separation * i and x < lowest + separation * (i + 1):
            return chr(97 + i)
    return ""

def get_row_number(y, lowest, highest, separation):
    for i in range(0,8):
        if y > lowest and y < highest and y > lowest and y < highest and y > lowest + separation * i and y < lowest + separation * (i+1):
            return 8-i
    return ""

def makeMove(move: chess.Move, window: MainWindow):
    window.chessboard.push(chess.Move(
        from_square=move.from_square, to_square=move.to_square))
    
    if window.chessboard.is_check():
        window.chessboardSvg = chess.svg.board(
            window.chessboard,
            lastmove=move,
            check=window.chessboard.king(window.chessboard.turn)  
        ).encode("UTF-8")
    else :
        window.chessboardSvg = chess.svg.board(
            window.chessboard,
            lastmove=move,
            check=None    
        ).encode("UTF-8")
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

    result = sortMoveList(board.legal_moves, board)
    attackMoves = result[0]
    noAttackMoves = result[1]

    sortedMoves = []
    # Quiescence base case 
    if depth == -1 :
        value = (prevMove, evaluationFunction(board=board))
        if(value == 1000):
            print("is 1000 1") 
        return value
    # Regular base case 
    elif depth == 0 or len(attackMoves) + len(noAttackMoves) == 0:
        value = (prevMove, evaluationFunction(board=board))
        if(value == 1000):
            print("is 1000 1") 
        return value
    # Quiescence continuation
    elif depth == 0 and len(attackMoves) != 0:
        sortedMoves = attackMoves
    # Regular continuation
    elif depth != 0 and len(attackMoves) + len(noAttackMoves) != 0:
        # Makes the algorithm pick attacks first 
        sortedMoves = attackMoves + noAttackMoves
    else :
        print("WTF")
        exit()

    # MAX
    if board.turn == chess.BLACK :
       value = -998
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
        value = 999
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
    app.exec()




    