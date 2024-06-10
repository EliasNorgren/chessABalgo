import multiprocessing.process
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
from concurrent.futures import ProcessPoolExecutor
from multiprocessing import Manager
import traceback
import cProfile
import pstats
import multiprocessing

# class MainWindow(QWidget):
#     def __init__(self, AIStart):
#         super().__init__()
#         self.boardSize = 800
#         self.setGeometry(0, 0, self.boardSize, self.boardSize)
#         self.AIStart = AIStart
#         self.widgetSvg = QSvgWidget(parent=self)
#         self.widgetSvg.setGeometry(0, 0, self.boardSize, self.boardSize)
#         self.chessboard = chess.Board()
#         # self.chessboard = chess.Board(fen="1k6/8/8/7R/3K4/8/8/8 w - - 0 1")

#         self.chessboardSvg = chess.svg.board(self.chessboard).encode("UTF-8")
#         self.widgetSvg.load(self.chessboardSvg)
#         self.chessMove = ""
#         self.running = False
#         if AIStart:
#             self.running = True
#             AIMove(self.chessboard, self)
#             self.running = False

#         # self.revertMoveButton = QPushButton(text="Revert")
#         # self.revertMoveButton.move(900, 100)

#     def mousePressEvent(self, event):
#         if self.running:
#             return
#         if not self.AIStart:
#             self.running = True
#             if humanMove(self, event):
#                 AIMove(self.chessboard, self)

#             self.AIsTurn = False
#             self.running = False
#         else:
#             self.running = True
#             if (humanMove(self, event)):
#                 AIMove(self.chessboard, self)
#             self.running = False

# def makeMove(move: chess.Move, window: MainWindow):
#     window.chessboard.promoted
#     window.chessboard.push(chess.Move(
#         from_square=move.from_square, to_square=move.to_square, promotion=move.promotion))

#     if window.chessboard.is_check():
#         window.chessboardSvg = chess.svg.board(
#             window.chessboard,
#             lastmove=move,
#             check=window.chessboard.king(window.chessboard.turn)
#         ).encode("UTF-8")
#     else:
#         window.chessboardSvg = chess.svg.board(
#             window.chessboard,
#             lastmove=move,
#             check=None
#         ).encode("UTF-8")
#     window.widgetSvg.load(window.chessboardSvg)
#     # window.update()
#     window.repaint()

# def humanMove(self, event):
#     if len(list(self.chessboard.legal_moves)) == 0:
#         print("Human lost")
#         return False
#     if self.chessMove == "":
#         self.chessMove = getSquare(
#             event, (self.boardSize*0.075)/2, self.boardSize - (self.boardSize*0.075)/2)
#         return False
#     elif len(self.chessMove) == 2:
#         self.chessMove = self.chessMove + \
#             getSquare(event, (self.boardSize*0.075)/2,
#                       self.boardSize - (self.boardSize*0.075)/2)

#     move = legalMove(window.chessboard, self.chessMove)
#     if move == None:
#         print("Invalid move", self.chessMove)
#         self.chessMove = ""
#         return False

#     # Human move
#     print("Human move :", self.chessMove)

#     makeMove(move, window)
#     self.chessMove = ""
#     return True

