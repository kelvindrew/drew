package com.example.kasa.data.repository

import com.example.kasa.data.model.*
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.ListenerRegistration
import com.google.firebase.firestore.SetOptions
import java.util.Date
import java.util.UUID

object FirebaseManager {
    private const val TAG = "KasaFirebase"
    private var db: FirebaseFirestore? = null
    private var auth: FirebaseAuth? = null

    private var householdListener: ListenerRegistration? = null
    private var budgetListener: ListenerRegistration? = null
    private var categoriesListener: ListenerRegistration? = null
    private var expensesListener: ListenerRegistration? = null
    private var provisionsListener: ListenerRegistration? = null
    private var messagesListener: ListenerRegistration? = null
    private var pollsListener: ListenerRegistration? = null
    private var membersListener: ListenerRegistration? = null
    private var adminNotificationsListener: ListenerRegistration? = null
    private var tasksListener: ListenerRegistration? = null
    private var eventsListener: ListenerRegistration? = null
    private var mealNotesListener: ListenerRegistration? = null
    private var postItsListener: ListenerRegistration? = null
    private var houseEssentialsListener: ListenerRegistration? = null
    private var guestEventsListener: ListenerRegistration? = null

    private fun logD(tag: String, msg: String) {
        try {
            android.util.Log.d(tag, msg)
        } catch (_: Throwable) {
            println("[$tag] $msg")
        }
    }

    private fun logE(tag: String, msg: String, tr: Throwable? = null) {
        try {
            android.util.Log.e(tag, msg, tr)
        } catch (_: Throwable) {
            println("[$tag ERROR] $msg: ${tr?.message}")
        }
    }

    private fun logW(tag: String, msg: String) {
        try {
            android.util.Log.w(tag, msg)
        } catch (_: Throwable) {
            println("[$tag WARN] $msg")
        }
    }

    fun initAuth(onReady: () -> Unit = {}) {
        try {
            auth = FirebaseAuth.getInstance()
            val currentAuthUser = auth?.currentUser
            if (currentAuthUser == null) {
                logD(TAG, "Signing in anonymously...")
                auth?.signInAnonymously()
                    ?.addOnSuccessListener { result ->
                        logD(TAG, "Anonymous sign-in success: ${result.user?.uid}")
                        onReady()
                    }
                    ?.addOnFailureListener { e ->
                        logE(TAG, "Anonymous sign-in failed: ${e.message}", e)
                        onReady()
                    }
            } else {
                logD(TAG, "Already authenticated as: ${currentAuthUser.uid}")
                onReady()
            }
        } catch (t: Throwable) {
            logE(TAG, "Auth init error: ${t.message}", t)
            onReady()
        }
    }

    fun getDb(): FirebaseFirestore? {
        if (db == null) {
            db = try {
                val f = FirebaseFirestore.getInstance()
                try {
                    val settings = com.google.firebase.firestore.FirebaseFirestoreSettings.Builder()
                        .setPersistenceEnabled(true)
                        .build()
                    f.firestoreSettings = settings
                } catch (ignored: Throwable) {
                    // Settings might already be configured
                }
                f
            } catch (e: Throwable) {
                logE(TAG, "Firestore getInstance error: ${e.message}", e)
                null
            }
        }
        return db
    }

