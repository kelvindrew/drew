package com.example.kasa.data.repository

import android.content.Context
import com.example.kasa.data.model.*
import com.example.kasa.service.AlertCategory
import com.example.kasa.service.KasaNotificationManager
import com.example.kasa.util.CurrencyFormatter
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import java.util.Date
import java.util.UUID

object KasaRepository {
    private var storage: KasaStorage? = null

    // Current active user
    private val _currentUser = MutableStateFlow<UserModel?>(null)
    val currentUser: StateFlow<UserModel?> = _currentUser.asStateFlow()

    // Current household
    private val _currentHousehold = MutableStateFlow<HouseholdModel?>(null)
    val currentHousehold: StateFlow<HouseholdModel?> = _currentHousehold.asStateFlow()

    // Monthly Budget
    private val _monthlyBudget = MutableStateFlow(MonthlyBudgetModel(totalBudget = 0.0))
    val monthlyBudget: StateFlow<MonthlyBudgetModel> = _monthlyBudget.asStateFlow()

    // Categories
    private val _categories = MutableStateFlow<List<CategoryModel>>(
        listOf(
            CategoryModel(id = "c_loyer", name = "Loyer", emoji = "🏠", plannedAmount = 0.0),
            CategoryModel(id = "c_connexion", name = "Connexion", emoji = "🌐", plannedAmount = 0.0),
            CategoryModel(id = "c_provisions", name = "Provisions", emoji = "🛒", plannedAmount = 0.0, isProvision = true)
        )
    )
    val categories: StateFlow<List<CategoryModel>> = _categories.asStateFlow()

    // Expenses (Clean initial state)
    private val _expenses = MutableStateFlow<List<ExpenseModel>>(emptyList())
    val expenses: StateFlow<List<ExpenseModel>> = _expenses.asStateFlow()

    // Provision Items (Clean initial state)
    private val _provisions = MutableStateFlow<List<ProvisionItemModel>>(emptyList())
    val provisions: StateFlow<List<ProvisionItemModel>> = _provisions.asStateFlow()

    // Chat Messages (Clean initial state)
    private val _messages = MutableStateFlow<List<MessageModel>>(emptyList())
    val messages: StateFlow<List<MessageModel>> = _messages.asStateFlow()

    // Polls (Clean initial state)
    private val _polls = MutableStateFlow<List<PollModel>>(emptyList())
    val polls: StateFlow<List<PollModel>> = _polls.asStateFlow()

    // Members
    private val _members = MutableStateFlow<List<UserModel>>(emptyList())
    val members: StateFlow<List<UserModel>> = _members.asStateFlow()

    // Admin Notifications Feed
    private val _adminNotifications = MutableStateFlow<List<AdminNotificationModel>>(emptyList())
    val adminNotifications: StateFlow<List<AdminNotificationModel>> = _adminNotifications.asStateFlow()

    // Tasks & Chores
    private val _tasks = MutableStateFlow<List<TaskModel>>(emptyList())
    val tasks: StateFlow<List<TaskModel>> = _tasks.asStateFlow()

    // Events & Agenda
    private val _events = MutableStateFlow<List<EventModel>>(emptyList())
    val events: StateFlow<List<EventModel>> = _events.asStateFlow()

    // Meal Plan
    private val _currentMealPlan = MutableStateFlow<MealPlanModel>(MealPlanModel())
    val currentMealPlan: StateFlow<MealPlanModel> = _currentMealPlan.asStateFlow()

    // Meal Notes (Bloc-notes des Repas du Foyer)
    private val _mealNotes = MutableStateFlow<List<MealNoteModel>>(emptyList())
    val mealNotes: StateFlow<List<MealNoteModel>> = _mealNotes.asStateFlow()

    // Corkboard (Tableau de Liège)
    private val _postIts = MutableStateFlow<List<PostItModel>>(emptyList())
    val postIts: StateFlow<List<PostItModel>> = _postIts.asStateFlow()

    // House Essentials & Digicodes / Wifi
    private val _houseEssentials = MutableStateFlow<HouseEssentialInfo>(HouseEssentialInfo())
    val houseEssentials: StateFlow<HouseEssentialInfo> = _houseEssentials.asStateFlow()

    // Mode Soirée & Invités
    private val _activeGuestEvent = MutableStateFlow<GuestPartyEvent?>(null)
    val activeGuestEvent: StateFlow<GuestPartyEvent?> = _activeGuestEvent.asStateFlow()

    // Scheduled Reminders
    private val _scheduledReminders = MutableStateFlow<List<ScheduledReminderModel>>(emptyList())
    val scheduledReminders: StateFlow<List<ScheduledReminderModel>> = _scheduledReminders.asStateFlow()

    private val deletedExpenseIds = mutableSetOf<String>()
    private val deletedProvisionIds = mutableSetOf<String>()
    private var appContext: Context? = null

    fun init(context: Context) {
        appContext = context.applicationContext
        val s = KasaStorage(context.applicationContext)
        storage = s
        KasaNotificationManager.init(context)
        deletedExpenseIds.addAll(s.getDeletedExpenseIds())
        deletedProvisionIds.addAll(s.getDeletedProvisionIds())

        if (s.isInitialized()) {
            _currentUser.value = s.loadUser()
            val loadedHh = s.loadHousehold()
            _currentHousehold.value = loadedHh
            val loadedBudget = s.loadBudget()
            _monthlyBudget.value = if ((loadedHh?.monthlyBudget ?: 0.0) > 0.0 && loadedBudget.totalBudget == 0.0) {
                loadedBudget.copy(
                    id = "budget_${loadedHh!!.id}",
                    householdId = loadedHh.id,
                    totalBudget = loadedHh.monthlyBudget
                )
            } else {
                loadedBudget
            }
            _categories.value = ensureDefaultCategories(s.loadCategories())
            _expenses.value = s.loadExpenses().filter { it.id !in deletedExpenseIds }
            _provisions.value = s.loadProvisions().filter { it.id !in deletedProvisionIds }
            _messages.value = s.loadMessages()
            _polls.value = s.loadPolls()
            _tasks.value = s.loadTasks()
            _events.value = s.loadEvents()
            _currentMealPlan.value = s.loadMealPlan() ?: MealPlanModel()
            _mealNotes.value = s.loadMealNotes()
            _postIts.value = s.loadPostIts()
            _houseEssentials.value = s.loadHouseEssentialInfo()
            _activeGuestEvent.value = s.loadActiveGuestEvent()
            _scheduledReminders.value = s.loadScheduledReminders()
            val cachedMembers = s.loadMembers()
            _members.value = if (cachedMembers.isNotEmpty()) cachedMembers else (_currentUser.value?.let { listOf(it) } ?: emptyList())
        }

        // Initialize Firebase Auth first, then start real-time cloud sync
        FirebaseManager.initAuth {
            _currentHousehold.value?.let { hh ->
                startCloudSync(hh.id)
            }
        }
    }

    private fun ensureDefaultCategories(list: List<CategoryModel>): List<CategoryModel> {
        val result = list.toMutableList()
        if (result.none { it.name.equals("Loyer", ignoreCase = true) }) {
            result.add(0, CategoryModel(id = "c_loyer", name = "Loyer", emoji = "🏠", plannedAmount = 0.0))
        }
        if (result.none { it.name.equals("Connexion", ignoreCase = true) || it.name.equals("Internet", ignoreCase = true) }) {
            result.add(1.coerceAtMost(result.size), CategoryModel(id = "c_connexion", name = "Connexion", emoji = "🌐", plannedAmount = 0.0))
        }
        if (result.none { it.name.equals("Provisions", ignoreCase = true) || it.isProvision }) {
            result.add(2.coerceAtMost(result.size), CategoryModel(id = "c_provisions", name = "Provisions", emoji = "🛒", plannedAmount = 0.0, isProvision = true))
        }
        return result.map {
            if (it.name.equals("Provisions", ignoreCase = true)) it.copy(isProvision = true, emoji = "🛒") else it
        }
    }

