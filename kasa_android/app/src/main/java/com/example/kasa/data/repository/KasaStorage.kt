package com.example.kasa.data.repository

import android.content.Context
import android.content.SharedPreferences
import com.example.kasa.data.model.*
import org.json.JSONArray
import org.json.JSONObject
import java.util.Date
import java.util.UUID

class KasaStorage(context: Context) {
    private val prefs: SharedPreferences = context.getSharedPreferences("kasa_prefs", Context.MODE_PRIVATE)

    companion object {
        private const val KEY_IS_INITIALIZED = "is_initialized"
        private const val KEY_USER = "user_json"
        private const val KEY_HOUSEHOLD = "household_json"
        private const val KEY_BUDGET = "budget_json"
        private const val KEY_CATEGORIES = "categories_json"
        private const val KEY_EXPENSES = "expenses_json"
        private const val KEY_PROVISIONS = "provisions_json"
        private const val KEY_MESSAGES = "messages_json"
        private const val KEY_POLLS = "polls_json"
        private const val KEY_TASKS = "tasks_json"
        private const val KEY_EVENTS = "events_json"
        private const val KEY_MEMBERS = "members_json"
        private const val KEY_DELETED_EXPENSES = "deleted_expenses_set"
        private const val KEY_DELETED_PROVISIONS = "deleted_provisions_set"
    }

    fun isInitialized(): Boolean = prefs.getBoolean(KEY_IS_INITIALIZED, false)

