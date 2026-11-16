package ni.edu.uam.administracionfx.util;

import javafx.scene.control.Alert;
import javafx.scene.control.ButtonBar;
import javafx.scene.control.ButtonType;
import javafx.scene.control.DialogPane;
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
        t.printStackTrace();
        String causa = causaRaiz(t);
        String c = causa.toLowerCase();

        if (c.contains("connection refused") || c.contains("connection to localhost")
                || c.contains("connectexception") || c.contains("connection attempt failed")) {
            return "PostgreSQL no está encendido o no escucha en localhost:5432. "
                    + "Inicia el servicio de PostgreSQL y vuelve a intentar.";
        }
        if (c.contains("password authentication failed") || c.contains("autentificación password falló")
                || c.contains("autenticación password falló") || c.contains("no password was provided")) {
            return "PostgreSQL rechazó el usuario o la contraseña. "
                    + "Corrige jdbc.user y jdbc.password en persistence.xml.";
        }
        if (c.contains("database") && (c.contains("does not exist") || c.contains("no existe"))
                || c.contains("la base de datos") && c.contains("no existe")) {
            return "La base de datos no existe en PostgreSQL. Créala con:  CREATE DATABASE estudiante;\n\n"
                    + "Detalle: " + causa;
        }
        if (c.contains("no persistence provider") || c.contains("no suitable driver")) {
            return "Falta Hibernate o el driver de PostgreSQL al ejecutar. "
                    + "Recarga el proyecto Maven y ejecútalo con «mvn javafx:run».\n\nDetalle: " + causa;
        }
        return "Error con la base de datos:\n" + causa;
    }

    /** Mensaje de la excepción más profunda (la causa real). */
    private static String causaRaiz(Throwable t) {
        Throwable raiz = t;
        while (raiz.getCause() != null && raiz.getCause() != raiz) {
            raiz = raiz.getCause();
        }
        String msg = raiz.getMessage();
        return raiz.getClass().getSimpleName() + (msg == null ? "" : ": " + msg.trim());
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