    fun startSync(
        householdId: String,
        onHouseholdUpdate: (HouseholdModel) -> Unit,
        onBudgetUpdate: (MonthlyBudgetModel) -> Unit,
        onCategoriesUpdate: (List<CategoryModel>) -> Unit,
        onExpensesUpdate: (List<ExpenseModel>) -> Unit,
        onProvisionsUpdate: (List<ProvisionItemModel>) -> Unit,
        onMessagesUpdate: (List<MessageModel>) -> Unit,
        onPollsUpdate: (List<PollModel>) -> Unit,
        onMembersUpdate: (List<UserModel>) -> Unit,
        onAdminNotificationsUpdate: ((List<AdminNotificationModel>) -> Unit)? = null,
        onTasksUpdate: ((List<TaskModel>) -> Unit)? = null,
        onEventsUpdate: ((List<EventModel>) -> Unit)? = null,
        onMealNotesUpdate: ((List<MealNoteModel>) -> Unit)? = null,
        onPostItsUpdate: ((List<PostItModel>) -> Unit)? = null,
        onHouseEssentialsUpdate: ((HouseEssentialInfo) -> Unit)? = null,
        onActiveGuestEventUpdate: ((GuestPartyEvent?) -> Unit)? = null
    ) {
        try {
            val firestore = getDb() ?: return
            stopSync()
            logD(TAG, "Starting sync for household: $householdId")

            // 1. Household listener
            householdListener = firestore.collection("households").document(householdId)
                .addSnapshotListener { snapshot, error ->
                    try {
                        if (error != null) {
                            logE(TAG, "Household listener error: ${error.message}", error)
                            return@addSnapshotListener
                        }
                        if (snapshot == null || !snapshot.exists()) return@addSnapshotListener
                        val data = snapshot.data ?: return@addSnapshotListener
                        val monthlyBudget = (data["monthlyBudget"] as? Number)?.toDouble() ?: 0.0
                        val hh = HouseholdModel(
                            id = snapshot.id,
                            name = data["name"] as? String ?: "Mon Foyer",
                            inviteCode = data["inviteCode"] as? String ?: "",
                            currency = data["currency"] as? String ?: "USD",
                            secondaryCurrency = data["secondaryCurrency"] as? String ?: "CDF",
                            exchangeRate = (data["exchangeRate"] as? Number)?.toDouble() ?: 2250.0,
                            ownerId = data["ownerId"] as? String ?: "",
                            homeLatitude = (data["homeLatitude"] as? Number)?.toDouble(),
                            homeLongitude = (data["homeLongitude"] as? Number)?.toDouble(),
                            homeAddress = data["homeAddress"] as? String ?: "",
                            monthlyBudget = monthlyBudget,
                            appIcon = data["appIcon"] as? String ?: "default"
                        )
                        logD(TAG, "Household updated from cloud: ${hh.name} (monthlyBudget: $monthlyBudget)")
                        onHouseholdUpdate(hh)
                        if (monthlyBudget > 0.0) {
                            onBudgetUpdate(
                                MonthlyBudgetModel(
                                    id = "budget_${snapshot.id}",
                                    householdId = snapshot.id,
                                    monthKey = data["monthKey"] as? String ?: "",
                                    totalBudget = monthlyBudget
                                )
                            )
                        }
                    } catch (t: Throwable) {
                        logE(TAG, "Household parse error: ${t.message}", t)
                    }
                }

            // 2. Budget listener (listens directly to household budget document)
            val budgetDocId = "budget_$householdId"
            budgetListener = firestore.collection("budgets").document(budgetDocId)
                .addSnapshotListener { snapshot, error ->
                    try {
                        if (error != null) {
                            logE(TAG, "Budget listener error: ${error.message}", error)
                            return@addSnapshotListener
                        }
                        if (snapshot == null || !snapshot.exists()) return@addSnapshotListener
                        val data = snapshot.data ?: return@addSnapshotListener
                        val total = (data["totalBudget"] as? Number)?.toDouble() ?: 0.0
                        val b = MonthlyBudgetModel(
                            id = snapshot.id,
                            householdId = householdId,
                            monthKey = data["monthKey"] as? String ?: "",
                            totalBudget = total,
                            lastModifiedBy = data["lastModifiedBy"] as? String ?: "",
                            lastModifiedAt = (data["updatedAt"] as? Number)?.let { Date(it.toLong()) }
                        )
                        logD(TAG, "Budget updated from cloud doc: ${b.totalBudget}")
                        onBudgetUpdate(b)
                    } catch (t: Throwable) {
                        logE(TAG, "Budget parse error: ${t.message}", t)
                    }
                }

            // 3. Categories listener
            categoriesListener = firestore.collection("categories")
                .whereEqualTo("householdId", householdId)
                .addSnapshotListener { snapshots, error ->
                    try {
                        if (error != null) {
                            logE(TAG, "Categories listener error: ${error.message}", error)
                            return@addSnapshotListener
                        }
                        if (snapshots == null) return@addSnapshotListener
                        val list = snapshots.documents.mapNotNull { doc ->
                            val data = doc.data ?: return@mapNotNull null
                            CategoryModel(
                                id = doc.id,
                                householdId = householdId,
                                name = data["name"] as? String ?: "Catégorie",
                                emoji = data["emoji"] as? String ?: "📦",
                                plannedAmount = (data["plannedAmount"] as? Number)?.toDouble() ?: 0.0,
                                colorHex = data["colorHex"] as? String ?: "#10B981",
                                isProvision = data["isProvision"] as? Boolean ?: false
                            )
                        }
                        logD(TAG, "Categories updated from cloud: ${list.size} items")
                        if (list.isNotEmpty()) onCategoriesUpdate(list)
                    } catch (t: Throwable) {
                        logE(TAG, "Categories parse error: ${t.message}", t)
                    }
                }

            // 4. Expenses listener
            expensesListener = firestore.collection("expenses")
                .whereEqualTo("householdId", householdId)
                .addSnapshotListener { snapshots, error ->
                    try {
                        if (error != null) {
                            logE(TAG, "Expenses listener error: ${error.message}", error)
                            return@addSnapshotListener
                        }
                        if (snapshots == null) return@addSnapshotListener
                        val list = snapshots.documents.mapNotNull { doc ->
                            val data = doc.data ?: return@mapNotNull null
                            val ts = (data["timestamp"] as? Number)?.toLong() ?: System.currentTimeMillis()
                            ExpenseModel(
                                id = doc.id,
                                householdId = householdId,
                                userId = data["userId"] as? String ?: "",
                                userName = data["userName"] as? String ?: "Membre",
                                categoryId = data["categoryId"] as? String ?: "",
                                categoryName = data["categoryName"] as? String ?: "",
                                categoryEmoji = data["categoryEmoji"] as? String ?: "📦",
                                amount = (data["amount"] as? Number)?.toDouble() ?: 0.0,
                                description = data["description"] as? String ?: "",
                                comment = data["comment"] as? String ?: "",
                                lastModifiedByName = data["lastModifiedByName"] as? String,
                                lastModifiedAt = (data["lastModifiedAt"] as? Number)?.let { Date(it.toLong()) },
                                date = Date(ts)
                            )
                        }.sortedByDescending { it.date }
                        logD(TAG, "Expenses updated from cloud: ${list.size} expenses")
                        onExpensesUpdate(list)
                    } catch (t: Throwable) {
                        logE(TAG, "Expenses parse error: ${t.message}", t)
                    }
                }

            // 5. Provisions listener
            provisionsListener = firestore.collection("provisions")
                .whereEqualTo("householdId", householdId)
                .addSnapshotListener { snapshots, error ->
                    try {
                        if (error != null) {
                            logE(TAG, "Provisions listener error: ${error.message}", error)
                            return@addSnapshotListener
                        }
                        if (snapshots == null) return@addSnapshotListener
                        val list = snapshots.documents.mapNotNull { doc ->
                            val data = doc.data ?: return@mapNotNull null
                            val actual = (data["actualPrice"] as? Number)?.toDouble()
                            ProvisionItemModel(
                                id = doc.id,
                                householdId = householdId,
                                name = data["name"] as? String ?: "",
                                quantity = (data["quantity"] as? Number)?.toInt() ?: 1,
                                unit = data["unit"] as? String ?: "pièce",
                                plannedPrice = (data["plannedPrice"] as? Number)?.toDouble() ?: 0.0,
                                actualPrice = actual,
                                status = data["status"] as? String ?: "planned",
                                priority = data["priority"] as? String ?: "medium",
                                comment = data["comment"] as? String ?: "",
                                addedBy = data["addedBy"] as? String ?: "",
                                lastModifiedBy = data["lastModifiedBy"] as? String,
                                lastModifiedAt = (data["lastModifiedAt"] as? Number)?.let { Date(it.toLong()) },
                                purchasedBy = data["purchasedBy"] as? String,
                                purchasedAt = (data["purchasedAt"] as? Number)?.let { Date(it.toLong()) }
                            )
                        }
                        logD(TAG, "Provisions updated from cloud: ${list.size} items")
                        onProvisionsUpdate(list)
                    } catch (t: Throwable) {
                        logE(TAG, "Provisions parse error: ${t.message}", t)
                    }
                }

            // 6. Messages listener
            messagesListener = firestore.collection("messages")
                .whereEqualTo("householdId", householdId)
                .addSnapshotListener { snapshots, error ->
                    try {
                        if (error != null) {
                            logE(TAG, "Messages listener error: ${error.message}", error)
                            return@addSnapshotListener
                        }
                        if (snapshots == null) return@addSnapshotListener
                        val list = snapshots.documents.mapNotNull { doc ->
                            val data = doc.data ?: return@mapNotNull null
                            val ts = (data["timestamp"] as? Number)?.toLong() ?: System.currentTimeMillis()
                            val isPinned = data["isPinned"] as? Boolean ?: false
                            val rawReactions = data["reactions"] as? Map<*, *>
                            val reactionsMap = mutableMapOf<String, List<String>>()
                            rawReactions?.forEach { (k, v) ->
                                val key = k as? String ?: return@forEach
                                val list = (v as? List<*>)?.mapNotNull { it as? String } ?: emptyList()
                                reactionsMap[key] = list
                            }
                            MessageModel(
                                id = doc.id,
                                householdId = householdId,
                                userId = data["userId"] as? String ?: "",
                                userName = data["userName"] as? String ?: "Membre",
                                text = data["text"] as? String ?: "",
                                type = data["type"] as? String ?: "text",
                                replyToMessageId = data["replyToMessageId"] as? String,
                                replyToUserName = data["replyToUserName"] as? String,
                                replyToText = data["replyToText"] as? String,
                                isPinned = isPinned,
                                reactions = reactionsMap,
                                createdAt = Date(ts)
                            )
                        }.sortedBy { it.createdAt }
                        logD(TAG, "Messages updated from cloud: ${list.size} messages")
                        onMessagesUpdate(list)
                    } catch (t: Throwable) {
                        logE(TAG, "Messages parse error: ${t.message}", t)
                    }
                }

            // 7. Polls listener
            pollsListener = firestore.collection("polls")
                .whereEqualTo("householdId", householdId)
                .addSnapshotListener { snapshots, error ->
                    try {
                        if (error != null) {
                            logE(TAG, "Polls listener error: ${error.message}", error)
                            return@addSnapshotListener
                        }
                        if (snapshots == null) return@addSnapshotListener
                        val list = snapshots.documents.mapNotNull { doc ->
                            val data = doc.data ?: return@mapNotNull null
                            @Suppress("UNCHECKED_CAST")
                            val rawOpts = data["options"] as? List<Map<String, Any>> ?: emptyList()
                            val options = rawOpts.map { optMap ->
                                @Suppress("UNCHECKED_CAST")
                                val voters = optMap["voterIds"] as? List<String> ?: emptyList()
                                PollOption(
                                    id = optMap["id"] as? String ?: UUID.randomUUID().toString(),
                                    text = optMap["text"] as? String ?: "",
                                    voterIds = voters
                                )
                            }
                            val createdAtTs = (data["createdAt"] as? Number)?.toLong() ?: (data["timestamp"] as? Number)?.toLong() ?: System.currentTimeMillis()
                            PollModel(
                                id = doc.id,
                                householdId = householdId,
                                creatorId = data["creatorId"] as? String ?: "",
                                creatorName = data["creatorName"] as? String ?: "",
                                question = data["question"] as? String ?: "",
                                options = options,
                                isClosed = data["isClosed"] as? Boolean ?: false,
                                createdAt = Date(createdAtTs)
                            )
                        }
                        logD(TAG, "Polls updated from cloud: ${list.size} polls")
                        onPollsUpdate(list)
                    } catch (t: Throwable) {
                        logE(TAG, "Polls parse error: ${t.message}", t)
                    }
                }

            // 8. Members listener
            membersListener = firestore.collection("users")
                .whereEqualTo("householdId", householdId)
                .addSnapshotListener { snapshots, error ->
                    try {
                        if (error != null) {
                            logE(TAG, "Members listener error: ${error.message}", error)
                            return@addSnapshotListener
                        }
                        if (snapshots == null) return@addSnapshotListener
                        val list = snapshots.documents.mapNotNull { doc ->
                            val data = doc.data ?: return@mapNotNull null
                            UserModel(
                                id = doc.id,
                                name = data["name"] as? String ?: "Membre",
                                email = data["email"] as? String ?: "",
                                role = data["role"] as? String ?: "member",
                                householdId = householdId,
                                trackingEnabled = data["trackingEnabled"] as? Boolean ?: false,
                                latitude = (data["latitude"] as? Number)?.toDouble(),
                                longitude = (data["longitude"] as? Number)?.toDouble(),
                                locationName = data["locationName"] as? String ?: "",
                                locationUpdatedAt = (data["locationUpdatedAt"] as? Number)?.let { Date(it.toLong()) },
                                batteryLevel = (data["batteryLevel"] as? Number)?.toInt() ?: 100,
                                isCharging = data["isCharging"] as? Boolean ?: false,
                                deviceModel = data["deviceModel"] as? String ?: "",
                                deviceStatusUpdatedAt = (data["deviceStatusUpdatedAt"] as? Number)?.let { Date(it.toLong()) },
                                pingRequestedAt = (data["pingRequestedAt"] as? Number)?.toLong(),
                                isHome = data["isHome"] as? Boolean ?: false,
                                distanceFromHomeMeters = (data["distanceFromHomeMeters"] as? Number)?.toDouble(),
                                arrivedHomeAt = (data["arrivedHomeAt"] as? Number)?.let { Date(it.toLong()) }
                            )
                        }
                        logD(TAG, "Members updated from cloud: ${list.size} users")
                        if (list.isNotEmpty()) onMembersUpdate(list)
                    } catch (t: Throwable) {
                        logE(TAG, "Members parse error: ${t.message}", t)
                    }
                }

            // 9. Admin notifications listener
            adminNotificationsListener = firestore.collection("admin_notifications")
                .whereEqualTo("householdId", householdId)
                .addSnapshotListener { snapshots, error ->
                    try {
                        if (error != null || snapshots == null) return@addSnapshotListener
                        val list = snapshots.documents.mapNotNull { doc ->
                            val data = doc.data ?: return@mapNotNull null
                            AdminNotificationModel(
                                id = doc.id,
                                householdId = householdId,
                                title = data["title"] as? String ?: "Alerte",
                                message = data["message"] as? String ?: "",
                                type = data["type"] as? String ?: "general",
                                memberId = data["memberId"] as? String ?: "",
                                memberName = data["memberName"] as? String ?: "",
                                createdAt = (data["timestamp"] as? Number)?.let { Date(it.toLong()) } ?: Date(),
                                isRead = data["isRead"] as? Boolean ?: false
                            )
                        }.sortedByDescending { it.createdAt }
                        onAdminNotificationsUpdate?.invoke(list)
                    } catch (t: Throwable) {
                        logE(TAG, "AdminNotifications parse error: ${t.message}", t)
                    }
                }

            // 10. Tasks listener
            tasksListener = firestore.collection("tasks")
                .whereEqualTo("householdId", householdId)
                .addSnapshotListener { snapshots, error ->
                    try {
                        if (error != null || snapshots == null) return@addSnapshotListener
                        val list = snapshots.documents.mapNotNull { doc ->
                            val data = doc.data ?: return@mapNotNull null
                            TaskModel(
                                id = doc.id,
                                householdId = householdId,
                                title = data["title"] as? String ?: "",
                                description = data["description"] as? String ?: "",
                                assignedMemberId = data["assignedMemberId"] as? String ?: "",
                                assignedMemberName = data["assignedMemberName"] as? String ?: "",
                                dueDate = (data["dueDate"] as? Number)?.let { Date(it.toLong()) },
                                recurrence = data["recurrence"] as? String ?: "none",
                                status = data["status"] as? String ?: "todo",
                                points = (data["points"] as? Number)?.toInt() ?: 10,
                                completedAt = (data["completedAt"] as? Number)?.let { Date(it.toLong()) },
                                completedByMemberId = data["completedByMemberId"] as? String ?: "",
                                completedByMemberName = data["completedByMemberName"] as? String ?: "",
                                createdAt = (data["createdAt"] as? Number)?.let { Date(it.toLong()) } ?: Date()
                            )
                        }.sortedByDescending { it.createdAt }
                        onTasksUpdate?.invoke(list)
                    } catch (t: Throwable) {
                        logE(TAG, "Tasks parse error: ${t.message}", t)
                    }
                }

            // 11. Events listener
            eventsListener = firestore.collection("events")
                .whereEqualTo("householdId", householdId)
                .addSnapshotListener { snapshots, error ->
                    try {
                        if (error != null || snapshots == null) return@addSnapshotListener
                        val list = snapshots.documents.mapNotNull { doc ->
                            val data = doc.data ?: return@mapNotNull null
                            val pList = (data["participantMemberIds"] as? List<*>)?.mapNotNull { it as? String } ?: emptyList()
                            EventModel(
                                id = doc.id,
                                householdId = householdId,
                                title = data["title"] as? String ?: "",
                                description = data["description"] as? String ?: "",
                                location = data["location"] as? String ?: "",
                                startDate = (data["startDate"] as? Number)?.let { Date(it.toLong()) } ?: Date(),
                                endDate = (data["endDate"] as? Number)?.let { Date(it.toLong()) },
                                category = data["category"] as? String ?: "general",
                                categoryEmoji = data["categoryEmoji"] as? String ?: "📌",
                                participantMemberIds = pList,
                                createdById = data["createdById"] as? String ?: "",
                                createdByName = data["createdByName"] as? String ?: "",
                                createdAt = (data["createdAt"] as? Number)?.let { Date(it.toLong()) } ?: Date()
                            )
                        }.sortedBy { it.startDate }
                        onEventsUpdate?.invoke(list)
                    } catch (t: Throwable) {
                        logE(TAG, "Events parse error: ${t.message}", t)
                    }
                }

            // 12. Meal Notes listener (Bloc-notes des repas partagé)
            mealNotesListener = firestore.collection("meal_notes")
                .whereEqualTo("householdId", householdId)
                .addSnapshotListener { snapshots, error ->
                    try {
                        if (error != null || snapshots == null) return@addSnapshotListener
                        val list = snapshots.documents.mapNotNull { doc ->
                            val data = doc.data ?: return@mapNotNull null
                            @Suppress("UNCHECKED_CAST")
                            val upvoters = data["upvoterUserIds"] as? List<String> ?: emptyList()
                            val createdAtTs = (data["createdAt"] as? Number)?.toLong() ?: System.currentTimeMillis()
                            MealNoteModel(
                                id = doc.id,
                                householdId = householdId,
                                mealTitle = data["mealTitle"] as? String ?: "",
                                noteDetails = data["noteDetails"] as? String ?: "",
                                suggestedByUserId = data["suggestedByUserId"] as? String ?: "",
                                suggestedByUserName = data["suggestedByUserName"] as? String ?: "",
                                createdAt = Date(createdAtTs),
                                upvoterUserIds = upvoters,
                                status = data["status"] as? String ?: "ACTIVE"
                            )
                        }.sortedByDescending { it.upvoteCount }
                        logD(TAG, "Meal notes updated from cloud: ${list.size} notes")
                        onMealNotesUpdate?.invoke(list)
                    } catch (t: Throwable) {
                        logE(TAG, "Meal notes parse error: ${t.message}", t)
                    }
                }

            // 13. Post-its listener (Tableau de Liège)
            postItsListener = firestore.collection("post_its")
                .whereEqualTo("householdId", householdId)
                .addSnapshotListener { snapshots, error ->
                    try {
                        if (error != null || snapshots == null) return@addSnapshotListener
                        val list = snapshots.documents.mapNotNull { doc ->
                            val data = doc.data ?: return@mapNotNull null
                            val createdAtTs = (data["createdAt"] as? Number)?.toLong() ?: System.currentTimeMillis()
                            PostItModel(
                                id = doc.id,
                                householdId = householdId,
                                text = data["text"] as? String ?: "",
                                colorHex = data["colorHex"] as? String ?: "#FEF08A",
                                emoji = data["emoji"] as? String ?: "📌",
                                authorId = data["authorId"] as? String ?: "",
                                authorName = data["authorName"] as? String ?: "",
                                createdAt = Date(createdAtTs),
                                isPinned = data["isPinned"] as? Boolean ?: false
                            )
                        }.sortedWith(compareByDescending<PostItModel> { it.isPinned }.thenByDescending { it.createdAt })
                        logD(TAG, "Post-its updated from cloud: ${list.size} notes")
                        onPostItsUpdate?.invoke(list)
                    } catch (t: Throwable) {
                        logE(TAG, "Post-its parse error: ${t.message}", t)
                    }
                }

            // 14. House Essentials listener (Infos Essentielles & Wifi)
            val essentialsDocId = "essentials_$householdId"
            houseEssentialsListener = firestore.collection("house_essentials").document(essentialsDocId)
                .addSnapshotListener { snapshot, error ->
                    try {
                        if (error != null || snapshot == null || !snapshot.exists()) return@addSnapshotListener
                        val data = snapshot.data ?: return@addSnapshotListener
                        val rawRules = data["houseRules"] as? List<*>
                        val rules = rawRules?.mapNotNull { it as? String } ?: emptyList()
                        val updatedAtTs = (data["lastUpdatedAt"] as? Number)?.toLong()
                        val info = HouseEssentialInfo(
                            wifiSsid = data["wifiSsid"] as? String ?: "",
                            wifiPassword = data["wifiPassword"] as? String ?: "",
                            gateCode = data["gateCode"] as? String ?: "",
                            buildingCode = data["buildingCode"] as? String ?: "",
                            landlordName = data["landlordName"] as? String ?: "",
                            landlordPhone = data["landlordPhone"] as? String ?: "",
                            emergencyNotes = data["emergencyNotes"] as? String ?: "",
                            houseRules = rules,
                            lastUpdatedBy = data["lastUpdatedBy"] as? String ?: "",
                            lastUpdatedAt = updatedAtTs?.let { Date(it) }
                        )
                        logD(TAG, "House essentials updated from cloud")
                        onHouseEssentialsUpdate?.invoke(info)
                    } catch (t: Throwable) {
                        logE(TAG, "House essentials parse error: ${t.message}", t)
                    }
                }

            // 15. Active Guest Events listener (Mode Soirée & Invités)
            guestEventsListener = firestore.collection("guest_events")
                .whereEqualTo("householdId", householdId)
                .whereEqualTo("isActive", true)
                .addSnapshotListener { snapshots, error ->
                    try {
                        if (error != null || snapshots == null) return@addSnapshotListener
                        val activeEvents = snapshots.documents.mapNotNull { doc ->
                            val data = doc.data ?: return@mapNotNull null
                            val startTs = (data["startTime"] as? Number)?.toLong() ?: System.currentTimeMillis()
                            val endTs = (data["endTime"] as? Number)?.toLong()
                            GuestPartyEvent(
                                id = doc.id,
                                householdId = householdId,
                                hostUserId = data["hostUserId"] as? String ?: "",
                                hostUserName = data["hostUserName"] as? String ?: "",
                                guestCount = (data["guestCount"] as? Number)?.toInt() ?: 2,
                                description = data["description"] as? String ?: "",
                                startTime = Date(startTs),
                                endTime = endTs?.let { Date(it) },
                                isActive = data["isActive"] as? Boolean ?: true
                            )
                        }.sortedByDescending { it.startTime }
                        val currentActive = activeEvents.firstOrNull()
                        onActiveGuestEventUpdate?.invoke(currentActive)
                    } catch (t: Throwable) {
                        logE(TAG, "Guest events parse error: ${t.message}", t)
                    }
                }
        } catch (e: Throwable) {
            logE(TAG, "startSync error: ${e.message}", e)
        }
    }

