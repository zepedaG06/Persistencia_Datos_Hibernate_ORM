package ni.edu.uam.administracionfx.controller;

import javafx.application.Platform;
import javafx.beans.property.SimpleStringProperty;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.collections.transformation.FilteredList;
import javafx.collections.transformation.SortedList;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Node;
import javafx.scene.control.Button;
import javafx.scene.control.ButtonBar;
import javafx.scene.control.ButtonType;
import javafx.scene.control.Dialog;
import javafx.scene.control.DialogPane;
import javafx.scene.control.Label;
import javafx.scene.control.TableCell;
import javafx.scene.control.TableColumn;
import javafx.scene.control.TableRow;
import javafx.scene.control.TableView;
import javafx.scene.control.TextField;
import javafx.scene.layout.HBox;
import javafx.scene.layout.Region;
import javafx.scene.layout.VBox;
import javafx.geometry.Pos;
import ni.edu.uam.administracionfx.model.Usuario;
import ni.edu.uam.administracionfx.service.ServiceUsuario;
import ni.edu.uam.administracionfx.util.Alertas;
import ni.edu.uam.administracionfx.util.Async;
import ni.edu.uam.administracionfx.util.Navegador;
import ni.edu.uam.administracionfx.util.Sesion;

import java.io.IOException;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.List;
import java.util.Locale;
import java.util.Optional;

public class DashboardController {

    private static final DateTimeFormatter FECHA = DateTimeFormatter.ofPattern("dd/MM/yyyy");

    @FXML private Label lblAvatar;
    @FXML private Label lblNombreSesion;
    @FXML private Label lblUsuarioSesion;
    @FXML private Label lblBienvenida;
    @FXML private Label lblTotal;
    @FXML private Label lblMes;
    @FXML private Label lblMostrando;
    @FXML private TextField txtBuscar;
    @FXML private Button btnEditar;
    @FXML private Button btnEliminar;
    @FXML private TableView<Usuario> tabla;
    @FXML private TableColumn<Usuario, String> colNombre;
    @FXML private TableColumn<Usuario, String> colUsuario;
    @FXML private TableColumn<Usuario, String> colCorreo;
    @FXML private TableColumn<Usuario, String> colFecha;
    @FXML private Label lblEstado;

    private final ServiceUsuario service = new ServiceUsuario();
    private final ObservableList<Usuario> usuarios = FXCollections.observableArrayList();
    private FilteredList<Usuario> filtrados;

    @FXML
    private void initialize() {
        if (!Sesion.activa()) {
            Platform.runLater(() -> Navegador.ir("login-view.fxml", "Iniciar sesión"));
            return;
        }
        mostrarSesion(Sesion.getUsuario());
        configurarTabla();
        cargar();
    }

    // ----------------------------------------------------------------- UI setup

    private void mostrarSesion(Usuario u) {
        lblAvatar.setText(u.getIniciales());
        lblNombreSesion.setText(u.getPrimerNombre() + " " + u.getPrimerApellido());
        lblUsuarioSesion.setText("@" + u.getUsuario());
        lblBienvenida.setText("Hola, " + u.getPrimerNombre() + ". Aquí puedes administrar todas las cuentas.");
    }

