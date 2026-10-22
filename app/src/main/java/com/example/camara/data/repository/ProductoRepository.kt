package com.example.camara.data.repository

import com.example.camara.data.local.MovimientoDao
import com.example.camara.data.local.MovimientoEntity
import com.example.camara.data.local.ProductoDao
import com.example.camara.data.local.ProductoEntity
import kotlinx.coroutines.flow.Flow

/** Única fuente de datos para el módulo de productos. El ViewModel nunca toca el DAO directamente. */
class ProductoRepository(private val dao: ProductoDao, private val movimientoDao: MovimientoDao) {

    fun observarProductos(filtro: String = ""): Flow<List<ProductoEntity>> =
        dao.observarProductos(filtro.trim())

    suspend fun obtenerPorId(id: Int): ProductoEntity? = dao.obtenerPorId(id)

    suspend fun obtenerPorCodigo(codigo: String): ProductoEntity? = dao.obtenerPorCodigo(codigo.trim())

    suspend fun guardar(producto: ProductoEntity) {
        if (producto.id == 0) dao.insertar(producto) else dao.actualizar(producto)
    }

    /** Registra una venta: descuenta stock y guarda el movimiento. false = no hay stock suficiente. */
    suspend fun comprar(producto: ProductoEntity, cantidad: Int, usuario: String): Boolean {
        if (dao.descontar(producto.id, cantidad) == 0) return false
        movimientoDao.insertar(
            MovimientoEntity(
                productoId = producto.id, tipo = "VENTA", cantidad = cantidad,
                fecha = System.currentTimeMillis(),
                existenciaAnterior = producto.cantidad, existenciaResultante = producto.cantidad - cantidad,
                usuario = usuario
            )
        )
        return true
    }

    suspend fun eliminar(producto: ProductoEntity) = dao.eliminar(producto)
}