    fun stopSync() {
        try {
            householdListener?.remove()
            budgetListener?.remove()
            categoriesListener?.remove()
            expensesListener?.remove()
            provisionsListener?.remove()
            messagesListener?.remove()
            pollsListener?.remove()
            membersListener?.remove()
            adminNotificationsListener?.remove()
            tasksListener?.remove()
            eventsListener?.remove()
            mealNotesListener?.remove()
            postItsListener?.remove()
            houseEssentialsListener?.remove()
            guestEventsListener?.remove()
        } catch (e: Throwable) {
            // ignore
        }
    }

    // Firestore Write Helpers
    fun saveHouseholdToCloud(household: HouseholdModel, user: UserModel) {
        try {
            val firestore = getDb() ?: return
            logD(TAG, "Saving household ${household.id} (${household.name}) with code ${household.inviteCode}")
            val hhMap = hashMapOf<String, Any?>(
                "name" to household.name,
                "inviteCode" to household.inviteCode,
                "currency" to household.currency,
                "secondaryCurrency" to household.secondaryCurrency,
                "exchangeRate" to household.exchangeRate,
                "ownerId" to household.ownerId,
                "homeLatitude" to household.homeLatitude,
                "homeLongitude" to household.homeLongitude,
                "homeAddress" to household.homeAddress,
                "monthlyBudget" to household.monthlyBudget,
                "appIcon" to household.appIcon
            )
            firestore.collection("households").document(household.id).set(hhMap, SetOptions.merge())

            val userMap = hashMapOf(
                "name" to user.name,
                "email" to user.email,
                "role" to user.role,
                "householdId" to household.id
            )
            firestore.collection("users").document(user.id).set(userMap, SetOptions.merge())
        } catch (e: Throwable) {
            logE(TAG, "saveHouseholdToCloud error: ${e.message}", e)
        }
    }

