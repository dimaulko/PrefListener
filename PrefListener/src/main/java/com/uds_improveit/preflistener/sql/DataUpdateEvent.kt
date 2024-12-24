package com.uds_improveit.preflistener.sql

import android.content.Context
import androidx.room.ColumnInfo
import androidx.room.Dao
import androidx.room.Database
import androidx.room.Delete
import androidx.room.Entity
import androidx.room.Insert
import androidx.room.PrimaryKey
import androidx.room.Query
import androidx.room.Room
import androidx.room.RoomDatabase
import com.uds_improveit.preflistener.Builder
import com.uds_improveit.preflistener.Builder.UpdatesObject
import kotlinx.coroutines.flow.Flow
import kotlinx.serialization.json.Json

@Entity(tableName = "event")
data class DataUpdateEvent(
    @PrimaryKey(autoGenerate = true) val id: Int? = null,
    @ColumnInfo(name = "source_name") val sourceName: String,
    @ColumnInfo(name = "source_type") val sourceType: Builder.SOURCE,
    @ColumnInfo(name = "data") val data: String,
    @ColumnInfo(name = "timestamp") val timestamp: Long,
    @ColumnInfo(name = "re_connection") val reConnection: Boolean
)

fun DataUpdateEvent.toDTO(): UpdatesObject {
    val message = Builder(
        source = sourceType,
        sourceName = sourceName,
        reConnection = reConnection
    ).also { builder ->
        Json.decodeFromString<ArrayList<UpdatesObject.PrefField>>(data)
            .forEach { builder.putValue(it.keyName, it.value, it.valueType) }
    }.build()
    return message
}


@Dao
interface DataUpdateDao {
    @Insert
    suspend fun insertEvent(user: DataUpdateEvent)

    @Query("SELECT * FROM event ORDER BY timestamp ASC LIMIT 1")
    fun getLastEvent(): Flow<DataUpdateEvent?>

    @Delete
    fun deleteEvent(user: DataUpdateEvent)
}

@Database(entities = [DataUpdateEvent::class], version = 1, exportSchema = false)
abstract class PrefLoaderDatabase : RoomDatabase() {
    abstract fun userDao(): DataUpdateDao

    companion object {
        @Volatile
        private var INSTANCE: PrefLoaderDatabase? = null

        fun getDatabase(context: Context): PrefLoaderDatabase {
            return INSTANCE ?: synchronized(this) {
                val instance = Room.databaseBuilder(
                    context.applicationContext,
                    PrefLoaderDatabase::class.java,
                    "pref_history_database"
                ).fallbackToDestructiveMigration()
                    .build()
                INSTANCE = instance
                instance
            }
        }
    }
}