    fun saveAll(
        user: UserModel?,
        household: HouseholdModel?,
        budget: MonthlyBudgetModel,
        categories: List<CategoryModel>,
        expenses: List<ExpenseModel>,
        provisions: List<ProvisionItemModel>,
        messages: List<MessageModel>,
        polls: List<PollModel>,
        tasks: List<TaskModel> = emptyList(),
        events: List<EventModel> = emptyList(),
        members: List<UserModel> = emptyList()
    ) {
        val editor = prefs.edit()
        editor.putBoolean(KEY_IS_INITIALIZED, household != null)

        user?.let {
            val json = JSONObject().apply {
                put("id", it.id)
                put("name", it.name)
                put("email", it.email)
                put("role", it.role)
                put("householdId", it.householdId)
                if (it.latitude != null) put("latitude", it.latitude)
                if (it.longitude != null) put("longitude", it.longitude)
                put("locationName", it.locationName)
                put("isHome", it.isHome)
                if (it.distanceFromHomeMeters != null) put("distanceFromHomeMeters", it.distanceFromHomeMeters)
                put("batteryLevel", it.batteryLevel)
                put("isCharging", it.isCharging)
                put("deviceModel", it.deviceModel)
                put("trackingEnabled", it.trackingEnabled)
            }
            editor.putString(KEY_USER, json.toString())
        } ?: editor.remove(KEY_USER)

        household?.let {
            val json = JSONObject().apply {
                put("id", it.id)
                put("name", it.name)
                put("inviteCode", it.inviteCode)
                put("currency", it.currency)
                put("secondaryCurrency", it.secondaryCurrency)
                put("exchangeRate", it.exchangeRate)
                put("ownerId", it.ownerId)
                put("monthlyBudget", it.monthlyBudget)
                put("appIcon", it.appIcon)
                if (it.homeLatitude != null) put("homeLatitude", it.homeLatitude)
                if (it.homeLongitude != null) put("homeLongitude", it.homeLongitude)
                put("homeAddress", it.homeAddress)
                put("homeRadiusMeters", it.homeRadiusMeters)
            }
            editor.putString(KEY_HOUSEHOLD, json.toString())
        } ?: editor.remove(KEY_HOUSEHOLD)

        // Members Cache
        val membersArray = JSONArray()
        members.forEach { m ->
            val mObj = JSONObject().apply {
                put("id", m.id)
                put("name", m.name)
                put("email", m.email)
                put("role", m.role)
                put("householdId", m.householdId)
                if (m.latitude != null) put("latitude", m.latitude)
                if (m.longitude != null) put("longitude", m.longitude)
                put("locationName", m.locationName)
                put("isHome", m.isHome)
                if (m.distanceFromHomeMeters != null) put("distanceFromHomeMeters", m.distanceFromHomeMeters)
                put("batteryLevel", m.batteryLevel)
                put("isCharging", m.isCharging)
                put("deviceModel", m.deviceModel)
                put("trackingEnabled", m.trackingEnabled)
            }
            membersArray.put(mObj)
        }
        editor.putString(KEY_MEMBERS, membersArray.toString())

        // Budget
        val budgetJson = JSONObject().apply {
            put("id", budget.id)
            put("householdId", budget.householdId)
            put("monthKey", budget.monthKey)
            put("totalBudget", budget.totalBudget)
            put("lastModifiedBy", budget.lastModifiedBy)
        }
        editor.putString(KEY_BUDGET, budgetJson.toString())

        // Categories
        val catArray = JSONArray()
        categories.forEach {
            val obj = JSONObject().apply {
                put("id", it.id)
                put("householdId", it.householdId)
                put("name", it.name)
                put("emoji", it.emoji)
                put("plannedAmount", it.plannedAmount)
                put("colorHex", it.colorHex)
                put("isProvision", it.isProvision)
            }
            catArray.put(obj)
        }
        editor.putString(KEY_CATEGORIES, catArray.toString())

        // Expenses
        val expArray = JSONArray()
        expenses.forEach {
            val obj = JSONObject().apply {
                put("id", it.id)
                put("householdId", it.householdId)
                put("userId", it.userId)
                put("userName", it.userName)
                put("categoryId", it.categoryId)
                put("categoryName", it.categoryName)
                put("categoryEmoji", it.categoryEmoji)
                put("amount", it.amount)
                put("description", it.description)
                put("comment", it.comment)
                put("lastModifiedByName", it.lastModifiedByName ?: JSONObject.NULL)
                put("timestamp", it.date.time)
            }
            expArray.put(obj)
        }
        editor.putString(KEY_EXPENSES, expArray.toString())

        // Provisions
        val provArray = JSONArray()
        provisions.forEach {
            val obj = JSONObject().apply {
                put("id", it.id)
                put("householdId", it.householdId)
                put("name", it.name)
                put("quantity", it.quantity)
                put("unit", it.unit)
                put("plannedPrice", it.plannedPrice)
                put("actualPrice", it.actualPrice ?: JSONObject.NULL)
                put("status", it.status)
                put("priority", it.priority)
                put("comment", it.comment)
                put("addedBy", it.addedBy)
                put("lastModifiedBy", it.lastModifiedBy ?: JSONObject.NULL)
                put("purchasedBy", it.purchasedBy ?: JSONObject.NULL)
                put("purchasedAt", it.purchasedAt?.time ?: JSONObject.NULL)
            }
            provArray.put(obj)
        }
        editor.putString(KEY_PROVISIONS, provArray.toString())

        // Messages
        val msgArray = JSONArray()
        messages.forEach {
            val obj = JSONObject().apply {
                put("id", it.id)
                put("householdId", it.householdId)
                put("userId", it.userId)
                put("userName", it.userName)
                put("text", it.text)
                put("type", it.type)
                put("replyToMessageId", it.replyToMessageId ?: "")
                put("replyToUserName", it.replyToUserName ?: "")
                put("replyToText", it.replyToText ?: "")
                put("isPinned", it.isPinned)
                val reactObj = JSONObject()
                it.reactions.forEach { (emoji, userIds) ->
                    val arr = JSONArray()
                    userIds.forEach { uid -> arr.put(uid) }
                    reactObj.put(emoji, arr)
                }
                put("reactions", reactObj)
                put("timestamp", it.createdAt.time)
            }
            msgArray.put(obj)
        }
        editor.putString(KEY_MESSAGES, msgArray.toString())

        // Polls
        val pollArray = JSONArray()
        polls.forEach { p ->
            val pObj = JSONObject().apply {
                put("id", p.id)
                put("householdId", p.householdId)
                put("creatorId", p.creatorId)
                put("creatorName", p.creatorName)
                put("question", p.question)
                put("isClosed", p.isClosed)
                put("createdAt", p.createdAt.time)

                val optArray = JSONArray()
                p.options.forEach { opt ->
                    val optObj = JSONObject().apply {
                        put("id", opt.id)
                        put("text", opt.text)
                        val voters = JSONArray()
                        opt.voterIds.forEach { voters.put(it) }
                        put("voters", voters)
                    }
                    optArray.put(optObj)
                }
                put("options", optArray)
            }
            pollArray.put(pObj)
        }
        editor.putString(KEY_POLLS, pollArray.toString())

        // Tasks
        val taskArray = JSONArray()
        tasks.forEach {
            val tObj = JSONObject().apply {
                put("id", it.id)
                put("householdId", it.householdId)
                put("title", it.title)
                put("description", it.description)
                put("assignedMemberId", it.assignedMemberId)
                put("assignedMemberName", it.assignedMemberName)
                put("dueDate", it.dueDate?.time ?: -1L)
                put("recurrence", it.recurrence)
                put("status", it.status)
                put("points", it.points)
                put("completedAt", it.completedAt?.time ?: -1L)
                put("completedByMemberId", it.completedByMemberId)
                put("completedByMemberName", it.completedByMemberName)
                put("createdAt", it.createdAt.time)
            }
            taskArray.put(tObj)
        }
        editor.putString(KEY_TASKS, taskArray.toString())

        // Events
        val eventArray = JSONArray()
        events.forEach {
            val eObj = JSONObject().apply {
                put("id", it.id)
                put("householdId", it.householdId)
                put("title", it.title)
                put("description", it.description)
                put("location", it.location)
                put("startDate", it.startDate.time)
                put("endDate", it.endDate?.time ?: -1L)
                put("category", it.category)
                put("categoryEmoji", it.categoryEmoji)
                val partArr = JSONArray()
                it.participantMemberIds.forEach { pid -> partArr.put(pid) }
                put("participantMemberIds", partArr)
                put("createdById", it.createdById)
                put("createdByName", it.createdByName)
                put("createdAt", it.createdAt.time)
            }
            eventArray.put(eObj)
        }
        editor.putString(KEY_EVENTS, eventArray.toString())

        editor.apply()
    }