class ChessAI():
    def __init__(self) -> None:
        self.pawn_tableWHITE = [
            [0,  0,  0,  0,  0,  0,  0,  0],
            [50, 50, 50, 50, 50, 50, 50, 50],
            [10, 10, 20, 30, 30, 20, 10, 10],
            [5,  5, 10, 25, 25, 10,  5,  5],
            [0,  0,  0, 20, 20,  0,  0,  0],
            [5, -5, -10,  0,  0, -10, -5,  5],
            [5, 10, 10, -20, -20, 10, 10,  5],
            [0,  0,  0,  0,  0,  0,  0,  0]
        ]

        self.knight_tableWHITE = [
            [-50, -40, -30, -30, -30, -30, -40, -50],
            [-40, -20,  0,  0,  0,  0, -20, -40],
            [-30,  0, 10, 15, 15, 10,  0, -30],
            [-30,  5, 15, 20, 20, 15,  5, -30],
            [-30,  0, 15, 20, 20, 15,  0, -30],
            [-30,  5, 10, 15, 15, 10,  5, -30],
            [-40, -20,  0,  5,  5,  0, -20, -40],
            [-50, -40, -30, -30, -30, -30, -40, -50]
        ]

        self.bishop_tableWHITE = [
            [-20, -10, -10, -10, -10, -10, -10, -20],
            [-10,  0,  0,  0,  0,  0,  0, -10],
            [-10,  0,  5, 10, 10,  5,  0, -10],
            [-10,  5,  5, 10, 10,  5,  5, -10],
            [-10,  0, 10, 10, 10, 10,  0, -10],
            [-10, 10, 10, 10, 10, 10, 10, -10],
            [-10,  5,  0,  0,  0,  0,  5, -10],
            [-20, -10, -10, -10, -10, -10, -10, -20]
        ]

        self.rook_tableWHITE = [
            [0,  0,  0,  0,  0,  0,  0,  0],
            [5, 10, 10, 10, 10, 10, 10,  5],
            [-5,  0,  0,  0,  0,  0,  0, -5],
            [-5,  0,  0,  0,  0,  0,  0, -5],
            [-5,  0,  0,  0,  0,  0,  0, -5],
            [-5,  0,  0,  0,  0,  0,  0, -5],
            [-5,  0,  0,  0,  0,  0,  0, -5],
            [0,  0,  0,  5,  5,  0,  0,  0]
        ]

        self.queen_tableWHITE = [
            [-20, -10, -10, -5, -5, -10, -10, -20],
            [-10,  0,  0,  0,  0,  0,  0, -10],
            [-10,  0,  5,  5,  5,  5,  0, -10],
            [-5,  0,  5,  5,  5,  5,  0, -5],
            [0,  0,  5,  5,  5,  5,  0, -5],
            [-10,  5,  5,  5,  5,  5,  0, -10],
            [-10,  0,  5,  0,  0,  0,  0, -10],
            [-20, -10, -10, -5, -5, -10, -10, -20]
        ]

        self.king_tableWHITE = [
            [-30, -40, -40, -50, -50, -40, -40, -30],
            [-30, -40, -40, -50, -50, -40, -40, -30],
            [-30, -40, -40, -50, -50, -40, -40, -30],
            [-30, -40, -40, -50, -50, -40, -40, -30],
            [-20, -30, -30, -40, -40, -30, -30, -20],
            [-10, -20, -20, -20, -20, -20, -20, -10],
            [20, 20,  0,  0,  0,  0, 20, 20],
            [20, 30, 10,  0,  0, 10, 30, 20]
        ]

        self.pawn_tableBLACK = []
        self.knight_tableBLACK = []
        self.bishop_tableBLACK = []
        self.rook_tableBLACK = []
        self.queen_tableBLACK = []
        self.king_tableBLACK = []

        self.initPieceSquareTables(self.pawn_tableWHITE, self.pawn_tableBLACK)
        self.initPieceSquareTables(self.knight_tableWHITE, self.knight_tableBLACK)
        self.initPieceSquareTables(self.bishop_tableWHITE, self.bishop_tableBLACK)
        self.initPieceSquareTables(self.rook_tableWHITE, self.rook_tableBLACK)
        self.initPieceSquareTables(self.queen_tableWHITE, self.queen_tableBLACK)
        self.initPieceSquareTables(self.king_tableWHITE, self.king_tableBLACK)


    def chunkify(self, lst : chess.LegalMoveGenerator, n):
        print(lst.count())
        lst = list(lst)  # Convert LegalMoveGenerator to list
        k, m = divmod(len(lst), n)
        return [lst[i * k + min(i, m):(i + 1) * k + min(i + 1, m)] for i in range(n)]

    def process_chunk(self, chunk, chessboard_fen, depth : int, result_queue : multiprocessing.Queue):
        """Processes a chunk of items and puts results into the shared dictionary."""
        chessboard = chess.Board(fen=chessboard_fen)
        turn = chessboard.turn
        transPositionTable = dict()
        value = None
        if turn == chess.WHITE :
            value = -math.inf
        else :
            value = math.inf
        bestMove = None
        for move in chunk :
            chessboard.push(move)
            result = self.minMax(chessboard, depth=depth, prevMove=None, alpha=-math.inf, beta=math.inf, transPositionTable=transPositionTable)
            if turn == chess.WHITE and result[1] > value:
                bestMove = (move, result[1])
            elif turn == chess.BLACK and result[1] < value :
                bestMove = (move, result[1])
            chessboard.pop()
        result_queue.put(bestMove)
        return 0

    def get_best_move(self, chessboard : chess.Board, depth : int):
        start = time.perf_counter()
        turn = chessboard.turn
        # transPositionTable = dict()
        i = 3
        N = 4
        print("Depth - time (s) - score - move")
        move_chunks = self.chunkify(chessboard.legal_moves, N)

        while True:
            result_queue = multiprocessing.Queue()
            processes = [] 
            for p_index in range(N):
                process = multiprocessing.Process(target=self.process_chunk, args=(move_chunks[p_index], chessboard.fen(), i, result_queue))
                processes.append(process)
                process.start()
            for process in processes :
                process.join()
            
            results = []
            for _ in range(result_queue.qsize()) :
                results.append(result_queue.get())    

            # # Create a manager for shared data
            # results = []
            # # with Manager() as manager:
            #     # transPositionTable = manager.dict()

            # with ProcessPoolExecutor(max_workers=N) as executor:
            #     # Submit each chunk to the process pool with the shared dictionary
            #     futures = [executor.submit(self.process_chunk, chunk, chessboard.fen(), i) for chunk in move_chunks]
                
            #     # Ensure all processes complete
            #     for future in futures:
            #         try:
                        
            #             results.append(future.result())
            #         except Exception as e:
            #             traceback.print_exc()
            #             exit(1)



            bestMove = None
            value = -math.inf if turn == chess.WHITE else math.inf
            for res in results :
                if turn == chess.WHITE and res[1] > value :
                    bestMove = res 
                if turn == chess.BLACK and res[1] < value :
                    bestMove = res
            # if (result[0] == None and (chess.Board.is_fifty_moves(chessboard) or chess.Board.is_repetition(chessboard) or chess.Board.is_stalemate(chessboard) or chess.Board.is_fivefold_repetition(chessboard))):
            if (bestMove == None) :
                print("AI did not find any non-losing move", chessboard.move_stack)
                # makeMove(chess.Move.from_uci(
                    # str(list(chessboard.legal_moves)[0])), window)
                return str(list(chessboard.legal_moves)[0])

            end = time.perf_counter()
            ms = (end-start)
            print(f"{i:2} {int(ms):8} {bestMove[1]:10}     {str(bestMove[0]):8}")
            if ms >= depth or len(list(chessboard.legal_moves)) == 0 or bestMove[1] == -math.inf or bestMove[1] == math.inf:
                break
            i = i + 1


        print("AB end prediciton:", bestMove[1])
        print("AI move", bestMove[0])
        print("Current value:", self.evaluationFunction(chessboard))
        # print("Time taken", int(ms))
        print("-"*30)
        return str(bestMove[0])


    def getSquare(self, event, lowest, highest):
        separation = (highest - lowest) / 8
        x = event.pos().x()
        y = event.pos().y()
        y_line = self.get_column_letter(x, lowest, highest, separation)
        x_line = self.get_row_number(y, lowest, highest, separation)
        return y_line + str(x_line)


    def get_column_letter(self, x, lowest, highest, separation):
        for i in range(0, 8):
            if x > lowest and x < highest and x > lowest and x < highest and x > lowest + separation * i and x < lowest + separation * (i + 1):
                return chr(97 + i)
        return ""


    def get_row_number(self, y, lowest, highest, separation):
        for i in range(0, 8):
            if y > lowest and y < highest and y > lowest and y < highest and y > lowest + separation * i and y < lowest + separation * (i+1):
                return 8-i
        return ""


    def evaluationFunction2(self, board: chess.Board):

        # if chess.Board.is_fifty_moves(board) or chess.Board.is_repetition(board) or chess.Board.is_stalemate(board) or chess.Board.is_fivefold_repetition(board):
        #     if board.turn == chess.BLACK:
        #         return -99
        #     elif board.turn == chess.WHITE:
        #         return 99

        # if len(list(board.legal_moves)) == 0:
        #     if board.turn == chess.BLACK:
        #         return -math.inf
        #     elif board.turn == chess.WHITE:
        #         return math.inf

        pawnDiff = self.calcPieceDiff(chess.PAWN, board)
        knightDiff = self.calcPieceDiff(chess.KNIGHT, board) * 3
        bishopDiff = self.calcPieceDiff(chess.BISHOP, board) * 3
        rookDiff = self.calcPieceDiff(chess.ROOK, board) * 5
        queenDiff = self.calcPieceDiff(chess.QUEEN, board) * 9
        return pawnDiff + knightDiff + bishopDiff + rookDiff + queenDiff


    def initPieceSquareTables(self, whiteTable, blackTable):
        row = 7
        for i in range(8):
            newRow = []
            for col in range(8):
                newRow.append(whiteTable[row][col])
            blackTable.append(newRow)
            row -= 1

    def printPieceSqTable(self, table):
        print(20*"-")
        for row in table:
            for col in row:
                print(f"{'{:3}'.format(col)},", end="")
            print()
        print(20*"-")

    def evaluationFunction(self, board: chess.Board):


        # Can be changed to board.is_checkmate() for efficienfy
        if board.is_checkmate() :
        # if board.legal_moves.count() == 0:
            if board.turn == chess.BLACK:
                return math.inf
            elif board.turn == chess.WHITE:
                return -math.inf

        if chess.Board.is_stalemate(board) or chess.Board.is_fifty_moves(board) or chess.Board.is_repetition(board) or chess.Board.is_fivefold_repetition(board):
            return 0

        whiteSum = 0
        blackSum = 0
        piece_map = board.piece_map()

        for square, piece in piece_map.items():
            row, col = divmod(square, 8)
            if piece.piece_type == chess.PAWN:
                if piece.color == chess.WHITE:
                    whiteSum += 100
                    whiteSum += self.pawn_tableWHITE[7 - row][col]
                else:
                    blackSum += 100
                    blackSum += self.pawn_tableBLACK[7 - row][col]

            elif piece.piece_type == chess.KNIGHT:
                if piece.color == chess.WHITE:
                    whiteSum += 320
                    whiteSum += self.knight_tableWHITE[7 - row][col]
                else:
                    blackSum += 320
                    blackSum += self.knight_tableBLACK[7 - row][col]

            elif piece.piece_type == chess.BISHOP:
                if piece.color == chess.WHITE:
                    whiteSum += 330
                    whiteSum += self.bishop_tableWHITE[7 - row][col]
                else:
                    blackSum += 330
                    blackSum += self.bishop_tableBLACK[7 - row][col]

            elif piece.piece_type == chess.ROOK:
                if piece.color == chess.WHITE:
                    whiteSum += 500
                    whiteSum += self.rook_tableWHITE[7 - row][col]
                else:
                    blackSum += 500
                    blackSum += self.rook_tableBLACK[7 - row][col]

            elif piece.piece_type == chess.QUEEN:
                if piece.color == chess.WHITE:
                    whiteSum += 900
                    whiteSum += self.queen_tableWHITE[7 - row][col]
                else:
                    blackSum += 900
                    blackSum += self.queen_tableBLACK[7 - row][col]

            elif piece.piece_type == chess.KING:
                if piece.color == chess.WHITE:
                    whiteSum += 20000
                    whiteSum += self.king_tableWHITE[7 - row][col]
                else:
                    blackSum += 20000
                    blackSum += self.king_tableBLACK[7 - row][col]

        return whiteSum - blackSum


    def calcPieceDiff(self, piece: chess.PieceType, board: chess.Board) -> int:
        return len(chess.Board.pieces(board, piece, chess.BLACK)) - len(chess.Board.pieces(board, piece, chess.WHITE))

    # Could add checks

    def sortMoveList(self, moves: chess.LegalMoveGenerator, board: chess.Board):
        attackers = []
        nonAttackers = []

        for move in moves:
            # Determine if the move is an attacking move
            if board.is_capture(move) or board.is_into_check(move):
                attackers.append(move)
            else:
                nonAttackers.append(move)
        
        return attackers, nonAttackers

    def minMax(self, board: chess.Board, depth: int, prevMove: chess.Move, alpha: int, beta: int, transPositionTable: dict):

        result = self.sortMoveList(board.legal_moves, board)
        attackMoves = result[0]
        noAttackMoves = result[1]

        sortedMoves: chess.Move = []

        # # Quiescence continuation
        # if depth == 0 and len(attackMoves) != 0:
        #     sortedMoves = attackMoves + noAttackMoves
        # # Quiescence base case
        # elif depth == -1:
        #     value = (prevMove, evaluationFunction(board=board), -1)
        #     return value
        # Regular base case
        if depth == 0 or len(attackMoves) + len(noAttackMoves) == 0 or chess.Board.is_fifty_moves(board) or chess.Board.is_repetition(board):
            value = (prevMove, self.evaluationFunction(board=board), depth)
            return value

        # Regular continuation
        elif depth != 0 and len(attackMoves) + len(noAttackMoves) != 0:
            # Makes the algorithm pick attacks first
            sortedMoves = attackMoves + noAttackMoves
        else:
            print("WTF")
            exit()

        # MAX
        if board.turn == chess.WHITE:
            value = -math.inf
            bestMov = prevMove
            maxDepth = -math.inf

            for mov in sortedMoves:
                mov: chess.Move
                board.push(mov)
                ret = self.minMax(board=board, depth=depth-1, prevMove=mov,
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
        elif board.turn == chess.BLACK:
            value = math.inf
            bestMov = prevMove
            maxDepth = -math.inf
            for mov in sortedMoves:
                mov: chess.Move
                board.push(mov)
                ret = self.minMax(board=board, depth=depth-1, prevMove=mov,
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

    
    ai = ChessAI()
    cProfile.run('ai.get_best_move(chess.Board(fen=sys.argv[1]), int(sys.argv[2]))', filename='profile_results.prof')
    # Load the profiling results
    stats = pstats.Stats('profile_results.prof')

    # Sort the results by a specific metric, e.g., cumulative time
    stats.sort_stats('time')

    # Print the top 10 functions by cumulative time
    stats.print_stats(10)

    # app = QApplication([])
    # AIstart = False
    # if sys.argv[1] == "white":
    #     AIstart = True
    # window = MainWindow(AIStart=AIstart)
    # # window = MainWindow(AIStart=AIstart, depth=1)

    # # window.AIsTurn= sys.argv[1]
    # window.show()
    # app.exec()

    # # # chessboard = chess.Board(fen="1P6/3P4/8/3n4/8/8/3p4/8 w - - 0 1")
    # chessboard = chess.Board()
    # print(evaluationFunction(chessboard))
