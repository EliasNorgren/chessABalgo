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
