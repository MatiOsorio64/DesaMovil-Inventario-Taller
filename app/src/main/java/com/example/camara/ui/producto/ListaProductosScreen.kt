package com.example.camara.ui.producto

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.animateColorAsState
import androidx.compose.animation.animateContentSize
import androidx.compose.animation.core.tween
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Search
import androidx.compose.material.icons.filled.Warning
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.example.camara.data.local.ProductoEntity

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ListaProductosScreen(
    viewModel: ProductoViewModel,
    esAdmin: Boolean,
    onAgregar: () -> Unit,
    onEditar: (Int) -> Unit,
    onEscanear: () -> Unit,
    onVolver: () -> Unit
) {
    val state by viewModel.listaState.collectAsStateWithLifecycle()

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Productos") },
                navigationIcon = {
                    IconButton(onClick = onVolver) { Icon(Icons.AutoMirrored.Filled.ArrowBack, "Volver") }
                }
            )
        },
        floatingActionButton = {
            if (esAdmin) {
                ExtendedFloatingActionButton(
                    onClick = onAgregar,
                    icon = { Icon(Icons.Default.Add, null) },
                    text = { Text("Agregar") }
                )
            }
        }
    ) { padding ->
        Column(Modifier.padding(padding).padding(horizontal = 16.dp)) {
            OutlinedTextField(
                value = state.busqueda, onValueChange = viewModel::onBusquedaChange,
                label = { Text("Buscar por nombre o código") },
                leadingIcon = { Icon(Icons.Default.Search, null) },
                singleLine = true, modifier = Modifier.fillMaxWidth()
            )
            OutlinedButton(onClick = onEscanear, modifier = Modifier.fillMaxWidth().padding(top = 8.dp)) {
                Text("Escanear QR")
            }
            Spacer(Modifier.height(8.dp))

            if (state.productos.isEmpty() && !state.cargando) {
                Text("No hay productos registrados.", Modifier.padding(16.dp))
            }
            LazyColumn(verticalArrangement = Arrangement.spacedBy(8.dp), contentPadding = PaddingValues(bottom = 88.dp)) {
                items(state.productos, key = { it.id }) { p ->
                    ProductoItem(p, Modifier.animateItem(), onClick = { onEditar(p.id) })
                }
            }
        }
    }
}

@Composable
private fun ProductoItem(p: ProductoEntity, modifier: Modifier = Modifier, onClick: () -> Unit) {
    val colorFondo by animateColorAsState(
        if (p.stockBajo) MaterialTheme.colorScheme.errorContainer else MaterialTheme.colorScheme.surfaceVariant,
        animationSpec = tween(400), label = "colorTarjeta"
    )
    val colorTexto = if (p.stockBajo) MaterialTheme.colorScheme.onErrorContainer else MaterialTheme.colorScheme.onSurfaceVariant
    Card(
        modifier.fillMaxWidth().animateContentSize().clickable(onClick = onClick),
        colors = CardDefaults.cardColors(containerColor = colorFondo, contentColor = colorTexto)
    ) {
        Column(Modifier.padding(16.dp)) {
            Text(p.nombre, style = MaterialTheme.typography.titleMedium)
            Text("Código: ${p.codigo}  ·  ${p.categoria}", style = MaterialTheme.typography.bodyMedium)
            Text("Stock: ${p.cantidad}  (mínimo ${p.stockMinimo})", style = MaterialTheme.typography.bodyMedium)
            // Texto + icono: no depende solo del color (accesibilidad)
            AnimatedVisibility(visible = p.stockBajo) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(Icons.Default.Warning, null)
                    Spacer(Modifier.width(4.dp))
                    Text(if (p.agotado) "Agotado" else "Stock bajo", style = MaterialTheme.typography.labelLarge)
                }
            }
        }
    }
}
