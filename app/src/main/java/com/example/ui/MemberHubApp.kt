package com.example.ui

import androidx.compose.animation.*
import androidx.compose.animation.core.*
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.drawBehind
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.PathEffect
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.example.data.*
import com.example.ui.theme.*
import java.text.SimpleDateFormat
import java.util.*

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun MemberHubApp(viewModel: MainViewModel) {
    val profile by viewModel.profile.collectAsStateWithLifecycle()
    val attendanceRecords by viewModel.attendanceRecords.collectAsStateWithLifecycle()
    val paymentRecords by viewModel.paymentRecords.collectAsStateWithLifecycle()
    val notifications by viewModel.notifications.collectAsStateWithLifecycle()

    val isScanning by viewModel.isScanning.collectAsStateWithLifecycle()
    val scanSuccess by viewModel.scanSuccess.collectAsStateWithLifecycle()
    val showRazorpay by viewModel.showRazorpay.collectAsStateWithLifecycle()
    val paymentInProgress by viewModel.paymentInProgress.collectAsStateWithLifecycle()
    val paymentMessage by viewModel.paymentMessage.collectAsStateWithLifecycle()

    var activeTab by remember { mutableStateOf("ID") }

    Scaffold(
        topBar = {
            CenterAlignedTopAppBar(
                title = {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.Center
                    ) {
                        Box(
                            modifier = Modifier
                                .size(32.dp)
                                .clip(RoundedCornerShape(8.dp))
                                .background(MaterialTheme.colorScheme.primary)
                                .padding(4.dp),
                            contentAlignment = Alignment.Center
                        ) {
                            Icon(
                                imageVector = Icons.Default.Home,
                                contentDescription = null,
                                tint = MaterialTheme.colorScheme.onPrimary,
                                modifier = Modifier.size(18.dp)
                            )
                        }
                        Spacer(modifier = Modifier.width(8.dp))
                        Text(
                            text = if (profile != null) "${profile?.businessName}" else "Member Hub",
                            fontSize = 18.sp,
                            fontWeight = FontWeight.Bold,
                            fontFamily = FontFamily.SansSerif,
                            color = MaterialTheme.colorScheme.onBackground
                        )
                    }
                },
                colors = TopAppBarDefaults.centerAlignedTopAppBarColors(
                    containerColor = MaterialTheme.colorScheme.background,
                    titleContentColor = MaterialTheme.colorScheme.onBackground
                )
            )
        },
        bottomBar = {
            NavigationBar(
                containerColor = MaterialTheme.colorScheme.surface,
                tonalElevation = 8.dp,
                windowInsets = WindowInsets.navigationBars
            ) {
                NavigationBarItem(
                    selected = activeTab == "ID",
                    onClick = { activeTab = "ID" },
                    icon = { Icon(Icons.Default.AccountCircle, contentDescription = "Digital ID") },
                    label = { Text("ID Card", fontSize = 11.sp, fontWeight = FontWeight.Medium) },
                    colors = NavigationBarItemDefaults.colors(
                        selectedIconColor = MaterialTheme.colorScheme.primary,
                        unselectedIconColor = MaterialTheme.colorScheme.secondary
                    ),
                    modifier = Modifier.testTag("nav_id_card")
                )
                NavigationBarItem(
                    selected = activeTab == "ATTENDANCE",
                    onClick = { activeTab = "ATTENDANCE" },
                    icon = { Icon(Icons.Default.CheckCircle, contentDescription = "Attendance") },
                    label = { Text("Attendance", fontSize = 11.sp, fontWeight = FontWeight.Medium) },
                    colors = NavigationBarItemDefaults.colors(
                        selectedIconColor = MaterialTheme.colorScheme.primary,
                        unselectedIconColor = MaterialTheme.colorScheme.secondary
                    ),
                    modifier = Modifier.testTag("nav_attendance")
                )
                NavigationBarItem(
                    selected = activeTab == "PAYMENTS",
                    onClick = { activeTab = "PAYMENTS" },
                    icon = { Icon(Icons.Default.Settings, contentDescription = "Payments") }, // Use Settings icon as currency/card symbol representation
                    label = { Text("Payments", fontSize = 11.sp, fontWeight = FontWeight.Medium) },
                    colors = NavigationBarItemDefaults.colors(
                        selectedIconColor = MaterialTheme.colorScheme.primary,
                        unselectedIconColor = MaterialTheme.colorScheme.secondary
                    ),
                    modifier = Modifier.testTag("nav_payments")
                )
                NavigationBarItem(
                    selected = activeTab == "UPDATES",
                    onClick = { activeTab = "UPDATES" },
                    icon = {
                        Box {
                            Icon(Icons.Default.Notifications, contentDescription = "Updates")
                            val unreadCount = notifications.count { !it.isRead }
                            if (unreadCount > 0) {
                                Box(
                                    modifier = Modifier
                                        .size(16.dp)
                                        .align(Alignment.TopEnd)
                                        .background(Color.Red, CircleShape)
                                        .border(1.dp, MaterialTheme.colorScheme.surface, CircleShape),
                                    contentAlignment = Alignment.Center
                                ) {
                                    Text(
                                        text = unreadCount.toString(),
                                        color = Color.White,
                                        fontSize = 10.sp,
                                        fontWeight = FontWeight.Bold
                                    )
                                }
                            }
                        }
                    },
                    label = { Text("Notice Wall", fontSize = 11.sp, fontWeight = FontWeight.Medium) },
                    colors = NavigationBarItemDefaults.colors(
                        selectedIconColor = MaterialTheme.colorScheme.primary,
                        unselectedIconColor = MaterialTheme.colorScheme.secondary
                    ),
                    modifier = Modifier.testTag("nav_updates")
                )
            }
        },
        containerColor = MaterialTheme.colorScheme.background
    ) { innerPadding ->
        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
        ) {
            when (activeTab) {
                "ID" -> IDCardScreen(
                    profile = profile,
                    onSwitchTemplate = { viewModel.switchToTemplate(it) }
                )
                "ATTENDANCE" -> AttendanceScreen(
                    profile = profile,
                    records = attendanceRecords,
                    isScanning = isScanning,
                    scanSuccess = scanSuccess,
                    onStartScan = { viewModel.startQRScanner() },
                    onStopScan = { viewModel.stopQRScanner() },
                    onSimulateScanSuccess = { viewModel.simulateQRScanSuccess() },
                    onDismissScanSuccess = { viewModel.dismissScanSuccess() }
                )
                "PAYMENTS" -> PaymentsScreen(
                    profile = profile,
                    paymentRecords = paymentRecords,
                    onPayNow = { viewModel.openRazorpayCheckout() },
                    onToggleAutoRenew = { viewModel.toggleAutoRenew() }
                )
                "UPDATES" -> UpdatesScreen(
                    notifications = notifications,
                    onMarkAllRead = { viewModel.markNotificationsAsRead() }
                )
            }

            // Simulated Razorpay Checkout Dialog Portal
            if (showRazorpay && profile != null) {
                RazorpaySimulationDialog(
                    amount = profile!!.amountDue,
                    businessName = profile!!.businessName,
                    planName = profile!!.planName,
                    inProgress = paymentInProgress,
                    onConfirm = { viewModel.processRazorpayPayment(true) },
                    onCancel = { viewModel.cancelRazorpayCheckout() },
                    onFail = { viewModel.processRazorpayPayment(false) }
                )
            }

            // Dynamic Toast popups
            paymentMessage?.let { msg ->
                Dialog(onDismissRequest = { viewModel.dismissPaymentMessage() }) {
                    Card(
                        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant),
                        shape = RoundedCornerShape(16.dp),
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(16.dp)
                            .testTag("payment_status_banner")
                    ) {
                        Column(
                            modifier = Modifier.padding(24.dp),
                            horizontalAlignment = Alignment.CenterHorizontally
                        ) {
                            val isOk = msg.startsWith("Payment Successful")
                            Icon(
                                imageVector = if (isOk) Icons.Default.CheckCircle else Icons.Default.Warning,
                                contentDescription = null,
                                tint = if (isOk) Color(0xFF10B981) else Color(0xFFEF4444),
                                modifier = Modifier.size(54.dp)
                            )
                            Spacer(modifier = Modifier.height(16.dp))
                            Text(
                                text = if (isOk) "Success!" else "Transaction Alert",
                                fontWeight = FontWeight.Bold,
                                fontSize = 20.sp,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                            Spacer(modifier = Modifier.height(8.dp))
                            Text(
                                text = msg,
                                textAlign = TextAlign.Center,
                                fontSize = 14.sp,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                            Spacer(modifier = Modifier.height(24.dp))
                            Button(
                                onClick = { viewModel.dismissPaymentMessage() },
                                modifier = Modifier.fillMaxWidth().testTag("payment_status_dismiss")
                            ) {
                                Text("Acknowledge")
                            }
                        }
                    }
                }
            }
        }
    }
}

