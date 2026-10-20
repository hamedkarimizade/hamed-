package com.example.ui.viewmodel

import android.app.Application
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.example.BuildConfig
import com.example.data.*
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch
import kotlinx.coroutines.delay
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

class HSEViewModel(application: Application) : AndroidViewModel(application) {
    private val database = HSEDatabase.getDatabase(application)
    private val repository = HSERepository(database)

    fun logEvent(action: String, operator: String, details: String) {
        viewModelScope.launch {
            repository.addAuditLog(action, operator, details)
        }
    }

    // Flows from database
    val incidents: StateFlow<List<IncidentEntity>> = repository.allIncidents
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    val inspections: StateFlow<List<InspectionEntity>> = repository.allInspections
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    val ppeItems: StateFlow<List<PPEItemEntity>> = repository.allPPEItems
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    val trainingItems: StateFlow<List<TrainingItemEntity>> = repository.allTrainingItems
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    val auditLogs: StateFlow<List<AuditLogEntity>> = repository.allAuditLogs
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    val emergencyContacts: StateFlow<List<EmergencyContactEntity>> = repository.allEmergencyContacts
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    // UI States
    var loggedInUser by mutableStateOf<UserEntity?>(null)
        private set

    var loginError by mutableStateOf<String?>(null)
    var isLoggingIn by mutableStateOf(false)

    var syncStatus by mutableStateOf("همگام شده") // همگام شده, در حال همگام‌سازی, همگام‌سازی ناموفق (آفلاین), نیاز به همگام‌سازی
        private set

    var unsyncedCount by mutableStateOf(0)
        private set

    // Assistant Chat States
    var chatMessages by mutableStateOf(listOf(
        ChatMessage("HSE_SYSTEM", "سلام! من دستیار هوشمند ایمنی و بهداشت (HSE) کارخانه سیمان سفید نیریز هستم. چطور می‌توانم به شما در مورد دستورالعمل‌های کارخانه، برگه‌های اطلاعات ایمنی مواد (MSDS) یا شرایط اضطراری کمک کنم؟")
    ))
    var isChatLoading by mutableStateOf(false)

    // Quiz States
    var activeQuizId by mutableStateOf<String?>(null)
    var activeQuizQuestions = listOf<QuizQuestion>()
    var currentQuestionIndex by mutableStateOf(0)
    var selectedAnswerIndex by mutableStateOf(-1)
    var quizScore by mutableStateOf(0)
    var isQuizCompleted by mutableStateOf(false)
    var earnedCertificateName by mutableStateOf<String?>(null)

    // Init & Prepopulate
    init {
        viewModelScope.launch {
            repository.ensurePrepopulated()
            updateUnsyncedCount()
        }
    }

    fun updateUnsyncedCount() {
        viewModelScope.launch {
            unsyncedCount = repository.getUnsyncedCount()
            syncStatus = if (unsyncedCount > 0) "نیاز به همگام‌سازی" else "همگام شده"
        }
    }

    // Auth actions
    fun login(username: String, password: String, selectedRole: String = "", onFinished: (Boolean) -> Unit) {
        viewModelScope.launch {
            isLoggingIn = true
            loginError = null
            delay(800) // Realistic delay
            val user = repository.checkUserCredentials(username, password, selectedRole)
            isLoggingIn = false
            if (user != null) {
                loggedInUser = user
                onFinished(true)
            } else {
                loginError = "نام کاربری یا رمز عبور اشتباه است."
                onFinished(false)
            }
        }
    }

    fun logout() {
        viewModelScope.launch {
            loggedInUser?.let {
                repository.addAuditLog("خروج از سامانه", it.fullName, "کاربر از حساب کاربری خود خارج شد")
            }
            loggedInUser = null
        }
    }

    // Offline Sync
    fun triggerSync() {
        viewModelScope.launch {
            if (unsyncedCount == 0) return@launch
            syncStatus = "در حال همگام‌سازی"
            delay(1500) // Network delay simulation
            val result = repository.simulateSync()
            if (result) {
                updateUnsyncedCount()
                syncStatus = "همگام شده"
            } else {
                syncStatus = "همگام‌سازی ناموفق"
            }
        }
    }

    // Incident Reporting
    fun submitIncident(type: String, title: String, location: String, description: String, severity: String) {
        viewModelScope.launch {
            val reporterName = loggedInUser?.fullName ?: "مهمان"
            repository.reportIncident(type, title, location, description, severity, reporterName)
            updateUnsyncedCount()
        }
    }

    // Inspection Submission
    fun submitInspection(equipment: String, status: String, checklistText: String) {
        viewModelScope.launch {
            val inspectorName = loggedInUser?.fullName ?: "مهمان"
            repository.submitInspection(inspectorName, equipment, status, checklistText)
            updateUnsyncedCount()
        }
    }

