package com.example.camara.data.local

import androidx.room.Dao
import androidx.room.Entity
import androidx.room.Insert
import androidx.room.PrimaryKey

/** Historial de movimientos (por ahora: VENTA). Guarda existencia anterior y resultante. */
@Entity(tableName = "movimientos")
data class MovimientoEntity(
    @PrimaryKey(autoGenerate = true) val id: Int = 0,
    val productoId: Int,
    val tipo: String,
    val cantidad: Int,
    val fecha: Long,
    val existenciaAnterior: Int,
    val existenciaResultante: Int,
    val usuario: String
)

@Dao
interface MovimientoDao {
    @Insert
    suspend fun insertar(movimiento: MovimientoEntity)
}
