import 'package:cloud_firestore/cloud_firestore.dart';
import '../models/household_model.dart';
import '../models/category_model.dart';
import '../models/expense_model.dart';
import '../models/provision_item_model.dart';
import '../models/monthly_budget_model.dart';
import '../models/message_model.dart';
import '../models/poll_model.dart';
import '../models/user_model.dart';
import '../utils/date_formatter.dart';

class FirestoreService {
  final FirebaseFirestore _db = FirebaseFirestore.instance;

  // ─── HOUSEHOLD ──────────────────────────────────────────────
  
  Future<HouseholdModel> createHousehold({
    required String name,
    required String ownerId,
    String currency = 'USD',
    String secondaryCurrency = 'CDF',
    double exchangeRate = 2250.0,
  }) async {
    final code = _generateInviteCode();
    final household = HouseholdModel(
      id: '',
      name: name,
      inviteCode: code,
      currency: currency,
      secondaryCurrency: secondaryCurrency,
      exchangeRate: exchangeRate,
      ownerId: ownerId,
      memberIds: [ownerId],
      createdAt: DateTime.now(),
    );

    final ref = await _db.collection('households').add(household.toFirestore());
    final id = ref.id;

    // Create default categories
    final categories = CategoryModel.defaultCategories(id);
    for (final cat in categories) {
      await _db.collection('households').doc(id).collection('categories').add(cat.toFirestore());
    }

    // Create initial monthly budget
    final monthKey = DateFormatter.currentMonthKey();
    await _db.collection('households').doc(id).collection('monthly_budgets').doc(monthKey).set({
      'householdId': id,
      'month': monthKey,
      'totalBudget': 0,
      'createdAt': Timestamp.now(),
    });

    // Update user
    await _db.collection('users').doc(ownerId).update({
      'householdId': id,
      'role': 'admin',
    });

    return household.copyWith(id: id);
  }

  Future<HouseholdModel?> joinHousehold(String inviteCode, String userId) async {
    final code = inviteCode.trim().toUpperCase().replaceAll('KASA-', '');
    
    final query = await _db.collection('households')
        .where('inviteCode', isEqualTo: code)
        .limit(1)
        .get();

    if (query.docs.isEmpty) return null;

    final doc = query.docs.first;
    final household = HouseholdModel.fromFirestore(doc);

    // Add user to household
    await _db.collection('households').doc(doc.id).update({
      'memberIds': FieldValue.arrayUnion([userId]),
    });

    // Update user
    await _db.collection('users').doc(userId).update({
      'householdId': doc.id,
      'role': 'member',
    });

    return household;
  }

  Stream<HouseholdModel?> householdStream(String householdId) {
    return _db.collection('households').doc(householdId).snapshots().map(
      (doc) => doc.exists ? HouseholdModel.fromFirestore(doc) : null,
    );
  }

  Future<void> updateHousehold(String householdId, Map<String, dynamic> data) async {
    await _db.collection('households').doc(householdId).update(data);
  }

  // ─── USERS ──────────────────────────────────────────────────

  Future<UserModel?> getUser(String userId) async {
    final doc = await _db.collection('users').doc(userId).get();
    return doc.exists ? UserModel.fromFirestore(doc) : null;
  }

  Stream<UserModel?> userStream(String userId) {
    return _db.collection('users').doc(userId).snapshots().map(
      (doc) => doc.exists ? UserModel.fromFirestore(doc) : null,
    );
  }

  Future<List<UserModel>> getHouseholdMembers(List<String> memberIds) async {
    if (memberIds.isEmpty) return [];
    final users = <UserModel>[];
    // Firestore 'in' query max 30 items
    for (var i = 0; i < memberIds.length; i += 30) {
      final batch = memberIds.sublist(i, (i + 30 > memberIds.length) ? memberIds.length : i + 30);
      final query = await _db.collection('users')
          .where(FieldPath.documentId, whereIn: batch)
          .get();
      users.addAll(query.docs.map(UserModel.fromFirestore));
    }
    return users;
  }

  // ─── CATEGORIES ─────────────────────────────────────────────

  Stream<List<CategoryModel>> categoriesStream(String householdId) {
    return _db.collection('households').doc(householdId)
        .collection('categories')
        .orderBy('order')
        .snapshots()
        .map((snap) => snap.docs.map(CategoryModel.fromFirestore).toList());
  }

