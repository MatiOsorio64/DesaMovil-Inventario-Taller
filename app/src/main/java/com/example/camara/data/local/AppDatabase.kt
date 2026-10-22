package com.example.camara.data.local

import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase
import androidx.sqlite.db.SupportSQLiteDatabase

@Database(entities = [ProductoEntity::class, MovimientoEntity::class], version = 2, exportSchema = false)
abstract class AppDatabase : RoomDatabase() {

    abstract fun productoDao(): ProductoDao
    abstract fun movimientoDao(): MovimientoDao

    companion object {
        @Volatile private var INSTANCE: AppDatabase? = null

        fun getInstance(context: Context): AppDatabase =
            INSTANCE ?: synchronized(this) {
                INSTANCE ?: Room.databaseBuilder(
                    context.applicationContext,
                    AppDatabase::class.java,
                    "inventario_taller.db"
                )
                    .fallbackToDestructiveMigration(true)   // proyecto académico: reconstruye la BD al cambiar de versión
                    .addCallback(SeedCallback)
                    .build()
                    .also { INSTANCE = it }
            }

        /** Datos FICTICIOS iniciales: al crear la BD por primera vez y al reconstruirla por cambio de versión. */
        private object SeedCallback : RoomDatabase.Callback() {
            override fun onCreate(db: SupportSQLiteDatabase) {
                super.onCreate(db)
                sembrar(db)
            }

            override fun onDestructiveMigration(db: SupportSQLiteDatabase) {
                super.onDestructiveMigration(db)
                sembrar(db)
            }

            private fun sembrar(db: SupportSQLiteDatabase) {
                db.execSQL("INSERT INTO productos (codigo,nombre,categoria,cantidad,stockMinimo) VALUES ('LUB-001','Aceite motor 5W-30 1L','Lubricantes',24,10)")
                db.execSQL("INSERT INTO productos (codigo,nombre,categoria,cantidad,stockMinimo) VALUES ('FIL-001','Filtro de aceite estándar','Filtros',4,6)")
                db.execSQL("INSERT INTO productos (codigo,nombre,categoria,cantidad,stockMinimo) VALUES ('LIQ-001','Líquido de frenos DOT4','Líquidos',0,5)")
                db.execSQL("INSERT INTO productos (codigo,nombre,categoria,cantidad,stockMinimo) VALUES ('ADI-001','Aditivo limpiador de inyectores','Aditivos',12,4)")
            }
        }
    }
}
