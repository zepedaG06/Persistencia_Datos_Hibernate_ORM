package ni.edu.uam.administracionfx.util;

import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.stage.Stage;
import ni.edu.uam.administracionfx.MainApplication;

import java.io.IOException;
import java.io.UncheckedIOException;
import java.net.URL;

/** Cambia entre pantallas reutilizando la misma ventana. */
public final class Navegador {

    public static final String CSS = "css/estilos.css";
    private static final String TITULO_APP = "Administración UAM";

    private static Stage stage;

    private Navegador() {
    }

    public static void init(Stage principal) {
        stage = principal;
    }

    public static Stage getStage() {
        return stage;
    }

    public static <C> C ir(String fxml, String titulo) {
        FXMLLoader loader = loader(fxml);
        Parent root;
        try {
            root = loader.load();
        } catch (IOException e) {
            throw new UncheckedIOException("No se pudo cargar la vista " + fxml, e);
        }
        Scene scene = stage.getScene();
        if (scene == null) {
            scene = new Scene(root, 1100, 700);
            aplicarEstilos(scene);
            stage.setScene(scene);
        } else {
            scene.setRoot(root);
        }
        stage.setTitle(TITULO_APP + " · " + titulo);
        return loader.getController();
    }

    public static FXMLLoader loader(String fxml) {
        URL url = MainApplication.class.getResource("view/" + fxml);
        if (url == null) {
            throw new IllegalArgumentException("No existe la vista view/" + fxml);
        }
        return new FXMLLoader(url);
    }

    public static void aplicarEstilos(Scene scene) {
        scene.getStylesheets().add(MainApplication.class.getResource(CSS).toExternalForm());
    }

    public static String cssUrl() {
        return MainApplication.class.getResource(CSS).toExternalForm();
    }
}
