package com.example.ui.screens

import android.widget.Toast
import androidx.compose.animation.*
import androidx.compose.foundation.*
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.automirrored.filled.Send
import androidx.compose.material.icons.filled.*
import androidx.compose.material.icons.outlined.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.PathEffect
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.example.data.*
import com.example.ui.theme.*
import com.example.ui.viewmodel.HSEViewModel
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch

@Composable
fun MainAppScreen(viewModel: HSEViewModel) {
    val context = LocalContext.current
    var currentScreen by remember { mutableStateOf("login") }
    var selectedTab by remember { mutableStateOf("dashboard") }

    val user = viewModel.loggedInUser

    LaunchedEffect(user) {
        if (user != null) {
            currentScreen = "main"
        } else {
            currentScreen = "login"
        }
    }

    AnimatedContent(
        targetState = currentScreen,
        transitionSpec = {
            fadeIn() togetherWith fadeOut()
        },
        label = "ScreenTransition"
    ) { screen ->
        when (screen) {
            "login" -> LoginScreen(viewModel)
            "main" -> MainDashboardShell(viewModel, selectedTab) { selectedTab = it }
        }
    }
}

// --- LOGIN SCREEN ---
@Composable
fun LoginScreen(viewModel: HSEViewModel) {
    var username by remember { mutableStateOf("") }
    var password by remember { mutableStateOf("") }
    val context = LocalContext.current

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(
                Brush.verticalGradient(
                    colors = listOf(DarkCarbon, Color(0xFF0F1011))
                )
            )
            .padding(24.dp),
        contentAlignment = Alignment.Center
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .widthIn(max = 420.dp)
                .verticalScroll(rememberScrollState()),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            // Icon
            Box(
                modifier = Modifier
                    .size(84.dp)
                    .background(SafetyOrange.copy(alpha = 0.1f), CircleShape)
                    .border(2.dp, SafetyOrange, CircleShape),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    imageVector = Icons.Filled.Shield,
                    contentDescription = "Logo",
                    tint = SafetyOrange,
                    modifier = Modifier.size(44.dp)
                )
            }

            Spacer(modifier = Modifier.height(24.dp))

            Text(
                text = "کارخانه سیمان سفید نیریز",
                color = Color.White,
                fontSize = 24.sp,
                fontWeight = FontWeight.Bold,
                textAlign = TextAlign.Center
            )
            Text(
                text = "سامانه جامع مدیریت ایمنی، بهداشت و محیط زیست (HSE)",
                color = TextGray,
                fontSize = 13.sp,
                textAlign = TextAlign.Center,
                modifier = Modifier.padding(top = 8.dp)
            )

            Spacer(modifier = Modifier.height(36.dp))

            Card(
                colors = CardDefaults.cardColors(containerColor = CardCarbon),
                shape = RoundedCornerShape(16.dp),
                elevation = CardDefaults.cardElevation(defaultElevation = 8.dp)
            ) {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(24.dp)
                ) {
                    Text(
                        text = "ورود به سیستم",
                        color = Color.White,
                        fontSize = 18.sp,
                        fontWeight = FontWeight.Bold,
                        modifier = Modifier.align(Alignment.End)
                    )

                    Spacer(modifier = Modifier.height(16.dp))

                    OutlinedTextField(
                        value = username,
                        onValueChange = { username = it },
                        label = { Text("نام کاربری", color = TextGray) },
                        colors = OutlinedTextFieldDefaults.colors(
                            focusedTextColor = Color.White,
                            unfocusedTextColor = Color.White,
                            focusedBorderColor = SafetyOrange,
                            unfocusedBorderColor = ActiveCarbon,
                            cursorColor = SafetyOrange
                        ),
                        singleLine = true,
                        modifier = Modifier
                            .fillMaxWidth()
                            .testTag("username_input"),
                        trailingIcon = { Icon(Icons.Filled.Person, contentDescription = null, tint = TextGray) }
                    )

                    Spacer(modifier = Modifier.height(12.dp))

                    OutlinedTextField(
                        value = password,
                        onValueChange = { password = it },
                        label = { Text("کلمه عبور", color = TextGray) },
                        colors = OutlinedTextFieldDefaults.colors(
                            focusedTextColor = Color.White,
                            unfocusedTextColor = Color.White,
                            focusedBorderColor = SafetyOrange,
                            unfocusedBorderColor = ActiveCarbon,
                            cursorColor = SafetyOrange
                        ),
                        singleLine = true,
                        visualTransformation = PasswordVisualTransformation(),
                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Password),
                        modifier = Modifier
                            .fillMaxWidth()
                            .testTag("password_input"),
                        trailingIcon = { Icon(Icons.Filled.Lock, contentDescription = null, tint = TextGray) }
                    )

                    Spacer(modifier = Modifier.height(24.dp))

                    Button(
                        onClick = {
                            if (username.isBlank() || password.isBlank()) {
                                Toast.makeText(context, "لطفاً تمام فیلدها را پر کنید", Toast.LENGTH_SHORT).show()
                            } else {
                                viewModel.login(username, password) { success ->
                                    if (success) {
                                        Toast.makeText(context, "خوش آمدید", Toast.LENGTH_SHORT).show()
                                    }
                                }
                            }
                        },
                        colors = ButtonDefaults.buttonColors(containerColor = SafetyOrange),
                        shape = RoundedCornerShape(8.dp),
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(50.dp)
                            .testTag("login_button"),
                        enabled = !viewModel.isLoggingIn
                    ) {
                        if (viewModel.isLoggingIn) {
                            CircularProgressIndicator(color = Color.Black, modifier = Modifier.size(24.dp))
                        } else {
                            Text("ورود", color = Color.Black, fontSize = 16.sp, fontWeight = FontWeight.Bold)
                        }
                    }

                    viewModel.loginError?.let { err ->
                        Spacer(modifier = Modifier.height(12.dp))
                        Text(
                            text = err,
                            color = SafetyRed,
                            fontSize = 13.sp,
                            textAlign = TextAlign.Center,
                            modifier = Modifier.fillMaxWidth()
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.height(24.dp))

            Text(
                text = "تهیه کننده: مهندس حامد کریمی زاده",
                color = TextGray,
                fontSize = 12.sp,
                fontWeight = FontWeight.Bold,
                textAlign = TextAlign.Center
            )
        }
    }
}

// --- MAIN SHELL ---
@Composable
fun MainDashboardShell(
    viewModel: HSEViewModel,
    selectedTab: String,
    onTabSelected: (String) -> Unit
) {
    val syncState = viewModel.syncStatus
    val unsynced = viewModel.unsyncedCount
    val user = viewModel.loggedInUser ?: UserEntity("guest", "کاربر عمومی", "Operator", "تولید")

    Scaffold(
        topBar = {
            Column {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .background(DarkCarbon)
                        .padding(horizontal = 16.dp, vertical = 12.dp),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    IconButton(onClick = { viewModel.logout() }) {
                        Icon(Icons.Filled.ExitToApp, contentDescription = "خروج", tint = SafetyRed)
                    }

                    Column(horizontalAlignment = Alignment.CenterHorizontally) {
                        Text(
                            text = "HSE سیمان سفید نیریز",
                            color = Color.White,
                            fontSize = 18.sp,
                            fontWeight = FontWeight.Bold
                        )
                        Text(
                            text = user.fullName + " (${user.role})",
                            color = SafetyOrange,
                            fontSize = 11.sp
                        )
                    }

                    Box(
                        modifier = Modifier
                            .size(40.dp)
                            .background(SafetyOrange.copy(alpha = 0.1f), CircleShape)
                            .border(1.dp, SafetyOrange, CircleShape),
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(Icons.Filled.Shield, contentDescription = null, tint = SafetyOrange, modifier = Modifier.size(20.dp))
                    }
                }

                // Sync status banner
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .background(if (unsynced > 0) Color(0xFF3E2723) else Color(0xFF1B5E20))
                        .padding(horizontal = 16.dp, vertical = 6.dp)
                        .clickable { viewModel.triggerSync() },
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Icon(
                            imageVector = if (unsynced > 0) Icons.Filled.SyncProblem else Icons.Filled.CloudDone,
                            contentDescription = null,
                            tint = if (unsynced > 0) SafetyOrange else Color.White,
                            modifier = Modifier.size(16.dp)
                        )
                        Spacer(modifier = Modifier.width(6.dp))
                        Text(
                            text = if (unsynced > 0) "$unsynced داده در صف همگام‌سازی آفلاین" else "داده‌ها کاملاً همگام هستند",
                            color = Color.White,
                            fontSize = 11.sp
                        )
                    }
                    Text(
                        text = if (unsynced > 0) "کلیک جهت همگام‌سازی 🔄" else "همگام ابری",
                        color = Color.White,
                        fontSize = 11.sp,
                        fontWeight = FontWeight.Bold
                    )
                }
            }
        },
        bottomBar = {
            NavigationBar(
                containerColor = DarkCarbon,
                tonalElevation = 8.dp,
                modifier = Modifier.windowInsetsPadding(WindowInsets.navigationBars)
            ) {
                NavigationBarItem(
                    selected = selectedTab == "dashboard",
                    onClick = { onTabSelected("dashboard") },
                    icon = { Icon(Icons.Filled.Dashboard, contentDescription = "داشبورد") },
                    label = { Text("داشبورد", fontSize = 11.sp) },
                    colors = NavigationBarItemDefaults.colors(
                        selectedIconColor = Color.Black,
                        selectedTextColor = SafetyOrange,
                        indicatorColor = SafetyOrange,
                        unselectedIconColor = TextGray,
                        unselectedTextColor = TextGray
                    )
                )
                NavigationBarItem(
                    selected = selectedTab == "emergency",
                    onClick = { onTabSelected("emergency") },
                    icon = { Icon(Icons.Filled.Emergency, contentDescription = "اورژانس") },
                    label = { Text("SOS", fontSize = 11.sp) },
                    colors = NavigationBarItemDefaults.colors(
                        selectedIconColor = Color.Black,
                        selectedTextColor = SafetyOrange,
                        indicatorColor = SafetyOrange,
                        unselectedIconColor = TextGray,
                        unselectedTextColor = TextGray
                    )
                )
                NavigationBarItem(
                    selected = selectedTab == "ppe",
                    onClick = { onTabSelected("ppe") },
                    icon = { Icon(Icons.Filled.FactCheck, contentDescription = "بازرسی") },
                    label = { Text("تجهیزات", fontSize = 11.sp) },
                    colors = NavigationBarItemDefaults.colors(
                        selectedIconColor = Color.Black,
                        selectedTextColor = SafetyOrange,
                        indicatorColor = SafetyOrange,
                        unselectedIconColor = TextGray,
                        unselectedTextColor = TextGray
                    )
                )
                NavigationBarItem(
                    selected = selectedTab == "training",
                    onClick = { onTabSelected("training") },
                    icon = { Icon(Icons.Filled.School, contentDescription = "آموزش") },
                    label = { Text("آموزش", fontSize = 11.sp) },
                    colors = NavigationBarItemDefaults.colors(
                        selectedIconColor = Color.Black,
                        selectedTextColor = SafetyOrange,
                        indicatorColor = SafetyOrange,
                        unselectedIconColor = TextGray,
                        unselectedTextColor = TextGray
                    )
                )
                NavigationBarItem(
                    selected = selectedTab == "more",
                    onClick = { onTabSelected("more") },
                    icon = { Icon(Icons.Filled.MoreHoriz, contentDescription = "بیشتر") },
                    label = { Text("سایر", fontSize = 11.sp) },
                    colors = NavigationBarItemDefaults.colors(
                        selectedIconColor = Color.Black,
                        selectedTextColor = SafetyOrange,
                        indicatorColor = SafetyOrange,
                        unselectedIconColor = TextGray,
                        unselectedTextColor = TextGray
                    )
                )
            }
        },
        containerColor = DarkCarbon
    ) { innerPadding ->
        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
        ) {
            when (selectedTab) {
                "dashboard" -> ExecutiveDashboardScreen(viewModel) { onTabSelected(it) }
                "emergency" -> EmergencyEmergencyScreen(viewModel)
                "ppe" -> PPEManagementScreen(viewModel)
                "training" -> TrainingCenterScreen(viewModel)
                "more" -> MoreHubScreen(viewModel) { onTabSelected(it) }
                "ai_assistant" -> AIAssistantScreen(viewModel)
                "qr_scanner" -> QRScannerScreen(viewModel)
                "health_env" -> HealthEnvironmentalScreen(viewModel)
                "admin" -> AdminPanelScreen(viewModel)
                "safety_rules" -> SafetyRulesScreen(viewModel) { onTabSelected("more") }
            }
        }
    }
}