    private fun startCloudSync(householdId: String) {
        FirebaseManager.startSync(
            householdId = householdId,
            onHouseholdUpdate = { hh ->
                val prevIcon = _currentHousehold.value?.appIcon ?: "default"
                _currentHousehold.value = hh
                if (hh.monthlyBudget > 0.0) {
                    _monthlyBudget.value = _monthlyBudget.value.copy(
                        id = "budget_${hh.id}",
                        householdId = hh.id,
                        totalBudget = hh.monthlyBudget
                    )
                }
                persist()

                // Apply dynamic app icon if updated remotely
                if (hh.appIcon != prevIcon) {
                    appContext?.let { ctx ->
                        com.example.kasa.util.AppIconManager.applyAppIcon(ctx, hh.appIcon)
                        val iconName = com.example.kasa.util.AppIconManager.getIconDisplayName(hh.appIcon)
                        val iconEmoji = com.example.kasa.util.AppIconManager.getIconEmoji(hh.appIcon)
                        com.example.kasa.service.KasaNotificationManager.postAlert(
                            title = "🎨 Icône d'Application Modifiée",
                            message = "L'administrateur a changé l'icône de l'application du foyer pour $iconEmoji $iconName !",
                            category = com.example.kasa.service.AlertCategory.MODIFICATION,
                            emoji = iconEmoji,
                            showSystemNotification = true
                        )
                    }
                }
            },
            onBudgetUpdate = { b ->
                if (b.totalBudget > 0.0 || _monthlyBudget.value.totalBudget == 0.0) {
                    _monthlyBudget.value = b
                    val hh = _currentHousehold.value
                    if (hh != null && hh.monthlyBudget != b.totalBudget) {
                        _currentHousehold.value = hh.copy(monthlyBudget = b.totalBudget)
                    }
                    persist()
                }
            },
            onCategoriesUpdate = { catList ->
                val safe = ensureDefaultCategories(catList)
                _categories.value = safe
                persist()
                safe.filter { it.id in listOf("c_loyer", "c_connexion", "c_provisions") }.forEach {
                    FirebaseManager.saveCategoryToCloud(it)
                }
            },
            onExpensesUpdate = { expList ->
                val activeExpenses = expList.filter { it.id !in deletedExpenseIds }
                expList.filter { it.id in deletedExpenseIds }.forEach {
                    FirebaseManager.deleteExpenseFromCloud(it.id)
                }
                _expenses.value = activeExpenses
                persist()
            },
            onProvisionsUpdate = { provList ->
                val activeProvisions = provList.filter { it.id !in deletedProvisionIds }
                provList.filter { it.id in deletedProvisionIds }.forEach {
                    FirebaseManager.deleteProvisionFromCloud(it.id)
                }
                _provisions.value = activeProvisions
                persist()
            },
            onMessagesUpdate = { msgList ->
                val prevIds = _messages.value.map { it.id }.toSet()
                val currentUserId = _currentUser.value?.id

                if (prevIds.isNotEmpty()) {
                    val incoming = msgList.filter { it.id !in prevIds }
                    incoming.forEach { msg ->
                        if (msg.userId != currentUserId) {
                            val isSystem = msg.type == "system"
                            val emoji = when {
                                msg.text.startsWith("💰") -> "💰"
                                msg.text.startsWith("💸") -> "💸"
                                msg.text.startsWith("✏️") -> "✏️"
                                msg.text.startsWith("🗑️") -> "🗑️"
                                msg.text.startsWith("📋") -> "📋"
                                msg.text.startsWith("🛒") -> "🛒"
                                msg.text.startsWith("🏷️") -> "🏷️"
                                msg.text.startsWith("⚙️") -> "⚙️"
                                isSystem -> "📢"
                                else -> "💬"
                            }
                            KasaNotificationManager.postAlert(
                                title = if (isSystem) "Activité du Foyer" else "💬 ${msg.userName}",
                                message = msg.text,
                                category = if (isSystem) AlertCategory.MODIFICATION else AlertCategory.MESSAGE,
                                emoji = emoji,
                                showSystemNotification = true
                            )
                        }
                    }
                }

                if (msgList.isNotEmpty()) {
                    _messages.value = msgList
                    persist()
                }
            },
            onPollsUpdate = { pollList ->
                if (pollList.isNotEmpty()) {
                    _polls.value = pollList
                    persist()
                }
            },
            onMembersUpdate = { memberList ->
                _members.value = memberList
                val cur = _currentUser.value
                if (cur != null) {
                    val remoteMe = memberList.find { it.id == cur.id }
                    if (remoteMe != null) {
                        _currentUser.value = cur.copy(
                            trackingEnabled = remoteMe.trackingEnabled,
                            pingRequestedAt = remoteMe.pingRequestedAt,
                            role = remoteMe.role
                        )
                    }
                }
                persist()
            },
            onAdminNotificationsUpdate = { notifList ->
                _adminNotifications.value = notifList
            },
            onTasksUpdate = { taskList ->
                if (taskList.isNotEmpty()) {
                    _tasks.value = taskList
                    persist()
                } else if (_tasks.value.isNotEmpty()) {
                    _tasks.value.forEach { FirebaseManager.saveTaskToCloud(it) }
                }
            },
            onEventsUpdate = { eventList ->
                if (eventList.isNotEmpty()) {
                    _events.value = eventList
                    persist()
                } else if (_events.value.isNotEmpty()) {
                    _events.value.forEach { FirebaseManager.saveEventToCloud(it) }
                }
            },
            onMealNotesUpdate = { notes ->
                _mealNotes.value = notes
                storage?.saveMealNotes(notes)
            },
            onPostItsUpdate = { list ->
                _postIts.value = list
                storage?.savePostIts(list)
            },
            onHouseEssentialsUpdate = { info ->
                _houseEssentials.value = info
                storage?.saveHouseEssentialInfo(info)
            },
            onActiveGuestEventUpdate = { ev ->
                _activeGuestEvent.value = ev
                storage?.saveActiveGuestEvent(ev)
            }
        )
    }

    fun hasConfiguredHousehold(): Boolean {
        return _currentHousehold.value != null
    }

    private fun persist() {
        storage?.saveAll(
            user = _currentUser.value,
            household = _currentHousehold.value,
            budget = _monthlyBudget.value,
            categories = _categories.value,
            expenses = _expenses.value,
            provisions = _provisions.value,
            messages = _messages.value,
            polls = _polls.value,
            tasks = _tasks.value,
            events = _events.value,
            members = _members.value
        )
    }

    // Household & Profile Management
    fun createHousehold(name: String, primaryCurrency: String, secondaryCurrency: String, exchangeRate: Double, userName: String): HouseholdModel {
        val inviteCode = UUID.randomUUID().toString().take(6).uppercase()
        val userId = UUID.randomUUID().toString()
        val user = UserModel(id = userId, name = userName, role = "admin")
        val hhId = UUID.randomUUID().toString()
        val household = HouseholdModel(
            id = hhId,
            name = name,
            inviteCode = inviteCode,
            currency = primaryCurrency,
            secondaryCurrency = secondaryCurrency,
            exchangeRate = exchangeRate,
            ownerId = userId,
            memberIds = listOf(userId),
            monthlyBudget = _monthlyBudget.value.totalBudget
        )
        _currentUser.value = user
        _currentHousehold.value = household
        _monthlyBudget.value = _monthlyBudget.value.copy(
            id = "budget_$hhId",
            householdId = hhId
        )
        _members.value = listOf(user)
        persist()

        // Sync to cloud
        FirebaseManager.saveHouseholdToCloud(household, user)
        _categories.value.forEach { FirebaseManager.saveCategoryToCloud(it.copy(householdId = household.id)) }
        FirebaseManager.saveBudgetToCloud(_monthlyBudget.value)
        startCloudSync(household.id)

        return household
    }

