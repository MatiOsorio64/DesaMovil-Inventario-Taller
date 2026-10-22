package com.example.camara.ui.producto

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.LocalContext
import com.example.camara.recursos.NotificacionHelper
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle

/** idProducto = null -> registrar; distinto de null -> modificar. */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ProductoFormScreen(
    viewModel: ProductoViewModel,
    idProducto: Int?,
    codigoInicial: String?,
    soloLectura: Boolean = false,
    onVolver: () -> Unit
) {
    val state by viewModel.formState.collectAsStateWithLifecycle()
    var iniciado by rememberSaveable { mutableStateOf(false) }   // evita reiniciar al rotar la pantalla
    var confirmarEliminar by remember { mutableStateOf(false) }

    val context = LocalContext.current
    val permisoNotif = rememberLauncherForActivityResult(ActivityResultContracts.RequestPermission()) { }

    LaunchedEffect(Unit) {
        // Recurso nativo 2: pide el permiso de notificaciones al abrir el formulario de edición
        if (!soloLectura && NotificacionHelper.requierePermiso() && !NotificacionHelper.tienePermiso(context)) {
            permisoNotif.launch(android.Manifest.permission.POST_NOTIFICATIONS)
        }
        if (!iniciado) { viewModel.iniciarFormulario(idProducto, codigoInicial); iniciado = true }
    }
    LaunchedEffect(state.guardadoExitosamente) {
        if (state.guardadoExitosamente) {
            if (state.stockBajoAlGuardar) NotificacionHelper.avisarStockBajo(context, state.nombreGuardado, state.cantidad)
            viewModel.consumirGuardado(); onVolver()
        }
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text(if (soloLectura) "Detalle del producto" else if (state.esEdicion) "Editar producto" else "Nuevo producto") },
                navigationIcon = {
                    IconButton(onClick = onVolver) { Icon(Icons.AutoMirrored.Filled.ArrowBack, "Volver") }
                }
            )
        }
    ) { padding ->
        Column(
            Modifier.padding(padding).padding(16.dp).verticalScroll(rememberScrollState()),
            verticalArrangement = Arrangement.spacedBy(4.dp)
        ) {
            Campo("Código", state.codigo, state.codigoError, viewModel::onCodigoChange, soloLectura = soloLectura, icono = Icons.Default.Info)
            Campo("Nombre", state.nombre, state.nombreError, viewModel::onNombreChange, soloLectura = soloLectura, icono = Icons.Default.Edit)
            Campo("Categoría", state.categoria, state.categoriaError, viewModel::onCategoriaChange, soloLectura = soloLectura, icono = Icons.Default.List)
            Campo("Cantidad", state.cantidad, state.cantidadError, viewModel::onCantidadChange, numerico = true, soloLectura = soloLectura, icono = Icons.Default.ShoppingCart)
            Campo("Existencia mínima", state.stockMinimo, state.stockMinimoError, viewModel::onStockMinimoChange, numerico = true, soloLectura = soloLectura, icono = Icons.Default.Notifications)

            Spacer(Modifier.height(8.dp))
            if (soloLectura) {
                SeccionCompra(state, viewModel)
                OutlinedButton(onClick = onVolver, modifier = Modifier.fillMaxWidth()) { Text("Volver") }
                return@Column
            }
            AnimatedVisibility(visible = state.guardando) {
                LinearProgressIndicator(Modifier.fillMaxWidth())
            }
            Button(
                onClick = viewModel::guardar, enabled = !state.guardando,
                modifier = Modifier.fillMaxWidth().height(52.dp)
            ) { Text(if (state.guardando) "Guardando..." else "Guardar") }
            OutlinedButton(onClick = onVolver, modifier = Modifier.fillMaxWidth()) { Text("Cancelar") }
            if (state.esEdicion) {
                TextButton(onClick = { confirmarEliminar = true }, modifier = Modifier.fillMaxWidth()) {
                    Text("Eliminar producto", color = MaterialTheme.colorScheme.error)
                }
            }
        }
    }

    if (state.confirmandoCompra) {
        AlertDialog(
            onDismissRequest = viewModel::cancelarCompra,
            title = { Text("Confirmar compra") },
            text = { Text("¿Confirmas la compra de ${state.cantidadCompra} unidad(es) de \"${state.nombre}\"?") },
            confirmButton = { TextButton(onClick = { viewModel.confirmarCompra("cliente") }) { Text("Confirmar") } },
            dismissButton = { TextButton(onClick = viewModel::cancelarCompra) { Text("Cancelar") } }
        )
    }

    if (confirmarEliminar) {
        AlertDialog(
            onDismissRequest = { confirmarEliminar = false },
            title = { Text("Eliminar producto") },
            text = { Text("¿Seguro que deseas eliminar \"${state.nombre}\"? Esta acción no se puede deshacer.") },
            confirmButton = {
                TextButton(onClick = { confirmarEliminar = false; viewModel.eliminar() }) { Text("Eliminar") }
            },
            dismissButton = { TextButton(onClick = { confirmarEliminar = false }) { Text("Cancelar") } }
        )
    }
}

@Composable
private fun Campo(
    etiqueta: String, valor: String, error: String?,
    onChange: (String) -> Unit, numerico: Boolean = false, soloLectura: Boolean = false, icono: ImageVector? = null
) {
    OutlinedTextField(
        value = valor, onValueChange = onChange,
        label = { Text(etiqueta) }, singleLine = true, modifier = Modifier.fillMaxWidth(),
        readOnly = soloLectura,
        leadingIcon = icono?.let { ic -> { Icon(ic, null) } },
        trailingIcon = if (error != null) {
            { Icon(Icons.Default.Warning, contentDescription = "Error", tint = MaterialTheme.colorScheme.error) }
        } else null,
        keyboardOptions = KeyboardOptions(keyboardType = if (numerico) KeyboardType.Number else KeyboardType.Text),
        isError = error != null,
        supportingText = { Text(error ?: "") }
    )
}

@Composable
private fun SeccionCompra(state: ProductoUiState, viewModel: ProductoViewModel) {
    val hayStock = (state.cantidad.toIntOrNull() ?: 0) > 0
    Column(verticalArrangement = Arrangement.spacedBy(4.dp)) {
        HorizontalDivider(Modifier.padding(vertical = 8.dp))
        Text("Comprar este producto", style = MaterialTheme.typography.titleMedium)
        Text(if (hayStock) "Disponible: ${state.cantidad} unidades" else "Sin stock disponible")
        Campo(
            "Cantidad a comprar", state.cantidadCompra, state.compraError,
            viewModel::onCantidadCompraChange, numerico = true, icono = Icons.Default.ShoppingCart
        )
        Button(
            onClick = viewModel::solicitarCompra, enabled = hayStock,
            modifier = Modifier.fillMaxWidth().height(52.dp)
        ) { Text(if (hayStock) "Comprar" else "Sin stock") }
        AnimatedVisibility(visible = state.mensajeCompra != null) {
            Row(verticalAlignment = androidx.compose.ui.Alignment.CenterVertically) {
                Icon(Icons.Default.CheckCircle, null, tint = MaterialTheme.colorScheme.primary)
                Spacer(Modifier.width(8.dp))
                Text(state.mensajeCompra.orEmpty(), color = MaterialTheme.colorScheme.primary)
            }
        }
    }
}
