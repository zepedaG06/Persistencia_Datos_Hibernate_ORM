package ni.edu.uam.administracionfx.util;

import javafx.concurrent.Task;

import java.util.concurrent.Callable;
import java.util.function.Consumer;

/**
 * Ejecuta trabajo de base de datos fuera del hilo de JavaFX para que la
 * ventana no se congele, y devuelve el resultado al hilo de la interfaz.
 */
public final class Async {

    private Async() {
    }

    public static <T> void run(Callable<T> trabajo, Consumer<T> alTerminar, Consumer<Throwable> alFallar) {
        Task<T> task = new Task<>() {
            @Override
            protected T call() throws Exception {
                return trabajo.call();
            }
        };
        task.setOnSucceeded(e -> alTerminar.accept(task.getValue()));
        task.setOnFailed(e -> alFallar.accept(task.getException()));
        Thread hilo = new Thread(task, "db-worker");
        hilo.setDaemon(true);
        hilo.start();
    }
}
