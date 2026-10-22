package com.example.camara.data.local

import androidx.room.Dao
import androidx.room.Delete
import androidx.room.Insert
import androidx.room.Query
import androidx.room.Update
import kotlinx.coroutines.flow.Flow

@Dao
interface ProductoDao {

    // Flow: Room vuelve a emitir cada vez que cambia la tabla (reactividad)
    @Query(
        """SELECT * FROM productos
           WHERE nombre LIKE '%' || :filtro || '%' OR codigo LIKE '%' || :filtro || '%'
           ORDER BY nombre COLLATE NOCASE"""
    )
    fun observarProductos(filtro: String): Flow<List<ProductoEntity>>

    @Query("SELECT * FROM productos WHERE id = :id")
    suspend fun obtenerPorId(id: Int): ProductoEntity?

    @Query("SELECT * FROM productos WHERE codigo = :codigo COLLATE NOCASE LIMIT 1")
    suspend fun obtenerPorCodigo(codigo: String): ProductoEntity?

    @Insert
    suspend fun insertar(producto: ProductoEntity): Long

    @Update
    suspend fun actualizar(producto: ProductoEntity)

    /** Descuento atómico: solo se aplica si hay stock suficiente (nunca deja existencia negativa). */
    @Query("UPDATE productos SET cantidad = cantidad - :n WHERE id = :id AND cantidad >= :n")
    suspend fun descontar(id: Int, n: Int): Int

    @Delete
    suspend fun eliminar(producto: ProductoEntity)
}
