package com.example.camara.ui.scanner

import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.animation.animateContentSize
import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.tween
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.layout.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.example.camara.data.local.ProductoEntity
import com.example.camara.lecturaqr.utils.CameraPermissionHelper
import com.example.camara.lecturaqr.utils.QrScanner

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun QrScannerScreen(
    viewModel: QrViewModel,
    esAdmin: Boolean,
    onVerProducto: (Int) -> Unit,
    onRegistrarCodigo: (String) -> Unit,
    onVolver: () -> Unit
) {
    val state by viewModel.state.collectAsStateWithLifecycle()
    val context = LocalContext.current
    var permiso by remember { mutableStateOf(CameraPermissionHelper.hasCameraPermission(context)) }
    val launcher = rememberLauncherForActivityResult(ActivityResultContracts.RequestPermission()) { permiso = it }

    Scaffold(topBar = {
        TopAppBar(
            title = { Text("Escanear QR") },
            navigationIcon = { IconButton(onClick = onVolver) { Icon(Icons.AutoMirrored.Filled.ArrowBack, "Volver") } }
        )
    }) { padding ->
        Column(
            Modifier.padding(padding).fillMaxSize().padding(16.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            when {
                !permiso -> {
                    Text("Se necesita permiso de cámara para leer códigos QR", textAlign = TextAlign.Center)
                    Spacer(Modifier.height(16.dp))
                    Button(onClick = { launcher.launch(android.Manifest.permission.CAMERA) }) { Text("Conceder permiso") }
                }
                state.escaneando -> {
                    Text("Enfoca el QR del producto en el marco", style = MaterialTheme.typography.titleMedium)
                    Spacer(Modifier.height(12.dp))
                    val pulso = rememberInfiniteTransition(label = "pulso")
                    val alfa by pulso.animateFloat(
                        initialValue = 0.3f, targetValue = 1f,
                        animationSpec = infiniteRepeatable(tween(900), RepeatMode.Reverse), label = "alfa"
                    )
                    Box(Modifier.fillMaxWidth().height(400.dp)) {
                        QrScanner(onQrCodeScanned = viewModel::onQrDetected, modifier = Modifier.fillMaxSize())
                        Surface(
                            modifier = Modifier.size(250.dp).align(Alignment.Center),
                            color = Color.Transparent, shape = MaterialTheme.shapes.medium,
                            border = BorderStroke(3.dp, MaterialTheme.colorScheme.primary.copy(alpha = alfa))
                        ) {}
                    }
                }
                state.producto != null -> ResultadoProducto(
                    state.producto!!, esAdmin,
                    onEditar = { onVerProducto(state.producto!!.id) },
                    onOtro = viewModel::reiniciar
                )
                state.noEncontrado -> {
                    Text("Código leído: ${state.codigoLeido}", style = MaterialTheme.typography.titleMedium)
                    Text("No hay un producto registrado con este código.", Modifier.padding(vertical = 12.dp))
                    if (esAdmin) {
                        Button(onClick = { onRegistrarCodigo(state.codigoLeido.orEmpty()) }, modifier = Modifier.fillMaxWidth()) {
                            Text("Registrar producto con este código")
                        }
                    }
                    OutlinedButton(onClick = viewModel::reiniciar, modifier = Modifier.fillMaxWidth()) { Text("Escanear otro") }
                }
                else -> CircularProgressIndicator()
            }
        }
    }
}

@Composable
private fun ResultadoProducto(p: ProductoEntity, esAdmin: Boolean, onEditar: () -> Unit, onOtro: () -> Unit) {
    Card(Modifier.fillMaxWidth().animateContentSize()) {
        Column(Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(4.dp)) {
            Text(p.nombre, style = MaterialTheme.typography.titleLarge)
            Text("Código: ${p.codigo}")
            Text("Categoría: ${p.categoria}")
            Text("Cantidad disponible: ${p.cantidad}")
            Text("Existencia mínima: ${p.stockMinimo}")
            if (p.stockBajo) {
                Text(if (p.agotado) "⚠ Agotado" else "⚠ Stock bajo", color = MaterialTheme.colorScheme.error)
            }
        }
    }
    Spacer(Modifier.height(16.dp))
    Button(onClick = onEditar, modifier = Modifier.fillMaxWidth()) { Text(if (esAdmin) "Ver / editar producto" else "Ver detalle") }
    OutlinedButton(onClick = onOtro, modifier = Modifier.fillMaxWidth()) { Text("Escanear otro") }
}