    fun joinHousehold(
        code: String,
        userName: String,
        onSuccess: () -> Unit,
        onError: (String) -> Unit
    ) {
        val cleanCode = code.replace("UNK-", "").replace("KASA-", "").replace(" ", "").trim().uppercase()
        val userId = UUID.randomUUID().toString()
        val newUser = UserModel(id = userId, name = userName, role = "member")

        // Search in Cloud Firebase
        FirebaseManager.searchHouseholdByInviteCode(
            code = cleanCode,
            onSuccess = { cloudHousehold ->
                val userWithHh = newUser.copy(householdId = cloudHousehold.id)
                _currentUser.value = userWithHh
                _currentHousehold.value = cloudHousehold
                _members.value = listOf(userWithHh)
                if (cloudHousehold.monthlyBudget > 0.0) {
                    _monthlyBudget.value = MonthlyBudgetModel(
                        id = "budget_${cloudHousehold.id}",
                        householdId = cloudHousehold.id,
                        monthKey = "",
                        totalBudget = cloudHousehold.monthlyBudget
                    )
                }
                persist()

                FirebaseManager.saveHouseholdToCloud(cloudHousehold, userWithHh)
                // Start downloading all existing cloud data
                startCloudSync(cloudHousehold.id)
                onSuccess()
            },
            onError = { errorMsg ->
                onError(errorMsg)
            }
        )
    }

    fun updateHousehold(name: String, currency: String, secondaryCurrency: String, exchangeRate: Double) {
        val user = _currentUser.value ?: UserModel(id = "u1", name = "Moi")
        val current = _currentHousehold.value ?: return
        val updated = current.copy(
            name = name,
            currency = currency,
            secondaryCurrency = secondaryCurrency,
            exchangeRate = exchangeRate
        )
        _currentHousehold.value = updated
        persist()
        FirebaseManager.saveHouseholdToCloud(updated, user)
        sendMessage("⚙️ ${user.name} a modifié les paramètres du foyer ($name, $currency/$secondaryCurrency)", "system")
    }

    fun updateHouseholdAppIcon(appIconKey: String) {
        val hh = _currentHousehold.value ?: return
        val user = _currentUser.value ?: UserModel(id = "u1", name = "Moi")
        val updated = hh.copy(appIcon = appIconKey)
        _currentHousehold.value = updated
        persist()
        FirebaseManager.updateHouseholdAppIcon(hh.id, appIconKey)
        appContext?.let { ctx ->
            com.example.kasa.util.AppIconManager.applyAppIcon(ctx, appIconKey)
        }
        val iconName = com.example.kasa.util.AppIconManager.getIconDisplayName(appIconKey)
        val iconEmoji = com.example.kasa.util.AppIconManager.getIconEmoji(appIconKey)
        sendMessage("🎨 ${user.name} a changé l'icône de l'application du foyer pour \"$iconEmoji $iconName\"", "system")
        sendAdminAlert(
            title = "🎨 Icône de l'App Modifiée",
            message = "L'icône de l'application du foyer a été mise à jour ($iconEmoji $iconName) pour tous les membres.",
            type = "info"
        )
    }

    fun updateUserProfile(name: String) {
        val user = _currentUser.value ?: return
        val updated = user.copy(name = name)
        _currentUser.value = updated
        _members.value = _members.value.map { if (it.id == user.id) updated else it }
        persist()
        _currentHousehold.value?.let { FirebaseManager.saveHouseholdToCloud(it, updated) }
        FirebaseManager.saveUserToCloud(updated)
    }

    fun setHouseholdHomeLocation(lat: Double, lng: Double, address: String = "Domicile Familial") {
        val user = _currentUser.value ?: UserModel(id = "u1", name = "Moi")
        val hh = _currentHousehold.value ?: return
        val updated = hh.copy(homeLatitude = lat, homeLongitude = lng, homeAddress = address)
        _currentHousehold.value = updated

        // Recalculate immediate home status for current user
        if (user.latitude != null && user.longitude != null) {
            val results = FloatArray(1)
            android.location.Location.distanceBetween(user.latitude, user.longitude, lat, lng, results)
            val dist = results[0].toDouble()
            val isNowHome = dist <= updated.homeRadiusMeters
            val userUpdated = user.copy(
                isHome = isNowHome,
                distanceFromHomeMeters = dist,
                locationName = if (isNowHome) "🏠 À la maison" else "🚶 Dehors (à ${String.format("%.1f", dist / 1000.0)} km)",
                arrivedHomeAt = if (isNowHome && !user.isHome) Date() else user.arrivedHomeAt
            )
            _currentUser.value = userUpdated
            _members.value = _members.value.map { if (it.id == user.id) userUpdated else it }
            FirebaseManager.saveUserToCloud(userUpdated)
        }

        persist()
        FirebaseManager.updateHouseholdHomeLocation(hh.id, lat, lng, address)
        sendMessage("🏠 ${user.name} a enregistré l'emplacement de la maison : $address", "system")
        sendAdminAlert(
            title = "🏠 Emplacement Domicile Enregistré",
            message = "L'adresse du domicile a été définie ($address). Le radar familial et les alertes d'arrivée sont maintenant actifs.",
            type = "info"
        )
    }

    fun updateCurrentLocation(
        lat: Double,
        lng: Double,
        locationName: String = "",
        batteryLevel: Int = 100,
        isCharging: Boolean = false,
        deviceModel: String = ""
    ) {
        val user = _currentUser.value ?: return
        val hh = _currentHousehold.value

        var distanceMeters: Double? = null
        var isNowHome = false
        if (hh != null && hh.hasHomeLocation && hh.homeLatitude != null && hh.homeLongitude != null) {
            val results = FloatArray(1)
            android.location.Location.distanceBetween(
                lat, lng,
                hh.homeLatitude, hh.homeLongitude,
                results
            )
            distanceMeters = results[0].toDouble()
            isNowHome = distanceMeters <= hh.homeRadiusMeters
        }

        val wasHome = user.isHome
        val wasLowBattery = user.batteryLevel <= 15
        val isNowLowBattery = batteryLevel <= 15

        // Arrival detection
        val arrivedTime = if (isNowHome && !wasHome) Date() else if (isNowHome) (user.arrivedHomeAt ?: Date()) else null

        val finalLocName = when {
            isNowHome -> "🏠 À la maison"
            distanceMeters != null -> "🚶 Dehors (à ${String.format("%.1f", distanceMeters / 1000.0)} km)"
            else -> locationName.ifEmpty { "En déplacement" }
        }

        val updated = user.copy(
            latitude = lat,
            longitude = lng,
            locationName = finalLocName,
            locationUpdatedAt = Date(),
            batteryLevel = batteryLevel,
            isCharging = isCharging,
            deviceModel = deviceModel.ifEmpty { user.deviceModel },
            deviceStatusUpdatedAt = Date(),
            isHome = isNowHome,
            distanceFromHomeMeters = distanceMeters,
            arrivedHomeAt = arrivedTime
        )

        _currentUser.value = updated
        _members.value = _members.value.map { if (it.id == user.id) updated else it }
        persist()
        FirebaseManager.saveUserToCloud(updated)
        appContext?.let { com.example.kasa.widget.KasaHomeWidgetProvider.updateAllWidgets(it) }

        // Low Battery Alert to Admin
        if (!wasLowBattery && isNowLowBattery) {
            sendAdminAlert(
                title = "🪫 Alerte Batterie Faible",
                message = "Le téléphone de ${user.name} n'a plus que $batteryLevel% de batterie !",
                type = "battery",
                memberId = user.id,
                memberName = user.name
            )
        }

        // 🏠 Arrival notification -> SENT TO ADMIN ONLY!
        if (isNowHome && !wasHome) {
            sendAdminAlert(
                title = "🏠 Arrivée à la Maison",
                message = "${user.name} est bien rentré(e) à la maison !",
                type = "arrival",
                memberId = user.id,
                memberName = user.name
            )
        }

        // 🪫 Low battery notification -> SENT TO ADMIN ONLY!
        if (isNowLowBattery && !wasLowBattery) {
            sendAdminAlert(
                title = "🪫 Alerte Batterie Faible",
                message = "Le téléphone de ${user.name} n'a plus que $batteryLevel% de batterie !",
                type = "low_battery",
                memberId = user.id,
                memberName = user.name
            )
        }
    }