// ==========================================
// SCREEN 1: DIGITAL ID CARD & PROFILE
// ==========================================
@Composable
fun IDCardScreen(
    profile: BusinessProfile?,
    onSwitchTemplate: (String) -> Unit
) {
    if (profile == null) {
        Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
            CircularProgressIndicator()
        }
        return
    }

    // Dynamic days remaining calculation
    val daysRemaining = remember(profile.validUntilMills) {
        val delta = profile.validUntilMills - System.currentTimeMillis()
        val days = (delta / (24 * 3600 * 1000L)).coerceAtLeast(0L)
        days
    }

    LazyColumn(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        // Quick interactive Business Sandbox Switcher
        item {
            Card(
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                border = BorderStroke(1.dp, MaterialTheme.colorScheme.primary.copy(alpha = 0.2f)),
                shape = RoundedCornerShape(12.dp),
                modifier = Modifier.fillMaxWidth()
            ) {
                Column(modifier = Modifier.padding(16.dp)) {
                    Text(
                        text = "EXPLORE CLIENT SETUP DEMOS:",
                        fontSize = 11.sp,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.primary,
                        modifier = Modifier.padding(bottom = 8.dp)
                    )
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        listOf("Gym", "Tuition", "Library").forEach { type ->
                            val active = profile.businessType == type
                            Button(
                                onClick = { onSwitchTemplate(type) },
                                colors = ButtonDefaults.buttonColors(
                                    containerColor = if (active) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.surfaceVariant,
                                    contentColor = if (active) MaterialTheme.colorScheme.onPrimary else MaterialTheme.colorScheme.onSurfaceVariant
                                ),
                                shape = RoundedCornerShape(8.dp),
                                modifier = Modifier
                                    .weight(1f)
                                    .testTag("switch_to_${type.lowercase()}")
                            ) {
                                Text(
                                    text = when (type) {
                                        "Gym" -> "🏋️ Gym"
                                        "Tuition" -> "📚 Class"
                                        else -> "🏛️ Library"
                                    },
                                    fontSize = 12.sp,
                                    fontWeight = FontWeight.Bold
                                )
                            }
                        }
                    }
                }
            }
        }

        // The high-contrast visual interactive Digital ID card
        item {
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .aspectRatio(0.64f) // Standard credit/ID card portrait ratio
                    .clip(RoundedCornerShape(20.dp))
                    .background(
                        Brush.verticalGradient(
                            colors = listOf(
                                MaterialTheme.colorScheme.surface,
                                MaterialTheme.colorScheme.surface.copy(alpha = 0.9f),
                                Color(0xFF0F172A)
                            )
                        )
                    )
                    .border(2.dp, MaterialTheme.colorScheme.primary.copy(alpha = 0.4f), RoundedCornerShape(20.dp))
                    .testTag("digital_id_card")
            ) {
                // Diagonal background stripes representation using Canvas
                Canvas(modifier = Modifier.fillMaxSize()) {
                    drawCircle(
                        color = ElegantPrimary.copy(alpha = 0.08f),
                        radius = 200.dp.toPx(),
                        center = Offset(size.width, 0f)
                    )
                    drawCircle(
                        color = ElegantAccent.copy(alpha = 0.05f),
                        radius = 150.dp.toPx(),
                        center = Offset(0f, size.height)
                    )
                }

                Column(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(24.dp),
                    horizontalAlignment = Alignment.CenterHorizontally,
                    verticalArrangement = Arrangement.SpaceBetween
                ) {
                    // ID Header
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Text(
                            text = "DIGITAL MEMBER ID",
                            fontSize = 12.sp,
                            fontWeight = FontWeight.Bold,
                            color = MaterialTheme.colorScheme.primary,
                            letterSpacing = 1.5.sp
                        )
                        Box(
                            modifier = Modifier
                                .clip(RoundedCornerShape(6.dp))
                                .background(Color(0xFF10B981).copy(alpha = 0.15f))
                                .padding(horizontal = 8.dp, vertical = 4.dp)
                        ) {
                            Text(
                                text = "ACTIVE",
                                color = Color(0xFF10B981),
                                fontSize = 10.sp,
                                fontWeight = FontWeight.Bold
                            )
                        }
                    }

                    // Avatar & Info
                    Column(
                        horizontalAlignment = Alignment.CenterHorizontally,
                        modifier = Modifier.padding(vertical = 12.dp)
                    ) {
                        // User Avatar placeholder
                        Box(
                            modifier = Modifier
                                .size(72.dp)
                                .clip(CircleShape)
                                .background(MaterialTheme.colorScheme.primary.copy(alpha = 0.1f))
                                .border(1.5.dp, MaterialTheme.colorScheme.primary, CircleShape),
                            contentAlignment = Alignment.Center
                        ) {
                            Icon(
                                imageVector = Icons.Default.AccountCircle,
                                contentDescription = null,
                                tint = MaterialTheme.colorScheme.primary,
                                modifier = Modifier.size(48.dp)
                            )
                        }

                        Spacer(modifier = Modifier.height(12.dp))

                        Text(
                            text = profile.name,
                            fontSize = 20.sp,
                            fontWeight = FontWeight.Bold,
                            color = Color.White
                        )
                        Text(
                            text = profile.planName,
                            fontSize = 12.sp,
                            fontWeight = FontWeight.Medium,
                            color = Color.LightGray,
                            textAlign = TextAlign.Center,
                            modifier = Modifier.padding(horizontal = 8.dp)
                        )
                    }

                    // Simulated QR Code Graphic Widget
                    Card(
                        colors = CardDefaults.cardColors(containerColor = Color.White),
                        shape = RoundedCornerShape(12.dp),
                        modifier = Modifier
                            .size(130.dp)
                            .padding(8.dp)
                    ) {
                        Box(
                            modifier = Modifier
                                .fillMaxSize()
                                .padding(4.dp),
                            contentAlignment = Alignment.Center
                        ) {
                            QRCodeDrawing(payload = profile.qrCodePayload)
                        }
                    }

                    // Payload debug info under the card QR code
                    Text(
                        text = profile.qrCodePayload,
                        fontSize = 9.sp,
                        fontFamily = FontFamily.Monospace,
                        color = Color.Gray
                    )

                    // Card Footer with dates
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Column {
                            Text(
                                text = "MEMBER SINCE",
                                fontSize = 8.sp,
                                color = Color.Gray,
                                fontWeight = FontWeight.Bold
                            )
                            Text(
                                text = formatDate(profile.joinedDateMills),
                                fontSize = 11.sp,
                                color = Color.White,
                                fontWeight = FontWeight.SemiBold
                            )
                        }

                        Column(horizontalAlignment = Alignment.End) {
                            Text(
                                text = "VALID THRU",
                                fontSize = 8.sp,
                                color = Color.Gray,
                                fontWeight = FontWeight.Bold
                            )
                            Text(
                                text = formatDate(profile.validUntilMills),
                                fontSize = 11.sp,
                                color = Color.White,
                                fontWeight = FontWeight.SemiBold
                            )
                        }
                    }
                }
            }
        }

        // Validity remaining summary strip
        item {
            Card(
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant),
                shape = RoundedCornerShape(16.dp),
                modifier = Modifier.fillMaxWidth()
            ) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(16.dp),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Box(
                            modifier = Modifier
                                .size(40.dp)
                                .clip(CircleShape)
                                .background(MaterialTheme.colorScheme.primary.copy(alpha = 0.15f)),
                            contentAlignment = Alignment.Center
                        ) {
                            Icon(
                                imageVector = Icons.Default.Info, // simple standard info icon representation
                                contentDescription = null,
                                tint = MaterialTheme.colorScheme.primary
                            )
                        }
                        Spacer(modifier = Modifier.width(12.dp))
                        Column {
                            Text(
                                text = "Membership Validity",
                                fontSize = 14.sp,
                                fontWeight = FontWeight.Bold
                            )
                            Text(
                                text = "Remaining: $daysRemaining Days",
                                fontSize = 12.sp,
                                color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.8f)
                            )
                        }
                    }

                    Box(
                        modifier = Modifier
                            .clip(RoundedCornerShape(8.dp))
                            .background(
                                if (daysRemaining > 7) Color(0xFF10B981).copy(alpha = 0.15f)
                                else Color(0xFFF59E0B).copy(alpha = 0.15f)
                            )
                            .padding(horizontal = 10.dp, vertical = 6.dp)
                    ) {
                        Text(
                            text = if (daysRemaining > 7) "Healthy" else "Ending Soon",
                            color = if (daysRemaining > 7) Color(0xFF10B981) else Color(0xFFF59E0B),
                            fontSize = 11.sp,
                            fontWeight = FontWeight.Bold
                        )
                    }
                }
            }
        }

        // Basic Profile Contact Card Info Group
        item {
            Card(
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                shape = RoundedCornerShape(16.dp),
                modifier = Modifier.fillMaxWidth()
            ) {
                Column(modifier = Modifier.padding(16.dp)) {
                    Text(
                        text = "PERSONAL CREDENTIALS",
                        fontSize = 11.sp,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.secondary,
                        modifier = Modifier.padding(bottom = 12.dp)
                    )

                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(vertical = 4.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Icon(
                            imageVector = Icons.Default.AccountCircle, // standard email represent
                            contentDescription = null,
                            tint = MaterialTheme.colorScheme.primary,
                            modifier = Modifier.size(16.dp)
                        )
                        Spacer(modifier = Modifier.width(12.dp))
                        Column {
                            Text(text = "Email Address", fontSize = 10.sp, color = MaterialTheme.colorScheme.secondary)
                            Text(text = profile.email, fontSize = 13.sp, color = MaterialTheme.colorScheme.onSurface)
                        }
                    }

                    Divider(modifier = Modifier.padding(vertical = 10.dp), color = MaterialTheme.colorScheme.surfaceVariant)

                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(vertical = 4.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Icon(
                            imageVector = Icons.Default.AccountCircle, // standard phone represent
                            contentDescription = null,
                            tint = MaterialTheme.colorScheme.primary,
                            modifier = Modifier.size(16.dp)
                        )
                        Spacer(modifier = Modifier.width(12.dp))
                        Column {
                            Text(text = "Linked Mobile", fontSize = 10.sp, color = MaterialTheme.colorScheme.secondary)
                            Text(text = profile.phoneNumber, fontSize = 13.sp, color = MaterialTheme.colorScheme.onSurface)
                        }
                    }
                }
            }
        }
    }
}