// --- SUB SCREEN 1: EXECUTIVE DASHBOARD ---
@Composable
fun ExecutiveDashboardScreen(viewModel: HSEViewModel, onNavigate: (String) -> Unit) {
    val incidentsList by viewModel.incidents.collectAsStateWithLifecycle()
    val inspectionsList by viewModel.inspections.collectAsStateWithLifecycle()

    val totalIncidents = incidentsList.size
    val criticalIncidents = incidentsList.count { it.severity == "Critical" || it.severity == "High" }
    val completedInspections = inspectionsList.count { it.status == "Safe" }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .verticalScroll(rememberScrollState())
            .padding(16.dp)
    ) {
        // Executive Header Greeting
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(
                text = "نمای کلی وضعیت ایمنی کارخانه",
                color = Color.White,
                fontSize = 18.sp,
                fontWeight = FontWeight.Bold
            )
            Text(
                text = "شیفت فعال: روزکار",
                color = TextGray,
                fontSize = 12.sp
            )
        }

        Spacer(modifier = Modifier.height(16.dp))

        // Industrial Metric Grid
        Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(12.dp)) {
            DashboardMetricCard(
                title = "کل حوادث و شبه حوادث",
                value = totalIncidents.toString(),
                subText = "$criticalIncidents مورد بحرانی / بالا",
                icon = Icons.Filled.Warning,
                color = SafetyRed,
                modifier = Modifier.weight(1f)
            )
            DashboardMetricCard(
                title = "شاخص غبار خروجی کوره",
                value = "۸۵ mg/Nm³",
                subText = "حد مجاز: ۱۵۰ - ایمن ✅",
                icon = Icons.Filled.Air,
                color = SafetyGreen,
                modifier = Modifier.weight(1f)
            )
        }

        Spacer(modifier = Modifier.height(12.dp))

        Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(12.dp)) {
            DashboardMetricCard(
                title = "تجهیزات بازرسی شده",
                value = inspectionsList.size.toString(),
                subText = "$completedInspections سالم فیزیکی",
                icon = Icons.Filled.FactCheck,
                color = SafetyOrange,
                modifier = Modifier.weight(1f)
            )
            DashboardMetricCard(
                title = "ضریب تکرار حادثه (SFR)",
                value = "۱.۲",
                subText = "کاهش ۱۵٪ نسبت به پارسال",
                icon = Icons.Filled.TrendingDown,
                color = SafetyYellow,
                modifier = Modifier.weight(1f)
            )
        }

        Spacer(modifier = Modifier.height(20.dp))

        // Interactive Custom Canvas Charts
        Text(
            text = "آمارهای ایمنی (تحلیل زنده خطوط)",
            color = Color.White,
            fontSize = 16.sp,
            fontWeight = FontWeight.Bold,
            modifier = Modifier.padding(bottom = 12.dp)
        )

        Card(
            colors = CardDefaults.cardColors(containerColor = CardCarbon),
            shape = RoundedCornerShape(12.dp),
            modifier = Modifier.fillMaxWidth()
        ) {
            Column(modifier = Modifier.padding(16.dp)) {
                Text(
                    text = "نمودار غبار فیلترهای سالن آسیاب سیمان (۲۴ ساعت گذشته)",
                    color = TextLight,
                    fontSize = 13.sp,
                    fontWeight = FontWeight.Bold
                )
                Spacer(modifier = Modifier.height(16.dp))

                // Canvas line graph
                Canvas(
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(120.dp)
                ) {
                    val width = size.width
                    val height = size.height

                    // Draw baseline limit (150 mg)
                    drawLine(
                        color = SafetyRed.copy(alpha = 0.5f),
                        start = Offset(0f, height * 0.2f),
                        end = Offset(width, height * 0.2f),
                        strokeWidth = 2f,
                        pathEffect = PathEffect.dashPathEffect(floatArrayOf(10f, 10f), 0f)
                    )

                    // Draw points
                    val points = listOf(
                        Offset(width * 0.05f, height * 0.8f),
                        Offset(width * 0.2f, height * 0.75f),
                        Offset(width * 0.4f, height * 0.6f),
                        Offset(width * 0.6f, height * 0.45f),
                        Offset(width * 0.8f, height * 0.65f),
                        Offset(width * 0.95f, height * 0.55f)
                    )

                    for (i in 0 until points.size - 1) {
                        drawLine(
                            color = SafetyOrange,
                            start = points[i],
                            end = points[i + 1],
                            strokeWidth = 4f
                        )
                    }

                    points.forEach { pt ->
                        drawCircle(
                            color = SafetyOrange,
                            radius = 6f,
                            center = pt
                        )
                        drawCircle(
                            color = Color.White,
                            radius = 3f,
                            center = pt
                        )
                    }
                }

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Text("۰۶:۰۰", color = TextGray, fontSize = 10.sp)
                    Text("۱۲:۰۰", color = TextGray, fontSize = 10.sp)
                    Text("۱۸:۰۰", color = TextGray, fontSize = 10.sp)
                    Text("۲۴:۰۰", color = TextGray, fontSize = 10.sp)
                }
            }
        }

        Spacer(modifier = Modifier.height(20.dp))

        // Quick Shortcuts
        Text(
            text = "دسترسی سریع به بخش‌ها",
            color = Color.White,
            fontSize = 16.sp,
            fontWeight = FontWeight.Bold,
            modifier = Modifier.padding(bottom = 12.dp)
        )

        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            Button(
                onClick = { onNavigate("ai_assistant") },
                colors = ButtonDefaults.buttonColors(containerColor = ActiveCarbon),
                modifier = Modifier.weight(1f),
                shape = RoundedCornerShape(8.dp)
            ) {
                Icon(Icons.Filled.SmartButton, contentDescription = null, tint = SafetyOrange)
                Spacer(modifier = Modifier.width(6.dp))
                Text("دستیار AI", color = Color.White, fontSize = 12.sp)
            }

            Button(
                onClick = { onNavigate("qr_scanner") },
                colors = ButtonDefaults.buttonColors(containerColor = ActiveCarbon),
                modifier = Modifier.weight(1f),
                shape = RoundedCornerShape(8.dp)
            ) {
                Icon(Icons.Filled.QrCodeScanner, contentDescription = null, tint = SafetyOrange)
                Spacer(modifier = Modifier.width(6.dp))
                Text("اسکن QR", color = Color.White, fontSize = 12.sp)
            }

            Button(
                onClick = { onNavigate("health_env") },
                colors = ButtonDefaults.buttonColors(containerColor = ActiveCarbon),
                modifier = Modifier.weight(1f),
                shape = RoundedCornerShape(8.dp)
            ) {
                Icon(Icons.Filled.Eco, contentDescription = null, tint = SafetyOrange)
                Spacer(modifier = Modifier.width(6.dp))
                Text("محیط زیست", color = Color.White, fontSize = 12.sp)
            }
        }

        Spacer(modifier = Modifier.height(20.dp))

        // Latest Incident Log List
        Text(
            text = "آخرین حوادث ثبت شده کارخانه",
            color = Color.White,
            fontSize = 16.sp,
            fontWeight = FontWeight.Bold,
            modifier = Modifier.padding(bottom = 12.dp)
        )

        if (incidentsList.isEmpty()) {
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(24.dp),
                contentAlignment = Alignment.Center
            ) {
                Text("هیچ حادثه‌ای ثبت نشده است", color = TextGray)
            }
        } else {
            incidentsList.take(3).forEach { incident ->
                Card(
                    colors = CardDefaults.cardColors(containerColor = CardCarbon),
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(bottom = 8.dp),
                    border = BorderStroke(
                        1.dp,
                        if (incident.severity == "Critical") SafetyRed else ActiveCarbon
                    )
                ) {
                    Column(modifier = Modifier.padding(14.dp)) {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Box(
                                modifier = Modifier
                                    .background(
                                        when (incident.severity) {
                                            "Critical" -> SafetyRed
                                            "High" -> SafetyOrange
                                            "Medium" -> SafetyYellow
                                            else -> SafetyGreen
                                        }.copy(alpha = 0.15f),
                                        RoundedCornerShape(4.dp)
                                    )
                                    .padding(horizontal = 8.dp, vertical = 4.dp)
                            ) {
                                Text(
                                    text = incident.severity,
                                    color = when (incident.severity) {
                                        "Critical" -> SafetyRed
                                        "High" -> SafetyOrange
                                        "Medium" -> SafetyYellow
                                        else -> SafetyGreen
                                    },
                                    fontSize = 11.sp,
                                    fontWeight = FontWeight.Bold
                                )
                            }
                            Text(
                                text = incident.title,
                                color = Color.White,
                                fontSize = 14.sp,
                                fontWeight = FontWeight.Bold
                            )
                        }

                        Spacer(modifier = Modifier.height(8.dp))
                        Text(
                            text = incident.description,
                            color = TextGray,
                            fontSize = 12.sp,
                            textAlign = TextAlign.End,
                            modifier = Modifier.fillMaxWidth()
                        )

                        Spacer(modifier = Modifier.height(8.dp))
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween
                        ) {
                            Text(
                                text = "مکان: ${incident.location}",
                                color = SafetyOrange,
                                fontSize = 11.sp
                            )
                            Text(
                                text = incident.date,
                                color = TextGray,
                                fontSize = 11.sp
                            )
                        }
                    }
                }
            }
        }
    }
}

@Composable
fun DashboardMetricCard(
    title: String,
    value: String,
    subText: String,
    icon: ImageVector,
    color: Color,
    modifier: Modifier = Modifier
) {
    Card(
        colors = CardDefaults.cardColors(containerColor = CardCarbon),
        shape = RoundedCornerShape(12.dp),
        modifier = modifier
    ) {
        Column(modifier = Modifier.padding(14.dp)) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Box(
                    modifier = Modifier
                        .size(36.dp)
                        .background(color.copy(alpha = 0.15f), CircleShape),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(icon, contentDescription = null, tint = color, modifier = Modifier.size(18.dp))
                }
                Text(
                    text = value,
                    color = Color.White,
                    fontSize = 20.sp,
                    fontWeight = FontWeight.Bold
                )
            }
            Spacer(modifier = Modifier.height(10.dp))
            Text(
                text = title,
                color = TextGray,
                fontSize = 11.sp,
                textAlign = TextAlign.End,
                modifier = Modifier.fillMaxWidth()
            )
            Text(
                text = subText,
                color = color,
                fontSize = 10.sp,
                textAlign = TextAlign.End,
                modifier = Modifier.fillMaxWidth()
            )
        }
    }
}

