package view;

import java.util.concurrent.CountDownLatch;
import java.util.concurrent.FutureTask;
import java.util.concurrent.TimeUnit;
import javafx.application.Platform;

final class FxTestSupport {
    private static boolean started;

    private FxTestSupport() {
    }

    static synchronized void start() throws Exception {
        if (started) {
            return;
        }
        CountDownLatch ready = new CountDownLatch(1);
        Platform.startup(() -> {
            Platform.setImplicitExit(false);
            ready.countDown();
        });
        if (!ready.await(10, TimeUnit.SECONDS)) {
            throw new AssertionError("JavaFX startup timed out");
        }
        started = true;
    }

    static void onFx(Runnable action) throws Exception {
        FutureTask<Void> task = new FutureTask<>(action, null);
        Platform.runLater(task);
        task.get(15, TimeUnit.SECONDS);
    }
}
