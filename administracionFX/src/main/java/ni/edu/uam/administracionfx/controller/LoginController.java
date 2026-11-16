package ni.edu.uam.administracionfx.controller;

import javafx.application.Platform;
import javafx.fxml.FXML;
import javafx.scene.control.Button;
import javafx.scene.control.CheckBox;
import javafx.scene.control.Label;
import javafx.scene.control.PasswordField;
import javafx.scene.control.ProgressIndicator;
import javafx.scene.control.TextField;
import ni.edu.uam.administracionfx.service.ServiceUsuario;
import ni.edu.uam.administracionfx.util.Alertas;
import ni.edu.uam.administracionfx.util.Async;
import ni.edu.uam.administracionfx.util.Navegador;
import ni.edu.uam.administracionfx.util.Sesion;

public class LoginController {

    @FXML private TextField txtLogin;
    @FXML private PasswordField txtPassword;
    @FXML private TextField txtPasswordVisible;
    @FXML private CheckBox chkMostrar;
    @FXML private Label lblError;
    @FXML private Button btnEntrar;
    @FXML private ProgressIndicator spinner;

    private final ServiceUsuario service = new ServiceUsuario();

    @FXML
    private void initialize() {
        txtPasswordVisible.textProperty().bindBidirectional(txtPassword.textProperty());
        txtPasswordVisible.visibleProperty().bind(chkMostrar.selectedProperty());
        txtPassword.visibleProperty().bind(chkMostrar.selectedProperty().not());

        txtLogin.textProperty().addListener((o, a, b) -> ocultarError());
        txtPassword.textProperty().addListener((o, a, b) -> ocultarError());

        Platform.runLater(txtLogin::requestFocus);
    }

    /** Permite precargar el usuario recién registrado. */
    public void setLogin(String login) {
        txtLogin.setText(login);
        Platform.runLater(txtPassword::requestFocus);
    }

    @FXML
    private void onEntrar() {
        String login = txtLogin.getText();
        String password = txtPassword.getText();
        cargando(true);
        Async.run(
                () -> service.autenticar(login, password),
                usuario -> {
                    cargando(false);
                    Sesion.iniciar(usuario);
                    Navegador.ir("dashboard-view.fxml", "Usuarios");
                },
                error -> {
                    cargando(false);
                    txtPassword.clear(); // primero limpiar: el listener oculta el error
                    mostrarError(Alertas.mensajeDe(error));
                    txtPassword.requestFocus();
                });
    }

    @FXML
    private void onIrARegistro() {
        Navegador.ir("registro-view.fxml", "Crear cuenta");
    }

    private void cargando(boolean activo) {
        btnEntrar.setDisable(activo);
        btnEntrar.setText(activo ? "" : "Iniciar sesión");
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