    // Quick Update PPE Item
    fun inspectPPEItem(ppeType: String, inspected: Int, defective: Int, replacement: Int) {
        viewModelScope.launch {
            repository.updatePPEInspection(ppeType, inspected, defective, replacement)
        }
    }

    // --- Quiz Logic ---
    fun startQuiz(trainingId: String) {
        activeQuizId = trainingId
        currentQuestionIndex = 0
        selectedAnswerIndex = -1
        quizScore = 0
        isQuizCompleted = false
        earnedCertificateName = null

        activeQuizQuestions = when (trainingId) {
            "T1" -> listOf(
                QuizQuestion(
                    "کدام یک از خطرات زیر، تهدید بلندمدت اصلی در سالن‌های خردایش و آسیاب سیمان سفید نیریز است؟",
                    listOf("نویز و صدای بیش از حد کوره", "بیماری پوستی درماتیت حاد", "بیماری ریوی سیلیکوزیس ناشی از غبار سیلیس آزاد", "سوختگی شیمیایی شدید"),
                    2
                ),
                QuizQuestion(
                    "چه نوع ماسکی برای مقابله با غبار آزاد سیلیس معلق در هوا در سالن غبارگیر الزامی است؟",
                    listOf("ماسک کاغذی معمولی فاقد فیلتر", "ماسک تنفسی استاندار فیلتردار با رتبه حفاظتی N95 یا FFP3", "ماسک پارچه‌ای دست‌ساز", "بدون ماسک، صرفاً با بستن یک تکه پارچه"),
                    1
                )
            )
            "T3" -> listOf(
                QuizQuestion(
                    "برای ورود ایمن به فضای بسته کوره دوار (Rotary Kiln) جهت تعمیر آجر نسوز، اولین اقدام کدام است؟",
                    listOf("ورود سریع برای صرفه‌جویی در زمان شیفت", "سنجش اتمسفر کوره با گازسنج کالیبره جهت نشت مونوکسید کربن و گازهای خفه‌کننده و راه‌اندازی دمنده هوا", "تنها روشن کردن چراغ قوه", "آب‌پاشی بدنه داخلی کوره بدون تهویه هوا"),
                    1
                ),
                QuizQuestion(
                    "در ورود به فضاهای بسته کوره سیمان، نقش فرد کشیک (Standby Person) چیست؟",
                    listOf("همراهی کارگر به داخل کوره برای کمک فنی", "ایستادن بیرون از کوره، نظارت بر سلامت افراد داخل، حفظ ارتباط کلامی مداوم و هماهنگی اضطراری", "رفتن به بهداری برای آوردن تجهیزات", "ترک محل کوره جهت تحویل ناهار"),
                    1
                )
            )
            else -> listOf(
                QuizQuestion(
                    "کدام کپسول آتش‌نشانی برای اطفاء حریق برقی سالن کنترل آسیاب مناسب‌تر است؟",
                    listOf("کپسول کف شیمیایی", "کپسول آب و گاز", "کپسول دی‌اکسید کربن (CO2)", "سطل پر از آب"),
                    2
                )
            )
        }
    }

    fun submitAnswer() {
        if (selectedAnswerIndex == -1) return
        val currentQuestion = activeQuizQuestions[currentQuestionIndex]
        if (selectedAnswerIndex == currentQuestion.correctIndex) {
            quizScore++
        }

        if (currentQuestionIndex + 1 < activeQuizQuestions.size) {
            currentQuestionIndex++
            selectedAnswerIndex = -1
        } else {
            // Completed
            isQuizCompleted = true
            val passed = quizScore >= (activeQuizQuestions.size * 0.7) // 70% to pass
            viewModelScope.launch {
                activeQuizId?.let { id ->
                    repository.saveQuizResult(id, (quizScore * 100 / activeQuizQuestions.size), passed)
                    if (passed) {
                        val item = trainingItems.value.find { it.id == id }
                        earnedCertificateName = item?.titleFa ?: "گواهینامه ایمنی"
                    }
                }
            }
        }
    }

    // Direct CRUD methods for Hamed Karimizadeh to edit everything
    fun addOrUpdatePPEItem(item: PPEItemEntity, onFinished: () -> Unit = {}) {
        viewModelScope.launch {
            repository.addOrUpdatePPEItem(item)
            onFinished()
        }
    }

    fun deletePPEItem(ppeType: String, onFinished: () -> Unit = {}) {
        viewModelScope.launch {
            repository.deletePPEItem(ppeType)
            onFinished()
        }
    }

    fun addOrUpdateTrainingItem(item: TrainingItemEntity, onFinished: () -> Unit = {}) {
        viewModelScope.launch {
            repository.addOrUpdateTrainingItem(item)
            onFinished()
        }
    }

    fun deleteTrainingItem(id: String, onFinished: () -> Unit = {}) {
        viewModelScope.launch {
            repository.deleteTrainingItem(id)
            onFinished()
        }
    }