// --- SUB SCREEN 2: EMERGENCY RESPONSE & SOS ---
@Composable
fun EmergencyEmergencyScreen(viewModel: HSEViewModel) {
    var isAlarmActive by remember { mutableStateOf(false) }
    var selectedMapArea by remember { mutableStateOf<String?>(null) }
    val contacts by viewModel.emergencyContacts.collectAsStateWithLifecycle()
    val context = LocalContext.current

    val isAdmin = viewModel.loggedInUser?.username == "hamed" || viewModel.loggedInUser?.username == "admin"

    var showAddContactDialog by remember { mutableStateOf(false) }
    var editingContact by remember { mutableStateOf<EmergencyContactEntity?>(null) }
    
    var contactName by remember { mutableStateOf("") }
    var contactRole by remember { mutableStateOf("") }
    var contactPhone by remember { mutableStateOf("") }

    if (showAddContactDialog || editingContact != null) {
        val isEdit = editingContact != null
        AlertDialog(
            onDismissRequest = {
                showAddContactDialog = false
                editingContact = null
                contactName = ""
                contactRole = ""
                contactPhone = ""
            },
            title = {
                Text(
                    text = if (isEdit) "ویرایش مخاطب اضطراری" else "افزودن مخاطب اضطراری",
                    color = Color.White,
                    fontSize = 16.sp,
                    fontWeight = FontWeight.Bold,
                    modifier = Modifier.fillMaxWidth(),
                    textAlign = TextAlign.End
                )
            },
            text = {
                Column(
                    modifier = Modifier.fillMaxWidth(),
                    verticalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    OutlinedTextField(
                        value = contactName,
                        onValueChange = { contactName = it },
                        label = { Text("نام و نام خانوادگی", color = TextGray) },
                        modifier = Modifier.fillMaxWidth(),
                        colors = OutlinedTextFieldDefaults.colors(
                            focusedTextColor = Color.White,
                            unfocusedTextColor = Color.White,
                            focusedBorderColor = SafetyOrange,
                            unfocusedBorderColor = ActiveCarbon
                        )
                    )
                    OutlinedTextField(
                        value = contactRole,
                        onValueChange = { contactRole = it },
                        label = { Text("سمت (مثال: آتش‌نشانی کوره)", color = TextGray) },
                        modifier = Modifier.fillMaxWidth(),
                        colors = OutlinedTextFieldDefaults.colors(
                            focusedTextColor = Color.White,
                            unfocusedTextColor = Color.White,
                            focusedBorderColor = SafetyOrange,
                            unfocusedBorderColor = ActiveCarbon
                        )
                    )
                    OutlinedTextField(
                        value = contactPhone,
                        onValueChange = { contactPhone = it },
                        label = { Text("شماره تماس", color = TextGray) },
                        modifier = Modifier.fillMaxWidth(),
                        colors = OutlinedTextFieldDefaults.colors(
                            focusedTextColor = Color.White,
                            unfocusedTextColor = Color.White,
                            focusedBorderColor = SafetyOrange,
                            unfocusedBorderColor = ActiveCarbon
                        )
                    )
                }
            },
            confirmButton = {
                TextButton(
                    onClick = {
                        if (contactName.isNotBlank() && contactPhone.isNotBlank()) {
                            val contact = EmergencyContactEntity(
                                id = editingContact?.id ?: 0,
                                name = contactName,
                                roleFa = contactRole,
                                phone = contactPhone,
                                isSOSContact = editingContact?.isSOSContact ?: false
                            )
                            viewModel.addOrUpdateEmergencyContact(contact) {
                                Toast.makeText(context, "اطلاعات مخاطب با موفقیت ذخیره شد.", Toast.LENGTH_SHORT).show()
                            }
                            showAddContactDialog = false
                            editingContact = null
                            contactName = ""
                            contactRole = ""
                            contactPhone = ""
                        } else {
                            Toast.makeText(context, "لطفاً نام و شماره تلفن را وارد کنید.", Toast.LENGTH_SHORT).show()
                        }
                    }
                ) {
                    Text("ذخیره", color = SafetyOrange)
                }
            },
            dismissButton = {
                TextButton(
                    onClick = {
                        showAddContactDialog = false
                        editingContact = null
                        contactName = ""
                        contactRole = ""
                        contactPhone = ""
                    }
                ) {
                    Text("انصراف", color = TextGray)
                }
            },
            containerColor = CardCarbon
        )
    }

    LaunchedEffect(isAlarmActive) {
        if (isAlarmActive) {
            // Sound simulation
            delay(10000)
            isAlarmActive = false
        }
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .verticalScroll(rememberScrollState())
            .padding(16.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        // SOS Button Section
        Card(
            colors = CardDefaults.cardColors(containerColor = if (isAlarmActive) Color(0xFF5C1C1C) else CardCarbon),
            border = BorderStroke(2.dp, if (isAlarmActive) SafetyRed else Color.Transparent),
            shape = RoundedCornerShape(16.dp),
            modifier = Modifier.fillMaxWidth()
        ) {
            Column(
                modifier = Modifier.padding(20.dp),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                Text(
                    text = "سامانه اعلام اضطراری SOS کارخانه",
                    color = Color.White,
                    fontSize = 16.sp,
                    fontWeight = FontWeight.Bold,
                    textAlign = TextAlign.Center
                )
                Text(
                    text = "در صورت بروز آتش‌سوزی، ریزش سیلو یا نشت گاز دکمه زیر را فشار دهید.",
                    color = TextGray,
                    fontSize = 11.sp,
                    textAlign = TextAlign.Center,
                    modifier = Modifier.padding(top = 4.dp, bottom = 20.dp)
                )

                // Big Red Button with ripple pulses
                Box(
                    modifier = Modifier
                        .size(130.dp)
                        .background(SafetyRed.copy(alpha = if (isAlarmActive) 0.3f else 0.1f), CircleShape)
                        .border(
                            4.dp,
                            if (isAlarmActive) SafetyRed else SafetyRed.copy(alpha = 0.5f),
                            CircleShape
                        )
                        .clickable {
                            isAlarmActive = !isAlarmActive
                            if (isAlarmActive) {
                                viewModel.logEvent(
                                    "اعلام وضعیت اضطراری SOS",
                                    viewModel.loggedInUser?.fullName ?: "ناشناس",
                                    "آلارم هشدار زنده کوره توسط اپلیکیشن فعال شد."
                                )
                                Toast
                                    .makeText(
                                        context,
                                        "🚨 آلارم برای کل واحد HSE و بهداری ارسال شد!",
                                        Toast.LENGTH_LONG
                                    )
                                    .show()
                            }
                        },
                    contentAlignment = Alignment.Center
                ) {
                    Column(horizontalAlignment = Alignment.CenterHorizontally) {
                        Icon(
                            imageVector = Icons.Filled.Emergency,
                            contentDescription = "SOS",
                            tint = Color.White,
                            modifier = Modifier.size(44.dp)
                        )
                        Spacer(modifier = Modifier.height(4.dp))
                        Text(
                            text = if (isAlarmActive) "آلارم روشن 🚨" else "SOS",
                            color = Color.White,
                            fontWeight = FontWeight.Bold,
                            fontSize = 16.sp
                        )
                    }
                }

                if (isAlarmActive) {
                    Spacer(modifier = Modifier.height(16.dp))
                    Text(
                        text = "مکان فعلی شما: کارگاه سیمان سفید نیریز - دپارتمان کوره و پیش‌گرمکن (عرض ۳۴.۲۴، طول ۵۴.۱۲)",
                        color = SafetyYellow,
                        fontSize = 12.sp,
                        textAlign = TextAlign.Center
                    )
                }
            }
        }

        Spacer(modifier = Modifier.height(24.dp))

        // Factory Interactive Map drawn via Canvas
        Text(
            text = "نقشه ریسک و نقاط تجمع ایمن کارخانه",
            color = Color.White,
            fontSize = 16.sp,
            fontWeight = FontWeight.Bold,
            modifier = Modifier.align(Alignment.End)
        )
        Text(
            text = "برای نمایش اطلاعات ایمنی هر واحد، روی آن کلیک کنید.",
            color = TextGray,
            fontSize = 11.sp,
            textAlign = TextAlign.End,
            modifier = Modifier
                .fillMaxWidth()
                .padding(bottom = 12.dp)
        )

        Card(
            colors = CardDefaults.cardColors(containerColor = CardCarbon),
            shape = RoundedCornerShape(12.dp),
            modifier = Modifier
                .fillMaxWidth()
                .height(220.dp)
        ) {
            Box(modifier = Modifier.fillMaxSize()) {
                Canvas(
                    modifier = Modifier
                        .fillMaxSize()
                        .clickable {
                            // Touch simulation - toggle areas based on touch coordinates or simple sequence
                            val areas = listOf("کوره دوار", "سنگ‌شکن", "سیلوهای سیمان", "بارگیرخانه", "نقطه تجمع الف")
                            val nextIdx = (areas.indexOf(selectedMapArea) + 1) % areas.size
                            selectedMapArea = areas[nextIdx]
                        }
                ) {
                    val w = size.width
                    val h = size.height

                    // Draw Quarry (معدن)
                    drawRect(
                        color = Color(0xFF5D4037),
                        topLeft = Offset(w * 0.1f, h * 0.1f),
                        size = Size(w * 0.25f, h * 0.25f)
                    )

                    // Draw Preheater & Rotary Kiln (کوره)
                    drawRect(
                        color = SafetyOrange.copy(alpha = 0.8f),
                        topLeft = Offset(w * 0.45f, h * 0.2f),
                        size = Size(w * 0.45f, h * 0.15f)
                    )

                    // Draw Packing Plant (بارگیرخانه)
                    drawRect(
                        color = Color(0xFF37474F),
                        topLeft = Offset(w * 0.5f, h * 0.6f),
                        size = Size(w * 0.35f, h * 0.25f)
                    )

                    // Draw Assembly point A (نقطه تجمع الف)
                    drawCircle(
                        color = SafetyGreen,
                        radius = 20f,
                        center = Offset(w * 0.2f, h * 0.75f)
                    )
                }

                // UI Labels positioned absolutely
                Text("معدن کارخانه ⛰️", color = Color.White, fontSize = 10.sp, modifier = Modifier.offset(x = 16.dp, y = 16.dp))
                Text("خط کوره دوار 🔥", color = Color.White, fontSize = 11.sp, modifier = Modifier.offset(x = 200.dp, y = 44.dp))
                Text("بارگیرخانه 🚛", color = Color.White, fontSize = 11.sp, modifier = Modifier.offset(x = 220.dp, y = 140.dp))
                Text("تجمع ایمن الف 🟢", color = Color.White, fontSize = 11.sp, modifier = Modifier.offset(x = 44.dp, y = 160.dp))
            }
        }

        selectedMapArea?.let { area ->
            Spacer(modifier = Modifier.height(12.dp))
            Card(
                colors = CardDefaults.cardColors(containerColor = ActiveCarbon),
                border = BorderStroke(1.dp, SafetyOrange),
                modifier = Modifier.fillMaxWidth()
            ) {
                Column(modifier = Modifier.padding(14.dp)) {
                    Text(
                        text = "دستورالعمل واحد: $area",
                        color = SafetyOrange,
                        fontSize = 13.sp,
                        fontWeight = FontWeight.Bold,
                        modifier = Modifier.align(Alignment.End)
                    )
                    Spacer(modifier = Modifier.height(6.dp))
                    Text(
                        text = when (area) {
                            "کوره دوار" -> "خطرات اصلی: دمای بالای کوره، سقوط آجرهای نسوز، سوختگی‌های فیزیکی شدید. الزامات: عینک نسوز، لباس آستین بلند پنبه‌ای، هماهنگی کامل با اتاق فرمان مرکزی."
                            "سنگ‌شکن" -> "خطرات: نویز بالاتر از ۸۵ دسی‌بل، ذرات آزاد معلق غبار سنگ آهک. الزامات: استفاده از گوشی ایرموف و ماسک تنفسی فیلتردار."
                            "سیلوهای سیمان" -> "خطرات: خفگی ناشی از ورود به داخل سیلو، ریزش ناگهانی بار. الزامات: طناب نجات، هارنس ایمنی، مجوز ورود فضای بسته صادر شده توسط ایمنی."
                            "نقطه تجمع الف" -> "نقطه تجمع ایمن شماره ۱ کارخانه، مجاور ساختمان اداری اصلی. در شرایط زلزله یا نشت عمده مواد شیمیایی پرسنل بخش تولید فوراً باید در این بخش حاضر شوند."
                            else -> "تخلیه بار سنگ آهک از کامیون‌ها. مهار چرخ‌ها الزامی است."
                        },
                        color = Color.White,
                        fontSize = 11.sp,
                        textAlign = TextAlign.End,
                        modifier = Modifier.fillMaxWidth()
                    )
                }
            }
        }

        Spacer(modifier = Modifier.height(24.dp))

        // Emergency Contacts list
        Row(
            modifier = Modifier.fillMaxWidth().padding(bottom = 8.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            if (isAdmin) {
                Button(
                    onClick = { showAddContactDialog = true },
                    colors = ButtonDefaults.buttonColors(containerColor = SafetyOrange),
                    shape = RoundedCornerShape(6.dp),
                    contentPadding = PaddingValues(horizontal = 12.dp, vertical = 6.dp),
                    modifier = Modifier.height(32.dp)
                ) {
                    Icon(Icons.Filled.Add, contentDescription = null, tint = Color.Black, modifier = Modifier.size(14.dp))
                    Spacer(modifier = Modifier.width(4.dp))
                    Text("افزودن مخاطب", color = Color.Black, fontSize = 10.sp, fontWeight = FontWeight.Bold)
                }
            } else {
                Spacer(modifier = Modifier.width(1.dp))
            }
            Text(
                text = "شماره تماس‌های حیاتی و اضطراری",
                color = Color.White,
                fontSize = 16.sp,
                fontWeight = FontWeight.Bold
            )
        }
        Spacer(modifier = Modifier.height(12.dp))

        contacts.forEach { contact ->
            Card(
                colors = CardDefaults.cardColors(containerColor = CardCarbon),
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(bottom = 8.dp)
            ) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(14.dp),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        Box(
                            modifier = Modifier
                                .background(SafetyGreen.copy(alpha = 0.15f), CircleShape)
                                .size(36.dp)
                                .clickable {
                                    Toast
                                        .makeText(
                                            context,
                                            "شماره‌گیری: ${contact.phone}",
                                            Toast.LENGTH_SHORT
                                        )
                                        .show()
                                },
                            contentAlignment = Alignment.Center
                        ) {
                            Icon(Icons.Filled.Call, contentDescription = "تماس", tint = SafetyGreen, modifier = Modifier.size(16.dp))
                        }

                        if (isAdmin) {
                            Box(
                                modifier = Modifier
                                    .background(SafetyOrange.copy(alpha = 0.15f), CircleShape)
                                    .size(36.dp)
                                    .clickable {
                                        editingContact = contact
                                        contactName = contact.name
                                        contactRole = contact.roleFa
                                        contactPhone = contact.phone
                                    },
                                contentAlignment = Alignment.Center
                            ) {
                                Icon(Icons.Filled.Edit, contentDescription = "ویرایش", tint = SafetyOrange, modifier = Modifier.size(16.dp))
                            }

                            Box(
                                modifier = Modifier
                                    .background(SafetyRed.copy(alpha = 0.15f), CircleShape)
                                    .size(36.dp)
                                    .clickable {
                                        viewModel.deleteEmergencyContact(contact.id) {
                                            Toast.makeText(context, "مخاطب با موفقیت حذف شد.", Toast.LENGTH_SHORT).show()
                                        }
                                    },
                                contentAlignment = Alignment.Center
                            ) {
                                Icon(Icons.Filled.Delete, contentDescription = "حذف", tint = SafetyRed, modifier = Modifier.size(16.dp))
                            }
                        }
                    }

                    Column(horizontalAlignment = Alignment.End) {
                        Text(contact.name, color = Color.White, fontSize = 13.sp, fontWeight = FontWeight.Bold)
                        Text(contact.roleFa, color = TextGray, fontSize = 11.sp)
                        Text(contact.phone, color = SafetyOrange, fontSize = 12.sp, fontWeight = FontWeight.Bold)
                    }
                }
            }
        }
    }
}