    fun searchHouseholdByInviteCode(
        code: String,
        onSuccess: (household: HouseholdModel) -> Unit,
        onError: (String) -> Unit
    ) {
        try {
            val firestore = getDb()
            if (firestore == null) {
                onError("Connexion à la base de données indisponible.")
                return
            }

            val cleanCode = code.replace("UNK-", "").replace("KASA-", "").replace(" ", "").trim().uppercase()
            logD(TAG, "Searching for household with inviteCode = '$cleanCode'")

            firestore.collection("households")
                .whereEqualTo("inviteCode", cleanCode)
                .get()
                .addOnSuccessListener { querySnapshot ->
                    try {
                        if (!querySnapshot.isEmpty) {
                            val doc = querySnapshot.documents.first()
                            val data = doc.data
                            if (data != null) {
                                val hh = HouseholdModel(
                                    id = doc.id,
                                    name = data["name"] as? String ?: "Mon Foyer",
                                    inviteCode = data["inviteCode"] as? String ?: cleanCode,
                                    currency = data["currency"] as? String ?: "USD",
                                    secondaryCurrency = data["secondaryCurrency"] as? String ?: "CDF",
                                    exchangeRate = (data["exchangeRate"] as? Number)?.toDouble() ?: 2250.0,
                                    ownerId = data["ownerId"] as? String ?: "",
                                    homeLatitude = (data["homeLatitude"] as? Number)?.toDouble(),
                                    homeLongitude = (data["homeLongitude"] as? Number)?.toDouble(),
                                    homeAddress = data["homeAddress"] as? String ?: "",
                                    monthlyBudget = (data["monthlyBudget"] as? Number)?.toDouble() ?: 0.0
                                )
                                logD(TAG, "Household FOUND in Firestore: ${hh.name} (id: ${hh.id})")
                                onSuccess(hh)
                                return@addOnSuccessListener
                            }
                        }
                        logW(TAG, "No household found with inviteCode: '$cleanCode'")
                        onError("Aucun foyer trouvé avec le code : UNK-$cleanCode")
                    } catch (t: Throwable) {
                        logE(TAG, "Error parsing found household: ${t.message}", t)
                        onError("Erreur lors de la récupération du foyer.")
                    }
                }
                .addOnFailureListener { e ->
                    logE(TAG, "Firestore query failed: ${e.message}", e)
                    onError("Erreur réseau : ${e.localizedMessage ?: "Vérifiez votre connexion"}")
                }
        } catch (e: Throwable) {
            logE(TAG, "searchHouseholdByInviteCode error: ${e.message}", e)
            onError("Erreur inattendue.")
        }
    }

