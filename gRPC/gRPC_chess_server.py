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


class ChessEngineServicer(chess_pb2_grpc.ChessEngineServicer):

    def __init__(self) -> None:
        super().__init__()
        self.ai = ChessAI()

    def GetBestMove(self, request, context):
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
        best_move = self.ai.get_best_move(board, depth)

        return chess_pb2.GetBestMoveResponse(best_move=best_move)

    def get_best_move(self, board, depth):
        # Implement your chess engine here
        # This is a placeholder implementation
        # Replace it with your actual chess engine logic
        with chess.engine.SimpleEngine.popen_uci("/path/to/your/uci/engine") as engine:
            result = engine.play(board, chess.engine.Limit(depth=depth))
            best_move = result.move.uci()
        return best_move

def serve():
    server = grpc.server(futures.ThreadPoolExecutor(max_workers=10))
    chess_pb2_grpc.add_ChessEngineServicer_to_server(ChessEngineServicer(), server)
    server.add_insecure_port('[::]:50051')
    server.start()
    print("Server started, listening on port 50051")
    server.wait_for_termination()

if __name__ == '__main__':
    serve()