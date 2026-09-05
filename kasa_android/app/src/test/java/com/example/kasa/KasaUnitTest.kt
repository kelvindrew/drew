package com.example.kasa

import com.example.kasa.data.model.*
import com.example.kasa.data.repository.KasaRepository
import com.example.kasa.util.CurrencyFormatter
import com.example.kasa.util.DateFormatter
import org.junit.Assert.*
import org.junit.Test
import java.util.Date

class KasaUnitTest {

    @Test
    fun testCurrencyFormatter() {
        assertEquals("$150.00", CurrencyFormatter.format(150.0, "USD"))
        assertEquals("1.5K USD", CurrencyFormatter.formatCompact(1500.0, "USD"))
        assertEquals("2.0M USD", CurrencyFormatter.formatCompact(2000000.0, "USD"))
    }

    @Test
    fun testDateFormatter() {
        val date = Date()
        assertTrue(DateFormatter.formatDate(date).isNotEmpty())
        assertTrue(DateFormatter.formatTime(date).isNotEmpty())
        assertTrue(DateFormatter.currentMonthName().isNotEmpty())
    }

    @Test
    fun testUserModel() {
        val user = UserModel(
            id = "u1",
            name = "Landry",
            role = "admin"
        )
        assertTrue(user.isAdmin)
        assertEquals("L", user.initials)
    }

    @Test
    fun testProvisionItemModel() {
        val item = ProvisionItemModel(
            id = "p1",
            name = "Riz",
            quantity = 2,
            unit = "sac",
            plannedPrice = 25.0,
            actualPrice = 30.0,
            status = "planned"
        )
        assertTrue(item.isPlanned)
        assertEquals(5.0, item.variance, 0.001)
        assertTrue(item.isOverBudget)
    }

    @Test
    fun testHouseholdModel() {
        val household = HouseholdModel(
            id = "h1",
            name = "Maison Unknown",
            inviteCode = "8F3A2B",
            ownerId = "u1"
        )
        assertEquals("UNK-8F3A2B", household.formattedInviteCode)
        assertTrue(household.isOwner("u1"))
        assertFalse(household.isOwner("u2"))
    }

    @Test
    fun testPollPercentage() {
        val opt1 = PollOption(id = "o1", text = "Option 1", voterIds = listOf("u1", "u2"))
        val opt2 = PollOption(id = "o2", text = "Option 2", voterIds = listOf("u3"))
        val poll = PollModel(
            id = "poll_1",
            question = "Question ?",
            options = listOf(opt1, opt2)
        )

        assertEquals(3, poll.totalVotes)
        assertTrue(poll.hasVoted("u1"))
        assertFalse(poll.hasVoted("u4"))
        assertEquals(2f / 3f, poll.percentageForOption(opt1), 0.01f)
    }

    @Test
    fun testKasaRepositoryOperations() {
        // Test Budget Update
        KasaRepository.updateMonthlyBudget(1500.0)
        assertEquals(1500.0, KasaRepository.monthlyBudget.value.totalBudget, 0.001)

        // Test Category Add & Update & Delete
        val initCatCount = KasaRepository.categories.value.size
        KasaRepository.addCategory("Loisirs", "🎮", 100.0)
        assertEquals(initCatCount + 1, KasaRepository.categories.value.size)
        val addedCat = KasaRepository.categories.value.last()
        KasaRepository.updateCategory(addedCat.id, "Jeux & Loisirs", "🕹️", 120.0, false)
        assertEquals("Jeux & Loisirs", KasaRepository.categories.value.last().name)
        KasaRepository.deleteCategory(addedCat.id)
        assertEquals(initCatCount, KasaRepository.categories.value.size)

        // Test Expense Add, Update, Delete
        KasaRepository.addExpense("c1", 45.0, "Courses")
        val exp = KasaRepository.expenses.value.first()
        assertEquals(45.0, exp.amount, 0.001)
        KasaRepository.updateExpense(exp.id, "c1", 50.0, "Courses Supermarché", exp.date)
        assertEquals(50.0, KasaRepository.expenses.value.first().amount, 0.001)
        KasaRepository.deleteExpense(exp.id)
        assertEquals(0, KasaRepository.expenses.value.size)

        // Test Provision Add, Update, Delete
        KasaRepository.addProvisionItem("Farine", 3, "kg", 15.0, "medium")
        assertEquals(1, KasaRepository.provisions.value.size)
        val prov = KasaRepository.provisions.value.first()
        KasaRepository.updateProvisionItem(prov.id, "Farine T55", 4, "kg", 18.0, "high", "planned")
        assertEquals(4, KasaRepository.provisions.value.first().quantity)
        KasaRepository.deleteProvisionItem(prov.id)
        assertEquals(0, KasaRepository.provisions.value.size)
    }