    private void configurarTabla() {
        colNombre.setCellValueFactory(c -> new SimpleStringProperty(c.getValue().getNombreCompleto()));
        colUsuario.setCellValueFactory(c -> new SimpleStringProperty("@" + c.getValue().getUsuario()));
        colCorreo.setCellValueFactory(c -> new SimpleStringProperty(
                c.getValue().getCorreo() == null ? "—" : c.getValue().getCorreo()));
        colFecha.setCellValueFactory(c -> new SimpleStringProperty(
                c.getValue().getFechaRegistro() == null ? "—" : c.getValue().getFechaRegistro().format(FECHA)));
        colFecha.setComparator(this::compararFechas);

        // Columna "Nombre" con avatar de iniciales
        colNombre.setCellFactory(col -> new TableCell<>() {
            private final Label avatar = new Label();
            private final Label nombre = new Label();
            private final Label tuBadge = new Label("Tú");
            private final HBox box = new HBox(10, avatar, nombre, tuBadge);
            {
                avatar.getStyleClass().addAll("avatar", "avatar-sm");
                nombre.getStyleClass().add("cell-strong");
                tuBadge.getStyleClass().add("badge");
                tuBadge.setMinWidth(Region.USE_PREF_SIZE);
                avatar.setMinWidth(Region.USE_PREF_SIZE);
                box.setAlignment(Pos.CENTER_LEFT);
            }
            @Override
            protected void updateItem(String item, boolean empty) {
                super.updateItem(item, empty);
                if (empty || getTableRow() == null || getTableRow().getItem() == null) {
                    setGraphic(null);
                    return;
                }
                Usuario u = getTableRow().getItem();
                avatar.setText(u.getIniciales());
                nombre.setText(item);
                boolean esYo = u.getId() != null && u.getId().equals(Sesion.getUsuario().getId());
                tuBadge.setVisible(esYo);
                tuBadge.setManaged(esYo);
                setGraphic(box);
            }
        });

        filtrados = new FilteredList<>(usuarios, u -> true);
        SortedList<Usuario> ordenados = new SortedList<>(filtrados);
        ordenados.comparatorProperty().bind(tabla.comparatorProperty());
        tabla.setItems(ordenados);

        tabla.setPlaceholder(placeholder("Sin usuarios", "Crea el primero con «Nuevo usuario»."));

        txtBuscar.textProperty().addListener((o, a, texto) -> {
            String q = texto == null ? "" : texto.trim().toLowerCase(Locale.ROOT);
            filtrados.setPredicate(u -> q.isEmpty() || coincide(u, q));
            tabla.setPlaceholder(q.isEmpty()
                    ? placeholder("Sin usuarios", "Crea el primero con «Nuevo usuario».")
                    : placeholder("Sin resultados", "Ningún usuario coincide con «" + texto.trim() + "»."));
            actualizarResumen();
        });

        btnEditar.disableProperty().bind(tabla.getSelectionModel().selectedItemProperty().isNull());
        btnEliminar.disableProperty().bind(tabla.getSelectionModel().selectedItemProperty().isNull());

        tabla.setRowFactory(tv -> {
            TableRow<Usuario> row = new TableRow<>();
            row.setOnMouseClicked(e -> {
                if (e.getClickCount() == 2 && !row.isEmpty()) {
                    abrirFormulario(row.getItem());
                }
            });
            return row;
        });
    }

    // ------------------------------------------------------------------ datos

    private void cargar() {
        lblEstado.setText("Cargando usuarios…");
        Async.run(service::getAll, this::mostrar, error -> {
            lblEstado.setText("");
            Alertas.error("No se pudieron cargar los usuarios", Alertas.mensajeDe(error));
        });
    }

    private void mostrar(List<Usuario> lista) {
        Usuario seleccionado = tabla.getSelectionModel().getSelectedItem();
        usuarios.setAll(lista);
        if (seleccionado != null) {
            usuarios.stream()
                    .filter(u -> u.getId().equals(seleccionado.getId()))
                    .findFirst()
                    .ifPresent(u -> tabla.getSelectionModel().select(u));
        }
        actualizarResumen();
        lblEstado.setText("Doble clic en una fila para editarla.");
    }

    private void actualizarResumen() {
        lblTotal.setText(String.valueOf(usuarios.size()));
        LocalDate hoy = LocalDate.now();
        long esteMes = usuarios.stream()
                .filter(u -> u.getFechaRegistro() != null
                        && u.getFechaRegistro().getYear() == hoy.getYear()
                        && u.getFechaRegistro().getMonth() == hoy.getMonth())
                .count();
        lblMes.setText(String.valueOf(esteMes));
        lblMostrando.setText(filtrados == null ? "—" : String.valueOf(filtrados.size()));
    }

    // --------------------------------------------------------------- acciones

    @FXML
    private void onNuevo() {
        abrirFormulario(null);
    }

    @FXML
    private void onEditar() {
        Usuario u = tabla.getSelectionModel().getSelectedItem();
        if (u != null) abrirFormulario(u);
    }