    fun addOrUpdateEmergencyContact(contact: EmergencyContactEntity, onFinished: () -> Unit = {}) {
        viewModelScope.launch {
            repository.addOrUpdateEmergencyContact(contact)
            onFinished()
        }
    }

    fun deleteEmergencyContact(id: Int, onFinished: () -> Unit = {}) {
        viewModelScope.launch {
            repository.deleteEmergencyContact(id)
            onFinished()
        }
    }

    // --- Gemini AI Smart HSE Assistant ---
    fun askAssistant(question: String) {
        if (question.isBlank()) return
        
        // Add user message to state
        val updatedMessages = chatMessages.toMutableList()
        updatedMessages.add(ChatMessage("USER", question))
        chatMessages = updatedMessages
        isChatLoading = true

        viewModelScope.launch {
            try {
                val apiKey = BuildConfig.GEMINI_API_KEY
                
                // Construct System Instruction context customized for White Cement Neyriz Plant
                val systemInstruction = """
                    You are the AI Smart HSE Assistant for the White Cement Neyriz Plant (کارخانه سیمان سفید نیریز) in Fars, Iran. 
                    Your job is to answer safety-related questions from workers, officers, and executives using company procedures and MSDS sheets.
                    All responses must be professional, supportive of industrial safety culture, and in Farsi (Persian) with clear guidelines.
                    
                    Here are the official company guidelines and material data for reference:
                    
                    1. WHITE CEMENT (سیمان سفید) MSDS:
                       - Eye Hazard: Causes irritation and alkaline burns. Rinse immediately with plenty of water for 15 minutes and seek medical attention.
                       - Skin Hazard: Prolonged exposure causes Dermatitis. Must wear thick safety gloves.
                       - Respiratory Hazard: Grinding dust can lead to Silicosis. Respirators (N95 or FFP3) are MANDATORY in packing plant, raw mill, and cement mill.
                    
                    2. WORKING AT HEIGHT (کار در ارتفاع):
                       - Required for heights exceeding 2 meters.
                       - Mandatory: Double lanyard safety harness (هارنس ایمنی), helmet with chin strap, life-line, and scaffolding checks.
                       - Anchor point must be a secure structure, never a generic pipe.
                    
                    3. ROTARY KILN (کوره دوار) & CONFINED SPACE (فضای بسته) ENTRY:
                       - Must obtain Confined Space Permit (مجوز ورود به فضای بسته) signed by HSE.
                       - Air testing for oxygen (19.5%-23.5%), CO, and explosive gases is MANDATORY before entry.
                       - Continuous ventilation via blower fans is required.
                       - A standby person (نفر کشیک) must be present outside at all times.
                    
                    4. FIRE EXTINGUISHER INSPECTION (بازرسی کپسول حریق):
                       - Conducted monthly. Check: pressure gauge in green zone, nozzle and hose clean/flexible, locking pin intact, and weight level.
                    
                    5. CHEMICAL SPILL (نشت مواد شیمیایی - Grinding Aids):
                       - Shut off source immediately if safe. Avoid inhalation.
                       - Keep away from storm drains and water paths.
                       - Use spill kit: absorb with sand or clay. Neutralize if necessary. Dispose in designated toxic waste bags.
                    
                    Ensure your answers are structured with bullet points where appropriate, highlighting safety precautions (هشدار مهم) in a very visible way. 
                    Be precise about Neyriz white cement process specific conditions.
                """.trimIndent()

                val request = GeminiRequest(
                    contents = listOf(
                        GeminiContent(parts = listOf(GeminiPart(question)))
                    ),
                    systemInstruction = GeminiContent(parts = listOf(GeminiPart(systemInstruction)))
                )

                val response = GeminiRetrofitClient.service.generateContent(apiKey, request)
                val responseText = response.candidates?.firstOrNull()?.content?.parts?.firstOrNull()?.text
                    ?: "خطایی در دریافت پاسخ از دستیار هوشمند رخ داد. مجدداً تلاش کنید."

                val finalMessages = chatMessages.toMutableList()
                finalMessages.add(ChatMessage("ASSISTANT", responseText))
                chatMessages = finalMessages
            } catch (e: Exception) {
                val finalMessages = chatMessages.toMutableList()
                finalMessages.add(ChatMessage("ASSISTANT", "متأسفانه به دلیل اختلال ارتباطی موقت، دستیار آفلاین است. لطفا مطمئن شوید که کلید معتبر Gemini API در بخش تنظیمات تنظیم شده است. خطا: ${e.localizedMessage}"))
                chatMessages = finalMessages
            } finally {
                isChatLoading = false
            }
        }
    }
}

data class ChatMessage(
    val sender: String, // USER, ASSISTANT, HSE_SYSTEM
    val content: String,
    val time: String = SimpleDateFormat("HH:mm", Locale.getDefault()).format(Date())
)

data class QuizQuestion(
    val questionText: String,
    val options: List<String>,
    val correctIndex: Int
)