    fun saveBudgetToCloud(budget: MonthlyBudgetModel) {
        try {
            val firestore = getDb() ?: return
            val hhId = budget.householdId.ifEmpty { "hh_1" }
            val docId = "budget_$hhId"
            val map = hashMapOf(
                "householdId" to hhId,
                "monthKey" to budget.monthKey,
                "totalBudget" to budget.totalBudget,
                "lastModifiedBy" to budget.lastModifiedBy,
                "updatedAt" to System.currentTimeMillis()
            )
            firestore.collection("budgets").document(docId).set(map, SetOptions.merge())
                .addOnSuccessListener { logD(TAG, "Budget doc synced: ${budget.totalBudget}") }
                .addOnFailureListener { e -> logE(TAG, "Failed syncing budget doc: ${e.message}", e) }

            // Also update monthlyBudget in household document directly
            firestore.collection("households").document(hhId)
                .set(mapOf("monthlyBudget" to budget.totalBudget, "lastModifiedBy" to budget.lastModifiedBy), SetOptions.merge())
                .addOnSuccessListener { logD(TAG, "Household root monthlyBudget updated to: ${budget.totalBudget}") }
                .addOnFailureListener { e -> logE(TAG, "Failed updating household monthlyBudget: ${e.message}", e) }
        } catch (e: Throwable) {
            logE(TAG, "saveBudgetToCloud error: ${e.message}", e)
        }
    }

