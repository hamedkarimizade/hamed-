package com.example.data

import android.content.Context
import com.example.data.*
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.first
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

class HSERepository(private val database: HSEDatabase) {
    private val dao = database.hseDao()

    val allIncidents: Flow<List<IncidentEntity>> = dao.getAllIncidents()
    val allInspections: Flow<List<InspectionEntity>> = dao.getAllInspections()
    val allPPEItems: Flow<List<PPEItemEntity>> = dao.getAllPPEItems()
    val allTrainingItems: Flow<List<TrainingItemEntity>> = dao.getAllTrainingItems()
    val allAuditLogs: Flow<List<AuditLogEntity>> = dao.getAllAuditLogs()
    val allEmergencyContacts: Flow<List<EmergencyContactEntity>> = dao.getAllEmergencyContacts()

    suspend fun getUnsyncedCount(): Int {
        return dao.getUnsyncedIncidentsCount() + dao.getUnsyncedInspectionsCount()
    }

    private fun getCurrentDateString(): String {
        val sdf = SimpleDateFormat("yyyy-MM-dd HH:mm", Locale.getDefault())
        return sdf.format(Date())
    }

    // Prepopulate system with realistic Neyriz cement plant data
    suspend fun ensurePrepopulated() {
        // 1. Prepopulate Emergency Contacts
        val currentContacts = dao.getAllEmergencyContacts().first()
        if (currentContacts.isEmpty()) {
            val contacts = listOf(
                EmergencyContactEntity(name = "مهندس همایونی (رئیس ایمنی و بهداشت)", roleFa = "رئیس واحد ایمنی و بهداشت کتبی", phone = "۰۹۱۲۳۴۵۶۷۸۹", isSOSContact = true),
                EmergencyContactEntity(name = "مهندس قربانی (مدیر تولید)", roleFa = "نظارت مستقیم کوره و آسیاب", phone = "۰۹۱۲۹۸۷۶۵۴۳", isSOSContact = true),
                EmergencyContactEntity(name = "مرکز فوریت‌های پزشکی کارخانه", roleFa = "بهداری داخل کارخانه", phone = "۰۷۱۳۵۲۶۲۳۰۱", isSOSContact = true),
                EmergencyContactEntity(name = "ایستگاه آتش‌نشانی شماره ۱ نیریز", roleFa = "پشتیبان حریق شهری", phone = "۱۲۵", isSOSContact = false),
                EmergencyContactEntity(name = "دفتر مدیریت کارخانه (مهندس سپهرارا)", roleFa = "هماهنگی کل کارخانه", phone = "۰۷۱۳۵۲۶۲۳۰۰", isSOSContact = false)
            )
            dao.insertEmergencyContacts(contacts)
        }

        // 2. Prepopulate PPE Items and Stocks
        val currentPPE = dao.getAllPPEItems().first()
        if (currentPPE.isEmpty()) {
            val ppeList = listOf(
                PPEItemEntity(
                    "Helmet", "کلاه ایمنی کلاس E", 120, 115, 4, 4,
                    "۱۴۰۵-۰۴-۱۰", "۱۴۰۵-۱۰-۱۰", "استفاده دائم در تمام خطوط تولید سیمان بخصوص بخش سنگ‌شکن و کوره."
                ),
                PPEItemEntity(
                    "Safety Glasses", "عینک ایمنی محافظ", 200, 190, 8, 8,
                    "۱۴۰۵-۰۴-۱۲", "۱۴۰۵-۰۸-۱۲", "الزامی جهت واحد سنگ‌شکن، کارگاه‌های تراشکاری و عملیات تخلیه بار."
                ),
                PPEItemEntity(
                    "Respirators", "ماسک تنفسی ضد غبار N95", 500, 480, 0, 45,
                    "۱۴۰۵-۰۴-۱۵", "۱۴۰۵-۰۵-۱۵", "بسیار مهم: استفاده اجباری در سالن آسیاب سیمان، پری‌هیتر و گالری‌های نقاله جهت جلوگیری از سیلیکوزیس."
                ),
                PPEItemEntity(
                    "Gloves", "دستکش چرمی جوشکاری/ضد برش", 150, 145, 12, 12,
                    "۱۴۰۵-۰۴-۰۸", "۱۴۰۵-۰۷-۰۸", "جهت کارگران کوره، جوشکاران و تخلیه کیسه‌های سیمان سنگین."
                ),
                PPEItemEntity(
                    "Safety Shoes", "کفش ایمنی با پنجه فولادی", 180, 175, 5, 5,
                    "۱۴۰۵-۰۳-۲۰", "۱۴۰۵-۱۲-۲۰", "الزامی در تمام محوطه کارخانه جهت پیشگیری از سقوط اجسام سنگین و سوختگی پا."
                ),
                PPEItemEntity(
                    "Harness", "هارنس ایمنی کار در ارتفاع", 40, 38, 2, 2,
                    "۱۴۰۵-۰۴-۰۱", "۱۴۰۵-۱۰-۰۱", "الزامی هنگام کار روی برج پیش‌گرمکن (پری‌هیتر)، بالای سیلوهای کلینکر و سیمان."
                ),
                PPEItemEntity(
                    "Face Shield", "شیلد محافظ صورت", 30, 29, 1, 1,
                    "۱۴۰۵-۰۴-۰۵", "۱۴۰۵-۱۱-۰۵", "مورد استفاده در آزمایشگاه شیمی سیمان و زمان برش‌کاری فلزات."
                ),
                PPEItemEntity(
                    "Hearing Protection", "گوشی حفاظتی (ایرموف)", 90, 85, 3, 3,
                    "۱۴۰۵-۰۴-۰۷", "۱۴۰۵-۱۰-۰۷", "الزامی در سالن آسیاب سیمان و آسیاب مواد خام به دلیل نویز بالاتر از ۸۵ دسی‌بل."
                )
            )
            dao.insertPPEItems(ppeList)
        }

        // 3. Prepopulate Training Items
        val currentTraining = dao.getAllTrainingItems().first()
        if (currentTraining.isEmpty()) {
            val trainingList = listOf(
                TrainingItemEntity(
                    "T1", "Silicosis Prevention", "پیشگیری از بیماری سیلیکوزیس در کارخانجات سیمان", "Video", "کار در ارتفاع",
                    "این ویدئو آموزشی به بررسی خطرات ذرات ریز سیلیس آزاد معلق در سالن‌های خردایش و آسیاب سیمان سفید نیریز می‌پردازد. روش‌های کنترل مهندسی غبار (مانند فیلترهای کیسه‌ای و کارکرد درست جت فیلترها) و استفاده از ماسک‌های تنفسی مناسب FFP3 به زبان ساده تشریح می‌شود.",
                    "۱۵ دقیقه", 0, false, true, -1, false
                ),
                TrainingItemEntity(
                    "T2", "Toolbox Talk: Working at Preheater Tower", "توجیهی ایمنی: کار روی برج پیش‌گرمکن (پری‌هیتر)", "Toolbox Talk", "کوره دوار",
                    "دستورالعمل بستن صحیح داربست‌ها، مهار کامل طناب نجات (Life Line)، بررسی سلامت فیزیکی هارنس‌ها، و لزوم هماهنگی کامل با اتاق کنترل قبل از ورود به سکوهای ارتفاع در کارخانه سیمان سفید نیریز.",
                    "۱۰ صفحه", 0, false, false, -1, false
                ),
                TrainingItemEntity(
                    "T3", "Confined Space Entry in Rotary Kiln", "دستورالعمل ورود به فضای بسته داخل کوره دوار", "Lesson Learned", "فضای بسته",
                    "گزارش حادثه واقعی ناشی از عدم سنجش گازهای خفه‌کننده و مونواکسید کربن در تعمیرات کوره دوار و لزوم استفاده دائم از دمنده‌های هوا، فن‌های تهویه مناسب و گازسنج‌های کالیبره قبل از ورود اکیپ تعمیراتی.",
                    "۱۲ صفحه", 0, false, true, -1, false
                ),
                TrainingItemEntity(
                    "T4", "Chemical Spill Control & Safety Sheets (MSDS)", "کنترل نشت مواد شیمیایی و استفاده از برگه‌های MSDS", "Video", "کمک‌های اولیه",
                    "بررسی خطرات مواد شیمیایی کمک‌سایش (Grinding Aids) در بخش سایش سیمان، نحوه مسدودسازی چاه‌های آب مجاور در صورت نشت، استفاده از پودرهای جاذب و کیت کنترل نشت مواد شیمیایی موجود در سالن آسیاب.",
                    "۲۰ دقیقه", 0, false, true, -1, false
                )
            )
            dao.insertTrainingItems(trainingList)
        }

        // 4. Populate default incidents if empty to look complete
        val currentIncidents = dao.getAllIncidents().first()
        if (currentIncidents.isEmpty()) {
            val defaultIncidents = listOf(
                IncidentEntity(
                    type = "Unsafe Condition",
                    reporter = "مهندس قربانی",
                    title = "نقص فیلتر کیسه‌ای آسیاب مواد خام",
                    location = "دپارتمان آسیاب مواد خام",
                    description = "انتشار غبار بیش از حد در سالن غبارگیر به دلیل پارگی کیسه‌های فیلتر شماره ۳. این وضعیت پتانسیل بروز مشکلات ریوی شدید برای پرسنل سالن را دارد.",
                    date = getCurrentDateString(),
                    severity = "High",
                    isSynced = true,
                    correctiveAction = "تعویض کیسه‌های پاره فیلتر کیسه‌ای توسط اکیپ تعمیرات مکانیک و ثبت در سیستم مانیتورینگ آنلاین آلایندگی."
                ),
                IncidentEntity(
                    type = "Near Miss",
                    reporter = "مهندس همایونی",
                    title = "سقوط آجر نسوز کوره در فاز خنک‌سازی",
                    location = "داخل کوره دوار",
                    description = "هنگام بازدید بازرس ایمنی از بدنه داخلی کوره، یک قطعه آجر نسوز لق شده به فاصله ۱ متری او سقوط کرد. خوشبختانه آسیب جانی نداشته است اما کار بدون تایید نهایی ایمنی متوقف گردید.",
                    date = getCurrentDateString(),
                    severity = "Critical",
                    isSynced = true,
                    correctiveAction = "اصلاح روش لق‌گیری آجرهای نسوز دیواره کوره دوار و اعمال دستورالعمل جدید استفاده از ابزارهای هیدرولیک از راه دور."
                )
            )
            for (inc in defaultIncidents) {
                dao.insertIncident(inc)
            }
        }
    }