// --- SUB SCREEN 3: PPE & EQUIPMENT INSPECTIONS ---
@Composable
fun PPEManagementScreen(viewModel: HSEViewModel) {
    val ppeList by viewModel.ppeItems.collectAsStateWithLifecycle()
    val inspectionsList by viewModel.inspections.collectAsStateWithLifecycle()

    val isAdmin = viewModel.loggedInUser?.username == "hamed" || viewModel.loggedInUser?.username == "admin"

    var showPPEItemDialog by remember { mutableStateOf(false) }
    var editingPPEItem by remember { mutableStateOf<PPEItemEntity?>(null) }

    var ppeType by remember { mutableStateOf("") }
    var ppeNameFa by remember { mutableStateOf("") }
    var ppeTotalStock by remember { mutableStateOf("100") }
    var ppeInspectedCount by remember { mutableStateOf("100") }
    var ppeDefectiveCount by remember { mutableStateOf("0") }
    var ppeReplacementRequired by remember { mutableStateOf("0") }
    var ppeLastInspectionDate by remember { mutableStateOf("1405-04-10") }
    var ppeNextReplacementDate by remember { mutableStateOf("1406-04-10") }
    var ppeInstructions by remember { mutableStateOf("") }

    var showNewIncidentDialog by remember { mutableStateOf(false) }
    var showNewInspectionDialog by remember { mutableStateOf(false) }

    // Forms states
    var incidentType by remember { mutableStateOf("Incident") }
    var incidentTitle by remember { mutableStateOf("") }
    var incidentLoc by remember { mutableStateOf("") }
    var incidentDesc by remember { mutableStateOf("") }
    var incidentSeverity by remember { mutableStateOf("Medium") }

    var inspectEquipmentName by remember { mutableStateOf("") }
    var inspectStatus by remember { mutableStateOf("Safe") }
    var inspectChecklist by remember { mutableStateOf("") }

    val context = LocalContext.current

    if (showPPEItemDialog || editingPPEItem != null) {
        val isEdit = editingPPEItem != null
        AlertDialog(
            onDismissRequest = {
                showPPEItemDialog = false
                editingPPEItem = null
                ppeType = ""
                ppeNameFa = ""
                ppeTotalStock = "100"
                ppeInspectedCount = "100"
                ppeDefectiveCount = "0"
                ppeReplacementRequired = "0"
                ppeLastInspectionDate = "1405-04-10"
                ppeNextReplacementDate = "1406-04-10"
                ppeInstructions = ""
            },
            title = {
                Text(
                    text = if (isEdit) "ویرایش اطلاعات تجهیزات حفاظتی" else "افزودن تجهیزات حفاظتی جدید",
                    color = Color.White,
                    fontSize = 16.sp,
                    fontWeight = FontWeight.Bold,
                    modifier = Modifier.fillMaxWidth(),
                    textAlign = TextAlign.End
                )
            },
            text = {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .verticalScroll(rememberScrollState()),
                    verticalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    if (!isEdit) {
                        OutlinedTextField(
                            value = ppeType,
                            onValueChange = { ppeType = it },
                            label = { Text("شناسه انگلیسی (مثال: Helmet)", color = TextGray) },
                            modifier = Modifier.fillMaxWidth(),
                            colors = OutlinedTextFieldDefaults.colors(
                                focusedTextColor = Color.White,
                                unfocusedTextColor = Color.White,
                                focusedBorderColor = SafetyOrange,
                                unfocusedBorderColor = ActiveCarbon
                            )
                        )
                    }
                    OutlinedTextField(
                        value = ppeNameFa,
                        onValueChange = { ppeNameFa = it },
                        label = { Text("نام فارسی تجهیز (مثال: کلاه ایمنی مهندسی)", color = TextGray) },
                        modifier = Modifier.fillMaxWidth(),
                        colors = OutlinedTextFieldDefaults.colors(
                            focusedTextColor = Color.White,
                            unfocusedTextColor = Color.White,
                            focusedBorderColor = SafetyOrange,
                            unfocusedBorderColor = ActiveCarbon
                        )
                    )
                    OutlinedTextField(
                        value = ppeTotalStock,
                        onValueChange = { ppeTotalStock = it },
                        label = { Text("کل موجودی انبار", color = TextGray) },
                        modifier = Modifier.fillMaxWidth(),
                        colors = OutlinedTextFieldDefaults.colors(
                            focusedTextColor = Color.White,
                            unfocusedTextColor = Color.White,
                            focusedBorderColor = SafetyOrange,
                            unfocusedBorderColor = ActiveCarbon
                        )
                    )
                    OutlinedTextField(
                        value = ppeInstructions,
                        onValueChange = { ppeInstructions = it },
                        label = { Text("دستورالعمل نگهداری و استفاده", color = TextGray) },
                        modifier = Modifier.fillMaxWidth(),
                        colors = OutlinedTextFieldDefaults.colors(
                            focusedTextColor = Color.White,
                            unfocusedTextColor = Color.White,
                            focusedBorderColor = SafetyOrange,
                            unfocusedBorderColor = ActiveCarbon
                        )
                    )
                    OutlinedTextField(
                        value = ppeNextReplacementDate,
                        onValueChange = { ppeNextReplacementDate = it },
                        label = { Text("تاریخ تعویض/انقضا بعدی (مثال: ۱۴۰۶-۰۴-۱۰)", color = TextGray) },
                        modifier = Modifier.fillMaxWidth(),
                        colors = OutlinedTextFieldDefaults.colors(
                            focusedTextColor = Color.White,
                            unfocusedTextColor = Color.White,
                            focusedBorderColor = SafetyOrange,
                            unfocusedBorderColor = ActiveCarbon
                        )
                    )
                }
            },
            confirmButton = {
                TextButton(
                    onClick = {
                        val finalType = if (isEdit) editingPPEItem!!.ppeType else ppeType.trim()
                        if (finalType.isNotBlank() && ppeNameFa.isNotBlank()) {
                            val item = PPEItemEntity(
                                ppeType = finalType,
                                nameFa = ppeNameFa,
                                totalStock = ppeTotalStock.toIntOrNull() ?: 100,
                                inspectedCount = ppeInspectedCount.toIntOrNull() ?: 100,
                                defectiveCount = ppeDefectiveCount.toIntOrNull() ?: 0,
                                replacementRequired = ppeReplacementRequired.toIntOrNull() ?: 0,
                                lastInspectionDate = ppeLastInspectionDate,
                                nextReplacementDate = ppeNextReplacementDate,
                                instructions = ppeInstructions
                            )
                            viewModel.addOrUpdatePPEItem(item) {
                                Toast.makeText(context, "اطلاعات تجهیز با موفقیت ذخیره شد.", Toast.LENGTH_SHORT).show()
                            }
                            showPPEItemDialog = false
                            editingPPEItem = null
                            ppeType = ""
                            ppeNameFa = ""
                            ppeTotalStock = "100"
                            ppeInspectedCount = "100"
                            ppeDefectiveCount = "0"
                            ppeReplacementRequired = "0"
                            ppeLastInspectionDate = "1405-04-10"
                            ppeNextReplacementDate = "1406-04-10"
                            ppeInstructions = ""
                        } else {
                            Toast.makeText(context, "لطفاً شناسه و نام فارسی را پر کنید.", Toast.LENGTH_SHORT).show()
                        }
                    }
                ) {
                    Text("ذخیره", color = SafetyOrange)
                }
            },
            dismissButton = {
                TextButton(
                    onClick = {
                        showPPEItemDialog = false
                        editingPPEItem = null
                        ppeType = ""
                        ppeNameFa = ""
                        ppeTotalStock = "100"
                        ppeInspectedCount = "100"
                        ppeDefectiveCount = "0"
                        ppeReplacementRequired = "0"
                        ppeLastInspectionDate = "1405-04-10"
                        ppeNextReplacementDate = "1406-04-10"
                        ppeInstructions = ""
                    }
                ) {
                    Text("انصراف", color = TextGray)
                }
            },
            containerColor = CardCarbon
        )
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .verticalScroll(rememberScrollState())
            .padding(16.dp)
    ) {
        // Quick Action buttons to report issues
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            Button(
                onClick = { showNewIncidentDialog = true },
                colors = ButtonDefaults.buttonColors(containerColor = SafetyRed),
                shape = RoundedCornerShape(8.dp),
                modifier = Modifier
                    .weight(1f)
                    .height(44.dp)
            ) {
                Icon(Icons.Filled.AddAlert, contentDescription = null, tint = Color.White)
                Spacer(modifier = Modifier.width(6.dp))
                Text("گزارش حادثه / خطر ⚠️", color = Color.White, fontSize = 11.sp, fontWeight = FontWeight.Bold)
            }

            Button(
                onClick = { showNewInspectionDialog = true },
                colors = ButtonDefaults.buttonColors(containerColor = SafetyOrange),
                shape = RoundedCornerShape(8.dp),
                modifier = Modifier
                    .weight(1f)
                    .height(44.dp)
            ) {
                Icon(Icons.Filled.AssignmentTurnedIn, contentDescription = null, tint = Color.Black)
                Spacer(modifier = Modifier.width(6.dp))
                Text("ثبت بازرسی فنی 📋", color = Color.Black, fontSize = 11.sp, fontWeight = FontWeight.Bold)
            }
        }

        Spacer(modifier = Modifier.height(24.dp))

        // PPE Inventory list & Replacement countdowns
        Row(
            modifier = Modifier.fillMaxWidth().padding(bottom = 8.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            if (isAdmin) {
                Button(
                    onClick = { showPPEItemDialog = true },
                    colors = ButtonDefaults.buttonColors(containerColor = SafetyOrange),
                    shape = RoundedCornerShape(6.dp),
                    contentPadding = PaddingValues(horizontal = 12.dp, vertical = 6.dp),
                    modifier = Modifier.height(32.dp)
                ) {
                    Icon(Icons.Filled.Add, contentDescription = null, tint = Color.Black, modifier = Modifier.size(14.dp))
                    Spacer(modifier = Modifier.width(4.dp))
                    Text("افزودن تجهیز", color = Color.Black, fontSize = 10.sp, fontWeight = FontWeight.Bold)
                }
            } else {
                Spacer(modifier = Modifier.width(1.dp))
            }
            Text(
                text = "انبار و پایش وضعیت تجهیزات حفاظت فردی (PPE)",
                color = Color.White,
                fontSize = 16.sp,
                fontWeight = FontWeight.Bold
            )
        }
        Spacer(modifier = Modifier.height(12.dp))

        ppeList.forEach { ppe ->
            Card(
                colors = CardDefaults.cardColors(containerColor = CardCarbon),
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(bottom = 10.dp)
            ) {
                Column(modifier = Modifier.padding(14.dp)) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(
                            text = "تاریخ انقضا: ${ppe.nextReplacementDate}",
                            color = SafetyYellow,
                            fontSize = 11.sp
                        )
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            if (isAdmin) {
                                IconButton(
                                    onClick = {
                                        editingPPEItem = ppe
                                        ppeType = ppe.ppeType
                                        ppeNameFa = ppe.nameFa
                                        ppeTotalStock = ppe.totalStock.toString()
                                        ppeInstructions = ppe.instructions
                                        ppeNextReplacementDate = ppe.nextReplacementDate
                                    },
                                    modifier = Modifier.size(24.dp)
                                ) {
                                    Icon(Icons.Filled.Edit, contentDescription = "ویرایش", tint = SafetyOrange, modifier = Modifier.size(16.dp))
                                }
                                Spacer(modifier = Modifier.width(4.dp))
                                IconButton(
                                    onClick = {
                                        viewModel.deletePPEItem(ppe.ppeType) {
                                            Toast.makeText(context, "تجهیز با موفقیت حذف شد.", Toast.LENGTH_SHORT).show()
                                        }
                                    },
                                    modifier = Modifier.size(24.dp)
                                ) {
                                    Icon(Icons.Filled.Delete, contentDescription = "حذف", tint = SafetyRed, modifier = Modifier.size(16.dp))
                                }
                                Spacer(modifier = Modifier.width(8.dp))
                            }
                            Text(
                                text = ppe.nameFa,
                                color = Color.White,
                                fontSize = 14.sp,
                                fontWeight = FontWeight.Bold
                            )
                        }
                    }

                    Spacer(modifier = Modifier.height(8.dp))
                    Text(
                        text = ppe.instructions,
                        color = TextGray,
                        fontSize = 11.sp,
                        textAlign = TextAlign.End,
                        modifier = Modifier.fillMaxWidth()
                    )

                    Spacer(modifier = Modifier.height(12.dp))
                    // Simple inventory indicators
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Text("موجودی کل: ${ppe.totalStock}", color = TextLight, fontSize = 11.sp)
                        Text("بررسی شده: ${ppe.inspectedCount}", color = SafetyGreen, fontSize = 11.sp)
                        Text("نیاز به تعویض: ${ppe.replacementRequired}", color = SafetyRed, fontSize = 11.sp)
                    }
                }
            }
        }

        Spacer(modifier = Modifier.height(24.dp))

        // Historical Inspections
        Text(
            text = "آخرین بازرسی‌های انجام شده کپسول و ماشین‌آلات",
            color = Color.White,
            fontSize = 16.sp,
            fontWeight = FontWeight.Bold,
            modifier = Modifier.align(Alignment.End)
        )
        Spacer(modifier = Modifier.height(12.dp))

        inspectionsList.forEach { inspection ->
            Card(
                colors = CardDefaults.cardColors(containerColor = CardCarbon),
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(bottom = 8.dp)
            ) {
                Column(modifier = Modifier.padding(12.dp)) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Box(
                            modifier = Modifier
                                .background(
                                    if (inspection.status == "Safe") SafetyGreen.copy(alpha = 0.15f) else SafetyRed.copy(alpha = 0.15f),
                                    RoundedCornerShape(4.dp)
                                )
                                .padding(horizontal = 8.dp, vertical = 4.dp)
                        ) {
                            Text(
                                text = if (inspection.status == "Safe") "ایمن ✅" else "دارای نقص ❌",
                                color = if (inspection.status == "Safe") SafetyGreen else SafetyRed,
                                fontSize = 11.sp,
                                fontWeight = FontWeight.Bold
                            )
                        }

                        Text(
                            text = inspection.equipmentName,
                            color = Color.White,
                            fontSize = 13.sp,
                            fontWeight = FontWeight.Bold
                        )
                    }

                    Spacer(modifier = Modifier.height(6.dp))
                    Text(
                        text = "چک‌لیست: ${inspection.checklistResult}",
                        color = TextGray,
                        fontSize = 11.sp,
                        textAlign = TextAlign.End,
                        modifier = Modifier.fillMaxWidth()
                    )

                    Spacer(modifier = Modifier.height(6.dp))
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Text("بازرس: ${inspection.inspectorName}", color = TextGray, fontSize = 11.sp)
                        Text(inspection.inspectionDate, color = TextGray, fontSize = 11.sp)
                    }
                }
            }
        }
    }

    // New Incident dialog
    if (showNewIncidentDialog) {
        AlertDialog(
            onDismissRequest = { showNewIncidentDialog = false },
            confirmButton = {
                TextButton(onClick = {
                    if (incidentTitle.isBlank() || incidentDesc.isBlank() || incidentLoc.isBlank()) {
                        Toast.makeText(context, "تمامی فیلدها را پر کنید", Toast.LENGTH_SHORT).show()
                    } else {
                        viewModel.submitIncident(
                            incidentType,
                            incidentTitle,
                            incidentLoc,
                            incidentDesc,
                            incidentSeverity
                        )
                        showNewIncidentDialog = false
                        Toast.makeText(context, "گزارش شما بصورت آفلاین ذخیره شد.", Toast.LENGTH_LONG).show()
                        // Reset
                        incidentTitle = ""
                        incidentLoc = ""
                        incidentDesc = ""
                    }
                }) {
                    Text("ثبت گزارش", color = SafetyOrange)
                }
            },
            dismissButton = {
                TextButton(onClick = { showNewIncidentDialog = false }) {
                    Text("انصراف", color = TextGray)
                }
            },
            title = {
                Text(
                    "گزارش حادثه، شبه حادثه یا ناایمنی جدید",
                    color = Color.White,
                    fontSize = 16.sp,
                    fontWeight = FontWeight.Bold,
                    textAlign = TextAlign.End,
                    modifier = Modifier.fillMaxWidth()
                )
            },
            text = {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .verticalScroll(rememberScrollState()),
                    verticalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    OutlinedTextField(
                        value = incidentTitle,
                        onValueChange = { incidentTitle = it },
                        label = { Text("عنوان گزارش (مثال: نشت روغن در کوره)", color = TextGray) },
                        colors = OutlinedTextFieldDefaults.colors(focusedTextColor = Color.White, unfocusedTextColor = Color.White),
                        modifier = Modifier.fillMaxWidth()
                    )

                    OutlinedTextField(
                        value = incidentLoc,
                        onValueChange = { incidentLoc = it },
                        label = { Text("موقعیت فیزیکی دقیق (مثال: بدنه غربی کوره)", color = TextGray) },
                        colors = OutlinedTextFieldDefaults.colors(focusedTextColor = Color.White, unfocusedTextColor = Color.White),
                        modifier = Modifier.fillMaxWidth()
                    )

                    OutlinedTextField(
                        value = incidentDesc,
                        onValueChange = { incidentDesc = it },
                        label = { Text("شرح کامل واقعه و پتانسیل بروز خطر", color = TextGray) },
                        colors = OutlinedTextFieldDefaults.colors(focusedTextColor = Color.White, unfocusedTextColor = Color.White),
                        modifier = Modifier.fillMaxWidth(),
                        maxLines = 4
                    )

                    // Severity Selector Row
                    Text("شدت خطر:", color = Color.White, fontSize = 12.sp)
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(6.dp)
                    ) {
                        listOf("Low", "Medium", "High", "Critical").forEach { level ->
                            Box(
                                modifier = Modifier
                                    .background(
                                        if (incidentSeverity == level) SafetyOrange else ActiveCarbon,
                                        RoundedCornerShape(4.dp)
                                    )
                                    .border(
                                        1.dp,
                                        if (incidentSeverity == level) Color.White else Color.Transparent,
                                        RoundedCornerShape(4.dp)
                                    )
                                    .clickable { incidentSeverity = level }
                                    .padding(horizontal = 8.dp, vertical = 6.dp)
                                    .weight(1f),
                                contentAlignment = Alignment.Center
                            ) {
                                Text(
                                    text = level,
                                    color = if (incidentSeverity == level) Color.Black else Color.White,
                                    fontSize = 10.sp,
                                    fontWeight = FontWeight.Bold
                                )
                            }
                        }
                    }

                    // Type Selector Row
                    Text("نوع گزارش:", color = Color.White, fontSize = 12.sp)
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(6.dp)
                    ) {
                        listOf("Incident", "Near Miss", "Unsafe Act", "Unsafe Condition").forEach { type ->
                            Box(
                                modifier = Modifier
                                    .background(
                                        if (incidentType == type) SafetyOrange else ActiveCarbon,
                                        RoundedCornerShape(4.dp)
                                    )
                                    .clickable { incidentType = type }
                                    .padding(horizontal = 6.dp, vertical = 6.dp)
                                    .weight(1f),
                                contentAlignment = Alignment.Center
                            ) {
                                Text(
                                    text = type,
                                    color = if (incidentType == type) Color.Black else Color.White,
                                    fontSize = 8.sp,
                                    fontWeight = FontWeight.Bold
                                )
                            }
                        }
                    }
                }
            },
            containerColor = CardCarbon
        )
    }

    // New Inspection dialog
    if (showNewInspectionDialog) {
        AlertDialog(
            onDismissRequest = { showNewInspectionDialog = false },
            confirmButton = {
                TextButton(onClick = {
                    if (inspectEquipmentName.isBlank() || inspectChecklist.isBlank()) {
                        Toast.makeText(context, "لطفاً موارد خالی را پر کنید", Toast.LENGTH_SHORT).show()
                    } else {
                        viewModel.submitInspection(
                            inspectEquipmentName,
                            inspectStatus,
                            inspectChecklist
                        )
                        showNewInspectionDialog = false
                        Toast.makeText(context, "بازرسی با موفقیت ثبت شد", Toast.LENGTH_SHORT).show()
                        inspectEquipmentName = ""
                        inspectChecklist = ""
                    }
                }) {
                    Text("ثبت نهایی", color = SafetyOrange)
                }
            },
            dismissButton = {
                TextButton(onClick = { showNewInspectionDialog = false }) {
                    Text("انصراف", color = TextGray)
                }
            },
            title = {
                Text(
                    "برگه بازرسی دوره‌ای تجهیزات صنعتی کارخانه",
                    color = Color.White,
                    fontSize = 16.sp,
                    fontWeight = FontWeight.Bold,
                    textAlign = TextAlign.End,
                    modifier = Modifier.fillMaxWidth()
                )
            },
            text = {
                Column(
                    modifier = Modifier.fillMaxWidth(),
                    verticalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    OutlinedTextField(
                        value = inspectEquipmentName,
                        onValueChange = { inspectEquipmentName = it },
                        label = { Text("نام و کد تجهیز (مثال: کپسول پودری دیزل ژنراتور)", color = TextGray) },
                        colors = OutlinedTextFieldDefaults.colors(focusedTextColor = Color.White, unfocusedTextColor = Color.White),
                        modifier = Modifier.fillMaxWidth()
                    )

                    OutlinedTextField(
                        value = inspectChecklist,
                        onValueChange = { inspectChecklist = it },
                        label = { Text("جزئیات بازرسی (وضعیت ضامن، گیج فشار، شیر نازل)", color = TextGray) },
                        colors = OutlinedTextFieldDefaults.colors(focusedTextColor = Color.White, unfocusedTextColor = Color.White),
                        modifier = Modifier.fillMaxWidth(),
                        maxLines = 3
                    )

                    Text("وضعیت کلی تجهیز:", color = Color.White, fontSize = 12.sp)
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(10.dp)
                    ) {
                        listOf("Safe", "Needs Maintenance", "Unsafe").forEach { status ->
                            Box(
                                modifier = Modifier
                                    .background(
                                        if (inspectStatus == status) SafetyOrange else ActiveCarbon,
                                        RoundedCornerShape(4.dp)
                                    )
                                    .clickable { inspectStatus = status }
                                    .padding(vertical = 8.dp)
                                    .weight(1f),
                                contentAlignment = Alignment.Center
                            ) {
                                Text(
                                    text = when (status) {
                                        "Safe" -> "ایمن و سالم"
                                        "Needs Maintenance" -> "نیاز به تعمیر"
                                        else -> "غیر ایمن و پلمپ"
                                    },
                                    color = if (inspectStatus == status) Color.Black else Color.White,
                                    fontSize = 10.sp,
                                    fontWeight = FontWeight.Bold
                                )
                            }
                        }
                    }
                }
            },
            containerColor = CardCarbon
        )
    }
}