    fun loadUser(): UserModel? {
        val str = prefs.getString(KEY_USER, null) ?: return null
        return try {
            val json = JSONObject(str)
            val hasLat = json.has("latitude") && !json.isNull("latitude")
            val hasLng = json.has("longitude") && !json.isNull("longitude")
            val hasDist = json.has("distanceFromHomeMeters") && !json.isNull("distanceFromHomeMeters")
            UserModel(
                id = json.optString("id", "u1"),
                name = json.optString("name", "Mon Profil"),
                email = json.optString("email", ""),
                role = json.optString("role", "admin"),
                householdId = json.optString("householdId", ""),
                latitude = if (hasLat) json.optDouble("latitude") else null,
                longitude = if (hasLng) json.optDouble("longitude") else null,
                locationName = json.optString("locationName", ""),
                isHome = json.optBoolean("isHome", false),
                distanceFromHomeMeters = if (hasDist) json.optDouble("distanceFromHomeMeters") else null,
                batteryLevel = json.optInt("batteryLevel", 100),
                isCharging = json.optBoolean("isCharging", false),
                deviceModel = json.optString("deviceModel", ""),
                trackingEnabled = json.optBoolean("trackingEnabled", false)
            )
        } catch (e: Exception) {
            null
        }
    }

    fun loadMembers(): List<UserModel> {
        val str = prefs.getString(KEY_MEMBERS, null) ?: return emptyList()
        return try {
            val array = JSONArray(str)
            val list = mutableListOf<UserModel>()
            for (i in 0 until array.length()) {
                val json = array.getJSONObject(i)
                val hasLat = json.has("latitude") && !json.isNull("latitude")
                val hasLng = json.has("longitude") && !json.isNull("longitude")
                val hasDist = json.has("distanceFromHomeMeters") && !json.isNull("distanceFromHomeMeters")
                list.add(
                    UserModel(
                        id = json.optString("id", "u_$i"),
                        name = json.optString("name", "Membre"),
                        email = json.optString("email", ""),
                        role = json.optString("role", "member"),
                        householdId = json.optString("householdId", ""),
                        latitude = if (hasLat) json.optDouble("latitude") else null,
                        longitude = if (hasLng) json.optDouble("longitude") else null,
                        locationName = json.optString("locationName", ""),
                        isHome = json.optBoolean("isHome", false),
                        distanceFromHomeMeters = if (hasDist) json.optDouble("distanceFromHomeMeters") else null,
                        batteryLevel = json.optInt("batteryLevel", 100),
                        isCharging = json.optBoolean("isCharging", false),
                        deviceModel = json.optString("deviceModel", ""),
                        trackingEnabled = json.optBoolean("trackingEnabled", false)
                    )
                )
            }
            list
        } catch (e: Exception) {
            emptyList()
        }
    }

    fun loadHousehold(): HouseholdModel? {
        val str = prefs.getString(KEY_HOUSEHOLD, null) ?: return null
        return try {
            val json = JSONObject(str)
            val hasLat = json.has("homeLatitude") && !json.isNull("homeLatitude")
            val hasLng = json.has("homeLongitude") && !json.isNull("homeLongitude")
            HouseholdModel(
                id = json.optString("id", "hh_1"),
                name = json.optString("name", "Mon Foyer"),
                inviteCode = json.optString("inviteCode", "8F3A2B"),
                currency = json.optString("currency", "USD"),
                secondaryCurrency = json.optString("secondaryCurrency", "CDF"),
                exchangeRate = json.optDouble("exchangeRate", 2250.0),
                ownerId = json.optString("ownerId", "u1"),
                homeLatitude = if (hasLat) json.optDouble("homeLatitude") else null,
                homeLongitude = if (hasLng) json.optDouble("homeLongitude") else null,
                homeAddress = json.optString("homeAddress", ""),
                homeRadiusMeters = json.optDouble("homeRadiusMeters", 150.0),
                monthlyBudget = json.optDouble("monthlyBudget", 0.0),
                appIcon = json.optString("appIcon", "default")
            )
        } catch (e: Exception) {
            null
        }
    }

