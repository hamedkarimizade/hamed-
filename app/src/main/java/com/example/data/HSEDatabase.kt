package com.example.data

import android.content.Context
import androidx.room.*
import kotlinx.coroutines.flow.Flow

// --- Room Entities ---

@Entity(tableName = "users")
data class UserEntity(
    @PrimaryKey val username: String,
    val fullName: String,
    val role: String, // Executive, HSE Officer, Operator
    val department: String,
    val activeToken: String = ""
)

@Entity(tableName = "incidents")
data class IncidentEntity(
    @PrimaryKey(autoGenerate = true) val id: Int = 0,
    val reporter: String,
    val type: String, // Incident, Near Miss, Unsafe Act, Unsafe Condition, Environmental
    val title: String,
    val location: String, // کوره دوار, آسیاب سیمان, بارگیرخانه, معدن, دپارتمان مواد خام
    val description: String,
    val date: String,
    val severity: String, // Low, Medium, High, Critical
    val isSynced: Boolean = false,
    val correctiveAction: String = "در حال بررسی توسط واحد ایمنی"
)

@Entity(tableName = "inspections")
data class InspectionEntity(
    @PrimaryKey(autoGenerate = true) val id: Int = 0,
    val inspectorName: String,
    val equipmentName: String, // کپسول آتش‌نشانی کوره, جرثقیل سقفی تعمیرات, نوار نقاله شماره ۴
    val status: String, // Safe, Unsafe, Needs Maintenance
    val inspectionDate: String,
    val checklistResult: String, // JSON or summarized text
    val isSynced: Boolean = false
)

@Entity(tableName = "ppe_items")
data class PPEItemEntity(
    @PrimaryKey val ppeType: String, // Helmet, Safety Glasses, Respirator, Gloves, Safety Shoes, Harness, Face Shield, Hearing Protection
    val nameFa: String,
    val totalStock: Int,
    val inspectedCount: Int,
    val defectiveCount: Int,
    val replacementRequired: Int,
    val lastInspectionDate: String,
    val nextReplacementDate: String,
    val instructions: String
)

@Entity(tableName = "training_items")
data class TrainingItemEntity(
    @PrimaryKey val id: String,
    val title: String,
    val titleFa: String,
    val type: String, // Video, Toolbox Talk, Lesson Learned
    val category: String, // کار در ارتفاع, کوره دوار, فضای بسته, کمک‌های اولیه
    val content: String,
    val durationOrLength: String,
    val progressPercent: Int = 0,
    val isCompleted: Boolean = false,
    val isQuizAvailable: Boolean = false,
    val quizScore: Int = -1,
    val quizPassed: Boolean = false
)

@Entity(tableName = "audit_logs")
data class AuditLogEntity(
    @PrimaryKey(autoGenerate = true) val id: Int = 0,
    val action: String,
    val operator: String,
    val timestamp: String,
    val details: String
)

@Entity(tableName = "emergency_contacts")
data class EmergencyContactEntity(
    @PrimaryKey(autoGenerate = true) val id: Int = 0,
    val name: String,
    val roleFa: String,
    val phone: String,
    val isSOSContact: Boolean = false
)

// --- DAOs ---

@Dao
interface HSEDao {
    // Users
    @Query("SELECT * FROM users WHERE username = :username LIMIT 1")
    suspend fun getUser(username: String): UserEntity?

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertUser(user: UserEntity)

    // Incidents
    @Query("SELECT * FROM incidents ORDER BY id DESC")
    fun getAllIncidents(): Flow<List<IncidentEntity>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertIncident(incident: IncidentEntity)

    @Query("UPDATE incidents SET isSynced = 1 WHERE id = :id")
    suspend fun markIncidentSynced(id: Int)

    @Query("DELETE FROM incidents WHERE id = :id")
    suspend fun deleteIncident(id: Int)

