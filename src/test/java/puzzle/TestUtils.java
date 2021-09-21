package puzzle;

import java.io.IOException;
import java.net.ServerSocket;
import java.util.concurrent.Callable;
import org.junit.*;

/**
 *
 * @author Hamado Dene
 */
public class TestUtils {

    public static void waitForCondition(Callable<Boolean> condition, int seconds) throws Exception {
        waitForCondition(condition, null, seconds);
    }

    public static void waitForCondition(Callable<Boolean> condition, Callable<Void> callback, int seconds) throws Exception {
        try {
            long _start = System.currentTimeMillis();
            long millis = seconds * 1000;
            while (System.currentTimeMillis() - _start <= millis) {
                if (condition.call()) {
                    return;
                }
                if (callback != null) {
                    callback.call();
                }
                Thread.sleep(100);
            }
        } catch (InterruptedException ee) {
            Assert.fail("test interrupted!");

            return;
        } catch (Exception ee) {
            Assert.fail("error while evalutaing condition:" + ee);
            return;
        }
        Assert.fail("condition not met in time!");
    }

    public static int getRandomPort() {
        try (ServerSocket serverSocket = new ServerSocket(0)) {
            return serverSocket.getLocalPort();
        } catch (IOException e) {
            Assert.fail("Port is not available");
            return -1;
        }
    }

    public static boolean isAvailable(int portNr) {
        boolean portFree;
        try (var ignored = new ServerSocket(portNr)) {
            portFree = true;
        } catch (IOException e) {
            portFree = false;
        }
        return portFree;
    }
}