    fun loadBudget(): MonthlyBudgetModel {
        val str = prefs.getString(KEY_BUDGET, null) ?: return MonthlyBudgetModel(totalBudget = 0.0)
        return try {
            val json = JSONObject(str)
            MonthlyBudgetModel(
                id = json.optString("id", "b_1"),
                householdId = json.optString("householdId", "hh_1"),
                monthKey = json.optString("monthKey", "2026-08"),
                totalBudget = json.optDouble("totalBudget", 0.0),
                lastModifiedBy = json.optString("lastModifiedBy", "")
            )
        } catch (e: Exception) {
            MonthlyBudgetModel(totalBudget = 0.0)
        }
    }

    fun loadCategories(): List<CategoryModel> {
        val str = prefs.getString(KEY_CATEGORIES, null) ?: return listOf(
            CategoryModel(id = "c_loyer", name = "Loyer", emoji = "🏠", plannedAmount = 0.0),
            CategoryModel(id = "c_connexion", name = "Connexion", emoji = "🌐", plannedAmount = 0.0),
            CategoryModel(id = "c_provisions", name = "Provisions", emoji = "🛒", plannedAmount = 0.0, isProvision = true)
        )
        return try {
            val array = JSONArray(str)
            val list = mutableListOf<CategoryModel>()
            for (i in 0 until array.length()) {
                val obj = array.getJSONObject(i)
                list.add(
                    CategoryModel(
                        id = obj.optString("id", "c_$i"),
                        householdId = obj.optString("householdId", "hh_1"),
                        name = obj.optString("name", "Catégorie"),
                        emoji = obj.optString("emoji", "📦"),
                        plannedAmount = obj.optDouble("plannedAmount", 0.0),
                        colorHex = obj.optString("colorHex", "#10B981"),
                        isProvision = obj.optBoolean("isProvision", false)
                    )
                )
            }
            list
        } catch (e: Exception) {
            emptyList()
        }
    }

    fun loadExpenses(): List<ExpenseModel> {
        val str = prefs.getString(KEY_EXPENSES, null) ?: return emptyList()
        return try {
            val array = JSONArray(str)
            val list = mutableListOf<ExpenseModel>()
            for (i in 0 until array.length()) {
                val obj = array.getJSONObject(i)
                list.add(
                    ExpenseModel(
                        id = obj.optString("id", "e_$i"),
                        householdId = obj.optString("householdId", "hh_1"),
                        userId = obj.optString("userId", "u1"),
                        userName = obj.optString("userName", "Moi"),
                        categoryId = obj.optString("categoryId", "c1"),
                        categoryName = obj.optString("categoryName", "Autre"),
                        categoryEmoji = obj.optString("categoryEmoji", "📦"),
                        amount = obj.optDouble("amount", 0.0),
                        description = obj.optString("description", ""),
                        comment = obj.optString("comment", ""),
                        lastModifiedByName = if (obj.isNull("lastModifiedByName")) null else obj.optString("lastModifiedByName"),
                        date = Date(obj.optLong("timestamp", System.currentTimeMillis()))
                    )
                )
            }
            list
        } catch (e: Exception) {
            emptyList()
        }
    }

    fun loadProvisions(): List<ProvisionItemModel> {
        val str = prefs.getString(KEY_PROVISIONS, null) ?: return emptyList()
        return try {
            val array = JSONArray(str)
            val list = mutableListOf<ProvisionItemModel>()
            for (i in 0 until array.length()) {
                val obj = array.getJSONObject(i)
                list.add(
                    ProvisionItemModel(
                        id = obj.optString("id", "p_$i"),
                        householdId = obj.optString("householdId", "hh_1"),
                        name = obj.optString("name", ""),
                        quantity = obj.optInt("quantity", 1),
                        unit = obj.optString("unit", "pièce"),
                        plannedPrice = obj.optDouble("plannedPrice", 0.0),
                        actualPrice = if (obj.isNull("actualPrice")) null else obj.optDouble("actualPrice"),
                        status = obj.optString("status", "planned"),
                        priority = obj.optString("priority", "medium"),
                        comment = obj.optString("comment", ""),
                        addedBy = obj.optString("addedBy", ""),
                        lastModifiedBy = if (obj.isNull("lastModifiedBy")) null else obj.optString("lastModifiedBy"),
                        purchasedBy = if (obj.isNull("purchasedBy")) null else obj.optString("purchasedBy"),
                        purchasedAt = if (obj.isNull("purchasedAt")) null else Date(obj.optLong("purchasedAt"))
                    )
                )
            }
            list
        } catch (e: Exception) {
            emptyList()
        }
    }

