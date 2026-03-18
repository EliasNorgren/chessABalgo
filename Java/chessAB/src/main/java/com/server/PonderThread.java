package com.server;

import com.github.bhlangonijr.chesslib.Board;
import com.server.chessAI.BestTurnInformation;
import com.server.chessAI.BoardWrapper;
import com.server.chessAI.ChessAI;

import java.util.List;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.Semaphore;
import java.util.function.Function;

public class PonderThread extends Thread{

    private final Board board;
    private final ChessAI ai = new ChessAI();
    Runnable exitRunner;


    public Semaphore getSemaphore() {
        return semaphore;
    }

    private final Semaphore semaphore = new Semaphore(0);

    public PonderThread(Board board){
        this.board = board;
    }

    @Override
    public void run() {
        super.run();
        System.out.println("Starting Ponder thread with moveStack: " + board.getFen());
        BoardWrapper boardWrapper = new BoardWrapper(board);
//        ai.resultFuture = this.resultFuture;
        ai.semaphore = this.semaphore;
        ai.getBestMove(boardWrapper, 60, true);
        System.out.println("Ponder thread exiting " + this.threadId());
        exitRunner.run();
    }

    public void stopThread() {
        ai.ponderThreadShouldRun = false;
    }

    public BestTurnInformation getBestMoveInfo() throws CloneNotSupportedException {
        ai.ponderInfoLock.writeLock().lock();
        BestTurnInformation copy = ai.ponderInfo.clone();
        ai.ponderInfoLock.writeLock().unlock();
        return copy;
    }

}