    // Inspections
    @Query("SELECT * FROM inspections ORDER BY id DESC")
    fun getAllInspections(): Flow<List<InspectionEntity>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertInspection(inspection: InspectionEntity)

    @Query("UPDATE inspections SET isSynced = 1 WHERE id = :id")
    suspend fun markInspectionSynced(id: Int)

    // PPE
    @Query("SELECT * FROM ppe_items")
    fun getAllPPEItems(): Flow<List<PPEItemEntity>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertPPEItems(items: List<PPEItemEntity>)

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertPPEItem(item: PPEItemEntity)

    @Query("DELETE FROM ppe_items WHERE ppeType = :ppeType")
    suspend fun deletePPEItem(ppeType: String)

    @Query("UPDATE ppe_items SET inspectedCount = :inspected, defectiveCount = :defective, replacementRequired = :replacement WHERE ppeType = :ppeType")
    suspend fun updatePPEInspection(ppeType: String, inspected: Int, defective: Int, replacement: Int)

    // Training
    @Query("SELECT * FROM training_items")
    fun getAllTrainingItems(): Flow<List<TrainingItemEntity>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertTrainingItems(items: List<TrainingItemEntity>)

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertTrainingItem(item: TrainingItemEntity)

    @Query("DELETE FROM training_items WHERE id = :id")
    suspend fun deleteTrainingItem(id: String)

    @Query("UPDATE training_items SET progressPercent = :progress, isCompleted = :completed WHERE id = :id")
    suspend fun updateTrainingProgress(id: String, progress: Int, completed: Boolean)

    @Query("UPDATE training_items SET quizScore = :score, quizPassed = :passed, isCompleted = 1, progressPercent = 100 WHERE id = :id")
    suspend fun saveQuizResult(id: String, score: Int, passed: Boolean)

    // Audit Logs
    @Query("SELECT * FROM audit_logs ORDER BY id DESC")
    fun getAllAuditLogs(): Flow<List<AuditLogEntity>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertAuditLog(log: AuditLogEntity)

    // Emergency Contacts
    @Query("SELECT * FROM emergency_contacts")
    fun getAllEmergencyContacts(): Flow<List<EmergencyContactEntity>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertEmergencyContacts(contacts: List<EmergencyContactEntity>)

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertEmergencyContact(contact: EmergencyContactEntity)

    @Query("DELETE FROM emergency_contacts WHERE id = :id")
    suspend fun deleteEmergencyContact(id: Int)

    // General sync queues
    @Query("SELECT COUNT(*) FROM incidents WHERE isSynced = 0")
    suspend fun getUnsyncedIncidentsCount(): Int

    @Query("SELECT COUNT(*) FROM inspections WHERE isSynced = 0")
    suspend fun getUnsyncedInspectionsCount(): Int

    @Query("SELECT * FROM incidents WHERE isSynced = 0")
    suspend fun getUnsyncedIncidents(): List<IncidentEntity>

    @Query("SELECT * FROM inspections WHERE isSynced = 0")
    suspend fun getUnsyncedInspections(): List<InspectionEntity>
}

// --- AppDatabase ---

@Database(
    entities = [
        UserEntity::class,
        IncidentEntity::class,
        InspectionEntity::class,
        PPEItemEntity::class,
        TrainingItemEntity::class,
        AuditLogEntity::class,
        EmergencyContactEntity::class
    ],
    version = 1,
    exportSchema = false
)
abstract class HSEDatabase : RoomDatabase() {
    abstract fun hseDao(): HSEDao

    companion object {
        @Volatile
        private var INSTANCE: HSEDatabase? = null

        fun getDatabase(context: Context): HSEDatabase {
            return INSTANCE ?: synchronized(this) {
                val instance = Room.databaseBuilder(
                    context.applicationContext,
                    HSEDatabase::class.java,
                    "hse_neyriz_database"
                )
                    .fallbackToDestructiveMigration()
                    .build()
                INSTANCE = instance
                instance
            }
        }
    }
}
