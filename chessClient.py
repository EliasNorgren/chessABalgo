import chess
import chess.svg
import random
import math
import os
import sys
import timeit
import socket


def AIMove(chessboard: chess.Board, depth):
    print("Thinking")

    result = minMax(chessboard, depth, None, alpha=-1000, beta=1000)
    if (result[0] == None):
        print("AI did not find move ", chessboard.move_stack)
        return
        
    
    print("AB end prediciton:", result[1])
    print("AI move", result[0])
    print("Current value:", evaluationFunction(chessboard))
    print("-"*30)
    return result




def makeMove(move: chess.Move, chessboard: chess.Board):
    chessboard.push(chess.Move(from_square=move.from_square, to_square=move.to_square))



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
    if depth == -1:
        value = (prevMove, evaluationFunction(board=board))
        if (value == 1000):
            print("is 1000 1")
        return value
    # Regular base case
    elif depth == 0 or len(attackMoves) + len(noAttackMoves) == 0 or chess.Board.is_fifty_moves(board) or chess.Board.is_repetition(board):
        value = (prevMove, evaluationFunction(board=board))
        if (value == 1000):
            print("is 1000 1")
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
        for mov in sortedMoves:
            board.push(mov)
            currentVal = minMax(board=board, depth=depth-1,
                                prevMove=mov, alpha=alpha, beta=beta)[1]
            board.pop()
            if currentVal > value:
                value = currentVal
                bestMov = mov
            alpha = max(alpha, value)
            if value >= beta:
                break
        return (bestMov, value)
    # MIN
    elif board.turn == chess.WHITE:
        value = 999
        bestMov = prevMove
        for mov in sortedMoves:
            board.push(mov)
            currentVal = minMax(board=board, depth=depth-1,
                                prevMove=mov, alpha=alpha, beta=beta)[1]
            board.pop()
            if currentVal < value:
                value = currentVal
                bestMov = mov
            beta = min(beta, value)
            if currentVal <= alpha:
                break

        return (bestMov, value)


def legalMove(board: chess.Board, move: str):
    found = False
    for m in list(board.legal_moves):
        mov_con = str(chess.Move.from_uci(str(m)))
        if move == mov_con:
            found = True
            break
    return found


if __name__ == "__main__":
    AIstart = False
    if sys.argv[1] == "white":
        AIstart = True

    TCP_IP = '85.224.76.133'
    TCP_PORT = 3030
    BUFFER_SIZE = 1024
    msg = "Hello, World!"

    s = socket.socket(socket.AF_INET, socket.SOCK_STREAM)
    s.connect((TCP_IP, TCP_PORT))
    print("Connected")

    board = chess.Board()
    AITurn = False
    while True:
        if AITurn :
            result = AIMove(board = board, depth=sys.argv[2])
            print("Sending", result[0])
            s.send(result[0])
        else :
            received = s.recv(BUFFER_SIZE)
            print("Got" + received)
            makeMove(move=str(chess.Move.from_uci(str(received))),board=board)


        if result == None:
            break
    