    fun saveCategoryToCloud(category: CategoryModel) {
        try {
            val firestore = getDb() ?: return
            val map = hashMapOf(
                "householdId" to category.householdId,
                "name" to category.name,
                "emoji" to category.emoji,
                "plannedAmount" to category.plannedAmount,
                "colorHex" to category.colorHex,
                "isProvision" to category.isProvision
            )
            firestore.collection("categories").document(category.id).set(map, SetOptions.merge())
        } catch (e: Throwable) {
            logE(TAG, "saveCategoryToCloud error: ${e.message}", e)
        }
    }

    fun deleteCategoryFromCloud(categoryId: String) {
        try {
            val firestore = getDb() ?: return
            firestore.collection("categories").document(categoryId).delete()
        } catch (e: Throwable) {
            logE(TAG, "deleteCategoryFromCloud error: ${e.message}", e)
        }
    }

    fun saveExpenseToCloud(expense: ExpenseModel) {
        try {
            val firestore = getDb() ?: return
            logD(TAG, "Saving expense to cloud: ${expense.amount} for household ${expense.householdId}")
            val map = hashMapOf<String, Any?>(
                "householdId" to expense.householdId,
                "userId" to expense.userId,
                "userName" to expense.userName,
                "categoryId" to expense.categoryId,
                "categoryName" to expense.categoryName,
                "categoryEmoji" to expense.categoryEmoji,
                "amount" to expense.amount,
                "description" to expense.description,
                "comment" to expense.comment,
                "lastModifiedByName" to expense.lastModifiedByName,
                "lastModifiedAt" to expense.lastModifiedAt?.time,
                "timestamp" to expense.date.time
            )
            firestore.collection("expenses").document(expense.id).set(map, SetOptions.merge())
        } catch (e: Throwable) {
            logE(TAG, "saveExpenseToCloud error: ${e.message}", e)
        }
    }

    fun deleteExpenseFromCloud(expenseId: String) {
        try {
            val firestore = getDb() ?: return
            firestore.collection("expenses").document(expenseId).delete()
        } catch (e: Throwable) {
            logE(TAG, "deleteExpenseFromCloud error: ${e.message}", e)
        }
    }

    fun saveProvisionToCloud(item: ProvisionItemModel) {
        try {
            val firestore = getDb() ?: return
            logD(TAG, "Saving provision to cloud: ${item.name} for household ${item.householdId}")
            val map = hashMapOf<String, Any?>(
                "householdId" to item.householdId,
                "name" to item.name,
                "quantity" to item.quantity,
                "unit" to item.unit,
                "plannedPrice" to item.plannedPrice,
                "actualPrice" to item.actualPrice,
                "status" to item.status,
                "priority" to item.priority,
                "comment" to item.comment,
                "addedBy" to item.addedBy,
                "lastModifiedBy" to item.lastModifiedBy,
                "lastModifiedAt" to item.lastModifiedAt?.time,
                "purchasedBy" to item.purchasedBy,
                "purchasedAt" to item.purchasedAt?.time
            )
            firestore.collection("provisions").document(item.id).set(map, SetOptions.merge())
        } catch (e: Throwable) {
            logE(TAG, "saveProvisionToCloud error: ${e.message}", e)
        }
    }

    fun deleteProvisionFromCloud(itemId: String) {
        try {
            val firestore = getDb() ?: return
            firestore.collection("provisions").document(itemId).delete()
        } catch (e: Throwable) {
            logE(TAG, "deleteProvisionFromCloud error: ${e.message}", e)
        }
    }

    fun saveMessageToCloud(message: MessageModel) {
        try {
            val firestore = getDb() ?: return
            logD(TAG, "Saving chat message to cloud: '${message.text}' for household ${message.householdId}")
            val map = hashMapOf(
                "householdId" to message.householdId,
                "userId" to message.userId,
                "userName" to message.userName,
                "text" to message.text,
                "type" to message.type,
                "replyToMessageId" to message.replyToMessageId,
                "replyToUserName" to message.replyToUserName,
                "replyToText" to message.replyToText,
                "isPinned" to message.isPinned,
                "reactions" to message.reactions,
                "timestamp" to message.createdAt.time
            )
            firestore.collection("messages").document(message.id).set(map, SetOptions.merge())
        } catch (e: Throwable) {
            logE(TAG, "saveMessageToCloud error: ${e.message}", e)
        }
    }

