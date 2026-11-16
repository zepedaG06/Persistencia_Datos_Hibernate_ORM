package ni.edu.uam.administracionfx.util;

import javafx.scene.control.Alert;
import javafx.scene.control.ButtonBar;
import javafx.scene.control.ButtonType;
import javafx.scene.control.DialogPane;
import ni.edu.uam.administracionfx.dao.DaoException;
import ni.edu.uam.administracionfx.service.ValidacionException;

import java.util.Optional;

/** Diálogos con el mismo estilo que el resto de la aplicación. */
public final class Alertas {

    private Alertas() {
    }

    public static void info(String titulo, String mensaje) {
        crear(Alert.AlertType.INFORMATION, titulo, mensaje).showAndWait();
    }

    public static void error(String titulo, String mensaje) {
        crear(Alert.AlertType.ERROR, titulo, mensaje).showAndWait();
    }

    public static boolean confirmar(String titulo, String mensaje, String textoAceptar) {
        Alert alert = crear(Alert.AlertType.CONFIRMATION, titulo, mensaje);
        ButtonType aceptar = new ButtonType(textoAceptar, ButtonBar.ButtonData.OK_DONE);
        ButtonType cancelar = new ButtonType("Cancelar", ButtonBar.ButtonData.CANCEL_CLOSE);
        alert.getButtonTypes().setAll(cancelar, aceptar);
        alert.getDialogPane().lookupButton(aceptar).getStyleClass().add("btn-danger");
        Optional<ButtonType> r = alert.showAndWait();
        return r.isPresent() && r.get() == aceptar;
    }

    /** Convierte una excepción en un mensaje entendible para el usuario. */
    public static String mensajeDe(Throwable t) {
        if (t instanceof ValidacionException) {
            return t.getMessage();
        }
        if (t instanceof DaoException || t instanceof ExceptionInInitializerError
                || (t.getCause() != null && t.getCause() instanceof DaoException)) {
            t.printStackTrace();
            return "No se pudo conectar con la base de datos. "
                    + "Verifica que PostgreSQL esté encendido y los datos de persistence.xml.";
        }
        t.printStackTrace();
        return "Ocurrió un error inesperado: " + t.getMessage();
    }

    private static Alert crear(Alert.AlertType tipo, String titulo, String mensaje) {
        Alert alert = new Alert(tipo);
        alert.initOwner(Navegador.getStage());
        alert.setTitle(titulo);
        alert.setHeaderText(titulo);
        alert.setContentText(mensaje);
        DialogPane pane = alert.getDialogPane();
        pane.getStylesheets().add(Navegador.cssUrl());
        pane.getStyleClass().add("dialogo");
        return alert;
    }
}