// Draw a beautiful modular simulated vector QR code grid onto Canvas perfectly
@Composable
fun QRCodeDrawing(payload: String) {
    Canvas(modifier = Modifier.fillMaxSize()) {
        val width = size.width
        val height = size.height

        // Background white
        drawRect(Color.White, size = Size(width, height))

        // Large 3 Outer Scanning Finder Position squares
        val finderSize = width * 0.28f
        val finderStroke = width * 0.035f

        fun drawFinder(x: Float, y: Float) {
            // Outer block outline
            drawRect(
                color = Color.Black,
                topLeft = Offset(x, y),
                size = Size(finderSize, finderSize),
                style = Stroke(width = finderStroke)
            )
            // Inner mini core
            val coreSize = finderSize * 0.4f
            val offset = (finderSize - coreSize) / 2f
            drawRect(
                color = Color.Black,
                topLeft = Offset(x + offset, y + offset),
                size = Size(coreSize, coreSize)
            )
        }

        // Top Left finder
        drawFinder(0f, 0f)
        // Top Right finder
        drawFinder(width - finderSize, 0f)
        // Bottom Left finder
        drawFinder(0f, height - finderSize)

        // Draw individual data dots based on payload string hashes to ensure variation between payloads
        val hash = payload.hashCode()
        val numGrid = 15
        val dotWidth = width / numGrid
        val dotHeight = height / numGrid

        val rand = Random(hash.toLong())

        for (r in 0 until numGrid) {
            for (c in 0 until numGrid) {
                // Avoid finder corners overlapping directly
                val inTopLeft = r < 5 && c < 5
                val inTopRight = r < 5 && c >= numGrid - 5
                val inBottomLeft = r >= numGrid - 5 && c < 5

                if (!inTopLeft && !inTopRight && !inBottomLeft) {
                    val isBlack = rand.nextBoolean()
                    if (isBlack) {
                        drawRect(
                            color = Color.Black,
                            topLeft = Offset(c * dotWidth, r * dotHeight),
                            size = Size(dotWidth * 0.85f, dotHeight * 0.85f)
                        )
                    }
                }
            }
        }
    }
}

