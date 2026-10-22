package com.example.camara.ui.login

import androidx.lifecycle.ViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import java.security.MessageDigest

data class LoginUiState(
    val usuario: String = "",
    val pin: String = "",
    val usuarioError: String? = null,
    val pinError: String? = null,
    val errorGeneral: String? = null,
    val autenticado: Boolean = false,
    val rol: Rol? = null
)

class LoginViewModel : ViewModel() {

    private val _state = MutableStateFlow(LoginUiState())
    val state: StateFlow<LoginUiState> = _state.asStateFlow()

    fun onUsuarioChange(v: String) = _state.update { it.copy(usuario = v, usuarioError = null, errorGeneral = null) }
    fun onPinChange(v: String) = _state.update { it.copy(pin = v.filter(Char::isDigit).take(6), pinError = null, errorGeneral = null) }

    fun ingresar() {
        val s = _state.value
        val uErr = if (s.usuario.isBlank()) "Ingresa el usuario" else null
        val pErr = if (s.pin.isBlank()) "Ingresa el PIN" else null
        if (uErr != null || pErr != null) {
            _state.update { it.copy(usuarioError = uErr, pinError = pErr) }
            return
        }
        // Cuentas FICTICIAS: admin / 1234 y cliente / 5678. Se compara el hash, no el texto del PIN.
        val hash = sha256(s.pin)
        val rol = when {
            s.usuario.trim().equals("admin", ignoreCase = true) && hash == PIN_ADMIN -> Rol.ADMIN
            s.usuario.trim().equals("cliente", ignoreCase = true) && hash == PIN_CLIENTE -> Rol.CLIENTE
            else -> null
        }
        _state.update {
            if (rol != null) it.copy(autenticado = true, rol = rol)
            else it.copy(errorGeneral = "Usuario o PIN incorrecto")
        }
    }

    fun consumirAutenticado() = _state.update { LoginUiState() }

    private fun sha256(texto: String) =
        MessageDigest.getInstance("SHA-256").digest(texto.toByteArray()).joinToString("") { "%02x".format(it) }

    private companion object {
        const val PIN_ADMIN = "03ac674216f3e15c761ee1a5e255f067953623c8b388b4459e13f978d7c846f4" // sha256("1234")
        const val PIN_CLIENTE = "f8638b979b2f4f793ddb6dbd197e0ee25a7a6ea32b0ae22f5e3c5d119d839e75" // sha256("5678")
    }
}