    fun loadMessages(): List<MessageModel> {
        val str = prefs.getString(KEY_MESSAGES, null) ?: return emptyList()
        return try {
            val array = JSONArray(str)
            val list = mutableListOf<MessageModel>()
            for (i in 0 until array.length()) {
                val obj = array.getJSONObject(i)
                val reactionsMap = mutableMapOf<String, List<String>>()
                val reactObj = obj.optJSONObject("reactions")
                if (reactObj != null) {
                    val keys = reactObj.keys()
                    while (keys.hasNext()) {
                        val key = keys.next()
                        val uArr = reactObj.optJSONArray(key)
                        if (uArr != null) {
                            val uList = mutableListOf<String>()
                            for (k in 0 until uArr.length()) {
                                uList.add(uArr.getString(k))
                            }
                            reactionsMap[key] = uList
                        }
                    }
                }
                list.add(
                    MessageModel(
                        id = obj.optString("id", "m_$i"),
                        householdId = obj.optString("householdId", "hh_1"),
                        userId = obj.optString("userId", "u1"),
                        userName = obj.optString("userName", "Moi"),
                        text = obj.optString("text", ""),
                        type = obj.optString("type", "text"),
                        replyToMessageId = obj.optString("replyToMessageId").takeIf { it.isNotEmpty() },
                        replyToUserName = obj.optString("replyToUserName").takeIf { it.isNotEmpty() },
                        replyToText = obj.optString("replyToText").takeIf { it.isNotEmpty() },
                        isPinned = obj.optBoolean("isPinned", false),
                        reactions = reactionsMap,
                        createdAt = Date(obj.optLong("timestamp", System.currentTimeMillis()))
                    )
                )
            }
            list
        } catch (e: Exception) {
            emptyList()
        }
    }

    fun loadPolls(): List<PollModel> {
        val str = prefs.getString(KEY_POLLS, null) ?: return emptyList()
        return try {
            val array = JSONArray(str)
            val list = mutableListOf<PollModel>()
            for (i in 0 until array.length()) {
                val obj = array.getJSONObject(i)
                val optArray = obj.optJSONArray("options") ?: JSONArray()
                val options = mutableListOf<PollOption>()
                for (j in 0 until optArray.length()) {
                    val optObj = optArray.getJSONObject(j)
                    val vArray = optObj.optJSONArray("voters") ?: JSONArray()
                    val voters = mutableListOf<String>()
                    for (k in 0 until vArray.length()) {
                        voters.add(vArray.getString(k))
                    }
                    options.add(
                        PollOption(
                            id = optObj.optString("id", "opt_$j"),
                            text = optObj.optString("text", ""),
                            voterIds = voters
                        )
                    )
                }
                list.add(
                    PollModel(
                        id = obj.optString("id", "poll_$i"),
                        householdId = obj.optString("householdId", "hh_1"),
                        creatorId = obj.optString("creatorId", "u1"),
                        creatorName = obj.optString("creatorName", "Moi"),
                        question = obj.optString("question", ""),
                        options = options,
                        isClosed = obj.optBoolean("isClosed", false),
                        createdAt = Date(obj.optLong("createdAt", System.currentTimeMillis()))
                    )
                )
            }
            list
        } catch (e: Exception) {
            emptyList()
        }
    }

    fun loadTasks(): List<TaskModel> {
        val str = prefs.getString(KEY_TASKS, null) ?: return emptyList()
        return try {
            val array = JSONArray(str)
            val list = mutableListOf<TaskModel>()
            for (i in 0 until array.length()) {
                val obj = array.getJSONObject(i)
                val due = obj.optLong("dueDate", -1L)
                val comp = obj.optLong("completedAt", -1L)
                list.add(
                    TaskModel(
                        id = obj.optString("id", "t_$i"),
                        householdId = obj.optString("householdId", "hh_1"),
                        title = obj.optString("title", ""),
                        description = obj.optString("description", ""),
                        assignedMemberId = obj.optString("assignedMemberId", ""),
                        assignedMemberName = obj.optString("assignedMemberName", ""),
                        dueDate = if (due > 0) Date(due) else null,
                        recurrence = obj.optString("recurrence", "none"),
                        status = obj.optString("status", "todo"),
                        points = obj.optInt("points", 10),
                        completedAt = if (comp > 0) Date(comp) else null,
                        completedByMemberId = obj.optString("completedByMemberId", ""),
                        completedByMemberName = obj.optString("completedByMemberName", ""),
                        createdAt = Date(obj.optLong("createdAt", System.currentTimeMillis()))
                    )
                )
            }
            list
        } catch (e: Exception) {
            emptyList()
        }
    }