// ==========================================
// SCREEN 2: ATTENDANCE SCANNER & RECORDS
// ==========================================
@Composable
fun AttendanceScreen(
    profile: BusinessProfile?,
    records: List<AttendanceRecord>,
    isScanning: Boolean,
    scanSuccess: String?,
    onStartScan: () -> Unit,
    onStopScan: () -> Unit,
    onSimulateScanSuccess: () -> Unit,
    onDismissScanSuccess: () -> Unit
) {
    if (profile == null) return

    val currentMonthRecords = remember(records) {
        val now = Calendar.getInstance()
        val thisMonth = now.get(Calendar.MONTH)
        records.filter {
            val cal = Calendar.getInstance().apply { timeInMillis = it.dateMills }
            cal.get(Calendar.MONTH) == thisMonth
        }
    }

    val attendancePercentage = remember(currentMonthRecords) {
        val totalExpected = 25
        val presentCount = currentMonthRecords.count { it.status == "Present" }
        ((presentCount.toFloat() / totalExpected.toFloat()) * 100f).coerceIn(0f, 100f)
    }

    if (isScanning) {
        // Holographic simulated QR scanner screen with lasers
        ScannerSimulationScreen(
            onCancel = onStopScan,
            onTriggerSuccess = onSimulateScanSuccess
        )
    } else {
        LazyColumn(
            modifier = Modifier
                .fillMaxSize()
                .padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            // Main check-in actions CTA Area
            item {
                Card(
                    colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                    shape = RoundedCornerShape(16.dp),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Column(
                        modifier = Modifier.padding(20.dp),
                        horizontalAlignment = Alignment.CenterHorizontally
                    ) {
                        Text(
                            text = "INSTANT TOUCHLESS ATTENDANCE",
                            fontSize = 11.sp,
                            fontWeight = FontWeight.Bold,
                            color = MaterialTheme.colorScheme.primary,
                            letterSpacing = 1.sp
                        )
                        Spacer(modifier = Modifier.height(4.dp))
                        Text(
                            text = "Scan Business Code to Check-in",
                            fontSize = 14.sp,
                            color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.7f),
                            textAlign = TextAlign.Center
                        )

                        Spacer(modifier = Modifier.height(20.dp))

                        // Large round scanning pulsing icon button
                        Box(
                            modifier = Modifier
                                .size(96.dp)
                                .clip(CircleShape)
                                .background(MaterialTheme.colorScheme.primary.copy(alpha = 0.1f))
                                .border(1.5.dp, MaterialTheme.colorScheme.primary, CircleShape)
                                .clickable { onStartScan() }
                                .testTag("qrcode_scan_button"),
                            contentAlignment = Alignment.Center
                        ) {
                            Icon(
                                imageVector = Icons.Default.Check, // Scanner checkmark icon representation
                                contentDescription = "Scan Now",
                                tint = MaterialTheme.colorScheme.primary,
                                modifier = Modifier.size(44.dp)
                            )
                        }

                        Spacer(modifier = Modifier.height(16.dp))

                        Button(
                            onClick = onStartScan,
                            shape = RoundedCornerShape(12.dp),
                            modifier = Modifier.fillMaxWidth().testTag("scan_trigger_button")
                        ) {
                            Text("Launch Scan Camera Client", fontWeight = FontWeight.Bold)
                        }
                    }
                }
            }

            // Quick display metrics: Monthly percentage indicator
            item {
                Card(
                    colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                    shape = RoundedCornerShape(16.dp),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(16.dp),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Column {
                            Text(
                                text = "ATTENDANCE PERCENTAGE",
                                fontSize = 10.sp,
                                fontWeight = FontWeight.Bold,
                                color = MaterialTheme.colorScheme.secondary
                            )
                            Spacer(modifier = Modifier.height(4.dp))

                            val presentCount = currentMonthRecords.count { it.status == "Present" }
                            Text(
                                text = when (profile.businessType) {
                                    "Gym" -> "Attended $presentCount/25 days this month"
                                    "Tuition" -> "${profile.planName.split(" ").firstOrNull() ?: "Class"}: ${attendancePercentage.toInt()}% attendance"
                                    else -> "Study desks: $presentCount Active sessions"
                                },
                                fontSize = 14.sp,
                                fontWeight = FontWeight.Bold,
                                color = MaterialTheme.colorScheme.onSurface
                            )
                        }

                        // Circular progress indicator
                        Box(contentAlignment = Alignment.Center, modifier = Modifier.size(54.dp)) {
                            CircularProgressIndicator(
                                progress = attendancePercentage / 100f,
                                modifier = Modifier.fillMaxSize(),
                                strokeWidth = 5.dp,
                                color = if (attendancePercentage > 75) Color(0xFF10B981) else Color(0xFFF59E0B),
                                trackColor = MaterialTheme.colorScheme.surfaceVariant
                            )
                            Text(
                                text = "${attendancePercentage.toInt()}%",
                                fontSize = 12.sp,
                                fontWeight = FontWeight.Bold
                            )
                        }
                    }
                }
            }

            // Historical lists
            item {
                Text(
                    text = "ATTENDANCE HISTORIC LOGS",
                    fontSize = 11.sp,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.secondary,
                    modifier = Modifier.padding(start = 4.dp, top = 8.dp)
                )
            }

            if (records.isEmpty()) {
                item {
                    Card(
                        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                        shape = RoundedCornerShape(12.dp),
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Column(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(32.dp),
                            horizontalAlignment = Alignment.CenterHorizontally
                        ) {
                            Icon(
                                imageVector = Icons.Default.Warning,
                                contentDescription = null,
                                tint = MaterialTheme.colorScheme.secondary,
                                modifier = Modifier.size(44.dp)
                            )
                            Spacer(modifier = Modifier.height(8.dp))
                            Text(text = "No Check-in history found.", color = MaterialTheme.colorScheme.secondary)
                        }
                    }
                }
            } else {
                items(records) { item ->
                    Card(
                        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant),
                        shape = RoundedCornerShape(12.dp),
                        modifier = Modifier
                            .fillMaxWidth()
                            .testTag("attendance_item_${item.id}")
                    ) {
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(14.dp),
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.SpaceBetween
                        ) {
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Box(
                                    modifier = Modifier
                                        .size(36.dp)
                                        .clip(CircleShape)
                                        .background(
                                            if (item.status == "Present") Color(0xFF10B981).copy(alpha = 0.15f)
                                            else Color(0xFFEF4444).copy(alpha = 0.15f)
                                        ),
                                    contentAlignment = Alignment.Center
                                ) {
                                    Icon(
                                        imageVector = if (item.status == "Present") Icons.Default.Check else Icons.Default.Warning,
                                        contentDescription = null,
                                        tint = if (item.status == "Present") Color(0xFF10B981) else Color(0xFFEF4444),
                                        modifier = Modifier.size(18.dp)
                                    )
                                }
                                Spacer(modifier = Modifier.width(12.dp))
                                Column {
                                    Text(
                                        text = item.details,
                                        fontSize = 13.sp,
                                        fontWeight = FontWeight.Bold
                                    )
                                    Text(
                                        text = formatDate(item.dateMills),
                                        fontSize = 11.sp,
                                        color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.7f)
                                    )
                                }
                            }

                            Box(
                                modifier = Modifier
                                    .clip(RoundedCornerShape(6.dp))
                                    .background(
                                        if (item.status == "Present") Color(0xFF10B981).copy(alpha = 0.1f)
                                        else Color(0xFFEF4444).copy(alpha = 0.1f)
                                    )
                                    .padding(horizontal = 8.dp, vertical = 4.dp)
                            ) {
                                Text(
                                    text = item.status.uppercase(),
                                    color = if (item.status == "Present") Color(0xFF10B981) else Color(0xFFEF4444),
                                    fontSize = 10.sp,
                                    fontWeight = FontWeight.Bold
                                )
                            }
                        }
                    }
                }
            }
        }
    }

    // Success check-in result dialog
    scanSuccess?.let { successMsg ->
        Dialog(onDismissRequest = { onDismissScanSuccess() }) {
            Card(
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                shape = RoundedCornerShape(16.dp),
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(16.dp)
                    .testTag("attendance_success_banner")
            ) {
                Column(
                    modifier = Modifier.padding(24.dp),
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    Box(
                        modifier = Modifier
                            .size(64.dp)
                            .clip(CircleShape)
                            .background(Color(0xFF10B981).copy(alpha = 0.2f)),
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(
                            imageVector = Icons.Default.CheckCircle,
                            contentDescription = null,
                            tint = Color(0xFF10B981),
                            modifier = Modifier.size(44.dp)
                        )
                    }

                    Spacer(modifier = Modifier.height(16.dp))

                    Text(
                        text = "Attendance Valid!",
                        fontWeight = FontWeight.Bold,
                        fontSize = 18.sp
                    )

                    Spacer(modifier = Modifier.height(8.dp))

                    Text(
                        text = "Checked-in successfully under: ${profile.businessName}. Have a wonderful session!",
                        textAlign = TextAlign.Center,
                        fontSize = 13.sp,
                        color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.8f)
                    )

                    Spacer(modifier = Modifier.height(24.dp))

                    Button(
                        onClick = onDismissScanSuccess,
                        colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF10B981)),
                        modifier = Modifier.fillMaxWidth().testTag("attendance_success_dismiss")
                    ) {
                        Text("Awesome", color = Color.White, fontWeight = FontWeight.Bold)
                    }
                }
            }
        }
    }
}