    fun updateDeviceStatus(batteryLevel: Int, isCharging: Boolean, deviceModel: String) {
        val user = _currentUser.value ?: return
        val wasLowBattery = user.batteryLevel <= 15
        val isNowLowBattery = batteryLevel <= 15

        val updated = user.copy(
            batteryLevel = batteryLevel,
            isCharging = isCharging,
            deviceModel = deviceModel.ifEmpty { user.deviceModel },
            deviceStatusUpdatedAt = Date()
        )

        _currentUser.value = updated
        _members.value = _members.value.map { if (it.id == user.id) updated else it }
        persist()
        FirebaseManager.saveUserToCloud(updated)

        // Low Battery Alert to Admin
        if (!wasLowBattery && isNowLowBattery) {
            sendAdminAlert(
                title = "🪫 Alerte Batterie Faible",
                message = "Le téléphone de ${user.name} n'a plus que $batteryLevel% de batterie !",
                type = "low_battery",
                memberId = user.id,
                memberName = user.name
            )
        }
    }

    fun sendAdminAlert(
        title: String,
        message: String,
        type: String = "general",
        memberId: String = "",
        memberName: String = ""
    ) {
        val hhId = _currentHousehold.value?.id ?: return
        val notif = AdminNotificationModel(
            id = UUID.randomUUID().toString(),
            householdId = hhId,
            title = title,
            message = message,
            type = type,
            memberId = memberId,
            memberName = memberName,
            createdAt = Date(),
            isRead = false
        )
        _adminNotifications.value = listOf(notif) + _adminNotifications.value.filter { it.id != notif.id }
        FirebaseManager.saveAdminNotificationToCloud(notif)
    }

    fun markAdminNotificationRead(notificationId: String) {
        _adminNotifications.value = _adminNotifications.value.map {
            if (it.id == notificationId) it.copy(isRead = true) else it
        }
        FirebaseManager.markAdminNotificationRead(notificationId)
    }

    fun clearAllAdminNotifications() {
        val hhId = _currentHousehold.value?.id ?: return
        _adminNotifications.value = emptyList()
        FirebaseManager.clearAdminNotifications(hhId)
    }

    fun setMemberTrackingRemote(memberId: String, enabled: Boolean) {
        _members.value = _members.value.map {
            if (it.id == memberId) it.copy(trackingEnabled = enabled) else it
        }
        if (_currentUser.value?.id == memberId) {
            _currentUser.value = _currentUser.value?.copy(trackingEnabled = enabled)
        }
        persist()
        FirebaseManager.setMemberTrackingRemote(memberId, enabled)
    }

    fun requestMemberLocationPing(memberId: String) {
        val member = _members.value.find { it.id == memberId }
        FirebaseManager.requestLocationPingRemote(memberId)
        if (member != null) {
            val user = _currentUser.value ?: UserModel(id = "u1", name = "Moi")
            sendMessage("📡 ${user.name} a demandé l'actualisation de la position de ${member.name}.", "system")
        }
    }

    fun requestAllMembersLocationPing() {
        val user = _currentUser.value ?: UserModel(id = "u1", name = "Moi")
        val otherMembers = _members.value.filter { it.id != user.id }
        otherMembers.forEach { m ->
            FirebaseManager.requestLocationPingRemote(m.id)
        }
        sendMessage("📡 ${user.name} a demandé l'actualisation de la position de tous les membres du foyer.", "system")
    }

    // Budget Management
    fun updateMonthlyBudget(totalBudget: Double) {
        val user = _currentUser.value ?: UserModel(id = "u1", name = "Moi")
        val hh = _currentHousehold.value
        val hhId = hh?.id ?: "hh_1"
        val currency = hh?.currency ?: "USD"
        val updated = _monthlyBudget.value.copy(
            id = "budget_$hhId",
            householdId = hhId,
            totalBudget = totalBudget,
            lastModifiedBy = user.name,
            lastModifiedAt = Date()
        )
        _monthlyBudget.value = updated
        if (hh != null && hh.monthlyBudget != totalBudget) {
            _currentHousehold.value = hh.copy(monthlyBudget = totalBudget)
        }
        persist()
        FirebaseManager.saveBudgetToCloud(updated)
        sendMessage("💰 ${user.name} a modifié le budget mensuel à ${CurrencyFormatter.format(totalBudget, currency)}", "system")
    }

    // Category Management
    fun addCategory(name: String, emoji: String, plannedAmount: Double, isProvision: Boolean = false) {
        val user = _currentUser.value ?: UserModel(id = "u1", name = "Moi")
        val newCat = CategoryModel(
            id = UUID.randomUUID().toString(),
            householdId = _currentHousehold.value?.id ?: "hh_1",
            name = name,
            emoji = emoji.ifEmpty { "📦" },
            plannedAmount = plannedAmount,
            isProvision = isProvision
        )
        _categories.value = _categories.value + newCat
        persist()
        FirebaseManager.saveCategoryToCloud(newCat)
        sendMessage("🏷️ ${user.name} a créé la catégorie $emoji $name", "system")
    }

    fun updateCategory(id: String, name: String, emoji: String, plannedAmount: Double, isProvision: Boolean) {
        val user = _currentUser.value ?: UserModel(id = "u1", name = "Moi")
        var updatedCat: CategoryModel? = null
        _categories.value = _categories.value.map {
            if (it.id == id) {
                val u = it.copy(name = name, emoji = emoji, plannedAmount = plannedAmount, isProvision = isProvision)
                updatedCat = u
                u
            } else it
        }
        persist()
        updatedCat?.let {
            FirebaseManager.saveCategoryToCloud(it)
            sendMessage("✏️ ${user.name} a modifié la catégorie ${it.emoji} ${it.name}", "system")
        }
    }

    fun deleteCategory(categoryId: String) {
        val user = _currentUser.value ?: UserModel(id = "u1", name = "Moi")
        val cat = _categories.value.find { it.id == categoryId }
        _categories.value = _categories.value.filter { it.id != categoryId }
        persist()
        FirebaseManager.deleteCategoryFromCloud(categoryId)
        if (cat != null) {
            sendMessage("🗑️ ${user.name} a supprimé la catégorie ${cat.emoji} ${cat.name}", "system")
        }
    }