    @FXML
    private void onEliminar() {
        Usuario u = tabla.getSelectionModel().getSelectedItem();
        if (u == null) return;
        if (u.getId().equals(Sesion.getUsuario().getId())) {
            Alertas.info("Acción no permitida", "No puedes eliminar tu propia cuenta mientras tienes la sesión iniciada.");
            return;
        }
        boolean ok = Alertas.confirmar("¿Eliminar usuario?",
                "Se eliminará permanentemente a " + u.getNombreCompleto() + " (@" + u.getUsuario() + ").\n"
                        + "Esta acción no se puede deshacer.",
                "Eliminar");
        if (!ok) return;

        Async.run(() -> {
            service.eliminar(u);
            return null;
        }, r -> {
            lblEstado.setText("Se eliminó a @" + u.getUsuario() + ".");
            cargar();
        }, error -> Alertas.error("No se pudo eliminar", Alertas.mensajeDe(error)));
    }

    @FXML
    private void onRefrescar() {
        cargar();
    }

    @FXML
    private void onCerrarSesion() {
        if (Alertas.confirmar("Cerrar sesión", "¿Seguro que quieres salir?", "Cerrar sesión")) {
            Sesion.cerrar();
            Navegador.ir("login-view.fxml", "Iniciar sesión");
        }
    }

    // ----------------------------------------------------- diálogo formulario

    private void abrirFormulario(Usuario existente) {
        FXMLLoader loader = Navegador.loader("usuario-form-view.fxml");
        VBox contenido;
        try {
            contenido = loader.load();
        } catch (IOException e) {
            Alertas.error("Error", "No se pudo abrir el formulario.");
            return;
        }
        UsuarioFormController form = loader.getController();
        if (existente != null) form.modoEditar(existente);

        Dialog<Usuario> dialog = new Dialog<>();
        dialog.initOwner(Navegador.getStage());
        dialog.setTitle(existente == null ? "Nuevo usuario" : "Editar usuario");
        dialog.setHeaderText(existente == null ? "Nuevo usuario" : "Editar usuario");

        DialogPane pane = dialog.getDialogPane();
        pane.getStylesheets().add(Navegador.cssUrl());
        pane.getStyleClass().add("dialogo");
        pane.setContent(contenido);

        ButtonType guardar = new ButtonType(existente == null ? "Crear usuario" : "Guardar cambios",
                ButtonBar.ButtonData.OK_DONE);
        pane.getButtonTypes().addAll(ButtonType.CANCEL, guardar);

        // Guardamos al presionar el botón; si hay error, el diálogo no se cierra.
        final Usuario[] resultado = new Usuario[1];
        Node btnGuardar = pane.lookupButton(guardar);
        btnGuardar.addEventFilter(ActionEvent.ACTION, e -> {
            resultado[0] = form.guardar();
            if (resultado[0] == null) e.consume();
        });
        dialog.setResultConverter(bt -> bt == guardar ? resultado[0] : null);
        Platform.runLater(form::enfocar);

        Optional<Usuario> r = dialog.showAndWait();
        r.ifPresent(guardado -> {
            if (Sesion.getUsuario().getId().equals(guardado.getId())) {
                Sesion.iniciar(guardado);
                mostrarSesion(guardado);
            }
            lblEstado.setText(existente == null
                    ? "Se creó a @" + guardado.getUsuario() + "."
                    : "Se actualizó a @" + guardado.getUsuario() + ".");
            cargar();
        });
    }

    // ---------------------------------------------------------------- helpers

    private static boolean coincide(Usuario u, String q) {
        return contiene(u.getNombreCompleto(), q)
                || contiene(u.getUsuario(), q)
                || contiene(u.getCorreo(), q);
    }

    private static boolean contiene(String campo, String q) {
        return campo != null && campo.toLowerCase(Locale.ROOT).contains(q);
    }

    private int compararFechas(String a, String b) {
        return toSortable(a).compareTo(toSortable(b));
    }

    private static String toSortable(String ddMMyyyy) {
        if (ddMMyyyy == null || ddMMyyyy.length() != 10) return "";
        return ddMMyyyy.substring(6) + ddMMyyyy.substring(3, 5) + ddMMyyyy.substring(0, 2);
    }

    private static VBox placeholder(String titulo, String detalle) {
        Label t = new Label(titulo);
        t.getStyleClass().add("h2");
        Label d = new Label(detalle);
        d.getStyleClass().add("muted");
        VBox box = new VBox(6, t, d);
        box.setAlignment(Pos.CENTER);
        return box;
    }
}
