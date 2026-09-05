import 'package:cloud_firestore/cloud_firestore.dart';

class MonthlyBudgetModel {
  final String id;
  final String householdId;
  final String month;
  final double totalBudget;
  final DateTime createdAt;

  MonthlyBudgetModel({
    required this.id,
    required this.householdId,
    required this.month,
    required this.totalBudget,
    required this.createdAt,
  });

  factory MonthlyBudgetModel.fromFirestore(DocumentSnapshot doc) {
    final data = doc.data() as Map<String, dynamic>;
    return MonthlyBudgetModel(
      id: doc.id,
      householdId: data['householdId'] ?? '',
      month: data['month'] ?? '',
      totalBudget: (data['totalBudget'] ?? 0).toDouble(),
      createdAt: (data['createdAt'] as Timestamp?)?.toDate() ?? DateTime.now(),
    );
  }

  Map<String, dynamic> toFirestore() {
    return {
      'householdId': householdId,
      'month': month,
      'totalBudget': totalBudget,
      'createdAt': Timestamp.fromDate(createdAt),
    };
  }

  static String currentMonth() {
    final now = DateTime.now();
    return '${now.year}-${now.month.toString().padLeft(2, '0')}';
  }

  MonthlyBudgetModel copyWith({
    String? id,
    String? householdId,
    String? month,
    double? totalBudget,
    DateTime? createdAt,
  }) {
    return MonthlyBudgetModel(
      id: id ?? this.id,
      householdId: householdId ?? this.householdId,
      month: month ?? this.month,
      totalBudget: totalBudget ?? this.totalBudget,
      createdAt: createdAt ?? this.createdAt,
    );
  }
}