// Full screen custom grid animated holograph scanner simulation using Compose Canvas
@Composable
fun ScannerSimulationScreen(
    onCancel: () -> Unit,
    onTriggerSuccess: () -> Unit
) {
    var scannerYOffset by remember { mutableStateOf(0f) }

    // Loop transition to animate scanning vertical red laser bar
    val infiniteTransition = rememberInfiniteTransition(label = "laser")
    val laserMultiplier by infiniteTransition.animateFloat(
        initialValue = 0f,
        targetValue = 1f,
        animationSpec = infiniteRepeatable(
            animation = tween(2300, easing = LinearEasing),
            repeatMode = RepeatMode.Reverse
        ),
        label = "laser_pos"
    )

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(ElegantBackground)
    ) {
        // Futuristic cyber grids
        Canvas(modifier = Modifier.fillMaxSize()) {
            val stepsCols = 15
            val stepsRows = 25
            val xGap = size.width / stepsCols
            val yGap = size.height / stepsRows

            // Draw grid lines
            for (i in 1..stepsCols) {
                drawLine(
                    color = ElegantPrimary.copy(alpha = 0.12f),
                    start = Offset(i * xGap, 0f),
                    end = Offset(i * xGap, size.height),
                    strokeWidth = 1f
                )
            }
            for (i in 1..stepsRows) {
                drawLine(
                    color = ElegantPrimary.copy(alpha = 0.12f),
                    start = Offset(0f, i * yGap),
                    end = Offset(size.width, i * yGap),
                    strokeWidth = 1f
                )
            }

            // Scanning laser line drawing
            val currentLaserY = size.height * laserMultiplier
            drawLine(
                color = ElegantAccent,
                start = Offset(0f, currentLaserY),
                end = Offset(size.width, currentLaserY),
                strokeWidth = 4.dp.toPx()
            )

            // Laser ambient glow underneath
            drawRect(
                brush = Brush.verticalGradient(
                    colors = listOf(
                        ElegantAccent.copy(alpha = 0.15f),
                        Color.Transparent
                    )
                ),
                topLeft = Offset(0f, currentLaserY),
                size = Size(size.width, 40.dp.toPx())
            )
        }

        // Overlay Scanning Bracket Window
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(32.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.SpaceBetween
        ) {
            Column(horizontalAlignment = Alignment.CenterHorizontally) {
                Text(
                    text = "QR SCANNER ENGAGED",
                    color = ElegantAccent,
                    fontWeight = FontWeight.Bold,
                    fontSize = 14.sp,
                    letterSpacing = 2.sp
                )
                Text(
                    text = "Align gym or coaching desk QR code to fit target window",
                    color = Color.LightGray,
                    fontSize = 11.sp,
                    textAlign = TextAlign.Center,
                    modifier = Modifier.padding(top = 4.dp)
                )
            }

            // Standard target visual square brackets
            Box(
                modifier = Modifier
                    .size(220.dp)
                    .border(2.dp, ElegantPrimary.copy(alpha = 0.3f), RoundedCornerShape(16.dp)),
                contentAlignment = Alignment.Center
            ) {
                // Brackets inside
                Canvas(modifier = Modifier.fillMaxSize()) {
                    val pad = 12.dp.toPx()
                    val len = 32.dp.toPx()
                    val st = 4.dp.toPx()
                    val w = size.width
                    val h = size.height

                    // Top Left
                    drawLine(ElegantPrimary, Offset(pad, pad), Offset(pad + len, pad), strokeWidth = st)
                    drawLine(ElegantPrimary, Offset(pad, pad), Offset(pad, pad + len), strokeWidth = st)

                    // Top Right
                    drawLine(ElegantPrimary, Offset(w - pad, pad), Offset(w - pad - len, pad), strokeWidth = st)
                    drawLine(ElegantPrimary, Offset(w - pad, pad), Offset(w - pad, pad + len), strokeWidth = st)

                    // Bottom Left
                    drawLine(ElegantPrimary, Offset(pad, h - pad), Offset(pad + len, h - pad), strokeWidth = st)
                    drawLine(ElegantPrimary, Offset(pad, h - pad), Offset(pad, h - pad - len), strokeWidth = st)

                    // Bottom Right
                    drawLine(ElegantPrimary, Offset(w - pad, h - pad), Offset(w - pad - len, h - pad), strokeWidth = st)
                    drawLine(ElegantPrimary, Offset(w - pad, h - pad), Offset(w - pad, h - pad - len), strokeWidth = st)
                }

                // Inner pulsing QR graphic represent
                Card(
                    colors = CardDefaults.cardColors(containerColor = Color.White.copy(alpha = 0.85f)),
                    shape = RoundedCornerShape(12.dp),
                    modifier = Modifier.size(110.dp)
                ) {
                    Box(modifier = Modifier.padding(8.dp)) {
                        QRCodeDrawing(payload = "SCAN-MOCK-GRID")
                    }
                }
            }

            // CTAs
            Column(
                modifier = Modifier.fillMaxWidth(),
                verticalArrangement = Arrangement.spacedBy(10.dp),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                Button(
                    onClick = onTriggerSuccess,
                    colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF10B981)),
                    modifier = Modifier
                        .fillMaxWidth()
                        .testTag("simulate_scanned_success")
                ) {
                    Text("Simulate Scaled QR Detection", fontWeight = FontWeight.Bold, color = Color.White)
                }

                TextButton(onClick = onCancel) {
                    Text("Cancel Camera Stream", color = Color.White.copy(alpha = 0.6f))
                }
            }
        }
    }
}