  Future<void> addCategory(String householdId, CategoryModel category) async {
    await _db.collection('households').doc(householdId)
        .collection('categories').add(category.toFirestore());
  }

  Future<void> updateCategory(String householdId, String categoryId, Map<String, dynamic> data) async {
    await _db.collection('households').doc(householdId)
        .collection('categories').doc(categoryId).update(data);
  }

  Future<void> deleteCategory(String householdId, String categoryId) async {
    await _db.collection('households').doc(householdId)
        .collection('categories').doc(categoryId).delete();
  }

  // ─── MONTHLY BUDGET ─────────────────────────────────────────

  Stream<MonthlyBudgetModel?> budgetStream(String householdId) {
    final monthKey = DateFormatter.currentMonthKey();
    return _db.collection('households').doc(householdId)
        .collection('monthly_budgets').doc(monthKey).snapshots()
        .map((doc) => doc.exists ? MonthlyBudgetModel.fromFirestore(doc) : null);
  }

  Future<void> updateBudget(String householdId, double totalBudget) async {
    final monthKey = DateFormatter.currentMonthKey();
    await _db.collection('households').doc(householdId)
        .collection('monthly_budgets').doc(monthKey).set({
      'householdId': householdId,
      'month': monthKey,
      'totalBudget': totalBudget,
      'createdAt': Timestamp.now(),
    }, SetOptions(merge: true));
  }

  // ─── EXPENSES ───────────────────────────────────────────────

  Stream<List<ExpenseModel>> expensesStream(String householdId, {String? monthKey}) {
    final mk = monthKey ?? DateFormatter.currentMonthKey();
    final start = DateTime.parse('${mk}-01');
    final end = DateTime(start.year, start.month + 1, 0, 23, 59, 59);

    return _db.collection('households').doc(householdId)
        .collection('expenses')
        .where('createdAt', isGreaterThanOrEqualTo: Timestamp.fromDate(start))
        .where('createdAt', isLessThanOrEqualTo: Timestamp.fromDate(end))
        .orderBy('createdAt', descending: true)
        .snapshots()
        .map((snap) => snap.docs.map(ExpenseModel.fromFirestore).toList());
  }

  Future<void> addExpense(ExpenseModel expense) async {
    await _db.collection('households').doc(expense.householdId)
        .collection('expenses').add(expense.toFirestore());
  }

  Future<void> deleteExpense(String householdId, String expenseId) async {
    await _db.collection('households').doc(householdId)
        .collection('expenses').doc(expenseId).delete();
  }

  double calculateTotalExpenses(List<ExpenseModel> expenses, {String? categoryId}) {
    return expenses
        .where((e) => categoryId == null || e.categoryId == categoryId)
        .fold(0.0, (sum, e) => sum + e.amount);
  }

  // ─── PROVISION ITEMS ────────────────────────────────────────

  Stream<List<ProvisionItemModel>> provisionItemsStream(String householdId) {
    return _db.collection('households').doc(householdId)
        .collection('provision_items')
        .orderBy('createdAt', descending: true)
        .snapshots()
        .map((snap) => snap.docs.map(ProvisionItemModel.fromFirestore).toList());
  }

  Future<void> addProvisionItem(ProvisionItemModel item) async {
    await _db.collection('households').doc(item.householdId)
        .collection('provision_items').add(item.toFirestore());
  }

  Future<void> updateProvisionItem(String householdId, String itemId, Map<String, dynamic> data) async {
    await _db.collection('households').doc(householdId)
        .collection('provision_items').doc(itemId).update(data);
  }

  Future<void> purchaseProvisionItem({
    required String householdId,
    required String itemId,
    required double actualPrice,
    required String purchasedBy,
  }) async {
    await _db.collection('households').doc(householdId)
        .collection('provision_items').doc(itemId).update({
      'actualPrice': actualPrice,
      'status': 'purchased',
      'purchasedBy': purchasedBy,
      'purchasedAt': Timestamp.now(),
    });
  }

  Future<void> deleteProvisionItem(String householdId, String itemId) async {
    await _db.collection('households').doc(householdId)
        .collection('provision_items').doc(itemId).delete();
  }