    fun toggleMessageReactionCloud(messageId: String, emoji: String, userId: String) {
        try {
            val firestore = getDb() ?: return
            val docRef = firestore.collection("messages").document(messageId)
            firestore.runTransaction { transaction ->
                val snapshot = transaction.get(docRef)
                if (snapshot.exists()) {
                    val rawReactions = snapshot.get("reactions") as? Map<*, *>
                    val reactionsMap = mutableMapOf<String, MutableList<String>>()
                    rawReactions?.forEach { (k, v) ->
                        val key = k as? String ?: return@forEach
                        val list = (v as? List<*>)?.mapNotNull { it as? String }?.toMutableList() ?: mutableListOf()
                        reactionsMap[key] = list
                    }
                    val userList = reactionsMap.getOrPut(emoji) { mutableListOf() }
                    if (userList.contains(userId)) {
                        userList.remove(userId)
                        if (userList.isEmpty()) reactionsMap.remove(emoji)
                    } else {
                        userList.add(userId)
                    }
                    transaction.update(docRef, "reactions", reactionsMap)
                }
            }
        } catch (e: Throwable) {
            logE(TAG, "toggleMessageReactionCloud error: ${e.message}", e)
        }
    }

    fun togglePinMessageCloud(messageId: String, isPinned: Boolean) {
        try {
            val firestore = getDb() ?: return
            firestore.collection("messages").document(messageId).update("isPinned", isPinned)
        } catch (e: Throwable) {
            logE(TAG, "togglePinMessageCloud error: ${e.message}", e)
        }
    }

    fun deleteMessageCloud(messageId: String) {
        try {
            val firestore = getDb() ?: return
            firestore.collection("messages").document(messageId).delete()
        } catch (e: Throwable) {
            logE(TAG, "deleteMessageCloud error: ${e.message}", e)
        }
    }

    fun savePollToCloud(poll: PollModel) {
        try {
            val firestore = getDb() ?: return
            logD(TAG, "Saving poll to cloud: '${poll.question}' for household ${poll.householdId}")
            val optsList = poll.options.map { opt ->
                hashMapOf(
                    "id" to opt.id,
                    "text" to opt.text,
                    "voterIds" to opt.voterIds
                )
            }
            val map = hashMapOf(
                "householdId" to poll.householdId,
                "creatorId" to poll.creatorId,
                "creatorName" to poll.creatorName,
                "question" to poll.question,
                "options" to optsList,
                "isClosed" to poll.isClosed,
                "createdAt" to poll.createdAt.time
            )
            firestore.collection("polls").document(poll.id).set(map, SetOptions.merge())
        } catch (e: Throwable) {
            logE(TAG, "savePollToCloud error: ${e.message}", e)
        }
    }

    fun saveUserToCloud(user: UserModel) {
        try {
            val firestore = getDb() ?: return
            val map = hashMapOf<String, Any?>(
                "name" to user.name,
                "email" to user.email,
                "role" to user.role,
                "householdId" to user.householdId,
                "trackingEnabled" to user.trackingEnabled,
                "latitude" to user.latitude,
                "longitude" to user.longitude,
                "locationName" to user.locationName,
                "locationUpdatedAt" to (user.locationUpdatedAt?.time ?: System.currentTimeMillis()),
                "batteryLevel" to user.batteryLevel,
                "isCharging" to user.isCharging,
                "deviceModel" to user.deviceModel,
                "deviceStatusUpdatedAt" to (user.deviceStatusUpdatedAt?.time ?: System.currentTimeMillis()),
                "pingRequestedAt" to user.pingRequestedAt,
                "isHome" to user.isHome,
                "distanceFromHomeMeters" to user.distanceFromHomeMeters,
                "arrivedHomeAt" to user.arrivedHomeAt?.time
            )
            firestore.collection("users").document(user.id).set(map, SetOptions.merge())
        } catch (e: Throwable) {
            logE(TAG, "saveUserToCloud error: ${e.message}", e)
        }
    }

    fun setMemberTrackingRemote(memberId: String, enabled: Boolean) {
        try {
            val firestore = getDb() ?: return
            firestore.collection("users").document(memberId).update("trackingEnabled", enabled)
        } catch (e: Throwable) {
            logE(TAG, "setMemberTrackingRemote error: ${e.message}", e)
        }
    }

    fun requestLocationPingRemote(memberId: String) {
        try {
            val firestore = getDb() ?: return
            firestore.collection("users").document(memberId).update("pingRequestedAt", System.currentTimeMillis())
        } catch (e: Throwable) {
            logE(TAG, "requestLocationPingRemote error: ${e.message}", e)
        }
    }

    fun updateHouseholdHomeLocation(householdId: String, lat: Double, lng: Double, address: String) {
        try {
            val firestore = getDb() ?: return
            val map = hashMapOf<String, Any?>(
                "homeLatitude" to lat,
                "homeLongitude" to lng,
                "homeAddress" to address
            )
            firestore.collection("households").document(householdId).set(map, SetOptions.merge())
        } catch (e: Throwable) {
            logE(TAG, "updateHouseholdHomeLocation error: ${e.message}", e)
        }
    }

    fun updateHouseholdAppIcon(householdId: String, appIcon: String) {
        try {
            val firestore = getDb() ?: return
            firestore.collection("households").document(householdId).update("appIcon", appIcon)
        } catch (e: Throwable) {
            logE(TAG, "updateHouseholdAppIcon error: ${e.message}", e)
        }
    }

    fun saveAdminNotificationToCloud(notification: AdminNotificationModel) {
        try {
            val firestore = getDb() ?: return
            val map = hashMapOf<String, Any?>(
                "householdId" to notification.householdId,
                "title" to notification.title,
                "message" to notification.message,
                "type" to notification.type,
                "memberId" to notification.memberId,
                "memberName" to notification.memberName,
                "timestamp" to notification.createdAt.time,
                "isRead" to notification.isRead
            )
            firestore.collection("admin_notifications").document(notification.id).set(map, SetOptions.merge())
        } catch (e: Throwable) {
            logE(TAG, "saveAdminNotificationToCloud error: ${e.message}", e)
        }
    }

    fun markAdminNotificationRead(notificationId: String) {
        try {
            val firestore = getDb() ?: return
            firestore.collection("admin_notifications").document(notificationId).update("isRead", true)
        } catch (e: Throwable) {
            logE(TAG, "markAdminNotificationRead error: ${e.message}", e)
        }
    }

    fun clearAdminNotifications(householdId: String) {
        try {
            val firestore = getDb() ?: return
            firestore.collection("admin_notifications")
                .whereEqualTo("householdId", householdId)
                .get()
                .addOnSuccessListener { snapshots ->
                    for (doc in snapshots.documents) {
                        doc.reference.delete()
                    }
                }
        } catch (e: Throwable) {
            logE(TAG, "clearAdminNotifications error: ${e.message}", e)
        }
    }