// --- SUB SCREEN 4: TRAINING CENTER & HSE QUIZZES ---
@Composable
fun TrainingCenterScreen(viewModel: HSEViewModel) {
    val trainingList by viewModel.trainingItems.collectAsStateWithLifecycle()
    var selectedVideoId by remember { mutableStateOf<String?>(null) }
    val context = LocalContext.current

    val isAdmin = viewModel.loggedInUser?.username == "hamed" || viewModel.loggedInUser?.username == "admin"

    var showTrainingDialog by remember { mutableStateOf(false) }
    var editingTrainingItem by remember { mutableStateOf<TrainingItemEntity?>(null) }

    var trainingId by remember { mutableStateOf("") }
    var trainingTitle by remember { mutableStateOf("") }
    var trainingTitleFa by remember { mutableStateOf("") }
    var trainingType by remember { mutableStateOf("Video") } // Video, Toolbox Talk, Lesson Learned
    var trainingCategory by remember { mutableStateOf("عمومی") } // کار در ارتفاع, کوره دوار, فضای بسته, کمک‌های اولیه, عمومی
    var trainingContent by remember { mutableStateOf("") }
    var trainingDuration by remember { mutableStateOf("") }

    if (showTrainingDialog || editingTrainingItem != null) {
        val isEdit = editingTrainingItem != null
        AlertDialog(
            onDismissRequest = {
                showTrainingDialog = false
                editingTrainingItem = null
                trainingId = ""
                trainingTitle = ""
                trainingTitleFa = ""
                trainingType = "Video"
                trainingCategory = "عمومی"
                trainingContent = ""
                trainingDuration = ""
            },
            title = {
                Text(
                    text = if (isEdit) "ویرایش آیتم آموزشی" else "افزودن آیتم آموزشی جدید",
                    color = Color.White,
                    fontSize = 16.sp,
                    fontWeight = FontWeight.Bold,
                    modifier = Modifier.fillMaxWidth(),
                    textAlign = TextAlign.End
                )
            },
            text = {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .verticalScroll(rememberScrollState()),
                    verticalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    if (!isEdit) {
                        OutlinedTextField(
                            value = trainingId,
                            onValueChange = { trainingId = it },
                            label = { Text("شناسه انگلیسی (مثال: height_safety)", color = TextGray) },
                            modifier = Modifier.fillMaxWidth(),
                            colors = OutlinedTextFieldDefaults.colors(
                                focusedTextColor = Color.White,
                                unfocusedTextColor = Color.White,
                                focusedBorderColor = SafetyOrange,
                                unfocusedBorderColor = ActiveCarbon
                            )
                        )
                    }
                    OutlinedTextField(
                        value = trainingTitleFa,
                        onValueChange = { trainingTitleFa = it },
                        label = { Text("عنوان فارسی دوره (مثال: کار با جرثقیل)", color = TextGray) },
                        modifier = Modifier.fillMaxWidth(),
                        colors = OutlinedTextFieldDefaults.colors(
                            focusedTextColor = Color.White,
                            unfocusedTextColor = Color.White,
                            focusedBorderColor = SafetyOrange,
                            unfocusedBorderColor = ActiveCarbon
                        )
                    )
                    OutlinedTextField(
                        value = trainingTitle,
                        onValueChange = { trainingTitle = it },
                        label = { Text("عنوان انگلیسی دوره (مثال: Crane Safety)", color = TextGray) },
                        modifier = Modifier.fillMaxWidth(),
                        colors = OutlinedTextFieldDefaults.colors(
                            focusedTextColor = Color.White,
                            unfocusedTextColor = Color.White,
                            focusedBorderColor = SafetyOrange,
                            unfocusedBorderColor = ActiveCarbon
                        )
                    )
                    OutlinedTextField(
                        value = trainingCategory,
                        onValueChange = { trainingCategory = it },
                        label = { Text("دسته‌بندی (مثال: کار در ارتفاع یا کوره دوار)", color = TextGray) },
                        modifier = Modifier.fillMaxWidth(),
                        colors = OutlinedTextFieldDefaults.colors(
                            focusedTextColor = Color.White,
                            unfocusedTextColor = Color.White,
                            focusedBorderColor = SafetyOrange,
                            unfocusedBorderColor = ActiveCarbon
                        )
                    )
                    OutlinedTextField(
                        value = trainingType,
                        onValueChange = { trainingType = it },
                        label = { Text("نوع (مثال: Video یا Toolbox Talk)", color = TextGray) },
                        modifier = Modifier.fillMaxWidth(),
                        colors = OutlinedTextFieldDefaults.colors(
                            focusedTextColor = Color.White,
                            unfocusedTextColor = Color.White,
                            focusedBorderColor = SafetyOrange,
                            unfocusedBorderColor = ActiveCarbon
                        )
                    )
                    OutlinedTextField(
                        value = trainingDuration,
                        onValueChange = { trainingDuration = it },
                        label = { Text("مدت زمان/حجم (مثال: 12 دقیقه)", color = TextGray) },
                        modifier = Modifier.fillMaxWidth(),
                        colors = OutlinedTextFieldDefaults.colors(
                            focusedTextColor = Color.White,
                            unfocusedTextColor = Color.White,
                            focusedBorderColor = SafetyOrange,
                            unfocusedBorderColor = ActiveCarbon
                        )
                    )
                    OutlinedTextField(
                        value = trainingContent,
                        onValueChange = { trainingContent = it },
                        label = { Text("محتوا یا توضیحات دوره", color = TextGray) },
                        modifier = Modifier.fillMaxWidth(),
                        colors = OutlinedTextFieldDefaults.colors(
                            focusedTextColor = Color.White,
                            unfocusedTextColor = Color.White,
                            focusedBorderColor = SafetyOrange,
                            unfocusedBorderColor = ActiveCarbon
                        )
                    )
                }
            },
            confirmButton = {
                TextButton(
                    onClick = {
                        val finalId = if (isEdit) editingTrainingItem!!.id else trainingId.trim()
                        if (finalId.isNotBlank() && trainingTitleFa.isNotBlank()) {
                            val item = TrainingItemEntity(
                                id = finalId,
                                title = if (trainingTitle.isBlank()) finalId else trainingTitle,
                                titleFa = trainingTitleFa,
                                type = trainingType,
                                category = trainingCategory,
                                content = trainingContent,
                                durationOrLength = trainingDuration,
                                isQuizAvailable = editingTrainingItem?.isQuizAvailable ?: false,
                                quizPassed = editingTrainingItem?.quizPassed ?: false,
                                quizScore = editingTrainingItem?.quizScore ?: 0
                            )
                            viewModel.addOrUpdateTrainingItem(item) {
                                Toast.makeText(context, "دوره آموزشی با موفقیت ذخیره شد.", Toast.LENGTH_SHORT).show()
                            }
                            showTrainingDialog = false
                            editingTrainingItem = null
                            trainingId = ""
                            trainingTitle = ""
                            trainingTitleFa = ""
                            trainingType = "Video"
                            trainingCategory = "عمومی"
                            trainingContent = ""
                            trainingDuration = ""
                        } else {
                            Toast.makeText(context, "لطفاً شناسه و عنوان دوره را پر کنید.", Toast.LENGTH_SHORT).show()
                        }
                    }
                ) {
                    Text("ذخیره", color = SafetyOrange)
                }
            },
            dismissButton = {
                TextButton(
                    onClick = {
                        showTrainingDialog = false
                        editingTrainingItem = null
                        trainingId = ""
                        trainingTitle = ""
                        trainingTitleFa = ""
                        trainingType = "Video"
                        trainingCategory = "عمومی"
                        trainingContent = ""
                        trainingDuration = ""
                    }
                ) {
                    Text("انصراف", color = TextGray)
                }
            },
            containerColor = CardCarbon
        )
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .verticalScroll(rememberScrollState())
            .padding(16.dp)
    ) {
        Row(
            modifier = Modifier.fillMaxWidth().padding(bottom = 8.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            if (isAdmin) {
                Button(
                    onClick = { showTrainingDialog = true },
                    colors = ButtonDefaults.buttonColors(containerColor = SafetyOrange),
                    shape = RoundedCornerShape(6.dp),
                    contentPadding = PaddingValues(horizontal = 12.dp, vertical = 6.dp),
                    modifier = Modifier.height(32.dp)
                ) {
                    Icon(Icons.Filled.Add, contentDescription = null, tint = Color.Black, modifier = Modifier.size(14.dp))
                    Spacer(modifier = Modifier.width(4.dp))
                    Text("افزودن دوره", color = Color.Black, fontSize = 10.sp, fontWeight = FontWeight.Bold)
                }
            } else {
                Spacer(modifier = Modifier.width(1.dp))
            }
            Text(
                text = "مرکز آموزش ایمنی و آزمون‌های HSE",
                color = Color.White,
                fontSize = 18.sp,
                fontWeight = FontWeight.Bold
            )
        }
        Text(
            text = "ویدئوها، توجیهی‌های کارگاهی (Toolbox Talks) و تجربیات حوادث",
            color = TextGray,
            fontSize = 11.sp,
            textAlign = TextAlign.End,
            modifier = Modifier
                .fillMaxWidth()
                .padding(bottom = 16.dp)
        )

        // Certificate generation visual state
        viewModel.earnedCertificateName?.let { certName ->
            Card(
                colors = CardDefaults.cardColors(containerColor = Color(0xFF1B5E20)),
                border = BorderStroke(2.dp, SafetyOrange),
                shape = RoundedCornerShape(12.dp),
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(bottom = 16.dp)
            ) {
                Column(
                    modifier = Modifier.padding(16.dp),
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    Icon(Icons.Filled.CardMembership, contentDescription = null, tint = SafetyYellow, modifier = Modifier.size(48.dp))
                    Spacer(modifier = Modifier.height(8.dp))
                    Text(
                        text = "🎖️ صدور گواهینامه الکترونیکی ایمنی",
                        color = Color.White,
                        fontSize = 16.sp,
                        fontWeight = FontWeight.Bold
                    )
                    Text(
                        text = "این گواهینامه تایید می‌کند که شما دوره آموزشی '$certName' را در کارخانه سیمان سفید نیریز با موفقیت گذرانده‌اید.",
                        color = TextLight,
                        fontSize = 12.sp,
                        textAlign = TextAlign.Center,
                        modifier = Modifier.padding(vertical = 6.dp)
                    )
                    Button(
                        onClick = {
                            Toast.makeText(context, "نسخه چاپی گواهینامه در حافظه دستگاه ذخیره شد 📄", Toast.LENGTH_LONG).show()
                        },
                        colors = ButtonDefaults.buttonColors(containerColor = SafetyOrange)
                    ) {
                        Text("دانلود گواهینامه به صورت PDF", color = Color.Black)
                    }
                }
            }
        }

        trainingList.forEach { item ->
            Card(
                colors = CardDefaults.cardColors(containerColor = CardCarbon),
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(bottom = 12.dp)
            ) {
                Column(modifier = Modifier.padding(14.dp)) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                            Box(
                                modifier = Modifier
                                    .background(SafetyOrange.copy(alpha = 0.15f), RoundedCornerShape(4.dp))
                                    .padding(horizontal = 8.dp, vertical = 4.dp)
                            ) {
                                Text(item.type, color = SafetyOrange, fontSize = 10.sp, fontWeight = FontWeight.Bold)
                            }

                            if (isAdmin) {
                                IconButton(
                                    onClick = {
                                        editingTrainingItem = item
                                        trainingId = item.id
                                        trainingTitle = item.title
                                        trainingTitleFa = item.titleFa
                                        trainingType = item.type
                                        trainingCategory = item.category
                                        trainingContent = item.content
                                        trainingDuration = item.durationOrLength
                                    },
                                    modifier = Modifier.size(24.dp)
                                ) {
                                    Icon(Icons.Filled.Edit, contentDescription = "ویرایش", tint = SafetyOrange, modifier = Modifier.size(16.dp))
                                }
                                IconButton(
                                    onClick = {
                                        viewModel.deleteTrainingItem(item.id) {
                                            Toast.makeText(context, "دوره با موفقیت حذف شد.", Toast.LENGTH_SHORT).show()
                                        }
                                    },
                                    modifier = Modifier.size(24.dp)
                                ) {
                                    Icon(Icons.Filled.Delete, contentDescription = "حذف", tint = SafetyRed, modifier = Modifier.size(16.dp))
                                }
                            }
                        }

                        Text(
                            text = item.titleFa,
                            color = Color.White,
                            fontSize = 14.sp,
                            fontWeight = FontWeight.Bold,
                            textAlign = TextAlign.End,
                            modifier = Modifier.weight(1f).padding(start = 12.dp)
                        )
                    }

                    Spacer(modifier = Modifier.height(8.dp))
                    Text(
                        text = item.content,
                        color = TextGray,
                        fontSize = 11.sp,
                        textAlign = TextAlign.End,
                        modifier = Modifier.fillMaxWidth(),
                        lineHeight = 18.sp
                    )

                    Spacer(modifier = Modifier.height(12.dp))
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Icon(Icons.Filled.Timer, contentDescription = null, tint = TextGray, modifier = Modifier.size(14.dp))
                            Spacer(modifier = Modifier.width(4.dp))
                            Text(item.durationOrLength, color = TextGray, fontSize = 11.sp)
                        }

                        if (item.quizPassed) {
                            Text("قبول شده در آزمون ✅", color = SafetyGreen, fontSize = 11.sp, fontWeight = FontWeight.Bold)
                        } else if (item.isQuizAvailable) {
                            Button(
                                onClick = { viewModel.startQuiz(item.id) },
                                colors = ButtonDefaults.buttonColors(containerColor = SafetyOrange),
                                contentPadding = PaddingValues(horizontal = 12.dp, vertical = 4.dp),
                                shape = RoundedCornerShape(4.dp),
                                modifier = Modifier.height(28.dp)
                            ) {
                                Text("شروع آزمون ✍️", color = Color.Black, fontSize = 10.sp, fontWeight = FontWeight.Bold)
                            }
                        }
                    }
                }
            }
        }
    }

    // Active Quiz Dialog
    if (viewModel.activeQuizId != null && !viewModel.isQuizCompleted) {
        val qIdx = viewModel.currentQuestionIndex
        val question = viewModel.activeQuizQuestions.getOrNull(qIdx)

        if (question != null) {
            AlertDialog(
                onDismissRequest = { viewModel.activeQuizId = null },
                confirmButton = {
                    TextButton(
                        onClick = { viewModel.submitAnswer() },
                        enabled = viewModel.selectedAnswerIndex != -1
                    ) {
                        Text("ثبت پاسخ", color = SafetyOrange)
                    }
                },
                dismissButton = {
                    TextButton(onClick = { viewModel.activeQuizId = null }) {
                        Text("انصراف", color = TextGray)
                    }
                },
                title = {
                    Text(
                        "آزمون شماره ${qIdx + 1}",
                        color = Color.White,
                        fontSize = 16.sp,
                        fontWeight = FontWeight.Bold,
                        textAlign = TextAlign.End,
                        modifier = Modifier.fillMaxWidth()
                    )
                },
                text = {
                    Column(modifier = Modifier.fillMaxWidth()) {
                        Text(
                            text = question.questionText,
                            color = Color.White,
                            fontSize = 13.sp,
                            fontWeight = FontWeight.Bold,
                            textAlign = TextAlign.End,
                            modifier = Modifier.fillMaxWidth()
                        )

                        Spacer(modifier = Modifier.height(16.dp))

                        question.options.forEachIndexed { optIdx, option ->
                            Row(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(vertical = 4.dp)
                                    .background(
                                        if (viewModel.selectedAnswerIndex == optIdx) SafetyOrange.copy(alpha = 0.2f) else ActiveCarbon,
                                        RoundedCornerShape(8.dp)
                                    )
                                    .border(
                                        1.dp,
                                        if (viewModel.selectedAnswerIndex == optIdx) SafetyOrange else Color.Transparent,
                                        RoundedCornerShape(8.dp)
                                    )
                                    .clickable { viewModel.selectedAnswerIndex = optIdx }
                                    .padding(12.dp),
                                horizontalArrangement = Arrangement.End,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Text(
                                    text = option,
                                    color = Color.White,
                                    fontSize = 11.sp,
                                    textAlign = TextAlign.End
                                )
                                Spacer(modifier = Modifier.width(10.dp))
                                RadioButton(
                                    selected = viewModel.selectedAnswerIndex == optIdx,
                                    onClick = { viewModel.selectedAnswerIndex = optIdx },
                                    colors = RadioButtonDefaults.colors(selectedColor = SafetyOrange)
                                )
                            }
                        }
                    }
                },
                containerColor = CardCarbon
            )
        }
    }
}