  double calculateProvisionSpent(List<ProvisionItemModel> items) {
    return items
        .where((i) => i.isPurchased && i.actualPrice != null)
        .fold(0.0, (sum, i) => sum + i.actualPrice!);
  }

  double calculateProvisionRemaining(double budget, List<ProvisionItemModel> items) {
    return budget - calculateProvisionSpent(items);
  }

  double calculatePlannedUnpurchased(List<ProvisionItemModel> items) {
    return items
        .where((i) => i.isPlanned)
        .fold(0.0, (sum, i) => sum + i.plannedPrice);
  }

  double calculateProvisionForecast(double remaining, List<ProvisionItemModel> items) {
    return remaining - calculatePlannedUnpurchased(items);
  }

  // ─── MESSAGES / CHAT ────────────────────────────────────────

  Stream<List<MessageModel>> messagesStream(String householdId) {
    return _db.collection('households').doc(householdId)
        .collection('messages')
        .orderBy('createdAt', descending: true)
        .limit(100)
        .snapshots()
        .map((snap) => snap.docs.map(MessageModel.fromFirestore).toList());
  }

  Future<void> sendMessage(MessageModel message) async {
    await _db.collection('households').doc(message.householdId)
        .collection('messages').add(message.toFirestore());
  }

  Future<void> pinMessage(String householdId, String messageId, bool pinned) async {
    await _db.collection('households').doc(householdId)
        .collection('messages').doc(messageId).update({'pinned': pinned});
  }

  // ─── POLLS ──────────────────────────────────────────────────

  Stream<List<PollModel>> pollsStream(String householdId) {
    return _db.collection('households').doc(householdId)
        .collection('polls')
        .where('active', isEqualTo: true)
        .orderBy('createdAt', descending: true)
        .snapshots()
        .map((snap) => snap.docs.map(PollModel.fromFirestore).toList());
  }

  Future<void> createPoll(PollModel poll) async {
    await _db.collection('households').doc(poll.householdId)
        .collection('polls').add(poll.toFirestore());
  }

  Future<void> votePoll(String householdId, String pollId, int optionIndex, String userId) async {
    final doc = await _db.collection('households').doc(householdId)
        .collection('polls').doc(pollId).get();
    
    if (!doc.exists) return;
    final poll = PollModel.fromFirestore(doc);
    
    final options = List<PollOption>.from(poll.options);
    // Remove existing vote
    for (var i = 0; i < options.length; i++) {
      final voters = List<String>.from(options[i].voters);
      voters.remove(userId);
      options[i] = PollOption(text: options[i].text, voters: voters);
    }
    // Add new vote
    final targetVoters = List<String>.from(options[optionIndex].voters);
    targetVoters.add(userId);
    options[optionIndex] = PollOption(text: options[optionIndex].text, voters: targetVoters);

    await _db.collection('households').doc(householdId)
        .collection('polls').doc(pollId).update({
      'options': options.map((o) => o.toMap()).toList(),
    });
  }

  // ─── STATISTICS ─────────────────────────────────────────────

  Future<Map<String, double>> getExpensesByCategory(
    String householdId, 
    List<CategoryModel> categories,
  ) async {
    final monthKey = DateFormatter.currentMonthKey();
    final expenses = await _db.collection('households').doc(householdId)
        .collection('expenses')
        .where('createdAt', isGreaterThanOrEqualTo: Timestamp.fromDate(DateTime.parse('${monthKey}-01')))
        .where('createdAt', isLessThanOrEqualTo: Timestamp.fromDate(DateTime(DateTime.parse('${monthKey}-01').year, DateTime.parse('${monthKey}-01').month + 1, 0)))
        .get();

    final map = <String, double>{};
    for (final cat in categories) {
      final total = expenses.docs
          .map((d) => ExpenseModel.fromFirestore(d))
          .where((e) => e.categoryId == cat.id)
          .fold(0.0, (sum, e) => sum + e.amount);
      map[cat.name] = total;
    }
    return map;
  }

  // ─── HELPERS ────────────────────────────────────────────────

  String _generateInviteCode() {
    const chars = 'ABCDEFGHIJKLMNOPQRSTUVWXYZ0123456789';
    final rng = DateTime.now().microsecondsSinceEpoch;
    var code = '';
    for (var i = 0; i < 4; i++) {
      code += chars[(rng + i * 7) % chars.length];
    }
    return code;
  }
}
