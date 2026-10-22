# Inventario Taller – Evaluación Parcial 2 (DSY1105 · Caso 006D)

App Android (Kotlin + Jetpack Compose + Material 3) para gestionar el inventario de un taller mecánico. Todos los datos son **ficticios**.

**Integrantes:** _Nombre Apellido_ · _Nombre Apellido_  **Equipo N°:** __
**Trello:** _enlace_  **Repositorio:** _enlace_

## Funcionalidades
- **Login con 2 perfiles** (PIN guardado como hash): `admin` / `1234` (gestiona productos) y `cliente` / `5678` (solo consulta).
- **Compra (cliente)**: desde el detalle de un producto el cliente ingresa la cantidad, se valida (> 0 y <= stock), confirma en un diálogo y se descuenta el stock en Room; queda un movimiento de VENTA con existencia anterior y resultante.
- **Productos**: listar, buscar por nombre/código, registrar, modificar y eliminar (con confirmación). Indicador "Stock bajo / Agotado" con texto e ícono.
- **Formularios validados**: íconos por campo, mensaje de error bajo el campo e ícono de advertencia. Validación en el `ViewModel` (obligatorios, numéricos >= 0, código único).
- **Gestión de estado**: `UiState` + `StateFlow`; la lista se actualiza sola al registrar (Room + `Flow`).
- **Persistencia local**: Room (`ProductoEntity`, `ProductoDao`, `AppDatabase`), los datos sobreviven al reiniciar.
- **Recursos nativos**: (1) **Cámara**: lectura de QR (CameraX + ML Kit) que muestra el producto; (2) **Notificaciones locales** de stock bajo al guardar un producto (con solicitud de permiso).
- **Animaciones**: transiciones entre pantallas, ítems de lista animados, color animado en stock bajo, barra de progreso al guardar, error de login animado y marco del escáner con pulso.

## Arquitectura (MVVM)
```
data/local        ProductoEntity · ProductoDao · AppDatabase
data/repository   ProductoRepository
ui/producto       ListaProductosScreen · ProductoFormScreen · ProductoViewModel · ProductoUiState
ui/scanner        QrScannerScreen · QrViewModel
ui/login · menu   LoginScreen/ViewModel/Rol · MenuScreen
ui/navigation     AppNavigation
lecturaqr/utils   QrScanner · CameraPermissionHelper
recursos          NotificacionHelper
```

## Cómo ejecutar
1. Android Studio (versión reciente), abrir la carpeta del proyecto y esperar el *Sync*.
2. Ejecutar en un teléfono con depuración USB o en un emulador.
3. Ingresar con `admin` / `1234` o `cliente` / `5678`.
4. Para probar el QR, generar un QR con el texto `LUB-001`, `FIL-001`, `LIQ-001` o `ADI-001`.
5. Tests: `./gradlew test`.
