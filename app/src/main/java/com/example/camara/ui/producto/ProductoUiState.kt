package com.example.camara.ui.producto

import com.example.camara.data.local.ProductoEntity

/** Estado del formulario Crear/Editar. Los campos son String para poder validarlos y mostrar errores. */
data class ProductoUiState(
    val editandoId: Int? = null,          // null = producto nuevo

    val codigo: String = "",
    val nombre: String = "",
    val categoria: String = "",
    val cantidad: String = "",
    val stockMinimo: String = "",

    val codigoError: String? = null,
    val nombreError: String? = null,
    val categoriaError: String? = null,
    val cantidadError: String? = null,
    val stockMinimoError: String? = null,

    val cargando: Boolean = false,
    val guardando: Boolean = false,
    val guardadoExitosamente: Boolean = false,
    val stockBajoAlGuardar: Boolean = false,   // dispara la notificación local
    val nombreGuardado: String = "",

    // Compra (perfil cliente)
    val cantidadCompra: String = "",
    val compraError: String? = null,
    val confirmandoCompra: Boolean = false,
    val mensajeCompra: String? = null
) {
    val esEdicion: Boolean get() = editandoId != null
}

/** Estado de la pantalla de listado. */
data class ListaUiState(
    val productos: List<ProductoEntity> = emptyList(),
    val busqueda: String = "",
    val cargando: Boolean = true
)
