package ni.edu.uam.administracionfx;

import javafx.application.Application;
import javafx.stage.Stage;
import ni.edu.uam.administracionfx.dao.EntityManagerAdmin;
import ni.edu.uam.administracionfx.util.Navegador;

public class MainApplication extends Application {

    @Override
    public void start(Stage stage) {
        Navegador.init(stage);
        stage.setMinWidth(900);
        stage.setMinHeight(620);
        Navegador.ir("login-view.fxml", "Iniciar sesión");
        stage.show();
    }

    @Override
    public void stop() {
        try {
            EntityManagerAdmin.close();
        } catch (Throwable ignored) {
            // Si la base nunca se conectó no hay nada que cerrar.
        }
    }

    public static void main(String[] args) {
        launch();
    }
}