// --- SUB SCREEN 5: MORE ADVANCED FEATURES HUB ---
@Composable
fun MoreHubScreen(viewModel: HSEViewModel, onNavigate: (String) -> Unit) {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .verticalScroll(rememberScrollState())
            .padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        Text(
            text = "امکانات پیشرفته و پنل مدیریتی",
            color = Color.White,
            fontSize = 18.sp,
            fontWeight = FontWeight.Bold,
            modifier = Modifier.align(Alignment.End)
        )

        Spacer(modifier = Modifier.height(12.dp))

        MoreMenuButton(
            title = "دستیار صوتی و متنی هوشمند AI HSE",
            subtitle = "پاسخ بر اساس آیین‌نامه‌ها و MSDS بوسیله Gemini",
            icon = Icons.Filled.SmartButton,
            color = SafetyOrange
        ) {
            onNavigate("ai_assistant")
        }

        MoreMenuButton(
            title = "اسکنر هوشمند کد QR کارخانه",
            subtitle = "شناسایی سریع تجهیزات، کوره، مواد شیمیایی و وضعیت بازرسی",
            icon = Icons.Filled.QrCodeScanner,
            color = SafetyYellow
        ) {
            onNavigate("qr_scanner")
        }

        MoreMenuButton(
            title = "پایش محیط زیست و سلامت پرسنل",
            subtitle = "شاخص آلودگی، مانیتورینگ دودکش کوره، بهداشت شغلی",
            icon = Icons.Filled.Eco,
            color = SafetyGreen
        ) {
            onNavigate("health_env")
        }

        MoreMenuButton(
            title = "رعایت موارد ایمنی در کارخانه",
            subtitle = "منشور ایمنی طلایی، خودارزیابی آمادگی و قوانین کارگاهی",
            icon = Icons.Filled.HealthAndSafety,
            color = SafetyGreen
        ) {
            onNavigate("safety_rules")
        }

        // Only allow managers or officers to view Admin Panel
        val userRole = viewModel.loggedInUser?.role ?: "Operator"
        val isManager = userRole == "HSE Officer" || userRole == "Executive"

        MoreMenuButton(
            title = "پنل مدیریت HSE و نظارت ارشد",
            subtitle = if (isManager) "گزارشات آماری، تایید انحرافات، مدیریت کاربران" else "مخصوص سرپرستان و مدیران ارشد",
            icon = Icons.Filled.AdminPanelSettings,
            color = if (isManager) SafetyRed else TextGray,
            enabled = isManager
        ) {
            onNavigate("admin")
        }

        Spacer(modifier = Modifier.height(16.dp))
        HorizontalDivider(color = ActiveCarbon, thickness = 1.dp)
        Spacer(modifier = Modifier.height(8.dp))
        Text(
            text = "تهیه کننده: مهندس حامد کریمی زاده",
            color = TextGray,
            fontSize = 11.sp,
            fontWeight = FontWeight.Bold,
            modifier = Modifier.align(Alignment.CenterHorizontally)
        )
    }
}

@Composable
fun MoreMenuButton(
    title: String,
    subtitle: String,
    icon: ImageVector,
    color: Color,
    enabled: Boolean = true,
    onClick: () -> Unit
) {
    Card(
        colors = CardDefaults.cardColors(
            containerColor = if (enabled) CardCarbon else CardCarbon.copy(alpha = 0.5f)
        ),
        modifier = Modifier
            .fillMaxWidth()
            .clickable(enabled = enabled, onClick = onClick),
        border = BorderStroke(1.dp, if (enabled) ActiveCarbon else Color.Transparent)
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Icon(Icons.Filled.ChevronLeft, contentDescription = null, tint = TextGray)

            Column(horizontalAlignment = Alignment.End, modifier = Modifier.weight(1f).padding(end = 16.dp)) {
                Text(
                    text = title,
                    color = if (enabled) Color.White else TextGray,
                    fontSize = 14.sp,
                    fontWeight = FontWeight.Bold
                )
                Text(
                    text = subtitle,
                    color = TextGray,
                    fontSize = 11.sp,
                    textAlign = TextAlign.End,
                    modifier = Modifier.fillMaxWidth()
                )
            }

            Box(
                modifier = Modifier
                    .size(44.dp)
                    .background(color.copy(alpha = 0.15f), CircleShape),
                contentAlignment = Alignment.Center
            ) {
                Icon(icon, contentDescription = null, tint = color, modifier = Modifier.size(20.dp))
            }
        }
    }
}

// --- SUB SCREEN 6: AI SMART HSE ASSISTANT ---
@Composable
fun AIAssistantScreen(viewModel: HSEViewModel) {
    var userPrompt by remember { mutableStateOf("") }
    val messages = viewModel.chatMessages
    val isLoading = viewModel.isChatLoading

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp)
    ) {
        // Warning Banner for API Key Extractions as mandated
        Card(
            colors = CardDefaults.cardColors(containerColor = Color(0xFF3E2723)),
            modifier = Modifier
                .fillMaxWidth()
                .padding(bottom = 12.dp)
        ) {
            Text(
                text = "🛡️ هشدار امنیت داده: این نسخه آزمایشی جهت دمو مجهز به مدل هوش مصنوعی Gemini 3.5 Flash می‌باشد. پاسخ‌ها مشورتی بوده و تایید نهایی ایمنی با افسر HSE شیفت است.",
                color = SafetyOrange,
                fontSize = 10.sp,
                modifier = Modifier.padding(10.dp),
                textAlign = TextAlign.Right
            )
        }

        // Chat List
        LazyColumn(
            modifier = Modifier
                .weight(1f)
                .fillMaxWidth(),
            reverseLayout = false,
            verticalArrangement = Arrangement.spacedBy(10.dp)
        ) {
            items(messages) { msg ->
                val isUser = msg.sender == "USER"
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = if (isUser) Arrangement.End else Arrangement.Start
                ) {
                    Card(
                        colors = CardDefaults.cardColors(
                            containerColor = if (isUser) SafetyOrange else CardCarbon
                        ),
                        shape = RoundedCornerShape(
                            topStart = 12.dp,
                            topEnd = 12.dp,
                            bottomStart = if (isUser) 12.dp else 0.dp,
                            bottomEnd = if (isUser) 0.dp else 12.dp
                        ),
                        modifier = Modifier.widthIn(max = 280.dp)
                    ) {
                        Column(modifier = Modifier.padding(12.dp)) {
                            Text(
                                text = msg.content,
                                color = if (isUser) Color.Black else Color.White,
                                fontSize = 12.sp,
                                lineHeight = 18.sp,
                                textAlign = TextAlign.End
                            )
                            Spacer(modifier = Modifier.height(4.dp))
                            Text(
                                text = msg.time,
                                color = if (isUser) Color.Black.copy(alpha = 0.6f) else TextGray,
                                fontSize = 9.sp,
                                modifier = Modifier.align(Alignment.Start)
                            )
                        }
                    }
                }
            }

            if (isLoading) {
                item {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(8.dp),
                        horizontalArrangement = Arrangement.Start
                    ) {
                        CircularProgressIndicator(
                            color = SafetyOrange,
                            modifier = Modifier.size(20.dp)
                        )
                        Spacer(modifier = Modifier.width(10.dp))
                        Text("درحال تحلیل مستندات کارخانه...", color = TextGray, fontSize = 11.sp)
                    }
                }
            }
        }

        // Suggestion Chips
        Spacer(modifier = Modifier.height(10.dp))
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .horizontalScroll(rememberScrollState()),
            horizontalArrangement = Arrangement.spacedBy(6.dp)
        ) {
            val suggestions = listOf(
                "تجهیزات الزامی برای جوشکاری چیست؟",
                "نحوه خاموش کردن حریق برقی سالن کنترل",
                "مخاطرات غبار سیلیس سیمان نیریز",
                "اقدام اول در نشت کمک‌سایش"
            )
            suggestions.forEach { promptText ->
                Box(
                    modifier = Modifier
                        .background(ActiveCarbon, RoundedCornerShape(16.dp))
                        .clickable {
                            userPrompt = promptText
                            viewModel.askAssistant(promptText)
                            userPrompt = ""
                        }
                        .padding(horizontal = 12.dp, vertical = 6.dp)
                ) {
                    Text(promptText, color = Color.White, fontSize = 10.sp)
                }
            }
        }

        Spacer(modifier = Modifier.height(10.dp))

        // Input field
        Row(
            modifier = Modifier.fillMaxWidth(),
            verticalAlignment = Alignment.CenterVertically
        ) {
            OutlinedTextField(
                value = userPrompt,
                onValueChange = { userPrompt = it },
                placeholder = { Text("سوال خود را به صورت طبیعی بپرسید...", color = TextGray) },
                colors = OutlinedTextFieldDefaults.colors(
                    focusedTextColor = Color.White,
                    unfocusedTextColor = Color.White,
                    focusedBorderColor = SafetyOrange,
                    unfocusedBorderColor = ActiveCarbon
                ),
                modifier = Modifier
                    .weight(1f)
                    .padding(end = 8.dp),
                maxLines = 2
            )

            IconButton(
                onClick = {
                    if (userPrompt.isNotBlank()) {
                        viewModel.askAssistant(userPrompt)
                        userPrompt = ""
                    }
                },
                modifier = Modifier
                    .background(SafetyOrange, CircleShape)
                    .size(48.dp)
            ) {
                Icon(Icons.AutoMirrored.Filled.Send, contentDescription = "ارسال", tint = Color.Black)
            }
        }
    }
}