    // Login checks
    suspend fun checkUserCredentials(username: String, password: String, selectedRole: String = ""): UserEntity? {
        val normalizedUser = username.lowercase().trim()
        val normalizedPass = password.trim()

        // Local checks
        var resolvedUser: UserEntity? = null
        
        // Single master user specifically for you: Hamed Karimizadeh
        if ((normalizedUser == "hamed" || normalizedUser == "admin") && (normalizedPass == "12345" || normalizedPass == "1384")) {
            resolvedUser = UserEntity("hamed", "مهندس حامد کریمی زاده", "Executive", "مدیریت ارشد HSE و کارخانه")
        } else if (normalizedUser == "operator" || normalizedUser == "user") {
            if (normalizedPass == "neyriz" || normalizedPass == "123" || normalizedPass == "12345") {
                resolvedUser = UserEntity("operator_user", "علی رستمی", "Operator", "واحد تولید و کوره")
            }
        }

        if (resolvedUser != null) {
            dao.insertUser(resolvedUser)
            addAuditLog("ورود به سامانه", resolvedUser.fullName, "ورود موفقیت‌آمیز به عنوان ${resolvedUser.role}")
        }
        return resolvedUser
    }

    suspend fun getActiveUser(username: String): UserEntity? {
        return dao.getUser(username)
    }

