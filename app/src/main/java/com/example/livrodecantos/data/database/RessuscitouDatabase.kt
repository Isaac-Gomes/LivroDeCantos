package com.example.livrodecantos.data.database

import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase
import com.example.livrodecantos.data.dao.CantoDao
import com.example.livrodecantos.data.dao.FichaDao
import com.example.livrodecantos.data.dao.IndiceBiblicoDao
import com.example.livrodecantos.data.dao.IndiceLiturgicoDao
import com.example.livrodecantos.data.entity.CantoEntity
import com.example.livrodecantos.data.entity.FichaEntity
import com.example.livrodecantos.data.entity.IndiceBiblicoEntity
import com.example.livrodecantos.data.entity.IndiceLiturgicoEntity
import androidx.room.migration.Migration
import androidx.sqlite.db.SupportSQLiteDatabase
import com.example.livrodecantos.data.dao.ListaDao
import com.example.livrodecantos.data.entity.ListaCantoCrossRef
import com.example.livrodecantos.data.entity.ListaEntity

@Database(
    entities = [
        CantoEntity::class,
        FichaEntity::class,
        IndiceLiturgicoEntity::class,
        IndiceBiblicoEntity::class,
        ListaEntity::class,
        ListaCantoCrossRef::class
    ],
    version = 4,
    exportSchema = false
)
abstract class RessuscitouDatabase : RoomDatabase() {

    abstract fun cantoDao(): CantoDao

    abstract fun fichaDao(): FichaDao

    abstract fun indiceLiturgicoDao():
            IndiceLiturgicoDao

    abstract fun indiceBiblicoDao():
            IndiceBiblicoDao

    abstract fun listaDao():
            ListaDao
    companion object {

        @Volatile
        private var INSTANCE:
                RessuscitouDatabase? = null

        private val MIGRATION_3_4 =

            object : Migration(
                3,
                4
            ) {

                override fun migrate(
                    database:
                    SupportSQLiteDatabase
                ) {

                    // ----------------------------------------
                    // LISTAS
                    // ----------------------------------------

                    database.execSQL(
                        """
                CREATE TABLE IF NOT EXISTS listas (
                    id INTEGER PRIMARY KEY AUTOINCREMENT NOT NULL,
                    nome TEXT NOT NULL,
                    criadoEm INTEGER NOT NULL
                )
                """.trimIndent()
                    )


                    // ----------------------------------------
                    // CANTOS DAS LISTAS
                    // ----------------------------------------

                    database.execSQL(
                        """
                CREATE TABLE IF NOT EXISTS lista_cantos (
                    listaId INTEGER NOT NULL,
                    cantoId INTEGER NOT NULL,
                    ordem INTEGER NOT NULL,
                    PRIMARY KEY(listaId, cantoId),
                    FOREIGN KEY(listaId)
                        REFERENCES listas(id)
                        ON DELETE CASCADE,
                    FOREIGN KEY(cantoId)
                        REFERENCES cantos(id)
                        ON DELETE CASCADE
                )
                """.trimIndent()
                    )


                    database.execSQL(
                        """
                CREATE INDEX IF NOT EXISTS
                index_lista_cantos_listaId
                ON lista_cantos(listaId)
                """.trimIndent()
                    )


                    database.execSQL(
                        """
                CREATE INDEX IF NOT EXISTS
                index_lista_cantos_cantoId
                ON lista_cantos(cantoId)
                """.trimIndent()
                    )
                }
            }
        fun getDatabase(
            context: Context
        ): RessuscitouDatabase {

            return INSTANCE
                ?: synchronized(this) {

                    val instance =
                        Room.databaseBuilder(
                            context.applicationContext,
                            RessuscitouDatabase::class.java,
                            "ressuscitou_database"
                        )
                            .addMigrations(
                                MIGRATION_3_4
                            )
                            .build()

                    INSTANCE = instance

                    instance
                }
        }
    }
}