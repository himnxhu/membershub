package com.example.data

import android.content.Context
import androidx.room.*
import kotlinx.coroutines.flow.Flow

// ==========================================
// 1. ROOM ENTITIES (DATA MODELS)
// ==========================================

@Entity(tableName = "business_profile")
data class BusinessProfile(
    @PrimaryKey val id: Int = 1, // Only 1 profile row
    val name: String,
    val businessName: String,
    val businessType: String, // "Gym", "Tuition class", "Coaching", "Library", "Club"
    val planName: String,     // e.g. "Gold Annual Plan", "Physics Crash Course"
    val joinedDateMills: Long,
    val validUntilMills: Long,
    val amountDue: Double,
    val autoRenew: Boolean,
    val qrCodePayload: String, // Digital ID payload
    val email: String,
    val phoneNumber: String
)

@Entity(tableName = "attendance_records")
data class AttendanceRecord(
    @PrimaryKey(autoGenerate = true) val id: Int = 0,
    val dateMills: Long,
    val status: String, // "Present", "Absent", "Holiday"
    val businessType: String,
    val details: String // e.g. "Evening Workout", "Mechanics Chapter Test", "Study Session"
)

@Entity(tableName = "payment_records")
data class PaymentRecord(
    @PrimaryKey(autoGenerate = true) val id: Int = 0,
    val paymentDateMills: Long,
    val amount: Double,
    val status: String,    // "Paid", "Pending", "Failed"
    val description: String, // e.g., "Subscription Renewal" or "Class Fee"
    val transactionRef: String
)

@Entity(tableName = "business_notifications")
data class BusinessNotification(
    @PrimaryKey(autoGenerate = true) val id: Int = 0,
    val dateMills: Long,
    val title: String,
    val message: String,
    val type: String, // "ALERT", "HOLIDAY", "UPDATE", "DUE"
    val isRead: Boolean = false
)

// ==========================================
// 2. DATA ACCESS OBJECT (DAO)
// ==========================================

@Dao
interface AppDao {
    @Query("SELECT * FROM business_profile WHERE id = 1 LIMIT 1")
    fun getProfile(): Flow<BusinessProfile?>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertOrUpdateProfile(profile: BusinessProfile)

    @Query("SELECT * FROM attendance_records ORDER BY dateMills DESC")
    fun getAttendanceRecords(): Flow<List<AttendanceRecord>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertAttendance(record: AttendanceRecord)

    @Query("DELETE FROM attendance_records")
    suspend fun clearAttendance()

    @Query("SELECT * FROM payment_records ORDER BY paymentDateMills DESC")
    fun getPaymentRecords(): Flow<List<PaymentRecord>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertPayment(record: PaymentRecord)

    @Query("DELETE FROM payment_records")
    suspend fun clearPayments()

    @Query("SELECT * FROM business_notifications ORDER BY dateMills DESC")
    fun getNotifications(): Flow<List<BusinessNotification>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertNotification(notification: BusinessNotification)

    @Update
    suspend fun updateNotification(notification: BusinessNotification)

    @Query("UPDATE business_notifications SET isRead = 1")
    suspend fun markAllNotificationsAsRead()

    @Query("DELETE FROM business_notifications")
    suspend fun clearNotifications()
}

// ==========================================
// 3. ROOM DATABASE HOLDER
// ==========================================

@Database(
    entities = [
        BusinessProfile::class,
        AttendanceRecord::class,
        PaymentRecord::class,
        BusinessNotification::class
    ],
    version = 1,
    exportSchema = false
)
abstract class AppDatabase : RoomDatabase() {
    abstract fun dao(): AppDao

    companion object {
        @Volatile
        private var INSTANCE: AppDatabase? = null

        fun getDatabase(context: Context): AppDatabase {
            return INSTANCE ?: synchronized(this) {
                val instance = Room.databaseBuilder(
                    context.applicationContext,
                    AppDatabase::class.java,
                    "member_hub_database"
                )
                .fallbackToDestructiveMigration()
                .build()
                INSTANCE = instance
                instance
            }
        }
    }
}

// ==========================================
// 4. REPOSITORY
// ==========================================

class AppRepository(private val dao: AppDao) {
    val profile: Flow<BusinessProfile?> = dao.getProfile()
    val attendanceRecords: Flow<List<AttendanceRecord>> = dao.getAttendanceRecords()
    val paymentRecords: Flow<List<PaymentRecord>> = dao.getPaymentRecords()
    val notifications: Flow<List<BusinessNotification>> = dao.getNotifications()

    suspend fun saveProfile(profile: BusinessProfile) {
        dao.insertOrUpdateProfile(profile)
    }

    suspend fun addAttendance(record: AttendanceRecord) {
        dao.insertAttendance(record)
    }

    suspend fun clearAttendance() {
        dao.clearAttendance()
    }

    suspend fun addPayment(record: PaymentRecord) {
        dao.insertPayment(record)
    }

    suspend fun clearPayments() {
        dao.clearPayments()
    }

    suspend fun addNotification(notification: BusinessNotification) {
        dao.insertNotification(notification)
    }

    suspend fun markAllNotificationsRead() {
        dao.markAllNotificationsAsRead()
    }

    suspend fun updateNotification(notification: BusinessNotification) {
        dao.updateNotification(notification)
    }

    suspend fun clearNotifications() {
        dao.clearNotifications()
    }
}