    fun loadEvents(): List<EventModel> {
        val str = prefs.getString(KEY_EVENTS, null) ?: return emptyList()
        return try {
            val array = JSONArray(str)
            val list = mutableListOf<EventModel>()
            for (i in 0 until array.length()) {
                val obj = array.getJSONObject(i)
                val endD = obj.optLong("endDate", -1L)
                val pArr = obj.optJSONArray("participantMemberIds") ?: JSONArray()
                val participants = mutableListOf<String>()
                for (k in 0 until pArr.length()) {
                    participants.add(pArr.getString(k))
                }
                list.add(
                    EventModel(
                        id = obj.optString("id", "e_$i"),
                        householdId = obj.optString("householdId", "hh_1"),
                        title = obj.optString("title", ""),
                        description = obj.optString("description", ""),
                        location = obj.optString("location", ""),
                        startDate = Date(obj.optLong("startDate", System.currentTimeMillis())),
                        endDate = if (endD > 0) Date(endD) else null,
                        category = obj.optString("category", "general"),
                        categoryEmoji = obj.optString("categoryEmoji", "📌"),
                        participantMemberIds = participants,
                        createdById = obj.optString("createdById", "u1"),
                        createdByName = obj.optString("createdByName", "Moi"),
                        createdAt = Date(obj.optLong("createdAt", System.currentTimeMillis()))
                    )
                )
            }
            list
        } catch (e: Exception) {
            emptyList()
        }
    }

    fun getThemeMode(): String = prefs.getString("theme_mode", "SYSTEM") ?: "SYSTEM"
    fun setThemeMode(mode: String) { prefs.edit().putString("theme_mode", mode).apply() }

    fun getAestheticTheme(): String = prefs.getString("aesthetic_theme", "EMERALD") ?: "EMERALD"
    fun setAestheticTheme(theme: String) { prefs.edit().putString("aesthetic_theme", theme).apply() }

    fun getChatWallpaper(): String = prefs.getString("chat_wallpaper", "MINIMAL") ?: "MINIMAL"
    fun setChatWallpaper(wallpaper: String) { prefs.edit().putString("chat_wallpaper", wallpaper).apply() }

    fun getCornerStyle(): String = prefs.getString("corner_style", "ROUNDED") ?: "ROUNDED"
    fun setCornerStyle(style: String) { prefs.edit().putString("corner_style", style).apply() }

    fun saveDeletedExpenseId(id: String) {
        val set = getDeletedExpenseIds().toMutableSet()
        set.add(id)
        prefs.edit().putStringSet(KEY_DELETED_EXPENSES, set).apply()
    }

    fun getDeletedExpenseIds(): Set<String> {
        return prefs.getStringSet(KEY_DELETED_EXPENSES, emptySet()) ?: emptySet()
    }

    fun saveDeletedProvisionId(id: String) {
        val set = getDeletedProvisionIds().toMutableSet()
        set.add(id)
        prefs.edit().putStringSet(KEY_DELETED_PROVISIONS, set).apply()
    }

    fun getDeletedProvisionIds(): Set<String> {
        return prefs.getStringSet(KEY_DELETED_PROVISIONS, emptySet()) ?: emptySet()
    }

    fun saveMealPlan(plan: MealPlanModel) {
        val obj = JSONObject().apply {
            put("id", plan.id)
            put("householdId", plan.householdId)
            put("date", plan.date)
            put("lunchTitle", plan.lunchTitle)
            put("lunchDetails", plan.lunchDetails)
            put("dinnerTitle", plan.dinnerTitle)
            put("dinnerDetails", plan.dinnerDetails)
            if (plan.chefUserId != null) put("chefUserId", plan.chefUserId)
            if (plan.chefUserName != null) put("chefUserName", plan.chefUserName)
            put("specialNote", plan.specialNote)
            put("attendeesDinnerYes", JSONArray(plan.attendeesDinnerYes))
            put("attendeesDinnerNo", JSONArray(plan.attendeesDinnerNo))
            put("lastModifiedByName", plan.lastModifiedByName)
            put("lastModifiedAt", plan.lastModifiedAt.time)
        }
        prefs.edit().putString("meal_plan_json", obj.toString()).apply()
    }

