package com.example.camara.ui.scanner

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.camara.data.local.ProductoEntity
import com.example.camara.data.repository.ProductoRepository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

data class QrUiState(
    val escaneando: Boolean = true,
    val codigoLeido: String? = null,
    val producto: ProductoEntity? = null,   // producto registrado que coincide con el QR
    val noEncontrado: Boolean = false
)

class QrViewModel(private val repository: ProductoRepository) : ViewModel() {

    private val _state = MutableStateFlow(QrUiState())
    val state: StateFlow<QrUiState> = _state.asStateFlow()

    /** El contenido del QR es el código interno del producto. */
    fun onQrDetected(contenido: String) {
        if (!_state.value.escaneando) return
        val codigo = contenido.trim()
        _state.value = QrUiState(escaneando = false, codigoLeido = codigo)
        viewModelScope.launch {
            val p = repository.obtenerPorCodigo(codigo)
            _state.value = QrUiState(
                escaneando = false, codigoLeido = codigo, producto = p, noEncontrado = p == null
            )
        }
    }

    fun reiniciar() { _state.value = QrUiState() }
}