    // Expenses Management
    fun addExpense(categoryId: String, amount: Double, description: String, comment: String = "", date: Date = Date(), notifyChat: Boolean = true) {
        val user = _currentUser.value ?: UserModel(id = "u1", name = "Moi")
        val cat = _categories.value.find { it.id == categoryId }
        val currency = _currentHousehold.value?.currency ?: "USD"
        val newExpense = ExpenseModel(
            id = UUID.randomUUID().toString(),
            householdId = _currentHousehold.value?.id ?: "hh_1",
            userId = user.id,
            userName = user.name,
            categoryId = categoryId,
            categoryName = cat?.name ?: "Autre",
            categoryEmoji = cat?.emoji ?: "📦",
            amount = amount,
            description = description,
            comment = comment,
            date = date
        )
        _expenses.value = listOf(newExpense) + _expenses.value
        persist()
        FirebaseManager.saveExpenseToCloud(newExpense)
        if (notifyChat) {
            val desc = if (description.isNotBlank()) description else (cat?.name ?: "Dépense")
            val commentPart = if (comment.isNotBlank()) " (Note: $comment)" else ""
            sendMessage("💸 ${user.name} a ajouté une dépense de ${CurrencyFormatter.format(amount, currency)} ($desc)$commentPart", "system")
        }

        val totalSpent = _expenses.value.sumOf { it.amount }
        val bgt = _monthlyBudget.value.totalBudget
        if (bgt > 0 && totalSpent > bgt) {
            KasaNotificationManager.postAlert(
                title = "⚠️ Alerte Dépassement Budget !",
                message = "Attention : Les dépenses actuelles (${CurrencyFormatter.format(totalSpent, currency)}) dépassent le budget alloué (${CurrencyFormatter.format(bgt, currency)}) !",
                category = AlertCategory.BUDGET_EXCEEDED,
                emoji = "⚠️",
                showSystemNotification = true
            )
        }
    }

    fun updateExpense(id: String, categoryId: String, amount: Double, description: String, date: Date = Date(), comment: String = "") {
        val user = _currentUser.value ?: UserModel(id = "u1", name = "Moi")
        val cat = _categories.value.find { it.id == categoryId }
        val currency = _currentHousehold.value?.currency ?: "USD"
        var updatedExp: ExpenseModel? = null
        _expenses.value = _expenses.value.map {
            if (it.id == id) {
                val u = it.copy(
                    categoryId = categoryId,
                    categoryName = cat?.name ?: it.categoryName,
                    categoryEmoji = cat?.emoji ?: it.categoryEmoji,
                    amount = amount,
                    description = description,
                    comment = comment,
                    date = date,
                    lastModifiedByName = user.name,
                    lastModifiedAt = Date()
                )
                updatedExp = u
                u
            } else it
        }
        persist()
        updatedExp?.let {
            FirebaseManager.saveExpenseToCloud(it)
            val desc = if (description.isNotBlank()) description else (cat?.name ?: "Dépense")
            sendMessage("✏️ ${user.name} a modifié la dépense \"$desc\" (${CurrencyFormatter.format(amount, currency)})", "system")
        }
    }

    fun deleteExpense(expenseId: String) {
        val user = _currentUser.value ?: UserModel(id = "u1", name = "Moi")
        val currency = _currentHousehold.value?.currency ?: "USD"
        val exp = _expenses.value.find { it.id == expenseId }

        deletedExpenseIds.add(expenseId)
        storage?.saveDeletedExpenseId(expenseId)

        _expenses.value = _expenses.value.filter { it.id != expenseId }
        persist()
        FirebaseManager.deleteExpenseFromCloud(expenseId)

        if (exp != null) {
            // If this expense was created from a provision purchase, reset provision status to "planned"
            val matchedProv = _provisions.value.find {
                it.status == "bought" && (exp.description.contains(it.name, ignoreCase = true) || "Achat: ${it.name}" in exp.description)
            }
            if (matchedProv != null) {
                updateProvisionItem(
                    id = matchedProv.id,
                    name = matchedProv.name,
                    quantity = matchedProv.quantity,
                    unit = matchedProv.unit,
                    plannedPrice = matchedProv.plannedPrice,
                    priority = matchedProv.priority,
                    status = "planned"
                )
            }

            val desc = if (exp.description.isNotBlank()) exp.description else exp.categoryName
            sendMessage("🗑️ ${user.name} a supprimé la dépense \"$desc\" (${CurrencyFormatter.format(exp.amount, currency)})", "system")
        }
    }

    // Provision Management
    fun addProvisionItem(name: String, quantity: Int, unit: String, plannedPrice: Double, priority: String, comment: String = "") {
        val user = _currentUser.value ?: UserModel(id = "u1", name = "Moi")
        val newItem = ProvisionItemModel(
            id = UUID.randomUUID().toString(),
            householdId = _currentHousehold.value?.id ?: "hh_1",
            name = name,
            quantity = quantity,
            unit = unit,
            plannedPrice = plannedPrice,
            priority = priority,
            status = "planned",
            comment = comment,
            addedBy = user.name
        )
        _provisions.value = _provisions.value + newItem
        persist()
        FirebaseManager.saveProvisionToCloud(newItem)
        val note = if (comment.isNotBlank()) " [Note: $comment]" else ""
        sendMessage("📋 ${user.name} a ajouté \"$name\" ($quantity $unit)$note aux provisions", "system")
    }

    fun updateProvisionItem(id: String, name: String, quantity: Int, unit: String, plannedPrice: Double, priority: String, status: String, comment: String = "") {
        val user = _currentUser.value ?: UserModel(id = "u1", name = "Moi")
        var updatedItem: ProvisionItemModel? = null
        _provisions.value = _provisions.value.map {
            if (it.id == id) {
                val u = it.copy(
                    name = name,
                    quantity = quantity,
                    unit = unit,
                    plannedPrice = plannedPrice,
                    priority = priority,
                    status = status,
                    comment = comment,
                    lastModifiedBy = user.name,
                    lastModifiedAt = Date()
                )
                updatedItem = u
                u
            } else it
        }
        persist()
        updatedItem?.let {
            FirebaseManager.saveProvisionToCloud(it)
            sendMessage("✏️ ${user.name} a modifié \"$name\" dans les provisions", "system")
        }
    }

    fun deleteProvisionItem(itemId: String) {
        val user = _currentUser.value ?: UserModel(id = "u1", name = "Moi")
        val item = _provisions.value.find { it.id == itemId }

        deletedProvisionIds.add(itemId)
        storage?.saveDeletedProvisionId(itemId)

        _provisions.value = _provisions.value.filter { it.id != itemId }
        persist()
        FirebaseManager.deleteProvisionFromCloud(itemId)
        if (item != null) {
            sendMessage("🗑️ ${user.name} a retiré \"${item.name}\" des provisions", "system")
        }
    }

    fun confirmPurchase(item: ProvisionItemModel, actualPrice: Double, comment: String = "") {
        val user = _currentUser.value ?: UserModel(id = "u1", name = "Moi")
        val currency = _currentHousehold.value?.currency ?: "USD"
        var updatedItem: ProvisionItemModel? = null
        val updatedList = _provisions.value.map {
            if (it.id == item.id) {
                val u = it.copy(
                    status = "bought",
                    actualPrice = actualPrice,
                    comment = if (comment.isNotBlank()) comment else it.comment,
                    purchasedBy = user.name,
                    purchasedAt = Date()
                )
                updatedItem = u
                u
            } else it
        }
        _provisions.value = updatedList

        // Add expense strictly in the Provisions category
        val provCat = _categories.value.find { it.name.equals("Provisions", ignoreCase = true) || it.isProvision }
            ?: _categories.value.firstOrNull()
        if (provCat != null) {
            addExpense(
                categoryId = provCat.id,
                amount = actualPrice,
                description = "Achat: ${item.name} (${item.quantity} ${item.unit})",
                comment = if (comment.isNotBlank()) comment else item.comment,
                notifyChat = false
            )
        }

        // Post chat message
        val note = if (comment.isNotBlank()) " [Note: $comment]" else ""
        sendMessage("🛒 ${user.name} a acheté \"${item.name}\" pour ${CurrencyFormatter.format(actualPrice, currency)}$note", "system")
        persist()
        updatedItem?.let { FirebaseManager.saveProvisionToCloud(it) }
    }