    fun loadMealPlan(): MealPlanModel? {
        val raw = prefs.getString("meal_plan_json", null) ?: return null
        return try {
            val obj = JSONObject(raw)
            val yesArr = obj.optJSONArray("attendeesDinnerYes") ?: JSONArray()
            val noArr = obj.optJSONArray("attendeesDinnerNo") ?: JSONArray()
            val yesList = mutableListOf<String>()
            val noList = mutableListOf<String>()
            for (i in 0 until yesArr.length()) yesList.add(yesArr.getString(i))
            for (i in 0 until noArr.length()) noList.add(noArr.getString(i))

            MealPlanModel(
                id = obj.optString("id", "meal_today"),
                householdId = obj.optString("householdId", ""),
                date = obj.optString("date", ""),
                lunchTitle = obj.optString("lunchTitle", ""),
                lunchDetails = obj.optString("lunchDetails", ""),
                dinnerTitle = obj.optString("dinnerTitle", ""),
                dinnerDetails = obj.optString("dinnerDetails", ""),
                chefUserId = if (obj.has("chefUserId")) obj.optString("chefUserId") else null,
                chefUserName = if (obj.has("chefUserName")) obj.optString("chefUserName") else null,
                specialNote = obj.optString("specialNote", ""),
                attendeesDinnerYes = yesList,
                attendeesDinnerNo = noList,
                lastModifiedByName = obj.optString("lastModifiedByName", ""),
                lastModifiedAt = Date(obj.optLong("lastModifiedAt", System.currentTimeMillis()))
            )
        } catch (e: Exception) {
            null
        }
    }

    fun saveMealNotes(notes: List<MealNoteModel>) {
        val arr = JSONArray()
        for (n in notes) {
            val obj = JSONObject().apply {
                put("id", n.id)
                put("householdId", n.householdId)
                put("mealTitle", n.mealTitle)
                put("noteDetails", n.noteDetails)
                put("suggestedByUserId", n.suggestedByUserId)
                put("suggestedByUserName", n.suggestedByUserName)
                put("createdAt", n.createdAt.time)
                val upvoters = JSONArray()
                n.upvoterUserIds.forEach { upvoters.put(it) }
                put("upvoterUserIds", upvoters)
                put("status", n.status)
            }
            arr.put(obj)
        }
        prefs.edit().putString("meal_notes_json", arr.toString()).apply()
    }

    fun loadMealNotes(): List<MealNoteModel> {
        val raw = prefs.getString("meal_notes_json", null) ?: return emptyList()
        return try {
            val arr = JSONArray(raw)
            val list = mutableListOf<MealNoteModel>()
            for (i in 0 until arr.length()) {
                val obj = arr.getJSONObject(i)
                val upArr = obj.optJSONArray("upvoterUserIds") ?: JSONArray()
                val upList = mutableListOf<String>()
                for (j in 0 until upArr.length()) upList.add(upArr.getString(j))

                list.add(
                    MealNoteModel(
                        id = obj.optString("id", UUID.randomUUID().toString()),
                        householdId = obj.optString("householdId", ""),
                        mealTitle = obj.optString("mealTitle", ""),
                        noteDetails = obj.optString("noteDetails", ""),
                        suggestedByUserId = obj.optString("suggestedByUserId", ""),
                        suggestedByUserName = obj.optString("suggestedByUserName", ""),
                        createdAt = Date(obj.optLong("createdAt", System.currentTimeMillis())),
                        upvoterUserIds = upList,
                        status = obj.optString("status", "ACTIVE")
                    )
                )
            }
            list
        } catch (e: Exception) {
            emptyList()
        }
    }

    fun savePostIts(notes: List<PostItModel>) {
        val arr = JSONArray()
        for (n in notes) {
            val obj = JSONObject().apply {
                put("id", n.id)
                put("householdId", n.householdId)
                put("text", n.text)
                put("colorHex", n.colorHex)
                put("emoji", n.emoji)
                put("authorId", n.authorId)
                put("authorName", n.authorName)
                put("createdAt", n.createdAt.time)
                put("isPinned", n.isPinned)
            }
            arr.put(obj)
        }
        prefs.edit().putString("post_its_json", arr.toString()).apply()
    }

    fun loadPostIts(): List<PostItModel> {
        val raw = prefs.getString("post_its_json", null) ?: return emptyList()
        return try {
            val arr = JSONArray(raw)
            val list = mutableListOf<PostItModel>()
            for (i in 0 until arr.length()) {
                val obj = arr.getJSONObject(i)
                list.add(
                    PostItModel(
                        id = obj.optString("id", UUID.randomUUID().toString()),
                        householdId = obj.optString("householdId", ""),
                        text = obj.optString("text", ""),
                        colorHex = obj.optString("colorHex", "#FEF08A"),
                        emoji = obj.optString("emoji", "📌"),
                        authorId = obj.optString("authorId", ""),
                        authorName = obj.optString("authorName", ""),
                        createdAt = Date(obj.optLong("createdAt", System.currentTimeMillis())),
                        isPinned = obj.optBoolean("isPinned", false)
                    )
                )
            }
            list
        } catch (e: Exception) {
            emptyList()
        }
    }

