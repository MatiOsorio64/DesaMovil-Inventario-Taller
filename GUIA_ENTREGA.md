# Guía de entrega (Parcial 2)

## Checklist contra la rúbrica
| Indicador | Evidencia en el proyecto |
|---|---|
| IE 2.1.1 Interfaz coherente y navegación | Material 3, Scaffold/TopAppBar, navegación Login→Menú→Lista→Form→QR |
| IE 2.1.2 Formularios con validación e íconos | Login y Producto: ícono por campo, error bajo el campo + ícono de advertencia |
| IE 2.2.1 Validación desacoplada | `ProductoViewModel.validar()` y `LoginViewModel`; la UI solo lee el estado |
| IE 2.2.2 Animaciones | Transiciones NavHost, animateItem, animateColorAsState, AnimatedVisibility, pulso en el escáner |
| IE 2.3.1 Modularidad + persistencia | Paquetes MVVM + Room |
| IE 2.3.2 GitHub + Trello | **Debes hacerlo ustedes** (ver abajo) |
| IE 2.4.1 Dos recursos nativos | Cámara (QR) + Notificaciones locales |

## GitHub (commits técnicos, de AMBOS integrantes)
Ejemplos de mensajes (no usen "cambios" ni "update"):
- `feat(data): agregar ProductoEntity, ProductoDao y AppDatabase con Room`
- `feat(data): crear ProductoRepository sobre ProductoDao`
- `feat(ui): implementar ProductoViewModel con ProductoUiState y StateFlow`
- `feat(ui): crear ListaProductosScreen con búsqueda e indicador de stock bajo`
- `feat(ui): crear ProductoFormScreen con validaciones y errores por campo`
- `feat(nav): agregar AppNavigation Login-Menú-Lista-Form`
- `feat(auth): agregar login con roles admin y cliente`
- `feat(camera): leer QR con CameraX y ML Kit e identificar producto`
- `feat(native): agregar notificaciones locales de stock bajo`
- `feat(ui): agregar animaciones de navegación, lista y escáner`
- `test: agregar pruebas unitarias de validación en ProductoViewModel`
- `docs: completar README con integrantes y pasos de ejecución`

Repartan los commits entre ambos, repositorio **público**, y den acceso al docente. **No hagan ningún cambio al repo después de la fecha de entrega** (1.0 automático).

## Trello (tablero con columnas Por hacer / En progreso / Hecho)
Tarjetas sugeridas, cada una asignada a un integrante: Configurar proyecto y Room · Repositorio y ViewModel · Lista de productos · Formulario y validaciones · Navegación y login · Roles admin/cliente · Lector QR · Notificaciones · Animaciones · Pruebas unitarias · README e informe · Entrega en AVA.

## AVA
Subir el código (mismo contenido del repo) por un solo integrante al AVA grupal, y activar cada uno el enlace individual con nombre, apellido, N° de equipo y nombre de la app.
