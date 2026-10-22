package com.example.camara.ui.menu

import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import com.example.camara.ui.login.Rol
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp

@Composable
fun MenuScreen(rol: Rol, onProductos: () -> Unit, onEscanear: () -> Unit, onCerrarSesion: () -> Unit) {
    Column(
        Modifier.fillMaxSize().padding(24.dp),
        verticalArrangement = Arrangement.Center,
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Text("Menú principal", style = MaterialTheme.typography.headlineMedium)
        Text("Sesión: ${rol.etiqueta}", style = MaterialTheme.typography.bodyMedium)
        Spacer(Modifier.height(24.dp))
        Button(onClick = onProductos, modifier = Modifier.fillMaxWidth().height(56.dp)) { Text("Productos") }
        Spacer(Modifier.height(12.dp))
        Button(onClick = onEscanear, modifier = Modifier.fillMaxWidth().height(56.dp)) { Text("Escanear QR de producto") }
        Spacer(Modifier.height(12.dp))
        OutlinedButton(onClick = onCerrarSesion, modifier = Modifier.fillMaxWidth().height(56.dp)) { Text("Cerrar sesión") }
    }
}