// ==========================================
// SCREEN 3: SUBSCRIPTION PAYMENT CENTER
// ==========================================
@Composable
fun PaymentsScreen(
    profile: BusinessProfile?,
    paymentRecords: List<PaymentRecord>,
    onPayNow: () -> Unit,
    onToggleAutoRenew: () -> Unit
) {
    if (profile == null) return

    LazyColumn(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        // Due alert card banner
        item {
            Card(
                colors = CardDefaults.cardColors(
                    containerColor = if (profile.amountDue > 0) MaterialTheme.colorScheme.primary.copy(alpha = 0.08f)
                    else MaterialTheme.colorScheme.surfaceVariant
                ),
                border = BorderStroke(
                    width = 1.5.dp,
                    color = if (profile.amountDue > 0) MaterialTheme.colorScheme.primary.copy(alpha = 0.3f)
                    else Color.Transparent
                ),
                shape = RoundedCornerShape(16.dp),
                modifier = Modifier.fillMaxWidth()
            ) {
                Column(modifier = Modifier.padding(20.dp)) {
                    Text(
                        text = "MEMBERSHIP FEE STATUS",
                        fontSize = 10.sp,
                        fontWeight = FontWeight.Bold,
                        color = if (profile.amountDue > 0) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.secondary,
                        letterSpacing = 1.sp
                    )

                    Spacer(modifier = Modifier.height(12.dp))

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Column {
                            Text(
                                text = if (profile.amountDue > 0) "$${profile.amountDue}" else "Fully Cleared",
                                fontSize = 28.sp,
                                fontWeight = FontWeight.Black,
                                color = if (profile.amountDue > 0) Color.White else Color(0xFF10B981)
                            )
                            Spacer(modifier = Modifier.height(2.dp))
                            Text(
                                text = if (profile.amountDue > 0) "Current Subscription Due" else "Active Subscription",
                                fontSize = 12.sp,
                                color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.8f)
                            )
                        }

                        if (profile.amountDue > 0) {
                            Button(
                                onClick = onPayNow,
                                colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.primary),
                                shape = RoundedCornerShape(10.dp),
                                modifier = Modifier.testTag("pay_dues_button")
                            ) {
                                Text("Pay Now ($${profile.amountDue})", fontWeight = FontWeight.Bold)
                            }
                        }
                    }

                    if (profile.amountDue > 0) {
                        Spacer(modifier = Modifier.height(16.dp))
                        Text(
                            text = "💡 Gym or Tuition Fees auto renewals happen at midnight of expiration date.",
                            fontSize = 11.sp,
                            color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.7f)
                        )
                    }
                }
            }
        }

        // Auto Renew slider switch
        item {
            Card(
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                shape = RoundedCornerShape(16.dp),
                modifier = Modifier.fillMaxWidth()
            ) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(16.dp),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Row(verticalAlignment = Alignment.CenterVertically, modifier = Modifier.weight(1f)) {
                        Box(
                            modifier = Modifier
                                .size(40.dp)
                                .clip(CircleShape)
                                .background(MaterialTheme.colorScheme.secondary.copy(alpha = 0.15f)),
                            contentAlignment = Alignment.Center
                        ) {
                            Icon(
                                imageVector = Icons.Default.Settings,
                                contentDescription = null,
                                tint = MaterialTheme.colorScheme.primary,
                                modifier = Modifier.size(20.dp)
                            )
                        }
                        Spacer(modifier = Modifier.width(12.dp))
                        Column {
                            Text(text = "Razorpay Auto-Renew", fontSize = 14.sp, fontWeight = FontWeight.Bold)
                            Text(
                                text = "Auto pays fee before expiration date",
                                fontSize = 11.sp,
                                color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.7f)
                            )
                        }
                    }

                    Switch(
                        checked = profile.autoRenew,
                        onCheckedChange = { onToggleAutoRenew() },
                        colors = SwitchDefaults.colors(
                            checkedThumbColor = Color.White,
                            checkedTrackColor = MaterialTheme.colorScheme.primary
                        ),
                        modifier = Modifier.testTag("auto_renew_toggle")
                    )
                }
            }
        }

        // Historic Payments list headers
        item {
            Text(
                text = "TRANSACTION HISTORY",
                fontSize = 11.sp,
                fontWeight = FontWeight.Bold,
                color = MaterialTheme.colorScheme.secondary,
                modifier = Modifier.padding(start = 4.dp, top = 8.dp)
            )
        }

        if (paymentRecords.isEmpty()) {
            item {
                Card(
                    colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                    shape = RoundedCornerShape(12.dp),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Box(modifier = Modifier.padding(24.dp), contentAlignment = Alignment.Center) {
                        Text(text = "No recorded transactions.", color = MaterialTheme.colorScheme.secondary)
                    }
                }
            }
        } else {
            items(paymentRecords) { transaction ->
                Card(
                    colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                    shape = RoundedCornerShape(12.dp),
                    modifier = Modifier
                        .fillMaxWidth()
                        .testTag("payment_item_${transaction.id}")
                ) {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(16.dp),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Box(
                                modifier = Modifier
                                    .size(36.dp)
                                    .clip(CircleShape)
                                    .background(Color(0xFF10B981).copy(alpha = 0.15f)),
                                contentAlignment = Alignment.Center
                            ) {
                                Icon(
                                    imageVector = Icons.Default.Check,
                                    contentDescription = null,
                                    tint = Color(0xFF10B981),
                                    modifier = Modifier.size(16.dp)
                                )
                            }
                            Spacer(modifier = Modifier.width(12.dp))
                            Column {
                                Text(
                                    text = transaction.description,
                                    fontSize = 13.sp,
                                    fontWeight = FontWeight.Bold
                                )
                                Spacer(modifier = Modifier.height(2.dp))
                                Text(
                                    text = "Ref: ${transaction.transactionRef} • ${formatDate(transaction.paymentDateMills)}",
                                    fontSize = 10.sp,
                                    color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.6f)
                                )
                            }
                        }

                        Text(
                            text = "+$${transaction.amount}",
                            fontSize = 15.sp,
                            fontWeight = FontWeight.Black,
                            color = Color(0xFF10B981)
                        )
                    }
                }
            }
        }
    }
}