    // Tasks Cloud Helpers
    fun saveTaskToCloud(task: TaskModel) {
        try {
            val firestore = getDb() ?: return
            val map = hashMapOf<String, Any?>(
                "householdId" to task.householdId,
                "title" to task.title,
                "description" to task.description,
                "assignedMemberId" to task.assignedMemberId,
                "assignedMemberName" to task.assignedMemberName,
                "dueDate" to task.dueDate?.time,
                "recurrence" to task.recurrence,
                "status" to task.status,
                "points" to task.points,
                "completedAt" to task.completedAt?.time,
                "completedByMemberId" to task.completedByMemberId,
                "completedByMemberName" to task.completedByMemberName,
                "createdAt" to task.createdAt.time
            )
            firestore.collection("tasks").document(task.id).set(map, SetOptions.merge())
        } catch (e: Throwable) {
            logE(TAG, "saveTaskToCloud error: ${e.message}", e)
        }
    }

    fun deleteTaskFromCloud(taskId: String) {
        try {
            val firestore = getDb() ?: return
            firestore.collection("tasks").document(taskId).delete()
        } catch (e: Throwable) {
            logE(TAG, "deleteTaskFromCloud error: ${e.message}", e)
        }
    }

    // Events Cloud Helpers
    fun saveEventToCloud(event: EventModel) {
        try {
            val firestore = getDb() ?: return
            val map = hashMapOf<String, Any?>(
                "householdId" to event.householdId,
                "title" to event.title,
                "description" to event.description,
                "location" to event.location,
                "startDate" to event.startDate.time,
                "endDate" to event.endDate?.time,
                "category" to event.category,
                "categoryEmoji" to event.categoryEmoji,
                "participantMemberIds" to event.participantMemberIds,
                "createdById" to event.createdById,
                "createdByName" to event.createdByName,
                "createdAt" to event.createdAt.time
            )
            firestore.collection("events").document(event.id).set(map, SetOptions.merge())
        } catch (e: Throwable) {
            logE(TAG, "saveEventToCloud error: ${e.message}", e)
        }
    }

    fun deleteEventFromCloud(eventId: String) {
        try {
            val firestore = getDb() ?: return
            firestore.collection("events").document(eventId).delete()
        } catch (e: Throwable) {
            logE(TAG, "deleteEventFromCloud error: ${e.message}", e)
        }
    }

    fun saveMealNote(householdId: String, note: MealNoteModel) {
        try {
            val firestore = getDb() ?: return
            val map = hashMapOf<String, Any?>(
                "householdId" to householdId,
                "mealTitle" to note.mealTitle,
                "noteDetails" to note.noteDetails,
                "suggestedByUserId" to note.suggestedByUserId,
                "suggestedByUserName" to note.suggestedByUserName,
                "createdAt" to note.createdAt.time,
                "upvoterUserIds" to note.upvoterUserIds,
                "status" to note.status
            )
            firestore.collection("meal_notes").document(note.id).set(map, SetOptions.merge())
        } catch (e: Throwable) {
            logE(TAG, "saveMealNote error: ${e.message}", e)
        }
    }

    fun deleteMealNote(householdId: String, noteId: String) {
        try {
            val firestore = getDb() ?: return
            firestore.collection("meal_notes").document(noteId).delete()
        } catch (e: Throwable) {
            logE(TAG, "deleteMealNote error: ${e.message}", e)
        }
    }

    // Corkboard (Tableau de Liège) Cloud Helpers
    fun savePostItToCloud(postIt: PostItModel) {
        try {
            val firestore = getDb() ?: return
            val map = hashMapOf<String, Any?>(
                "householdId" to postIt.householdId,
                "text" to postIt.text,
                "colorHex" to postIt.colorHex,
                "emoji" to postIt.emoji,
                "authorId" to postIt.authorId,
                "authorName" to postIt.authorName,
                "createdAt" to postIt.createdAt.time,
                "isPinned" to postIt.isPinned
            )
            firestore.collection("post_its").document(postIt.id).set(map, SetOptions.merge())
        } catch (e: Throwable) {
            logE(TAG, "savePostItToCloud error: ${e.message}", e)
        }
    }

    fun deletePostItFromCloud(postItId: String) {
        try {
            val firestore = getDb() ?: return
            firestore.collection("post_its").document(postItId).delete()
        } catch (e: Throwable) {
            logE(TAG, "deletePostItFromCloud error: ${e.message}", e)
        }
    }

    // House Essentials Cloud Helpers
    fun saveHouseEssentialsToCloud(householdId: String, info: HouseEssentialInfo) {
        try {
            val firestore = getDb() ?: return
            val map = hashMapOf<String, Any?>(
                "householdId" to householdId,
                "wifiSsid" to info.wifiSsid,
                "wifiPassword" to info.wifiPassword,
                "gateCode" to info.gateCode,
                "buildingCode" to info.buildingCode,
                "landlordName" to info.landlordName,
                "landlordPhone" to info.landlordPhone,
                "emergencyNotes" to info.emergencyNotes,
                "houseRules" to info.houseRules,
                "lastUpdatedBy" to info.lastUpdatedBy,
                "lastUpdatedAt" to (info.lastUpdatedAt?.time ?: System.currentTimeMillis())
            )
            firestore.collection("house_essentials").document("essentials_$householdId").set(map, SetOptions.merge())
        } catch (e: Throwable) {
            logE(TAG, "saveHouseEssentialsToCloud error: ${e.message}", e)
        }
    }

    // Guest Events Cloud Helpers
    fun saveGuestEventToCloud(event: GuestPartyEvent) {
        try {
            val firestore = getDb() ?: return
            val map = hashMapOf<String, Any?>(
                "householdId" to event.householdId,
                "hostUserId" to event.hostUserId,
                "hostUserName" to event.hostUserName,
                "guestCount" to event.guestCount,
                "description" to event.description,
                "startTime" to event.startTime.time,
                "endTime" to event.endTime?.time,
                "isActive" to event.isActive
            )
            firestore.collection("guest_events").document(event.id).set(map, SetOptions.merge())
        } catch (e: Throwable) {
            logE(TAG, "saveGuestEventToCloud error: ${e.message}", e)
        }
    }

    fun endGuestEventInCloud(eventId: String) {
        try {
            val firestore = getDb() ?: return
            firestore.collection("guest_events").document(eventId).update("isActive", false)
        } catch (e: Throwable) {
            logE(TAG, "endGuestEventInCloud error: ${e.message}", e)
        }
    }
}
