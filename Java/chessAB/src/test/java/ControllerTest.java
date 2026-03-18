import com.server.Controller;
import com.server.GetBestMoveRequest;
import com.server.GetBestMoveResponse;
import com.server.PonderThread;
import com.server.chessAI.BestTurnInformation;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.TimeoutException;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.mockito.Mockito.*;

public class ControllerTest {

    private Controller controller;

    @BeforeEach
    void setup() {
        controller = new Controller();
    }

    @Test
    void expectedPonderMove() throws Exception {
        // Arrange
        GetBestMoveRequest request = new GetBestMoveRequest();
        request.setMax_time("1");
        List<String> moveStack;
        moveStack = Arrays.asList("e4", "e5");
        request.setMoveStack(moveStack);

        // Act
        GetBestMoveResponse response = controller.getBestMove(request);
        String expectedMove = response.getPonderMove();
        System.out.println("PonderMove " + expectedMove);
        Thread.sleep(300);
        List<String> newStack = new ArrayList<>(moveStack);
        newStack.add(response.getBestMove());
        newStack.add(expectedMove);
        request.setMoveStack(newStack);
        controller.getBestMove(request);
        Thread.sleep(5000);
    }

    @Test
    void notExpectedMove() throws ExecutionException, InterruptedException, CloneNotSupportedException, TimeoutException {
        // Arrange
        GetBestMoveRequest request = new GetBestMoveRequest();
        request.setMax_time("1");
        List<String> moveStack;
        moveStack = Arrays.asList("e4", "e5");
        request.setMoveStack(moveStack);

        // Act
        GetBestMoveResponse response = controller.getBestMove(request);
        Thread.sleep(300);
        List<String> newStack = new ArrayList<>(moveStack);
        newStack.add(response.getBestMove());
        newStack.add("a2");
        request.setMoveStack(newStack);
        controller.getBestMove(request);

        Thread.sleep(5000);
    }
//
//    @Test
//    void testMatchingThread_reusesPonderResult() throws Exception {
//        // Arrange
//        GetBestMoveRequest request = new GetBestMoveRequest();
//        request.setMax_time("100");
//        request.setMoveStack(Arrays.asList("e4", "e5"));
//
//        int hash = request.getMoveStack().hashCode();
//
//        PonderThread mockThread = mock(PonderThread.class);
//        BestTurnInformation info = new BestTurnInformation();
//        info.timeElapsed = 200; // already enough
//        info.bestMove = mock(BestTurnInformation.BestMove.class);
//
//        when(mockThread.getBestMoveInfo()).thenReturn(info);
//
//        controller.threadMap.put(hash, mockThread);
//
//        // Act
//        GetBestMoveResponse response = controller.getBestMove(request);
//
//        // Assert
//        verify(mockThread).stopThread();
//        assertNotNull(response);
//    }
//
//    @Test
//    void testNewPonderThread_isAddedToMap() throws Exception {
//        // Arrange
//        GetBestMoveRequest request = new GetBestMoveRequest();
//        request.setMax_time("100");
//        request.setMoveStack(Arrays.asList("e4", "e5"));
//
//        // Act
//        controller.getBestMove(request);
//
//        // Assert
//        assertFalse(controller.threadMap.isEmpty());
//    }
//
//    @Test
//    void testExitRunner_removesThreadFromMap() {
//        // Arrange
//        ConcurrentHashMap<Integer, PonderThread> map = new ConcurrentHashMap<>();
//
//        int hash = 42;
//        PonderThread thread = new PonderThread();
//        thread.exitRunner = () -> map.remove(hash, thread);
//
//        map.put(hash, thread);
//
//        // Act
//        thread.exitRunner.run();
//
//        // Assert
//        assertFalse(map.containsKey(hash));
//    }
}
