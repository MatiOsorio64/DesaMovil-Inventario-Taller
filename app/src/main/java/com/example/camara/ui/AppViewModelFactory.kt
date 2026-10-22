package com.example.camara.ui

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import com.example.camara.data.repository.ProductoRepository
import com.example.camara.ui.producto.ProductoViewModel
import com.example.camara.ui.scanner.QrViewModel

class AppViewModelFactory(private val repository: ProductoRepository) : ViewModelProvider.Factory {
    @Suppress("UNCHECKED_CAST")
    override fun <T : ViewModel> create(modelClass: Class<T>): T = when {
        modelClass.isAssignableFrom(ProductoViewModel::class.java) -> ProductoViewModel(repository) as T
        modelClass.isAssignableFrom(QrViewModel::class.java) -> QrViewModel(repository) as T
        else -> throw IllegalArgumentException("ViewModel desconocido: ${modelClass.name}")
    }
}