// --- SUB SCREEN 7: SMART QR SCANNER ---
@Composable
fun QRScannerScreen(viewModel: HSEViewModel) {
    var simulatedScanCode by remember { mutableStateOf("") }
    var scannedEquipmentInfo by remember { mutableStateOf<ScannedEquipment?>(null) }
    var isScanning by remember { mutableStateOf(false) }

    LaunchedEffect(isScanning) {
        if (isScanning) {
            delay(2000)
            isScanning = false
            scannedEquipmentInfo = when (simulatedScanCode) {
                "KILN-01" -> ScannedEquipment(
                    code = "KILN-01",
                    name = "کوره دوار شماره ۱ (Rotary Kiln)",
                    status = "آماده به کار - دمای مجاز بدنه",
                    lastInspect = "۱۴۰۵-۰۴-۱۰ توسط مهندس همایونی",
                    msdsSummary = "مخاطرات: حرارت تشعشعی بالا، آجرهای نسوز لق، سقوط بار معلق کوره. تجهیزات الزامی: کلاه و عینک کارگاهی نسوز، کفش پنبه‌ای.",
                    emergencyRules = "در صورت آتش‌سوزی سوخت کوره (مازوت/گاز طبیعی)، فلکه‌های گاز خط اصلی را قطع کرده و از کف شیمیایی استفاده کنید."
                )
                "MILL-02" -> ScannedEquipment(
                    code = "MILL-02",
                    name = "آسیاب سیمان سفید شماره ۲ (Cement Mill)",
                    status = "فعال - سطح لرزش معمولی",
                    lastInspect = "۱۴۰۵-۰۴-۰۸ توسط علی رستمی",
                    msdsSummary = "مخاطرات: نویز بالاتر از ۸۸ دسی‌بل، غبار سیمان غلیظ معلق در هوا. تجهیزات الزامی: گوشی ایرموف حفاظتی، عینک ایمنی و ماسک غبار N95.",
                    emergencyRules = "کلید قطع اضطراری برق (Emergency Stop) در سمت چپ بدنه ورودی قرار دارد."
                )
                "CHEM-31" -> ScannedEquipment(
                    code = "CHEM-31",
                    name = "مخازن مایع کمک‌سایش (Grinding Aids)",
                    status = "پایش نشتی - سالم فیزیکی",
                    lastInspect = "۱۴۰۵-۰۴-۰۲ توسط واحد ایمنی",
                    msdsSummary = "ماده شیمیایی سوزاننده پوست و مخاط چشم. در صورت برخورد، موضع را با آب فراوان ۱۵ دقیقه شستشو دهید.",
                    emergencyRules = "در صورت نشت عمده، فوراً مسیر آبرفت‌های سالن را با پودر جاذب مسدود کنید."
                )
                else -> ScannedEquipment(
                    code = "EQUIP-GEN",
                    name = "کپسول آتش‌نشانی CO2 پیش‌گرمکن",
                    status = "سالم - فشار کامل گیج در فاز سبز",
                    lastInspect = "۱۴۰۵-۰۴-۱۲ توسط مهندس همایونی",
                    msdsSummary = "تجهیز تحت فشار بالا. در فضا چرخ و نازل را بررسی کنید.",
                    emergencyRules = "هنگام استفاده نازل سرد را مستقیم نگیرید تا دست دچار سرمازدگی نشود."
                )
            }
        }
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Text(
            text = "سامانه اسکنر بارکد و کد QR تجهیزات",
            color = Color.White,
            fontSize = 18.sp,
            fontWeight = FontWeight.Bold
        )

        Spacer(modifier = Modifier.height(16.dp))

        // Viewfinder representation with Canvas lines
        Box(
            modifier = Modifier
                .size(240.dp)
                .background(CardCarbon, RoundedCornerShape(16.dp))
                .border(2.dp, SafetyOrange, RoundedCornerShape(16.dp)),
            contentAlignment = Alignment.Center
        ) {
            Canvas(modifier = Modifier.fillMaxSize()) {
                val len = 40f
                // Draw target crosshairs
                drawLine(Color.White, Offset(20f, 20f), Offset(20f + len, 20f), strokeWidth = 6f)
                drawLine(Color.White, Offset(20f, 20f), Offset(20f, 20f + len), strokeWidth = 6f)

                drawLine(Color.White, Offset(size.width - 20f, 20f), Offset(size.width - 20f - len, 20f), strokeWidth = 6f)
                drawLine(Color.White, Offset(size.width - 20f, 20f), Offset(size.width - 20f, 20f + len), strokeWidth = 6f)

                drawLine(Color.White, Offset(20f, size.height - 20f), Offset(20f + len, size.height - 20f), strokeWidth = 6f)
                drawLine(Color.White, Offset(20f, size.height - 20f), Offset(20f, size.height - 20f - len), strokeWidth = 6f)

                drawLine(Color.White, Offset(size.width - 20f, size.height - 20f), Offset(size.width - 20f - len, size.height - 20f), strokeWidth = 6f)
                drawLine(Color.White, Offset(size.width - 20f, size.height - 20f), Offset(size.width - 20f, size.height - 20f - len), strokeWidth = 6f)

                // Laser scan line
                if (isScanning) {
                    drawLine(SafetyOrange, Offset(0f, size.height * 0.5f), Offset(size.width, size.height * 0.5f), strokeWidth = 4f)
                }
            }

            if (isScanning) {
                Text("در حال رمزگشایی کدهای تجهیز...", color = SafetyOrange, fontSize = 12.sp, fontWeight = FontWeight.Bold)
            } else {
                Icon(Icons.Filled.QrCode, contentDescription = null, tint = SafetyOrange.copy(alpha = 0.3f), modifier = Modifier.size(100.dp))
            }
        }

        Spacer(modifier = Modifier.height(20.dp))

        // Simulated scanner search keys
        Text("شبیه‌سازی اسکن با کدهای زیر:", color = TextGray, fontSize = 11.sp)
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            listOf("KILN-01", "MILL-02", "CHEM-31").forEach { code ->
                Button(
                    onClick = {
                        simulatedScanCode = code
                        isScanning = true
                    },
                    colors = ButtonDefaults.buttonColors(containerColor = ActiveCarbon),
                    modifier = Modifier.weight(1f),
                    contentPadding = PaddingValues(0.dp)
                ) {
                    Text(code, color = Color.White, fontSize = 10.sp)
                }
            }
        }

        Spacer(modifier = Modifier.height(20.dp))

        // Scanned Info Panel
        scannedEquipmentInfo?.let { equip ->
            Card(
                colors = CardDefaults.cardColors(containerColor = CardCarbon),
                border = BorderStroke(1.dp, SafetyOrange),
                modifier = Modifier
                    .fillMaxWidth()
                    .verticalScroll(rememberScrollState())
            ) {
                Column(modifier = Modifier.padding(16.dp)) {
                    Text(
                        text = equip.name,
                        color = Color.White,
                        fontSize = 16.sp,
                        fontWeight = FontWeight.Bold,
                        modifier = Modifier.align(Alignment.End)
                    )
                    Text(
                        text = "کد تجهیز: ${equip.code}",
                        color = SafetyOrange,
                        fontSize = 12.sp,
                        modifier = Modifier.align(Alignment.End)
                    )

                    HorizontalDivider(modifier = Modifier.padding(vertical = 10.dp), color = ActiveCarbon)

                    Text(
                        text = "وضعیت مانیتورینگ: ${equip.status}",
                        color = SafetyGreen,
                        fontSize = 12.sp,
                        fontWeight = FontWeight.Bold,
                        textAlign = TextAlign.End,
                        modifier = Modifier.fillMaxWidth()
                    )

                    Spacer(modifier = Modifier.height(8.dp))
                    Text(
                        text = "بررسی ایمنی: " + equip.msdsSummary,
                        color = TextLight,
                        fontSize = 12.sp,
                        textAlign = TextAlign.End,
                        modifier = Modifier.fillMaxWidth()
                    )

                    Spacer(modifier = Modifier.height(8.dp))
                    Text(
                        text = "واکنش اضطراری: " + equip.emergencyRules,
                        color = SafetyYellow,
                        fontSize = 12.sp,
                        textAlign = TextAlign.End,
                        modifier = Modifier.fillMaxWidth()
                    )
                }
            }
        }
    }
}

data class ScannedEquipment(
    val code: String,
    val name: String,
    val status: String,
    val lastInspect: String,
    val msdsSummary: String,
    val emergencyRules: String
)

// --- SUB SCREEN 8: HEALTH & ENVIRONMENT MONITORING ---
@Composable
fun HealthEnvironmentalScreen(viewModel: HSEViewModel) {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .verticalScroll(rememberScrollState())
            .padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        Text(
            text = "پایش زنده بهداشت شغلی و محیط زیست",
            color = Color.White,
            fontSize = 18.sp,
            fontWeight = FontWeight.Bold,
            modifier = Modifier.align(Alignment.End)
        )

        // Industrial Dust and air pollutions widget
        Card(
            colors = CardDefaults.cardColors(containerColor = CardCarbon),
            modifier = Modifier.fillMaxWidth()
        ) {
            Column(modifier = Modifier.padding(16.dp)) {
                Text(
                    text = "سنسور سنجش آلودگی هوای کارخانه (دودکش اصلی کوره)",
                    color = Color.White,
                    fontSize = 14.sp,
                    fontWeight = FontWeight.Bold
                )
                Spacer(modifier = Modifier.height(12.dp))

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Column(horizontalAlignment = Alignment.CenterHorizontally) {
                        Text("PM 2.5", color = TextGray, fontSize = 11.sp)
                        Text("۱۲.۵ µg/m³", color = SafetyGreen, fontSize = 16.sp, fontWeight = FontWeight.Bold)
                        Text("حد مجاز: ۲۵", color = TextGray, fontSize = 9.sp)
                    }

                    Column(horizontalAlignment = Alignment.CenterHorizontally) {
                        Text("PM 10", color = TextGray, fontSize = 11.sp)
                        Text("۴۵.۸ µg/m³", color = SafetyGreen, fontSize = 16.sp, fontWeight = FontWeight.Bold)
                        Text("حد مجاز: ۵۰", color = TextGray, fontSize = 9.sp)
                    }

                    Column(horizontalAlignment = Alignment.CenterHorizontally) {
                        Text("گاز CO", color = TextGray, fontSize = 11.sp)
                        Text("۱.۴ ppm", color = SafetyGreen, fontSize = 16.sp, fontWeight = FontWeight.Bold)
                        Text("حد مجاز: ۹", color = TextGray, fontSize = 9.sp)
                    }
                }
            }
        }

        // Noise and sound pollution widget
        Card(
            colors = CardDefaults.cardColors(containerColor = CardCarbon),
            modifier = Modifier.fillMaxWidth()
        ) {
            Column(modifier = Modifier.padding(16.dp)) {
                Text(
                    text = "میزان تراز صدا در واحدهای کارخانه",
                    color = Color.White,
                    fontSize = 14.sp,
                    fontWeight = FontWeight.Bold
                )
                Spacer(modifier = Modifier.height(12.dp))

                EnvironmentalNoiseRow(unit = "سالن آسیاب سیمان", noiseDb = "۸۸ دسی‌بل ⚠️", requiredPPE = "ایرموف و ایرپلاگ الزامی")
                EnvironmentalNoiseRow(unit = "اتاق کنترل مرکزی (CCR)", noiseDb = "۵۲ دسی‌بل", requiredPPE = "ایمن بدون نیاز به گوشی")
                EnvironmentalNoiseRow(unit = "سنگ‌شکن سنگ آهک", noiseDb = "۸۲ دسی‌بل", requiredPPE = "استفاده دائم در فاز فعال")
            }
        }

        // Silicosis prevention guides
        Card(
            colors = CardDefaults.cardColors(containerColor = CardCarbon),
            modifier = Modifier.fillMaxWidth()
        ) {
            Column(modifier = Modifier.padding(16.dp)) {
                Text(
                    text = "طرح پیشگیری بیماری ریوی سیلیکوزیس",
                    color = SafetyYellow,
                    fontSize = 14.sp,
                    fontWeight = FontWeight.Bold,
                    modifier = Modifier.align(Alignment.End)
                )
                Spacer(modifier = Modifier.height(8.dp))
                Text(
                    text = "بیماری سیلیکوزیس به دلیل استنشاق ذرات ریز دی‌اکسید سیلیسیم آزاد بوجود می‌آید. معاینات سالانه ریه (اسپیرومتری) و عکس قفسه سینه برای کلیه کارگران سالن آسیاب سیمان و گالری‌های نقاله اجباری است.",
                    color = TextLight,
                    fontSize = 11.sp,
                    textAlign = TextAlign.End,
                    modifier = Modifier.fillMaxWidth()
                )
            }
        }
    }
}

@Composable
fun EnvironmentalNoiseRow(unit: String, noiseDb: String, requiredPPE: String) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 4.dp),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        Text(requiredPPE, color = TextGray, fontSize = 11.sp)
        Text(noiseDb, color = SafetyOrange, fontSize = 12.sp, fontWeight = FontWeight.Bold)
        Text(unit, color = Color.White, fontSize = 12.sp)
    }
}