    // Direct CRUD operations for admin (Hamed Karimizadeh) to edit everything
    suspend fun addOrUpdatePPEItem(item: PPEItemEntity) {
        dao.insertPPEItem(item)
        addAuditLog("افزودن/ویرایش تجهیزات", item.nameFa, "ذخیره اطلاعات تجهیزات حفاظتی با موفقیت انجام شد.")
    }

    suspend fun deletePPEItem(ppeType: String) {
        dao.deletePPEItem(ppeType)
        addAuditLog("حذف تجهیزات", ppeType, "تجهیز حفاظتی حذف گردید.")
    }

    suspend fun addOrUpdateTrainingItem(item: TrainingItemEntity) {
        dao.insertTrainingItem(item)
        addAuditLog("افزودن/ویرایش دوره آموزشی", item.titleFa, "ذخیره دوره آموزشی با موفقیت انجام شد.")
    }

    suspend fun deleteTrainingItem(id: String) {
        dao.deleteTrainingItem(id)
        addAuditLog("حذف دوره آموزشی", id, "دوره آموزشی حذف گردید.")
    }

    suspend fun addOrUpdateEmergencyContact(contact: EmergencyContactEntity) {
        dao.insertEmergencyContact(contact)
        addAuditLog("افزودن/ویرایش مخاطب اضطراری", contact.name, "ذخیره اطلاعات تماس با موفقیت انجام شد.")
    }