    @Test
    fun testDebtCalculator() {
        val u1 = UserModel(id = "u1", name = "Alice")
        val u2 = UserModel(id = "u2", name = "Bob")
        val u3 = UserModel(id = "u3", name = "Charlie")
        val members = listOf(u1, u2, u3)

        val exp1 = ExpenseModel(id = "e1", userId = "u1", userName = "Alice", amount = 60.0, description = "Courses")
        val exp2 = ExpenseModel(id = "e2", userId = "u2", userName = "Bob", amount = 30.0, description = "Pizza")
        val expenses = listOf(exp1, exp2)

        val summary = com.example.kasa.util.DebtCalculator.calculate(expenses, members)

        assertEquals(90.0, summary.totalSpent, 0.001)
        assertEquals(30.0, summary.fairSharePerPerson, 0.001)

        val aliceBal = summary.balances.find { it.userId == "u1" }!!
        val bobBal = summary.balances.find { it.userId == "u2" }!!
        val charlieBal = summary.balances.find { it.userId == "u3" }!!

        assertEquals(30.0, aliceBal.netBalance, 0.001)
        assertTrue(aliceBal.isCreditor)
        assertEquals(0.0, bobBal.netBalance, 0.001)
        assertTrue(bobBal.isSettled)
        assertEquals(-30.0, charlieBal.netBalance, 0.001)
        assertTrue(charlieBal.isDebtor)

        // Only 1 transfer needed: Charlie -> Alice of 30.0
        assertEquals(1, summary.transfers.size)
        val transfer = summary.transfers.first()
        assertEquals("u3", transfer.fromUserId)
        assertEquals("u1", transfer.toUserId)
        assertEquals(30.0, transfer.amount, 0.001)
    }

    @Test
    fun testMealPlanModel() {
        val meal = MealPlanModel(
            dinnerTitle = "Raclette Party",
            chefUserName = "Landry",
            attendeesDinnerYes = listOf("Landry", "Sarah")
        )
        assertTrue(meal.hasMeal)
        assertTrue(meal.hasDinner)
        assertFalse(meal.hasLunch)
        assertEquals(2, meal.attendeesDinnerYes.size)
    }

    @Test
    fun testAudioMessageModel() {
        val msg = MessageModel(
            id = "m1",
            text = "audio_123.m4a",
            type = "audio",
            audioDurationSec = 12
        )
        assertTrue(msg.isAudio)
        assertEquals(12, msg.audioDurationSec)
    }

    @Test
    fun testCorkboardModel() {
        val postIt = PostItModel(
            id = "p1",
            text = "Penser aux sacs poubelles !",
            colorHex = "#FEF08A",
            emoji = "🧹",
            authorName = "Landry",
            isPinned = true
        )
        assertTrue(postIt.isPinned)
        assertEquals("🧹", postIt.emoji)
        assertEquals("#FEF08A", postIt.colorHex)

        val essentials = HouseEssentialInfo(
            wifiSsid = "KasaHome_5G",
            wifiPassword = "SuperSecurePassword123",
            gateCode = "1234A",
            buildingCode = "B7890",
            landlordName = "M. Dupont",
            landlordPhone = "+33612345678",
            houseRules = listOf("Pas de bruit après 22h", "Ménage le samedi")
        )
        assertEquals("KasaHome_5G", essentials.wifiSsid)
        assertEquals(2, essentials.houseRules.size)
        assertEquals("1234A", essentials.gateCode)
    }

    @Test
    fun testHouseSecurityModel() {
        val event = GuestPartyEvent(
            id = "g1",
            householdId = "hh_1",
            hostUserId = "u1",
            hostUserName = "Landry",
            guestCount = 5,
            description = "Soirée jeux",
            isActive = true
        )
        assertTrue(event.isActive)
        assertEquals(5, event.guestCount)

        val alert = EmergencyAlertModel(
            id = "sos_1",
            householdId = "hh_1",
            senderId = "u1",
            senderName = "Landry",
            type = EmergencyType.WATER_LEAK,
            message = "Fuite robinet cuisine",
            latitude = 48.8566,
            longitude = 2.3522
        )
        assertEquals(EmergencyType.WATER_LEAK, alert.type)
        assertEquals("💧", alert.type.emoji)
        assertEquals("Fuite d'eau urgente", alert.type.label)
    }

    @Test
    fun testScheduledReminderModel() {
        val past = System.currentTimeMillis() - 10000
        val future = System.currentTimeMillis() + 60000

        val dueReminder = ScheduledReminderModel(
            id = "r1",
            message = "Descendre les poubelles",
            triggerTimestamp = past,
            isSent = false
        )
        assertTrue(dueReminder.isDue)

        val futureReminder = ScheduledReminderModel(
            id = "r2",
            message = "Nettoyer le four",
            triggerTimestamp = future,
            isSent = false
        )
        assertFalse(futureReminder.isDue)
    }
}

