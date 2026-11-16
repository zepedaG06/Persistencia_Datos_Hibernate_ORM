package ni.edu.uam.administracionfx.controller;

import javafx.application.Platform;
import javafx.fxml.FXML;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.PasswordField;
import javafx.scene.control.ProgressIndicator;
import javafx.scene.control.TextField;
import ni.edu.uam.administracionfx.model.Usuario;
import ni.edu.uam.administracionfx.service.ServiceUsuario;
import ni.edu.uam.administracionfx.util.Alertas;
import ni.edu.uam.administracionfx.util.Async;
import ni.edu.uam.administracionfx.util.Navegador;

public class RegistroController {

    @FXML private TextField txtPrimerNombre;
    @FXML private TextField txtSegundoNombre;
    @FXML private TextField txtPrimerApellido;
    @FXML private TextField txtSegundoApellido;
    @FXML private TextField txtUsuario;
    @FXML private TextField txtCorreo;
    @FXML private PasswordField txtPassword;
    @FXML private PasswordField txtConfirmar;
    @FXML private Label lblFortaleza;
    @FXML private Label lblError;
    @FXML private Button btnRegistrar;
    @FXML private ProgressIndicator spinner;

    private final ServiceUsuario service = new ServiceUsuario();

    @FXML
    private void initialize() {
        txtPassword.textProperty().addListener((o, a, b) -> actualizarFortaleza(b));
        actualizarFortaleza("");
        Platform.runLater(txtPrimerNombre::requestFocus);
    }

    @FXML
    private void onRegistrar() {
        ocultarError();
        Usuario u = new Usuario();
        u.setPrimerNombre(txtPrimerNombre.getText());
        u.setSegundoNombre(txtSegundoNombre.getText());
        u.setPrimerApellido(txtPrimerApellido.getText());
        u.setSegundoApellido(txtSegundoApellido.getText());
        u.setUsuario(txtUsuario.getText());
        u.setCorreo(txtCorreo.getText());
        String password = txtPassword.getText();
        String confirmar = txtConfirmar.getText();

        cargando(true);
        Async.run(
                () -> service.registrar(u, password, confirmar),
                creado -> {
                    cargando(false);
                    Alertas.info("¡Cuenta creada!",
                            "Tu cuenta «" + creado.getUsuario() + "» se registró correctamente. "
                                    + "Ahora puedes iniciar sesión.");
                    LoginController login = Navegador.ir("login-view.fxml", "Iniciar sesión");
                    login.setLogin(creado.getUsuario());
                },
                error -> {
                    cargando(false);
                    mostrarError(Alertas.mensajeDe(error));
                });
    }

    @FXML
    private void onIrALogin() {
        Navegador.ir("login-view.fxml", "Iniciar sesión");
    }

    /** Indicador simple de qué tan segura es la contraseña. */
    private void actualizarFortaleza(String p) {
        lblFortaleza.getStyleClass().removeAll("hint-weak", "hint-ok", "hint-strong");
        if (p == null || p.isEmpty()) {
            lblFortaleza.setText("Usa al menos " + ServiceUsuario.MIN_PASSWORD
                    + " caracteres; mezcla mayúsculas, números y símbolos.");
            return;
        }
        int puntos = 0;
        if (p.length() >= ServiceUsuario.MIN_PASSWORD) puntos++;
        if (p.length() >= 10) puntos++;
        if (p.matches(".*[A-Z].*") && p.matches(".*[a-z].*")) puntos++;
        if (p.matches(".*\\d.*")) puntos++;
        if (p.matches(".*[^A-Za-z0-9].*")) puntos++;

        if (p.length() < ServiceUsuario.MIN_PASSWORD || puntos <= 2) {
            lblFortaleza.setText("Seguridad: débil");
            lblFortaleza.getStyleClass().add("hint-weak");
        } else if (puntos <= 3) {
            lblFortaleza.setText("Seguridad: aceptable");
            lblFortaleza.getStyleClass().add("hint-ok");
        } else {
            lblFortaleza.setText("Seguridad: fuerte");
            lblFortaleza.getStyleClass().add("hint-strong");
        }
    }

    private void cargando(boolean activo) {
        btnRegistrar.setDisable(activo);
        btnRegistrar.setText(activo ? "" : "Crear cuenta");
        spinner.setVisible(activo);
    }

    private void mostrarError(String mensaje) {
        lblError.setText(mensaje);
        lblError.setVisible(true);
        lblError.setManaged(true);
    }

    private void ocultarError() {
        lblError.setVisible(false);
        lblError.setManaged(false);
    }
}