    suspend fun deleteEmergencyContact(id: Int) {
        dao.deleteEmergencyContact(id)
        addAuditLog("حذف مخاطب اضطراری", "شناسه: $id", "مخاطب اضطراری حذف گردید.")
    }

    // Report Incidents
    suspend fun reportIncident(
        type: String,
        title: String,
        location: String,
        description: String,
        severity: String,
        reporter: String
    ) {
        val incident = IncidentEntity(
            reporter = reporter,
            type = type,
            title = title,
            location = location,
            description = description,
            date = getCurrentDateString(),
            severity = severity,
            isSynced = false
        )
        dao.insertIncident(incident)
        addAuditLog("ثبت گزارش ایمنی", reporter, "ثبت گزارش با عنوان: $title در محل: $location")
    }

    // Submit Equipment Inspections
    suspend fun submitInspection(
        inspectorName: String,
        equipmentName: String,
        status: String,
        checklistResult: String
    ) {
        val inspection = InspectionEntity(
            inspectorName = inspectorName,
            equipmentName = equipmentName,
            status = status,
            inspectionDate = getCurrentDateString(),
            checklistResult = checklistResult,
            isSynced = false
        )
        dao.insertInspection(inspection)
        addAuditLog("ثبت بازرسی تجهیزات", inspectorName, "بازرسی تجهزات: $equipmentName با وضعیت: $status")
    }

    // Update PPE Inspection Status
    suspend fun updatePPEInspection(ppeType: String, inspected: Int, defective: Int, replacement: Int) {
        dao.updatePPEInspection(ppeType, inspected, defective, replacement)
        addAuditLog("بروزرسانی بازرسی PPE", "سیستم", "اصلاح وضعیت بازرسی تجهیزات پپ برای: $ppeType")
    }

    // Update Training Progress
    suspend fun updateTrainingProgress(id: String, progress: Int, completed: Boolean) {
        dao.updateTrainingProgress(id, progress, completed)
    }

    // Save Quiz Result
    suspend fun saveQuizResult(id: String, score: Int, passed: Boolean) {
        dao.saveQuizResult(id, score, passed)
        addAuditLog("ثبت نمره کوییز آموزشی", "سیستم", "ثبت قبولی در آموزش شناسه: $id با نمره: $score")
    }

    // Add Audit Log
    suspend fun addAuditLog(action: String, operator: String, details: String) {
        val log = AuditLogEntity(
            action = action,
            operator = operator,
            timestamp = getCurrentDateString(),
            details = details
        )
        dao.insertAuditLog(log)
    }

    // Synchronization simulation
    suspend fun simulateSync(): Boolean {
        val unsyncedIncidents = dao.getUnsyncedIncidents()
        val unsyncedInspections = dao.getUnsyncedInspections()

        if (unsyncedIncidents.isEmpty() && unsyncedInspections.isEmpty()) {
            return false
        }

        // Simulate network latency (will be called inside viewModel in a coroutine)
        for (inc in unsyncedIncidents) {
            dao.markIncidentSynced(inc.id)
        }
        for (insp in unsyncedInspections) {
            dao.markInspectionSynced(insp.id)
        }

        addAuditLog("همگام‌سازی ابری", "سیستم خودکار", "همگام‌سازی موفق ${unsyncedIncidents.size} گزارش و ${unsyncedInspections.size} بازرسی با سرور ابری کارخانه.")
        return true
    }
}
