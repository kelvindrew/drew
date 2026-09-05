import 'package:cloud_firestore/cloud_firestore.dart';

class ExpenseModel {
  final String id;
  final String householdId;
  final String categoryId;
  final String userId;
  final double amount;
  final String currency;
  final String note;
  final String receiptUrl;
  final bool isProvisionItem;
  final String provisionItemId;
  final DateTime createdAt;

  ExpenseModel({
    required this.id,
    required this.householdId,
    required this.categoryId,
    required this.userId,
    required this.amount,
    this.currency = 'USD',
    this.note = '',
    this.receiptUrl = '',
    this.isProvisionItem = false,
    this.provisionItemId = '',
    required this.createdAt,
  });

  factory ExpenseModel.fromFirestore(DocumentSnapshot doc) {
    final data = doc.data() as Map<String, dynamic>;
    return ExpenseModel(
      id: doc.id,
      householdId: data['householdId'] ?? '',
      categoryId: data['categoryId'] ?? '',
      userId: data['userId'] ?? '',
      amount: (data['amount'] ?? 0).toDouble(),
      currency: data['currency'] ?? 'USD',
      note: data['note'] ?? '',
      receiptUrl: data['receiptUrl'] ?? '',
      isProvisionItem: data['isProvisionItem'] ?? false,
      provisionItemId: data['provisionItemId'] ?? '',
      createdAt: (data['createdAt'] as Timestamp?)?.toDate() ?? DateTime.now(),
    );
  }

  Map<String, dynamic> toFirestore() {
    return {
      'householdId': householdId,
      'categoryId': categoryId,
      'userId': userId,
      'amount': amount,
      'currency': currency,
      'note': note,
      'receiptUrl': receiptUrl,
      'isProvisionItem': isProvisionItem,
      'provisionItemId': provisionItemId,
      'createdAt': Timestamp.fromDate(createdAt),
    };
  }

  ExpenseModel copyWith({
    String? id,
    String? householdId,
    String? categoryId,
    String? userId,
    double? amount,
    String? currency,
    String? note,
    String? receiptUrl,
    bool? isProvisionItem,
    String? provisionItemId,
    DateTime? createdAt,
  }) {
    return ExpenseModel(
      id: id ?? this.id,
      householdId: householdId ?? this.householdId,
      categoryId: categoryId ?? this.categoryId,
      userId: userId ?? this.userId,
      amount: amount ?? this.amount,
      currency: currency ?? this.currency,
      note: note ?? this.note,
      receiptUrl: receiptUrl ?? this.receiptUrl,
      isProvisionItem: isProvisionItem ?? this.isProvisionItem,
      provisionItemId: provisionItemId ?? this.provisionItemId,
      createdAt: createdAt ?? this.createdAt,
    );
  }
}
