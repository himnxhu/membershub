package com.example.ui

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.example.data.*
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch
import java.util.*

class MainViewModel(application: Application) : AndroidViewModel(application) {

    private val repository: AppRepository

    init {
        val database = AppDatabase.getDatabase(application)
        repository = AppRepository(database.dao())
    }

    // Expose database outputs directly
    val profile: StateFlow<BusinessProfile?> = repository.profile
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), null)

    val attendanceRecords: StateFlow<List<AttendanceRecord>> = repository.attendanceRecords
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    val paymentRecords: StateFlow<List<PaymentRecord>> = repository.paymentRecords
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    val notifications: StateFlow<List<BusinessNotification>> = repository.notifications
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    // UI state states
    private val _isScanning = MutableStateFlow(false)
    val isScanning: StateFlow<Boolean> = _isScanning.asStateFlow()

    private val _scanSuccess = MutableStateFlow<String?>(null)
    val scanSuccess: StateFlow<String?> = _scanSuccess.asStateFlow()

    private val _showRazorpay = MutableStateFlow(false)
    val showRazorpay: StateFlow<Boolean> = _showRazorpay.asStateFlow()

    private val _paymentInProgress = MutableStateFlow(false)
    val paymentInProgress: StateFlow<Boolean> = _paymentInProgress.asStateFlow()

    private val _paymentMessage = MutableStateFlow<String?>(null)
    val paymentMessage: StateFlow<String?> = _paymentMessage.asStateFlow()

    init {
        // Initialize with default Gym template if profile is empty
        viewModelScope.launch {
            repository.profile.first()?.let {
                // Profile exists, don't overwrite
            } ?: run {
                switchToTemplate("Gym")
            }
        }
    }

    // Initialize/Change mock demo templates
    fun switchToTemplate(businessType: String) {
        viewModelScope.launch {
            repository.clearAttendance()
            repository.clearPayments()
            repository.clearNotifications()

            val calendar = Calendar.getInstance()
            val now = calendar.timeInMillis

            // Define profile parameters based on selected demo type
            val (name, bizName, planStr, due, qPayload, em, ph) = when (businessType) {
                "Gym" -> {
                    // GYM DEFAULT TEMPLATE
                    Tuple7(
                        "Alex Carter",
                        "Gold's Hyper Gym",
                        "Gold's Elite Annual Membership",
                        1500.0,
                        "MEMBER-GYM-89104",
                        "alex.carter@gymmail.com",
                        "+1 (555) 019-2810"
                    )
                }
                "Tuition" -> {
                    // TUITION DEFAULT TEMPLATE
                    Tuple7(
                        "Samantha Patel",
                        "Sigma Physics Academy",
                        "IIT-JEE Advanced Physics Batch",
                        2800.0,
                        "STUDENT-PHYS-20391",
                        "samantha.patel@sigmaedu.com",
                        "+1 (555) 710-2384"
                    )
                }
                else -> {
                    // LIBRARY DEFAULT TEMPLATE
                    Tuple7(
                        "Derrick Vance",
                        "The Quiet Desk Library",
                        "Premium Reserved Study Desk Space",
                        800.0,
                        "LIB-READER-40293",
                        "derrick.vance@librarydesk.org",
                        "+1 (555) 304-4928"
                    )
                }
            }

            // Expiry calendar (e.g. valid till end of next month)
            val expiryCal = Calendar.getInstance()
            expiryCal.add(Calendar.MONTH, 1)
            expiryCal.set(Calendar.DAY_OF_MONTH, 30)

            val profile = BusinessProfile(
                name = name,
                businessName = bizName,
                businessType = businessType,
                planName = planStr,
                joinedDateMills = now - (15 * 24 * 3600 * 1000L), // Joined 15 days ago
                validUntilMills = expiryCal.timeInMillis,
                amountDue = due,
                autoRenew = true,
                qrCodePayload = qPayload,
                email = em,
                phoneNumber = ph
            )
            repository.saveProfile(profile)

            // 1. Generate customized notifications
            when (businessType) {
                "Gym" -> {
                    repository.addNotification(
                        BusinessNotification(
                            dateMills = now - 3600000L,
                            title = "Gym Fee due in 3 days",
                            message = "Your premium monthly workout fee of $1500.0 is due soon. Click 'Pay Now' to avoid interruption.",
                            type = "DUE"
                        )
                    )
                    repository.addNotification(
                        BusinessNotification(
                            dateMills = now - 18000000L,
                            title = "New Gym Trainer Joined",
                            message = "Give a warm welcome to Sarah Croft, our new expert Crossfit coach!",
                            type = "UPDATE"
                        )
                    )
                    repository.addNotification(
                        BusinessNotification(
                            dateMills = now - (2 * 86400000L),
                            title = "Holiday Announcement: Sunday",
                            message = "Gold's Gym will remain closed this coming Sunday for annual equipment tuning.",
                            type = "HOLIDAY"
                        )
                    )
                }
                "Tuition" -> {
                    repository.addNotification(
                        BusinessNotification(
                            dateMills = now - 1200000L,
                            title = "Sigma Academy Course Fee Due",
                            message = "Admission & Physics Course fee of $2800.0 is pending. Please complete payment securely inside the portal.",
                            type = "DUE"
                        )
                    )
                    repository.addNotification(
                        BusinessNotification(
                            dateMills = now - 24 * 3600000L,
                            title = "Mega JEE Physics Test Scheduled",
                            message = "A comprehensive 3-hour test on 'Electromagnetism & Waves' will take place next Monday at 10:00 AM.",
                            type = "ALERT"
                        )
                    )
                    repository.addNotification(
                        BusinessNotification(
                            dateMills = now - 3 * 86400000L,
                            title = "Class Timing Shifted today",
                            message = "Notice: Today's Physics lecture is shifted to 5:30 PM due to seminar hall reservations.",
                            type = "UPDATE"
                        )
                    )
                }
                else -> {
                    repository.addNotification(
                        BusinessNotification(
                            dateMills = now - 600000L,
                            title = "Monthly Library Seat Booking Fee due",
                            message = "Your reading slot subscription ($800.0) is due. Prompt payment guarantees seat locking.",
                            type = "DUE"
                        )
                    )
                    repository.addNotification(
                        BusinessNotification(
                            dateMills = now - 2 * 86400000L,
                            title = "High-Speed Fiber Upgraded",
                            message = "The reading room's wireless internet routers have been upgraded to 1 Gbps ultra-fast fiber grids.",
                            type = "UPDATE"
                        )
                    )
                    repository.addNotification(
                        BusinessNotification(
                            dateMills = now - 5 * 86400000L,
                            title = "Strict Silence Reminder",
                            message = "Please keep mobile devices muted at all times inside reading bays. Let's build a productive space!",
                            type = "ALERT"
                        )
                    )
                }
            }

            // 2. Generate customized initial attendance histories
            val rand = Random()
            for (i in 1..14) {
                val pastDayMills = now - (i * 24 * 3600 * 1000L)
                val status = if (i % 7 == 0) "Absent" else "Present"
                val details = when (businessType) {
                    "Gym" -> {
                        val sessionTypes = listOf("Upper Body Hypertrophy", "Cardio Blast HIIT", "Strength Push Workout", "Leg Day Routine", "Active Mobility")
                        sessionTypes[rand.nextInt(sessionTypes.size)]
                    }
                    "Tuition" -> {
                        val sessionTypes = listOf("Kinematics & Vectors", "Newtonian Force Laws", "Friction Mechanics Lab", "Thermodynamics Lecture", "Fluid Statics Practice")
                        sessionTypes[rand.nextInt(sessionTypes.size)]
                    }
                    else -> {
                        val sessionTypes = listOf("Quiet Focused Study", "Data Structures Homework", "Self-Assessment Test Draft", "Reading Research Papers", "Academic Prep Hour")
                        sessionTypes[rand.nextInt(sessionTypes.size)]
                    }
                }

                if (i % 7 != 0 || i % 14 == 0) { // Keep some absent and some holidays
                    repository.addAttendance(
                        AttendanceRecord(
                            dateMills = pastDayMills,
                            status = status,
                            businessType = businessType,
                            details = details
                        )
                    )
                }
            }

            // 3. Generate initial simulated payment record
            repository.addPayment(
                PaymentRecord(
                    paymentDateMills = now - (30 * 24 * 3600 * 1000L),
                    amount = due,
                    status = "Paid",
                    description = "$planStr - Baseline Setup",
                    transactionRef = "pay_HBZ_" + rand.nextInt(900000 + 100000).toString()
                )
            )
        }
    }

    // Toggle auto-renew setting
    fun toggleAutoRenew() {
        viewModelScope.launch {
            val current = profile.value ?: return@launch
            val updated = current.copy(autoRenew = !current.autoRenew)
            repository.saveProfile(updated)
        }
    }

    // Simulated QR Attendance scan trigger
    fun startQRScanner() {
        _scanSuccess.value = null
        _isScanning.value = true
    }

    fun stopQRScanner() {
        _isScanning.value = false
    }

    // Simulation of a successful QR code scanning event
    fun simulateQRScanSuccess() {
        viewModelScope.launch {
            _isScanning.value = false
            val currentProfile = profile.value ?: return@launch

            val now = System.currentTimeMillis()
            val checkedInSession = when (currentProfile.businessType) {
                "Gym" -> "Gym Entry Check-in: Workout Routine Approved"
                "Tuition" -> "Tuition Hall Entry: Physics Class Checklist"
                else -> "Quiet Reading Desks: Seat Allocation Reserved"
            }

            repository.addAttendance(
                AttendanceRecord(
                    dateMills = now,
                    status = "Present",
                    businessType = currentProfile.businessType,
                    details = checkedInSession
                )
            )

            // Trigger notification about successful check-in
            repository.addNotification(
                BusinessNotification(
                    dateMills = now,
                    title = "Attendance Checked-in Successfully",
                    message = "Your check-in has been validated. Have a productive session at ${currentProfile.businessName}!",
                    type = "UPDATE"
                )
            )

            _scanSuccess.value = "Attendance Marked!"
        }
    }

    fun dismissScanSuccess() {
        _scanSuccess.value = null
    }

    // Payment simulation flows (Razorpay Checkout Dialog triggers)
    fun openRazorpayCheckout() {
        _paymentMessage.value = null
        _paymentInProgress.value = false
        _showRazorpay.value = true
    }

    fun cancelRazorpayCheckout() {
        _showRazorpay.value = false
    }

    // Simulate clicking "Pay Now" with Razorpay
    fun processRazorpayPayment(success: Boolean) {
        viewModelScope.launch {
            _paymentInProgress.value = true
            _paymentMessage.value = null

            // Artificially delay to show premium simulated spinner
            kotlinx.coroutines.delay(1800)

            _paymentInProgress.value = false
            _showRazorpay.value = false

            val currentProfile = profile.value ?: return@launch

            if (success) {
                val now = System.currentTimeMillis()

                // Register payment record
                val transId = "pay_RZP_" + (100000 + Random().nextInt(900000))
                repository.addPayment(
                    PaymentRecord(
                        paymentDateMills = now,
                        amount = currentProfile.amountDue,
                        status = "Paid",
                        description = "Monthly Renewal: ${currentProfile.planName}",
                        transactionRef = transId
                    )
                )

                // Update Profile to zero dues and push validity outward
                val newValidity = currentProfile.validUntilMills + (30 * 24 * 3600 * 1000L) // Add 30 days
                val updatedProfile = currentProfile.copy(
                    amountDue = 0.0,
                    validUntilMills = newValidity
                )
                repository.saveProfile(updatedProfile)

                // Push success notification
                repository.addNotification(
                    BusinessNotification(
                        dateMills = now,
                        title = "Payment Received: $transId",
                        message = "Heads up! Your fee payment of \$${currentProfile.amountDue} has been cleared via Razorpay. Validity extended to End of Next Term.",
                        type = "UPDATE"
                    )
                )

                _paymentMessage.value = "Payment Successful! TxRef: $transId"
            } else {
                _paymentMessage.value = "Payment Failed or Canceled. Please try again."
            }
        }
    }

    fun dismissPaymentMessage() {
        _paymentMessage.value = null
    }

    fun markNotificationsAsRead() {
        viewModelScope.launch {
            repository.markAllNotificationsRead()
        }
    }

    // Private helper tuple
    private data class Tuple7<A, B, C, D, E, F, G>(
        val first: A,
        val second: B,
        val third: C,
        val fourth: D,
        val fifth: E,
        val sixth: F,
        val seventh: G
    )
}