// ==========================================
// SCREEN 4: BUSINESS UPDATE NOTICE WALL
// ==========================================
@Composable
fun UpdatesScreen(
    notifications: List<BusinessNotification>,
    onMarkAllRead: () -> Unit
) {
    // Automatically flag nodes as read when view opens
    LaunchedEffect(notifications) {
        if (notifications.any { !it.isRead }) {
            onMarkAllRead()
        }
    }

    LazyColumn(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        item {
            Row(
                modifier = Modifier.fillMaxWidth().padding(bottom = 8.dp),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Text(
                    text = "LATEST BULLETINS & ALERTS",
                    fontSize = 11.sp,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.secondary
                )
                Box(
                    modifier = Modifier
                        .clip(RoundedCornerShape(6.dp))
                        .background(MaterialTheme.colorScheme.primary.copy(alpha = 0.15f))
                        .padding(horizontal = 8.dp, vertical = 4.dp)
                ) {
                    Text(
                        text = "NOTICE BOARD",
                        color = MaterialTheme.colorScheme.primary,
                        fontSize = 10.sp,
                        fontWeight = FontWeight.Bold
                    )
                }
            }
        }

        if (notifications.isEmpty()) {
            item {
                Card(
                    colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                    shape = RoundedCornerShape(12.dp),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Box(modifier = Modifier.padding(32.dp), contentAlignment = Alignment.Center) {
                        Text(text = "Notice board is empty.", color = MaterialTheme.colorScheme.secondary)
                    }
                }
            }
        } else {
            items(notifications) { post ->
                Card(
                    colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                    border = BorderStroke(
                        width = 1.dp,
                        color = if (post.type == "DUE") MaterialTheme.colorScheme.error.copy(alpha = 0.2f)
                        else Color.Transparent
                    ),
                    shape = RoundedCornerShape(16.dp),
                    modifier = Modifier
                        .fillMaxWidth()
                        .testTag("notice_item_${post.id}")
                ) {
                    Column(modifier = Modifier.padding(16.dp)) {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.SpaceBetween
                        ) {
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                val colorType = when (post.type) {
                                    "DUE", "ALERT" -> MaterialTheme.colorScheme.error
                                    "HOLIDAY" -> Color(0xFFF59E0B)
                                    else -> MaterialTheme.colorScheme.primary
                                }

                                Box(
                                    modifier = Modifier
                                        .size(8.dp)
                                        .clip(CircleShape)
                                        .background(colorType)
                                )
                                Spacer(modifier = Modifier.width(8.dp))
                                Text(
                                    text = post.type,
                                    fontSize = 10.sp,
                                    fontWeight = FontWeight.Bold,
                                    color = colorType
                                )
                            }

                            Text(
                                text = formatDate(post.dateMills),
                                fontSize = 10.sp,
                                color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.6f)
                            )
                        }

                        Spacer(modifier = Modifier.height(8.dp))

                        Text(
                            text = post.title,
                            fontSize = 15.sp,
                            fontWeight = FontWeight.Bold,
                            color = Color.White
                        )

                        Spacer(modifier = Modifier.height(4.dp))

                        Text(
                            text = post.message,
                            fontSize = 13.sp,
                            color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.8f),
                            lineHeight = 18.sp
                        )
                    }
                }
            }
        }
    }
}