// --- SUB SCREEN 9: ADMIN PANEL ---
@Composable
fun AdminPanelScreen(viewModel: HSEViewModel) {
    val incidentsList by viewModel.incidents.collectAsStateWithLifecycle()
    val auditLogsList by viewModel.auditLogs.collectAsStateWithLifecycle()
    val context = LocalContext.current

    Column(
        modifier = Modifier
            .fillMaxSize()
            .verticalScroll(rememberScrollState())
            .padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        Text(
            text = "پنل مدیریتی ارشد و بررسی انحرافات",
            color = Color.White,
            fontSize = 18.sp,
            fontWeight = FontWeight.Bold,
            modifier = Modifier.align(Alignment.End)
        )

        // Actions: Export Reports
        Card(
            colors = CardDefaults.cardColors(containerColor = CardCarbon),
            modifier = Modifier.fillMaxWidth()
        ) {
            Column(modifier = Modifier.padding(16.dp)) {
                Text(
                    text = "خروجی اسناد رسمی HSE کارخانه",
                    color = Color.White,
                    fontSize = 14.sp,
                    fontWeight = FontWeight.Bold
                )
                Spacer(modifier = Modifier.height(12.dp))

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    Button(
                        onClick = {
                            Toast.makeText(context, "گزارش اکسل آمار حوادث خروجی گرفته شد Excel 📊", Toast.LENGTH_SHORT).show()
                        },
                        colors = ButtonDefaults.buttonColors(containerColor = ActiveCarbon),
                        modifier = Modifier.weight(1f)
                    ) {
                        Text("خروجی اکسل", color = Color.White, fontSize = 11.sp)
                    }

                    Button(
                        onClick = {
                            Toast.makeText(context, "گزارش تفصیلی حوادث در قالب PDF ایجاد گردید PDF 📄", Toast.LENGTH_SHORT).show()
                        },
                        colors = ButtonDefaults.buttonColors(containerColor = ActiveCarbon),
                        modifier = Modifier.weight(1f)
                    ) {
                        Text("خروجی PDF", color = Color.White, fontSize = 11.sp)
                    }
                }
            }
        }

        // Active Hazards reviews
        Text(
            text = "بررسی گزارشات انحرافات ثبت شده پرسنل",
            color = Color.White,
            fontSize = 15.sp,
            fontWeight = FontWeight.Bold,
            modifier = Modifier.align(Alignment.End)
        )

        incidentsList.forEach { incident ->
            Card(
                colors = CardDefaults.cardColors(containerColor = CardCarbon),
                modifier = Modifier.fillMaxWidth()
            ) {
                Column(modifier = Modifier.padding(14.dp)) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Text(incident.severity, color = SafetyRed, fontSize = 11.sp, fontWeight = FontWeight.Bold)
                        Text(incident.title, color = Color.White, fontSize = 13.sp, fontWeight = FontWeight.Bold)
                    }
                    Spacer(modifier = Modifier.height(6.dp))
                    Text(
                        text = "گزارش‌دهنده: ${incident.reporter} | موقعیت: ${incident.location}",
                        color = TextGray,
                        fontSize = 11.sp,
                        textAlign = TextAlign.End,
                        modifier = Modifier.fillMaxWidth()
                    )
                    Spacer(modifier = Modifier.height(4.dp))
                    Text(
                        text = "اقدام اصلاحی فعلی: " + incident.correctiveAction,
                        color = SafetyOrange,
                        fontSize = 11.sp,
                        textAlign = TextAlign.End,
                        modifier = Modifier.fillMaxWidth()
                    )

                    Spacer(modifier = Modifier.height(10.dp))
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        Button(
                            onClick = {
                                Toast.makeText(context, "تایید نهایی مکتوب اعمال گردید.", Toast.LENGTH_SHORT).show()
                            },
                            colors = ButtonDefaults.buttonColors(containerColor = SafetyGreen),
                            modifier = Modifier.weight(1f),
                            shape = RoundedCornerShape(4.dp)
                        ) {
                            Text("تایید نهایی", color = Color.White, fontSize = 10.sp)
                        }

                        Button(
                            onClick = {
                                Toast.makeText(context, "پرونده جهت بازبینی به افسر ایمنی عودت شد.", Toast.LENGTH_SHORT).show()
                            },
                            colors = ButtonDefaults.buttonColors(containerColor = SafetyYellow),
                            modifier = Modifier.weight(1f),
                            shape = RoundedCornerShape(4.dp)
                        ) {
                            Text("نیاز به بررسی بیشتر", color = Color.Black, fontSize = 10.sp)
                        }
                    }
                }
            }
        }

        // Live Audit logs (Real-time tracking of what users do in the app)
        Text(
            text = "ردپای حسابرسی زنده (Live Audit Logs)",
            color = Color.White,
            fontSize = 15.sp,
            fontWeight = FontWeight.Bold,
            modifier = Modifier.align(Alignment.End)
        )

        Card(
            colors = CardDefaults.cardColors(containerColor = CardCarbon),
            modifier = Modifier.fillMaxWidth()
        ) {
            Column(modifier = Modifier.padding(14.dp)) {
                auditLogsList.take(6).forEach { log ->
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(vertical = 4.dp),
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Text(log.timestamp, color = TextGray, fontSize = 10.sp)
                        Text(
                            text = "${log.operator}: ${log.action} - ${log.details}",
                            color = TextLight,
                            fontSize = 11.sp,
                            textAlign = TextAlign.End,
                            modifier = Modifier.weight(1f).padding(start = 12.dp)
                        )
                    }
                }
            }
        }
    }
}

// --- SUB SCREEN: SAFETY RULES AND ADHERENCE ---
@Composable
fun SafetyRulesScreen(viewModel: HSEViewModel, onBack: () -> Unit) {
    var hasHelmet by remember { mutableStateOf(false) }
    var hasShoes by remember { mutableStateOf(false) }
    var hasMask by remember { mutableStateOf(false) }
    var hasEarmuff by remember { mutableStateOf(false) }
    var hasHarness by remember { mutableStateOf(false) }
    
    val allChecked = hasHelmet && hasShoes && hasMask && hasEarmuff && hasHarness

    Column(
        modifier = Modifier
            .fillMaxSize()
            .verticalScroll(rememberScrollState())
            .padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        // Header Row with Back Button
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            IconButton(onClick = onBack) {
                Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "بازگشت", tint = Color.White)
            }
            Text(
                text = "دستورالعمل‌ها و رعایت موارد ایمنی",
                color = Color.White,
                fontSize = 18.sp,
                fontWeight = FontWeight.Bold
            )
        }

        // Hero Banner
        Card(
            colors = CardDefaults.cardColors(containerColor = SafetyGreen.copy(alpha = 0.15f)),
            border = BorderStroke(1.dp, SafetyGreen),
            shape = RoundedCornerShape(12.dp),
            modifier = Modifier.fillMaxWidth()
        ) {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(16.dp),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.End
            ) {
                Column(
                    horizontalAlignment = Alignment.End,
                    modifier = Modifier.weight(1f)
                ) {
                    Text(
                        text = "منشور ایمنی طلایی کارخانه سیمان سفید نیریز",
                        color = Color.White,
                        fontSize = 14.sp,
                        fontWeight = FontWeight.Bold,
                        textAlign = TextAlign.End
                    )
                    Spacer(modifier = Modifier.height(4.dp))
                    Text(
                        text = "رعایت دقیق قوانین ایمنی ضامن سلامتی شما و همکارانتان است.",
                        color = TextGray,
                        fontSize = 11.sp,
                        textAlign = TextAlign.End
                    )
                }
                Spacer(modifier = Modifier.width(12.dp))
                Icon(
                    imageVector = Icons.Filled.HealthAndSafety,
                    contentDescription = null,
                    tint = SafetyGreen,
                    modifier = Modifier.size(44.dp)
                )
            }
        }

        // 3 Golden Rules Section
        Text(
            text = "سه قانون طلایی و حیاتی ایمنی",
            color = SafetyOrange,
            fontSize = 15.sp,
            fontWeight = FontWeight.Bold,
            modifier = Modifier.align(Alignment.End)
        )

        val goldenRules = listOf(
            Triple(
                "کار در ارتفاع بالای ۲ متر",
                "استفاده از سیستم کامل هارنس ضد سقوط با تاییدیه بازرسی روی برج پیش‌گرمکن (Preheater) و بالای سیلوها اجباری است. خط نجات (Life Line) باید همیشه مهار شده باشد.",
                Icons.Filled.Height
            ),
            Triple(
                "ورود به کوره دوار و فضاهای بسته",
                "هرگونه ورود به فضای بسته کوره دوار نیازمند اخذ مجوز کتبی ورود (PTW)، گازسنجی اکسیژن و مونوکسیدکربن و تایید سرپرست نوبت‌کاری ایمنی است.",
                Icons.Filled.ExitToApp
            ),
            Triple(
                "دستورالعمل قطع و قفل (Lockout Tagout)",
                "قبل از شروع کار تعمیراتی روی نوار نقاله‌ها، فن کوره یا تجهیزات آسیاب، کلید اصلی برق باید دیسکانکت گردیده و قفل ایمنی زرد رنگ در جعبه کلید قرار داده شود.",
                Icons.Filled.Lock
            )
        )

        goldenRules.forEach { rule ->
            Card(
                colors = CardDefaults.cardColors(containerColor = CardCarbon),
                shape = RoundedCornerShape(8.dp),
                modifier = Modifier.fillMaxWidth()
            ) {
                Column(
                    modifier = Modifier.padding(12.dp),
                    horizontalAlignment = Alignment.End
                ) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.End,
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Text(
                            text = rule.first,
                            color = Color.White,
                            fontSize = 13.sp,
                            fontWeight = FontWeight.Bold,
                            textAlign = TextAlign.End
                        )
                        Spacer(modifier = Modifier.width(8.dp))
                        Icon(rule.third, contentDescription = null, tint = SafetyOrange, modifier = Modifier.size(20.dp))
                    }
                    Spacer(modifier = Modifier.height(6.dp))
                    Text(
                        text = rule.second,
                        color = TextGray,
                        fontSize = 11.sp,
                        lineHeight = 18.sp,
                        textAlign = TextAlign.End
                    )
                }
            }
        }

        // Checklist Section for Personal Check
        Text(
            text = "خودارزیابی آمادگی ایمنی قبل از شروع کار",
            color = SafetyYellow,
            fontSize = 15.sp,
            fontWeight = FontWeight.Bold,
            modifier = Modifier.align(Alignment.End)
        )

        Card(
            colors = CardDefaults.cardColors(containerColor = CardCarbon),
            shape = RoundedCornerShape(12.dp),
            modifier = Modifier.fillMaxWidth()
        ) {
            Column(
                modifier = Modifier.padding(16.dp),
                horizontalAlignment = Alignment.End
            ) {
                Text(
                    text = "لطفاً چک لیست تجهیزات حفاظت فردی (PPE) خود را بررسی کنید:",
                    color = Color.White,
                    fontSize = 12.sp,
                    fontWeight = FontWeight.Bold,
                    modifier = Modifier.padding(bottom = 12.dp)
                )

                // Helmet Check
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(vertical = 4.dp),
                    horizontalArrangement = Arrangement.End,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text("کلاه ایمنی استاندارد کلاس E دارم", color = Color.White, fontSize = 12.sp)
                    Spacer(modifier = Modifier.width(8.dp))
                    Checkbox(
                        checked = hasHelmet,
                        onCheckedChange = { hasHelmet = it },
                        colors = CheckboxDefaults.colors(checkedColor = SafetyGreen)
                    )
                }

                // Shoes Check
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(vertical = 4.dp),
                    horizontalArrangement = Arrangement.End,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text("کفش ایمنی با سرپنجه فولادی پوشیده‌ام", color = Color.White, fontSize = 12.sp)
                    Spacer(modifier = Modifier.width(8.dp))
                    Checkbox(
                        checked = hasShoes,
                        onCheckedChange = { hasShoes = it },
                        colors = CheckboxDefaults.colors(checkedColor = SafetyGreen)
                    )
                }

                // Mask Check
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(vertical = 4.dp),
                    horizontalArrangement = Arrangement.End,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text("ماسک ضد غبار مناسب (در محیط آسیاب غبارخیز) همراه دارم", color = Color.White, fontSize = 12.sp)
                    Spacer(modifier = Modifier.width(8.dp))
                    Checkbox(
                        checked = hasMask,
                        onCheckedChange = { hasMask = it },
                        colors = CheckboxDefaults.colors(checkedColor = SafetyGreen)
                    )
                }

                // Earmuff Check
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(vertical = 4.dp),
                    horizontalArrangement = Arrangement.End,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text("گوشی حفاظتی ایرموف (جهت صدا خفه کن) دارم", color = Color.White, fontSize = 12.sp)
                    Spacer(modifier = Modifier.width(8.dp))
                    Checkbox(
                        checked = hasEarmuff,
                        onCheckedChange = { hasEarmuff = it },
                        colors = CheckboxDefaults.colors(checkedColor = SafetyGreen)
                    )
                }

                // Harness Check
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(vertical = 4.dp),
                    horizontalArrangement = Arrangement.End,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text("تجهیزات هارنس ایمنی را بررسی چشمی کرده‌ام", color = Color.White, fontSize = 12.sp)
                    Spacer(modifier = Modifier.width(8.dp))
                    Checkbox(
                        checked = hasHarness,
                        onCheckedChange = { hasHarness = it },
                        colors = CheckboxDefaults.colors(checkedColor = SafetyGreen)
                    )
                }

                AnimatedVisibility(visible = allChecked) {
                    Card(
                        colors = CardDefaults.cardColors(containerColor = Color(0xFF1B5E20)),
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(top = 12.dp)
                    ) {
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(12.dp),
                            horizontalArrangement = Arrangement.End,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Text(
                                text = "شما کاملاً آماده ورود ایمن به محیط کارگاه سیمان سفید هستید. خدا قوت!",
                                color = Color.White,
                                fontSize = 11.sp,
                                fontWeight = FontWeight.Bold,
                                textAlign = TextAlign.End,
                                modifier = Modifier.weight(1f)
                            )
                            Spacer(modifier = Modifier.width(8.dp))
                            Icon(Icons.Filled.CheckCircle, contentDescription = null, tint = Color.White)
                        }
                    }
                }
            }
        }

        // Producer Card (CRITICAL REQUIREMENT)
        Card(
            colors = CardDefaults.cardColors(containerColor = ActiveCarbon),
            border = BorderStroke(1.dp, SafetyOrange.copy(alpha = 0.4f)),
            shape = RoundedCornerShape(12.dp),
            modifier = Modifier.fillMaxWidth()
        ) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(16.dp),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                Icon(
                    imageVector = Icons.Filled.Verified,
                    contentDescription = null,
                    tint = SafetyOrange,
                    modifier = Modifier.size(32.dp)
                )
                Spacer(modifier = Modifier.height(8.dp))
                Text(
                    text = "تهیه کننده: مهندس حامد کریمی زاده",
                    color = Color.White,
                    fontSize = 14.sp,
                    fontWeight = FontWeight.Bold,
                    textAlign = TextAlign.Center
                )
                Spacer(modifier = Modifier.height(4.dp))
                Text(
                    text = "سامانه مدیریت جامع ایمنی و بهداشت (HSE)",
                    color = TextGray,
                    fontSize = 11.sp,
                    textAlign = TextAlign.Center
                )
                Text(
                    text = "کارخانه سیمان سفید نیریز - تابستان ۱۴۰۵",
                    color = TextGray,
                    fontSize = 10.sp,
                    textAlign = TextAlign.Center
                )
            }
        }
    }
}