    fun batchConfirmPurchases(purchases: List<Pair<ProvisionItemModel, Double>>, comment: String = "") {
        if (purchases.isEmpty()) return
        val user = _currentUser.value ?: UserModel(id = "u1", name = "Moi")
        val currency = _currentHousehold.value?.currency ?: "$"
        val totalAmount = purchases.sumOf { it.second }
        val now = Date()

        val updatedItems = mutableListOf<ProvisionItemModel>()
        val purchaseMap = purchases.associateBy({ it.first.id }, { it.second })

        _provisions.value = _provisions.value.map { item ->
            val price = purchaseMap[item.id]
            if (price != null) {
                val updated = item.copy(
                    status = "bought",
                    actualPrice = price,
                    purchasedBy = user.name,
                    purchasedAt = now
                )
                updatedItems.add(updated)
                updated
            } else {
                item
            }
        }

        // Add single aggregated expense strictly in the Provisions category
        val provCat = _categories.value.find { it.name.equals("Provisions", ignoreCase = true) || it.isProvision }
            ?: _categories.value.firstOrNull()
        if (provCat != null) {
            val names = purchases.joinToString(", ") { it.first.name }
            addExpense(
                categoryId = provCat.id,
                amount = totalAmount,
                description = "Course Express (${purchases.size} articles: $names)",
                comment = comment,
                notifyChat = false
            )
        }

        // Send alert message in Salon chat
        val itemsNames = purchases.take(3).joinToString(", ") { it.first.name } + if (purchases.size > 3) " +${purchases.size - 3} autres" else ""
        val note = if (comment.isNotBlank()) " [Note: $comment]" else ""
        sendMessage("🛒 ${user.name} a terminé les courses : ${purchases.size} articles achetés ($itemsNames) pour ${CurrencyFormatter.format(totalAmount, currency)}$note !", "system")

        persist()
        updatedItems.forEach { FirebaseManager.saveProvisionToCloud(it) }
    }

    // Chat & Polls
    fun sendMessage(
        text: String,
        type: String = "text",
        replyToMessageId: String? = null,
        replyToUserName: String? = null,
        replyToText: String? = null,
        audioDurationSec: Int = 0
    ) {
        val user = _currentUser.value ?: UserModel(id = "u1", name = "Moi")
        val newMsg = MessageModel(
            id = UUID.randomUUID().toString(),
            householdId = _currentHousehold.value?.id ?: "hh_1",
            userId = user.id,
            userName = user.name,
            text = text,
            type = type,
            replyToMessageId = replyToMessageId,
            replyToUserName = replyToUserName,
            replyToText = replyToText,
            audioDurationSec = audioDurationSec,
            createdAt = Date()
        )
        _messages.value = _messages.value + newMsg
        persist()
        FirebaseManager.saveMessageToCloud(newMsg)

        if (type == "system") {
            val emoji = when {
                text.startsWith("💰") -> "💰"
                text.startsWith("💸") -> "💸"
                text.startsWith("✏️") -> "✏️"
                text.startsWith("🗑️") -> "🗑️"
                text.startsWith("📋") -> "📋"
                text.startsWith("🛒") -> "🛒"
                text.startsWith("🏷️") -> "🏷️"
                text.startsWith("⚙️") -> "⚙️"
                else -> "📢"
            }
            KasaNotificationManager.postAlert(
                title = "Activité du Foyer",
                message = text,
                category = AlertCategory.MODIFICATION,
                emoji = emoji,
                showSystemNotification = false
            )
        }
    }

    fun reactToMessage(messageId: String, emoji: String) {
        val user = _currentUser.value ?: return
        val updated = _messages.value.map { msg ->
            if (msg.id == messageId) {
                val reactions = msg.reactions.toMutableMap()
                val userList = reactions[emoji]?.toMutableList() ?: mutableListOf()
                if (userList.contains(user.name)) {
                    userList.remove(user.name)
                    if (userList.isEmpty()) reactions.remove(emoji) else reactions[emoji] = userList
                } else {
                    userList.add(user.name)
                    reactions[emoji] = userList
                }
                msg.copy(reactions = reactions)
            } else msg
        }
        _messages.value = updated
        persist()
        FirebaseManager.toggleMessageReactionCloud(messageId, emoji, user.name)
    }

    fun togglePinMessage(messageId: String) {
        var newPinnedState = false
        val updated = _messages.value.map { msg ->
            if (msg.id == messageId) {
                newPinnedState = !msg.isPinned
                msg.copy(isPinned = newPinnedState)
            } else msg
        }
        _messages.value = updated
        persist()
        FirebaseManager.togglePinMessageCloud(messageId, newPinnedState)
    }

    fun deleteMessage(messageId: String) {
        _messages.value = _messages.value.filter { it.id != messageId }
        persist()
        FirebaseManager.deleteMessageCloud(messageId)
    }

    fun votePoll(pollId: String, optionId: String) {
        val userId = _currentUser.value?.id ?: return
        var updatedPoll: PollModel? = null
        val updatedPolls = _polls.value.map { poll ->
            if (poll.id == pollId) {
                val updatedOptions = poll.options.map { opt ->
                    val voters = opt.voterIds.toMutableList()
                    if (opt.id == optionId) {
                        if (!voters.contains(userId)) voters.add(userId)
                    } else {
                        voters.remove(userId)
                    }
                    opt.copy(voterIds = voters)
                }
                val u = poll.copy(options = updatedOptions)
                updatedPoll = u
                u
            } else poll
        }
        _polls.value = updatedPolls
        persist()
        updatedPoll?.let { FirebaseManager.savePollToCloud(it) }
    }

    fun createPoll(question: String, optionsTexts: List<String>) {
        val user = _currentUser.value ?: UserModel(id = "u1", name = "Moi")
        val newPoll = PollModel(
            id = UUID.randomUUID().toString(),
            householdId = _currentHousehold.value?.id ?: "hh_1",
            creatorId = user.id,
            creatorName = user.name,
            question = question,
            options = optionsTexts.filter { it.isNotBlank() }.map {
                PollOption(id = UUID.randomUUID().toString(), text = it.trim())
            }
        )
        _polls.value = listOf(newPoll) + _polls.value
        sendMessage("📊 Nouveau sondage : \"$question\"", "system")
        persist()
        FirebaseManager.savePollToCloud(newPoll)
    }

    // Tasks & Chores Management
    fun addTask(
        title: String,
        description: String = "",
        assignedMemberId: String = "",
        assignedMemberName: String = "",
        dueDate: Date? = null,
        recurrence: String = "none",
        points: Int = 10
    ) {
        val hhId = _currentHousehold.value?.id ?: "hh_1"
        val newTask = TaskModel(
            id = UUID.randomUUID().toString(),
            householdId = hhId,
            title = title.trim(),
            description = description.trim(),
            assignedMemberId = assignedMemberId,
            assignedMemberName = assignedMemberName,
            dueDate = dueDate,
            recurrence = recurrence,
            status = "todo",
            points = points,
            createdAt = Date()
        )
        _tasks.value = listOf(newTask) + _tasks.value
        persist()
        FirebaseManager.saveTaskToCloud(newTask)
    }

