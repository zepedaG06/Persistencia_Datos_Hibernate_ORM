package ni.edu.uam.administracionfx.controller;

import javafx.fxml.FXML;
import javafx.scene.control.Label;
import javafx.scene.control.PasswordField;
import javafx.scene.control.TextField;
import ni.edu.uam.administracionfx.model.Usuario;
import ni.edu.uam.administracionfx.service.ServiceUsuario;
import ni.edu.uam.administracionfx.util.Alertas;

/** Controlador del formulario de alta / edición que se muestra en un diálogo. */
public class UsuarioFormController {

    @FXML private Label lblSubtitulo;
    @FXML private TextField txtPrimerNombre;
    @FXML private TextField txtSegundoNombre;
    @FXML private TextField txtPrimerApellido;
    @FXML private TextField txtSegundoApellido;
    @FXML private TextField txtUsuario;
    @FXML private TextField txtCorreo;
    @FXML private Label lblPassword;
    @FXML private PasswordField txtPassword;
    @FXML private Label lblError;

    private final ServiceUsuario service = new ServiceUsuario();
    private Usuario original;

    @FXML
    private void initialize() {
        modoNuevo();
    }

    public void modoNuevo() {
        original = null;
        lblSubtitulo.setText("Completa los datos del nuevo usuario.");
        lblPassword.setText("Contraseña *");
        txtPassword.setPromptText("Mínimo " + ServiceUsuario.MIN_PASSWORD + " caracteres");
    }

    public void modoEditar(Usuario u) {
        original = u;
        lblSubtitulo.setText("Modifica los datos de " + u.getNombreCompleto() + ".");
        txtPrimerNombre.setText(u.getPrimerNombre());
        txtSegundoNombre.setText(u.getSegundoNombre());
        txtPrimerApellido.setText(u.getPrimerApellido());
        txtSegundoApellido.setText(u.getSegundoApellido());
        txtUsuario.setText(u.getUsuario());
        txtCorreo.setText(u.getCorreo());
        lblPassword.setText("Nueva contraseña");
        txtPassword.setPromptText("Déjala vacía para conservar la actual");
    }

    public void enfocar() {
        txtPrimerNombre.requestFocus();
    }

    /**
     * Valida y guarda. Devuelve el usuario guardado, o {@code null} si hubo un
     * error (que se muestra dentro del mismo formulario).
     */
    public Usuario guardar() {
        ocultarError();
        Usuario u = new Usuario();
        if (original != null) {
            u.setId(original.getId());
            u.setRutaFoto(original.getRutaFoto());
            u.setFechaRegistro(original.getFechaRegistro());
        }
        u.setPrimerNombre(txtPrimerNombre.getText());
        u.setSegundoNombre(txtSegundoNombre.getText());
        u.setPrimerApellido(txtPrimerApellido.getText());
        u.setSegundoApellido(txtSegundoApellido.getText());
        u.setUsuario(txtUsuario.getText());
        u.setCorreo(txtCorreo.getText());
        try {
            return original == null
                    ? service.crear(u, txtPassword.getText())
                    : service.actualizar(u, txtPassword.getText());
        } catch (Exception ex) {
            lblError.setText(Alertas.mensajeDe(ex));
            lblError.setVisible(true);
            lblError.setManaged(true);
            lblError.getScene().getWindow().sizeToScene();
            return null;
        }
    }

    private void ocultarError() {
        lblError.setVisible(false);
        lblError.setManaged(false);
    }
}
