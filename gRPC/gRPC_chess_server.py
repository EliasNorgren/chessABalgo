import sys
import os

# Add the parent directory to the Python path
sys.path.append("/home/elias/chess.com-stockfish/chessABalgo")
sys.path.append("..")


print(os.path.dirname(os.path.dirname(os.path.abspath(__file__))))

from concurrent import futures
import grpc
import chess
import chess.engine
import chess_pb2
import chess_pb2_grpc
from chessABalgo.chessAI import ChessAI
import math
import traceback

class ChessEngineServicer(chess_pb2_grpc.ChessEngineServicer):

    def __init__(self) -> None:
        super().__init__()
        self.ai = ChessAI()

    def GetBestMove(self, request, context):
        try :
            print(request)
            fen = request.fen
            move_stack = request.move_stack
            depth = request.depth
            processes = request.processes
            # Create a chess board from the FEN string
            board = chess.Board(fen)

            # Apply the move stack to the board
            for move in move_stack:
                board.push(chess.Move.from_uci(move))


            # Call your chess engine with the board, move stack, and depth
            res = self.ai.get_best_move(board, depth)
            eval_value = res[2]
            # # Convert evaluation value to int, handling infinity separately
            # if math.isinf(eval_value):
            #     eval_int = float('inf') if eval_value > 0 else float('-inf')
            # else:
            #     eval_int = int(eval_value)
            response = chess_pb2.GetBestMoveResponse()
            response.best_move = res[0]
            response.time_taken = res[1]
            response.eval = eval_value
        except Exception :
            traceback.print_exc()
            exit(1)
        return response


def serve():
    server = grpc.server(futures.ThreadPoolExecutor(max_workers=10))
    chess_pb2_grpc.add_ChessEngineServicer_to_server(ChessEngineServicer(), server)
    server.add_insecure_port('[::]:50051')
    server.start()
    print("Server started, listening on port 50051")
    server.wait_for_termination()

if __name__ == '__main__':
    serve()
