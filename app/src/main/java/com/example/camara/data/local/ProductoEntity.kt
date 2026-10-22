package com.example.camara.data.local

import androidx.room.Entity
import androidx.room.Index
import androidx.room.PrimaryKey

/** Tabla "productos". El código interno es único (también es el contenido del QR). */
@Entity(
    tableName = "productos",
    indices = [Index(value = ["codigo"], unique = true)]
)
data class ProductoEntity(
    @PrimaryKey(autoGenerate = true) val id: Int = 0,
    val codigo: String,
    val nombre: String,
    val categoria: String,
    val cantidad: Int,
    val stockMinimo: Int
) {
    /** Producto bajo (o igual a) su existencia mínima. */
    val stockBajo: Boolean get() = cantidad <= stockMinimo
    val agotado: Boolean get() = cantidad == 0
}
