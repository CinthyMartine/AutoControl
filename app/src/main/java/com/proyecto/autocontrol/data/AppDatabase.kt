package com.proyecto.autocontrol.data

import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase

@Database(
    entities = [Auto::class, Carga::class, Mantencion::class],
    version = 1,
    exportSchema = false
)
abstract class AppDatabase : RoomDatabase() {

    abstract fun autoDao(): AutoDao
    abstract fun cargaDao(): CargaDao
    abstract fun mantencionDao(): MantencionDao

    companion object {
        @Volatile
        private var instancia: AppDatabase? = null

        // Devuelve siempre la misma base de datos (patrón Singleton)
        fun obtener(context: Context): AppDatabase {
            return instancia ?: synchronized(this) {
                Room.databaseBuilder(
                    context.applicationContext,
                    AppDatabase::class.java,
                    "autocontrol.db"
                ).build().also { instancia = it }
            }
        }
    }
}