    fun saveHouseEssentialInfo(info: HouseEssentialInfo) {
        val rulesArr = JSONArray()
        info.houseRules.forEach { rulesArr.put(it) }
        val obj = JSONObject().apply {
            put("wifiSsid", info.wifiSsid)
            put("wifiPassword", info.wifiPassword)
            put("gateCode", info.gateCode)
            put("buildingCode", info.buildingCode)
            put("landlordName", info.landlordName)
            put("landlordPhone", info.landlordPhone)
            put("emergencyNotes", info.emergencyNotes)
            put("houseRules", rulesArr)
        }
        prefs.edit().putString("house_essential_info_json", obj.toString()).apply()
    }

    fun loadHouseEssentialInfo(): HouseEssentialInfo {
        val raw = prefs.getString("house_essential_info_json", null) ?: return HouseEssentialInfo()
        return try {
            val obj = JSONObject(raw)
            val rulesArr = obj.optJSONArray("houseRules") ?: JSONArray()
            val rulesList = mutableListOf<String>()
            for (i in 0 until rulesArr.length()) rulesList.add(rulesArr.getString(i))
            HouseEssentialInfo(
                wifiSsid = obj.optString("wifiSsid", ""),
                wifiPassword = obj.optString("wifiPassword", ""),
                gateCode = obj.optString("gateCode", ""),
                buildingCode = obj.optString("buildingCode", ""),
                landlordName = obj.optString("landlordName", ""),
                landlordPhone = obj.optString("landlordPhone", ""),
                emergencyNotes = obj.optString("emergencyNotes", ""),
                houseRules = rulesList
            )
        } catch (e: Exception) {
            HouseEssentialInfo()
        }
    }

    fun saveActiveGuestEvent(event: GuestPartyEvent?) {
        if (event == null) {
            prefs.edit().remove("active_guest_event_json").apply()
            return
        }
        val obj = JSONObject().apply {
            put("id", event.id)
            put("householdId", event.householdId)
            put("hostUserId", event.hostUserId)
            put("hostUserName", event.hostUserName)
            put("guestCount", event.guestCount)
            put("description", event.description)
            put("startTime", event.startTime.time)
            if (event.endTime != null) put("endTime", event.endTime.time)
            put("isActive", event.isActive)
        }
        prefs.edit().putString("active_guest_event_json", obj.toString()).apply()
    }

    fun loadActiveGuestEvent(): GuestPartyEvent? {
        val raw = prefs.getString("active_guest_event_json", null) ?: return null
        return try {
            val obj = JSONObject(raw)
            val endTs = if (obj.has("endTime")) obj.optLong("endTime") else null
            GuestPartyEvent(
                id = obj.optString("id", UUID.randomUUID().toString()),
                householdId = obj.optString("householdId", ""),
                hostUserId = obj.optString("hostUserId", ""),
                hostUserName = obj.optString("hostUserName", ""),
                guestCount = obj.optInt("guestCount", 2),
                description = obj.optString("description", ""),
                startTime = Date(obj.optLong("startTime", System.currentTimeMillis())),
                endTime = endTs?.let { Date(it) },
                isActive = obj.optBoolean("isActive", true)
            )
        } catch (e: Exception) {
            null
        }
    }

    fun saveScheduledReminders(reminders: List<ScheduledReminderModel>) {
        val arr = JSONArray()
        for (r in reminders) {
            val obj = JSONObject().apply {
                put("id", r.id)
                put("householdId", r.householdId)
                put("creatorId", r.creatorId)
                put("creatorName", r.creatorName)
                put("message", r.message)
                put("triggerTimestamp", r.triggerTimestamp)
                put("isSent", r.isSent)
                put("createdAt", r.createdAt.time)
            }
            arr.put(obj)
        }
        prefs.edit().putString("scheduled_reminders_json", arr.toString()).apply()
    }

    fun loadScheduledReminders(): List<ScheduledReminderModel> {
        val raw = prefs.getString("scheduled_reminders_json", null) ?: return emptyList()
        return try {
            val arr = JSONArray(raw)
            val list = mutableListOf<ScheduledReminderModel>()
            for (i in 0 until arr.length()) {
                val obj = arr.getJSONObject(i)
                list.add(
                    ScheduledReminderModel(
                        id = obj.optString("id", UUID.randomUUID().toString()),
                        householdId = obj.optString("householdId", ""),
                        creatorId = obj.optString("creatorId", ""),
                        creatorName = obj.optString("creatorName", ""),
                        message = obj.optString("message", ""),
                        triggerTimestamp = obj.optLong("triggerTimestamp", System.currentTimeMillis()),
                        isSent = obj.optBoolean("isSent", false),
                        createdAt = Date(obj.optLong("createdAt", System.currentTimeMillis()))
                    )
                )
            }
            list
        } catch (e: Exception) {
            emptyList()
        }
    }

    fun clear() {
        prefs.edit().clear().apply()
    }
}
