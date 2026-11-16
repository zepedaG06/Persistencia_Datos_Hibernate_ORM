# Persistencia de Datos con Hibernate ORM

Aplicación de escritorio **JavaFX + Hibernate (JPA) + PostgreSQL** para administrar usuarios.

## Funcionalidades

- **Inicio de sesión** con nombre de usuario o correo.
- **Registro** de cuentas nuevas, con validación de campos e indicador de seguridad de la contraseña.
- **Panel de administración (CRUD)**:
  - Listado de usuarios con búsqueda por nombre, usuario o correo.
  - Crear, editar (doble clic en una fila) y eliminar usuarios, con confirmación.
  - Tarjetas de resumen: total de usuarios y registros del mes.
- Contraseñas guardadas con **hash PBKDF2-HMAC-SHA256** y sal aleatoria.
  Los usuarios antiguos con contraseña en texto plano se convierten a hash automáticamente la próxima vez que inician sesión.
- Usuario y correo **únicos**, sin importar mayúsculas o minúsculas.
- Las consultas a la base de datos se hacen en segundo plano para que la ventana no se congele.

## Estructura (`administracionFX/`)

```
src/main/java/ni/edu/uam/administracionfx/
├── MainApplication.java        # Punto de entrada
├── controller/                 # Controladores JavaFX (Login, Registro, Dashboard, Formulario)
├── service/ServiceUsuario.java # Reglas de negocio y validaciones
├── dao/                        # Acceso a datos genérico con JPA (ImplDao)
├── model/Usuario.java          # Entidad JPA
└── util/                       # Navegación, sesión, diálogos, hash de contraseñas
src/main/resources/ni/edu/uam/administracionfx/
├── view/*.fxml                 # Pantallas
└── css/estilos.css             # Estilos
```

## Cómo ejecutar

1. Ten PostgreSQL corriendo y crea la base de datos:
   ```sql
   CREATE DATABASE estudiante;
   ```
2. Revisa usuario, contraseña y URL en
   `administracionFX/src/main/resources/META-INF/persistence.xml`.
   La tabla `usuario` se crea o actualiza sola (`hibernate.hbm2ddl.auto=update`).
3. Ejecuta la aplicación:
   ```bash
   cd administracionFX
   mvn clean javafx:run
   ```
   O desde IntelliJ: abre la carpeta `administracionFX` y ejecuta `MainApplication`
   (o el objetivo Maven `javafx:run`).
4. En la primera pantalla elige **Regístrate**, crea tu cuenta e inicia sesión.

Requisitos: JDK 21.
