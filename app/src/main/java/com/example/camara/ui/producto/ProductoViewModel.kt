package com.example.camara.ui.producto

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.camara.data.local.ProductoEntity
import com.example.camara.data.repository.ProductoRepository
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.flatMapLatest
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

@OptIn(ExperimentalCoroutinesApi::class)
class ProductoViewModel(private val repository: ProductoRepository) : ViewModel() {

    // ---------------- LISTA ----------------
    private val busqueda = MutableStateFlow("")

    /** Se recalcula sola cuando cambia la búsqueda O cuando Room modifica la tabla. */
    val listaState: StateFlow<ListaUiState> = busqueda
        .flatMapLatest { q ->
            repository.observarProductos(q).map { ListaUiState(productos = it, busqueda = q, cargando = false) }
        }
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5_000), ListaUiState())

    fun onBusquedaChange(valor: String) { busqueda.value = valor }

    // ---------------- FORMULARIO ----------------
    private val _formState = MutableStateFlow(ProductoUiState())
    val formState: StateFlow<ProductoUiState> = _formState.asStateFlow()

    /** idProducto = null -> nuevo (opcionalmente con código precargado desde el QR). */
    fun iniciarFormulario(idProducto: Int?, codigoInicial: String? = null) {
        if (idProducto == null) {
            _formState.value = ProductoUiState(codigo = codigoInicial.orEmpty())
            return
        }
        _formState.value = ProductoUiState(editandoId = idProducto, cargando = true)
        viewModelScope.launch {
            val p = repository.obtenerPorId(idProducto)
            _formState.value = if (p == null) ProductoUiState()
            else ProductoUiState(
                editandoId = p.id, codigo = p.codigo, nombre = p.nombre, categoria = p.categoria,
                cantidad = p.cantidad.toString(), stockMinimo = p.stockMinimo.toString()
            )
        }
    }

    fun onCodigoChange(v: String) = _formState.update { it.copy(codigo = v, codigoError = null) }
    fun onNombreChange(v: String) = _formState.update { it.copy(nombre = v, nombreError = null) }
    fun onCategoriaChange(v: String) = _formState.update { it.copy(categoria = v, categoriaError = null) }
    fun onCantidadChange(v: String) = _formState.update { it.copy(cantidad = v, cantidadError = null) }
    fun onStockMinimoChange(v: String) = _formState.update { it.copy(stockMinimo = v, stockMinimoError = null) }

    fun guardar() {
        val s = _formState.value
        if (s.guardando) return

        val validado = validar(s)
        if (validado.hayErrores()) {
            _formState.value = validado
            return
        }

        viewModelScope.launch {
            _formState.update { it.copy(guardando = true) }
            // Prevención de duplicados: el código debe ser único
            val existente = repository.obtenerPorCodigo(s.codigo)
            if (existente != null && existente.id != s.editandoId) {
                _formState.update { it.copy(guardando = false, codigoError = "Ya existe un producto con este código") }
                return@launch
            }
            val producto = ProductoEntity(
                id = s.editandoId ?: 0,
                codigo = s.codigo.trim(),
                nombre = s.nombre.trim(),
                categoria = s.categoria.trim(),
                cantidad = s.cantidad.trim().toInt(),
                stockMinimo = s.stockMinimo.trim().toInt()
            )
            repository.guardar(producto)
            _formState.update {
                it.copy(
                    guardando = false, guardadoExitosamente = true,
                    stockBajoAlGuardar = producto.stockBajo, nombreGuardado = producto.nombre
                )
            }
        }
    }

    fun eliminar() {
        val id = _formState.value.editandoId ?: return
        viewModelScope.launch {
            repository.obtenerPorId(id)?.let { repository.eliminar(it) }
            _formState.update { it.copy(guardadoExitosamente = true) }
        }
    }

    // ---------------- COMPRA (cliente) ----------------
    fun onCantidadCompraChange(v: String) =
        _formState.update { it.copy(cantidadCompra = v, compraError = null, mensajeCompra = null) }

    /** Valida y, si es correcto, pide confirmación al usuario. */
    fun solicitarCompra() {
        val s = _formState.value
        val id = s.editandoId ?: return
        viewModelScope.launch {
            val p = repository.obtenerPorId(id) ?: return@launch
            val error = validarCompra(s.cantidadCompra, p.cantidad)
            _formState.update { if (error != null) it.copy(compraError = error) else it.copy(confirmandoCompra = true) }
        }
    }

    fun cancelarCompra() = _formState.update { it.copy(confirmandoCompra = false) }

    fun confirmarCompra(usuario: String) {
        val s = _formState.value
        val id = s.editandoId ?: return
        _formState.update { it.copy(confirmandoCompra = false) }
        viewModelScope.launch {
            val p = repository.obtenerPorId(id) ?: return@launch
            val error = validarCompra(s.cantidadCompra, p.cantidad)
            if (error != null) { _formState.update { it.copy(compraError = error) }; return@launch }
            val n = s.cantidadCompra.trim().toInt()
            if (repository.comprar(p, n, usuario)) {
                _formState.update {
                    it.copy(
                        cantidad = (p.cantidad - n).toString(), cantidadCompra = "",
                        mensajeCompra = "Compra registrada: $n × ${p.nombre}"
                    )
                }
            } else {
                _formState.update { it.copy(compraError = "No hay stock suficiente") }
            }
        }
    }

    private fun validarCompra(valor: String, stock: Int): String? {
        if (valor.isBlank()) return "Ingresa la cantidad a comprar"
        val n = valor.trim().toIntOrNull() ?: return "La cantidad debe ser un número entero"
        if (n <= 0) return "La cantidad debe ser mayor a 0"
        if (n > stock) return "Solo quedan $stock unidades disponibles"
        return null
    }

    fun consumirGuardado() = _formState.update { it.copy(guardadoExitosamente = false) }

    // ---------------- VALIDACIONES ----------------
    private fun validar(s: ProductoUiState) = s.copy(
        codigoError = if (s.codigo.isBlank()) "El código es obligatorio" else null,
        nombreError = if (s.nombre.isBlank()) "El nombre es obligatorio" else null,
        categoriaError = if (s.categoria.isBlank()) "La categoría es obligatoria" else null,
        cantidadError = errorNumero(s.cantidad, "La cantidad"),
        stockMinimoError = errorNumero(s.stockMinimo, "La existencia mínima")
    )

    private fun errorNumero(valor: String, campo: String): String? {
        if (valor.isBlank()) return "$campo es obligatoria"
        val n = valor.trim().toIntOrNull() ?: return "$campo debe ser un número entero"
        return if (n < 0) "$campo debe ser mayor o igual a 0" else null
    }

    private fun ProductoUiState.hayErrores() = listOf(
        codigoError, nombreError, categoriaError, cantidadError, stockMinimoError
    ).any { it != null }
}