    fun updateTask(task: TaskModel) {
        _tasks.value = _tasks.value.map { if (it.id == task.id) task else it }
        persist()
        FirebaseManager.saveTaskToCloud(task)
    }

    fun toggleTaskStatus(taskId: String) {
        val user = _currentUser.value
        var updatedTask: TaskModel? = null
        _tasks.value = _tasks.value.map { task ->
            if (task.id == taskId) {
                val newStatus = if (task.status == "done") "todo" else "done"
                val isNowDone = newStatus == "done"
                val updated = task.copy(
                    status = newStatus,
                    completedAt = if (isNowDone) Date() else null,
                    completedByMemberId = if (isNowDone) (user?.id ?: "") else "",
                    completedByMemberName = if (isNowDone) (user?.name ?: "Un membre") else ""
                )
                updatedTask = updated
                updated
            } else task
        }
        persist()
        updatedTask?.let { task ->
            FirebaseManager.saveTaskToCloud(task)
            if (task.status == "done") {
                sendAdminAlert(
                    title = "🧹 Tâche Accomplie",
                    message = "${task.completedByMemberName.ifEmpty { "Un membre" }} a terminé : \"${task.title}\" (+${task.points} pts) !",
                    type = "info",
                    memberId = task.completedByMemberId,
                    memberName = task.completedByMemberName
                )
            }
        }
    }

    fun deleteTask(taskId: String) {
        _tasks.value = _tasks.value.filter { it.id != taskId }
        persist()
        FirebaseManager.deleteTaskFromCloud(taskId)
    }

    // Events & Agenda Management
    fun addEvent(
        title: String,
        description: String = "",
        location: String = "",
        startDate: Date = Date(),
        endDate: Date? = null,
        category: String = "general",
        categoryEmoji: String = "📌",
        participantMemberIds: List<String> = emptyList()
    ) {
        val user = _currentUser.value
        val hhId = _currentHousehold.value?.id ?: "hh_1"
        val newEvent = EventModel(
            id = UUID.randomUUID().toString(),
            householdId = hhId,
            title = title.trim(),
            description = description.trim(),
            location = location.trim(),
            startDate = startDate,
            endDate = endDate,
            category = category,
            categoryEmoji = categoryEmoji,
            participantMemberIds = participantMemberIds,
            createdById = user?.id ?: "",
            createdByName = user?.name ?: "",
            createdAt = Date()
        )
        _events.value = (listOf(newEvent) + _events.value).sortedBy { it.startDate }
        persist()
        FirebaseManager.saveEventToCloud(newEvent)
        sendAdminAlert(
            title = "📅 Nouvel Événement Familial",
            message = "Nouvel événement ajouté : \"${newEvent.title}\"",
            type = "info"
        )
    }

    fun updateEvent(event: EventModel) {
        _events.value = _events.value.map { if (it.id == event.id) event else it }.sortedBy { it.startDate }
        persist()
        FirebaseManager.saveEventToCloud(event)
    }

    fun deleteEvent(eventId: String) {
        _events.value = _events.value.filter { it.id != eventId }
        persist()
        FirebaseManager.deleteEventFromCloud(eventId)
    }

    // Meal Plan Management
    fun updateMealPlan(
        lunchTitle: String,
        lunchDetails: String = "",
        dinnerTitle: String,
        dinnerDetails: String = "",
        chefUserId: String? = null,
        chefUserName: String? = null,
        specialNote: String = ""
    ) {
        val user = _currentUser.value ?: UserModel(id = "u1", name = "Moi")
        val hh = _currentHousehold.value
        val updated = _currentMealPlan.value.copy(
            householdId = hh?.id ?: "",
            lunchTitle = lunchTitle.trim(),
            lunchDetails = lunchDetails.trim(),
            dinnerTitle = dinnerTitle.trim(),
            dinnerDetails = dinnerDetails.trim(),
            chefUserId = chefUserId,
            chefUserName = chefUserName,
            specialNote = specialNote.trim(),
            lastModifiedByName = user.name,
            lastModifiedAt = Date()
        )
        _currentMealPlan.value = updated
        storage?.saveMealPlan(updated)
        appContext?.let { com.example.kasa.widget.KasaHomeWidgetProvider.updateAllWidgets(it) }

        val chefInfo = if (!chefUserName.isNullOrBlank()) " (Chef : 👨‍🍳 $chefUserName)" else ""
        val menuText = when {
            lunchTitle.isNotBlank() && dinnerTitle.isNotBlank() -> "Midi : $lunchTitle | Soir : $dinnerTitle$chefInfo"
            dinnerTitle.isNotBlank() -> "Ce soir : $dinnerTitle$chefInfo"
            lunchTitle.isNotBlank() -> "Ce midi : $lunchTitle$chefInfo"
            else -> "Menu mis à jour"
        }
        sendMessage("🍽️ ${user.name} a planifié le repas : $menuText", "system")
    }

    fun toggleDinnerAttendance(userName: String, attends: Boolean) {
        val current = _currentMealPlan.value
        val yesList = current.attendeesDinnerYes.toMutableList()
        val noList = current.attendeesDinnerNo.toMutableList()

        if (attends) {
            noList.remove(userName)
            if (!yesList.contains(userName)) yesList.add(userName)
        } else {
            yesList.remove(userName)
            if (!noList.contains(userName)) noList.add(userName)
        }

        val updated = current.copy(
            attendeesDinnerYes = yesList,
            attendeesDinnerNo = noList
        )
        _currentMealPlan.value = updated
        storage?.saveMealPlan(updated)
        appContext?.let { com.example.kasa.widget.KasaHomeWidgetProvider.updateAllWidgets(it) }
    }

    // Meal Notes (Bloc-notes des Repas du Foyer)
    fun addMealNote(mealTitle: String, noteDetails: String = "") {
        val user = _currentUser.value ?: UserModel(id = "u1", name = "Moi")
        val hh = _currentHousehold.value
        val newNote = MealNoteModel(
            id = UUID.randomUUID().toString(),
            householdId = hh?.id ?: "",
            mealTitle = mealTitle.trim(),
            noteDetails = noteDetails.trim(),
            suggestedByUserId = user.id,
            suggestedByUserName = user.name,
            createdAt = Date(),
            upvoterUserIds = listOf(user.id),
            status = "ACTIVE"
        )
        val updated = _mealNotes.value + newNote
        _mealNotes.value = updated
        storage?.saveMealNotes(updated)
        hh?.id?.let { FirebaseManager.saveMealNote(it, newNote) }

        sendMessage("📝 ${user.name} a proposé une idée de repas : \"${newNote.mealTitle}\"", "system")
    }

    fun deleteMealNote(noteId: String) {
        val hh = _currentHousehold.value
        val updated = _mealNotes.value.filter { it.id != noteId }
        _mealNotes.value = updated
        storage?.saveMealNotes(updated)
        hh?.id?.let { FirebaseManager.deleteMealNote(it, noteId) }
    }

    fun toggleMealNoteUpvote(noteId: String) {
        val user = _currentUser.value ?: return
        val hh = _currentHousehold.value
        val updated = _mealNotes.value.map { note ->
            if (note.id == noteId) {
                val upvoters = note.upvoterUserIds.toMutableList()
                if (upvoters.contains(user.id)) {
                    upvoters.remove(user.id)
                } else {
                    upvoters.add(user.id)
                }
                val modified = note.copy(upvoterUserIds = upvoters)
                hh?.id?.let { FirebaseManager.saveMealNote(it, modified) }
                modified
            } else {
                note
            }
        }
        _mealNotes.value = updated
        storage?.saveMealNotes(updated)
    }