// ==========================================
// SIMULATED RAZORPAY SECURE DIALOG GATEWAY
// ==========================================
@Composable
fun RazorpaySimulationDialog(
    amount: Double,
    businessName: String,
    planName: String,
    inProgress: Boolean,
    onConfirm: () -> Unit,
    onCancel: () -> Unit,
    onFail: () -> Unit
) {
    Dialog(onDismissRequest = { if (!inProgress) onCancel() }) {
        Card(
            colors = CardDefaults.cardColors(containerColor = ElegantSurface), // Deep checkout dark theme
            border = BorderStroke(1.5.dp, ElegantPrimary),
            shape = RoundedCornerShape(16.dp),
            modifier = Modifier
                .fillMaxWidth()
                .padding(8.dp)
                .testTag("razorpay_dialog")
        ) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(20.dp)
            ) {
                // Header brand row
                Row(
                    modifier = Modifier.fillMaxWidth().padding(bottom = 16.dp),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Column {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Box(
                                modifier = Modifier
                                    .size(24.dp)
                                    .clip(CircleShape)
                                    .background(ElegantPrimary),
                                contentAlignment = Alignment.Center
                            ) {
                                Text("R", color = ElegantOnPrimary, fontWeight = FontWeight.Bold, fontSize = 11.sp)
                            }
                            Spacer(modifier = Modifier.width(6.dp))
                            Text(
                                text = "razorpay",
                                fontWeight = FontWeight.Black,
                                fontSize = 14.sp,
                                color = Color.White
                            )
                        }
                        Text(
                            text = "SECURE CHECKOUT WINDOW",
                            fontSize = 8.sp,
                            color = Color.LightGray
                        )
                    }

                    Box(
                        modifier = Modifier
                            .clip(RoundedCornerShape(4.dp))
                            .background(Color.White.copy(alpha = 0.08f))
                            .padding(4.dp)
                    ) {
                        Text("TEST MODE", color = Color(0xFFFF9900), fontSize = 8.sp, fontWeight = FontWeight.Bold)
                    }
                }

                Divider(color = Color.White.copy(alpha = 0.1f))

                if (inProgress) {
                    // Beautiful processing progress indicator
                    Column(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(32.dp),
                        horizontalAlignment = Alignment.CenterHorizontally
                    ) {
                        CircularProgressIndicator(color = ElegantPrimary)
                        Spacer(modifier = Modifier.height(16.dp))
                        Text(text = "Authorizing transaction securely...", color = Color.LightGray, fontSize = 12.sp)
                        Text(text = "Do not close this panel", color = Color.Gray, fontSize = 10.sp)
                    }
                } else {
                    // Billing details summary blocks
                    Spacer(modifier = Modifier.height(12.dp))
                    Text(text = "BILLING DETAILS", fontSize = 9.sp, color = Color.Gray, fontWeight = FontWeight.Bold)
                    Spacer(modifier = Modifier.height(4.dp))

                    Text(text = businessName, fontSize = 16.sp, fontWeight = FontWeight.Bold, color = Color.White)
                    Text(text = "Payment: $planName", fontSize = 11.sp, color = Color.LightGray)

                    Spacer(modifier = Modifier.height(16.dp))

                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .clip(RoundedCornerShape(8.dp))
                            .background(Color.White.copy(alpha = 0.04f))
                            .padding(12.dp),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(text = "Total amount due:", fontSize = 12.sp, color = Color.LightGray)
                        Text(text = "$$amount", fontSize = 18.sp, fontWeight = FontWeight.Black, color = SuccessGreen)
                    }

                    Spacer(modifier = Modifier.height(24.dp))

                    // Gateway response checkout options
                    Column(
                        modifier = Modifier.fillMaxWidth(),
                        verticalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        Button(
                            onClick = onConfirm,
                            colors = ButtonDefaults.buttonColors(containerColor = ElegantPrimary, contentColor = ElegantOnPrimary),
                            shape = RoundedCornerShape(8.dp),
                            modifier = Modifier.fillMaxWidth().testTag("razorpay_confirm_payment")
                        ) {
                            Text("Simulate Success (Clear Dues)", fontWeight = FontWeight.Bold)
                        }

                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.spacedBy(8.dp)
                        ) {
                            OutlinedButton(
                                onClick = onFail,
                                border = BorderStroke(1.dp, ElegantAccent),
                                shape = RoundedCornerShape(8.dp),
                                colors = ButtonDefaults.outlinedButtonColors(contentColor = ElegantAccent),
                                modifier = Modifier.weight(1f).testTag("razorpay_simulate_fail")
                            ) {
                                Text("Simulate Fail", fontSize = 11.sp, fontWeight = FontWeight.Bold)
                            }

                            OutlinedButton(
                                onClick = onCancel,
                                border = BorderStroke(1.dp, Color.White.copy(alpha = 0.3f)),
                                shape = RoundedCornerShape(8.dp),
                                colors = ButtonDefaults.outlinedButtonColors(contentColor = Color.LightGray),
                                modifier = Modifier.weight(1f).testTag("razorpay_cancel")
                            ) {
                                Text("Cancel", fontSize = 11.sp)
                            }
                        }
                    }
                }
            }
        }
    }
}

// Simple absolute date formatting helper
fun formatDate(mills: Long): String {
    val formatter = SimpleDateFormat("dd MMM yyyy, h:mm a", Locale.getDefault())
    return formatter.format(Date(mills))
}
