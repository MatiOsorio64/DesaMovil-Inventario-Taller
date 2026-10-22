package com.example.camara.ui.login

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.foundation.layout.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Lock
import androidx.compose.material.icons.filled.Person
import androidx.compose.material.icons.filled.Warning
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle

@Composable
fun LoginScreen(viewModel: LoginViewModel, onLoginOk: (Rol) -> Unit) {
    val state by viewModel.state.collectAsStateWithLifecycle()

    LaunchedEffect(state.autenticado) {
        if (state.autenticado && state.rol != null) {
            val rol = state.rol!!
            viewModel.consumirAutenticado(); onLoginOk(rol)
        }
    }

    Column(
        Modifier.fillMaxSize().padding(24.dp),
        verticalArrangement = Arrangement.Center,
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Text("Inventario Taller", style = MaterialTheme.typography.headlineMedium)
        Text("Administrador: admin / 1234", style = MaterialTheme.typography.bodySmall)
        Text("Cliente: cliente / 5678", style = MaterialTheme.typography.bodySmall)
        Spacer(Modifier.height(24.dp))

        OutlinedTextField(
            value = state.usuario, onValueChange = viewModel::onUsuarioChange,
            label = { Text("Usuario") }, singleLine = true, modifier = Modifier.fillMaxWidth(),
            leadingIcon = { Icon(Icons.Default.Person, null) },
            trailingIcon = if (state.usuarioError != null) {
                { Icon(Icons.Default.Warning, "Error", tint = MaterialTheme.colorScheme.error) }
            } else null,
            isError = state.usuarioError != null,
            supportingText = { Text(state.usuarioError ?: "") }
        )
        OutlinedTextField(
            value = state.pin, onValueChange = viewModel::onPinChange,
            label = { Text("PIN") }, singleLine = true, modifier = Modifier.fillMaxWidth(),
            leadingIcon = { Icon(Icons.Default.Lock, null) },
            trailingIcon = if (state.pinError != null) {
                { Icon(Icons.Default.Warning, "Error", tint = MaterialTheme.colorScheme.error) }
            } else null,
            visualTransformation = PasswordVisualTransformation(),
            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.NumberPassword),
            isError = state.pinError != null,
            supportingText = { Text(state.pinError ?: "") }
        )
        AnimatedVisibility(visible = state.errorGeneral != null) {
            Text(state.errorGeneral.orEmpty(), color = MaterialTheme.colorScheme.error, style = MaterialTheme.typography.bodyMedium)
        }
        Spacer(Modifier.height(16.dp))
        Button(onClick = viewModel::ingresar, modifier = Modifier.fillMaxWidth()) { Text("Ingresar") }
    }
}