    fun promoteMealNoteToDailyMeal(noteId: String, isDinner: Boolean) {
        val note = _mealNotes.value.find { it.id == noteId } ?: return
        val currentPlan = _currentMealPlan.value
        if (isDinner) {
            updateMealPlan(
                lunchTitle = currentPlan.lunchTitle,
                lunchDetails = currentPlan.lunchDetails,
                dinnerTitle = note.mealTitle,
                dinnerDetails = note.noteDetails,
                chefUserId = currentPlan.chefUserId,
                chefUserName = currentPlan.chefUserName,
                specialNote = currentPlan.specialNote
            )
        } else {
            updateMealPlan(
                lunchTitle = note.mealTitle,
                lunchDetails = note.noteDetails,
                dinnerTitle = currentPlan.dinnerTitle,
                dinnerDetails = currentPlan.dinnerDetails,
                chefUserId = currentPlan.chefUserId,
                chefUserName = currentPlan.chefUserName,
                specialNote = currentPlan.specialNote
            )
        }
    }

    // Corkboard Actions
    fun addPostIt(text: String, colorHex: String = "#FEF08A", emoji: String = "📌", isPinned: Boolean = false) {
        val user = _currentUser.value ?: return
        val hh = _currentHousehold.value ?: return
        val newPostIt = PostItModel(
            id = UUID.randomUUID().toString(),
            householdId = hh.id,
            text = text,
            colorHex = colorHex,
            emoji = emoji,
            authorId = user.id,
            authorName = user.name,
            createdAt = Date(),
            isPinned = isPinned
        )
        val updated = (_postIts.value + newPostIt).sortedWith(compareByDescending<PostItModel> { it.isPinned }.thenByDescending { it.createdAt })
        _postIts.value = updated
        storage?.savePostIts(updated)
        FirebaseManager.savePostItToCloud(newPostIt)
    }

    fun deletePostIt(postItId: String) {
        val updated = _postIts.value.filter { it.id != postItId }
        _postIts.value = updated
        storage?.savePostIts(updated)
        FirebaseManager.deletePostItFromCloud(postItId)
    }

    fun togglePostItPin(postItId: String) {
        val updated = _postIts.value.map {
            if (it.id == postItId) {
                val toggled = it.copy(isPinned = !it.isPinned)
                FirebaseManager.savePostItToCloud(toggled)
                toggled
            } else it
        }.sortedWith(compareByDescending<PostItModel> { it.isPinned }.thenByDescending { it.createdAt })
        _postIts.value = updated
        storage?.savePostIts(updated)
    }

    fun updateHouseEssentials(info: HouseEssentialInfo) {
        val user = _currentUser.value ?: return
        val hh = _currentHousehold.value ?: return
        val updatedInfo = info.copy(
            lastUpdatedBy = user.name,
            lastUpdatedAt = Date()
        )
        _houseEssentials.value = updatedInfo
        storage?.saveHouseEssentialInfo(updatedInfo)
        FirebaseManager.saveHouseEssentialsToCloud(hh.id, updatedInfo)
    }

    // Security & Connected House Actions
    fun declareGuestEvent(guestCount: Int, description: String, endTime: Date? = null) {
        val user = _currentUser.value ?: return
        val hh = _currentHousehold.value ?: return
        val event = GuestPartyEvent(
            id = UUID.randomUUID().toString(),
            householdId = hh.id,
            hostUserId = user.id,
            hostUserName = user.name,
            guestCount = guestCount,
            description = description,
            startTime = Date(),
            endTime = endTime,
            isActive = true
        )
        _activeGuestEvent.value = event
        storage?.saveActiveGuestEvent(event)
        FirebaseManager.saveGuestEventToCloud(event)

        val descText = if (description.isNotBlank()) " ($description)" else ""
        val alertMsg = "🎉 ${user.name} a activé le Mode Soirée : $guestCount invité(s) attendu(s)$descText !"
        sendMessage(alertMsg, "system")
        KasaNotificationManager.postAlert(
            title = "🎉 Mode Soirée Activé !",
            message = "${user.name} accueille $guestCount invité(s)$descText.",
            category = AlertCategory.MODIFICATION,
            emoji = "🎉",
            showSystemNotification = true
        )
    }

    fun endActiveGuestEvent() {
        val user = _currentUser.value ?: return
        val current = _activeGuestEvent.value ?: return
        _activeGuestEvent.value = null
        storage?.saveActiveGuestEvent(null)
        FirebaseManager.endGuestEventInCloud(current.id)

        sendMessage("🌙 ${user.name} a désactivé le Mode Soirée. Retour au calme !", "system")
    }

    fun sendEmergencyAlert(type: EmergencyType, details: String = "", includeLocation: Boolean = true) {
        val user = _currentUser.value ?: return
        val locationText = if (includeLocation && user.latitude != null && user.longitude != null) {
            "\n📍 Position : https://maps.google.com/?q=${user.latitude},${user.longitude} (${user.locationName.ifEmpty { "En déplacement" }})"
        } else ""
        val detailsText = if (details.isNotBlank()) " : \"$details\"" else ""
        val fullMsg = "${type.emoji} [ALERTE URGENCE] ${user.name} signale : ${type.label}$detailsText$locationText"

        sendMessage(fullMsg, "system")
        KasaNotificationManager.postAlert(
            title = "${type.emoji} ALERTE DU FOYER : ${type.label}",
            message = "${user.name} a besoin d'aide ! $details",
            category = AlertCategory.URGENCE,
            emoji = type.emoji,
            showSystemNotification = true
        )
    }

    // Scheduled Reminders Actions
    fun addScheduledReminder(message: String, triggerTimestamp: Long) {
        val user = _currentUser.value ?: return
        val hh = _currentHousehold.value ?: return
        val reminder = ScheduledReminderModel(
            id = UUID.randomUUID().toString(),
            householdId = hh.id,
            creatorId = user.id,
            creatorName = user.name,
            message = message,
            triggerTimestamp = triggerTimestamp,
            isSent = false,
            createdAt = Date()
        )
        val updated = _scheduledReminders.value + reminder
        _scheduledReminders.value = updated
        storage?.saveScheduledReminders(updated)
    }

    fun deleteScheduledReminder(reminderId: String) {
        val updated = _scheduledReminders.value.filter { it.id != reminderId }
        _scheduledReminders.value = updated
        storage?.saveScheduledReminders(updated)
    }

    fun checkDueReminders() {
        val now = System.currentTimeMillis()
        val current = _scheduledReminders.value
        val due = current.filter { !it.isSent && it.triggerTimestamp <= now }
        if (due.isEmpty()) return

        due.forEach { reminder ->
            sendMessage("⏰ [RAPPEL PROGRAMMÉ par ${reminder.creatorName}] ${reminder.message}", "system")
        }

        val updated = current.map { if (it in due) it.copy(isSent = true) else it }
        _scheduledReminders.value = updated
        storage?.saveScheduledReminders(updated)
    }

    fun resetHousehold() {
        FirebaseManager.stopSync()
        storage?.clear()
        _currentUser.value = null
        _currentHousehold.value = null
        _monthlyBudget.value = MonthlyBudgetModel(totalBudget = 0.0)
        _expenses.value = emptyList()
        _provisions.value = emptyList()
        _messages.value = emptyList()
        _polls.value = emptyList()
        _members.value = emptyList()
        _tasks.value = emptyList()
        _events.value = emptyList()
        _mealNotes.value = emptyList()
        _adminNotifications.value = emptyList()
        _postIts.value = emptyList()
        _houseEssentials.value = HouseEssentialInfo()
        _activeGuestEvent.value = null
        _scheduledReminders.value = emptyList()
    }
}
