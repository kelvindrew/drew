import 'package:cloud_firestore/cloud_firestore.dart';

class ProvisionItemModel {
  final String id;
  final String householdId;
  final String name;
  final int quantity;
  final String unit;
  final double plannedPrice;
  final double? actualPrice;
  final String status;
  final String priority;
  final String? purchasedBy;
  final DateTime? purchasedAt;
  final String? categoryId;
  final String? imageUrl;
  final DateTime createdAt;

  ProvisionItemModel({
    required this.id,
    required this.householdId,
    required this.name,
    this.quantity = 1,
    this.unit = 'pièce',
    required this.plannedPrice,
    this.actualPrice,
    this.status = 'planned',
    this.priority = 'medium',
    this.purchasedBy,
    this.purchasedAt,
    this.categoryId,
    this.imageUrl,
    required this.createdAt,
  });

  factory ProvisionItemModel.fromFirestore(DocumentSnapshot doc) {
    final data = doc.data() as Map<String, dynamic>;
    return ProvisionItemModel(
      id: doc.id,
      householdId: data['householdId'] ?? '',
      name: data['name'] ?? '',
      quantity: data['quantity'] ?? 1,
      unit: data['unit'] ?? 'pièce',
      plannedPrice: (data['plannedPrice'] ?? 0).toDouble(),
      actualPrice: data['actualPrice']?.toDouble(),
      status: data['status'] ?? 'planned',
      priority: data['priority'] ?? 'medium',
      purchasedBy: data['purchasedBy'],
      purchasedAt: (data['purchasedAt'] as Timestamp?)?.toDate(),
      categoryId: data['categoryId'],
      imageUrl: data['imageUrl'],
      createdAt: (data['createdAt'] as Timestamp?)?.toDate() ?? DateTime.now(),
    );
  }

  Map<String, dynamic> toFirestore() {
    return {
      'householdId': householdId,
      'name': name,
      'quantity': quantity,
      'unit': unit,
      'plannedPrice': plannedPrice,
      'actualPrice': actualPrice,
      'status': status,
      'priority': priority,
      'purchasedBy': purchasedBy,
      'purchasedAt': purchasedAt != null ? Timestamp.fromDate(purchasedAt!) : null,
      'categoryId': categoryId,
      'imageUrl': imageUrl,
      'createdAt': Timestamp.fromDate(createdAt),
    };
  }

  bool get isPurchased => status == 'purchased';
  bool get isPlanned => status == 'planned';

  double get variance {
    if (actualPrice == null) return 0;
    return actualPrice! - plannedPrice;
  }

  bool get isOverBudget => variance > 0;
  bool get isUnderBudget => variance < 0;

  ProvisionItemModel copyWith({
    String? id,
    String? householdId,
    String? name,
    int? quantity,
    String? unit,
    double? plannedPrice,
    double? actualPrice,
    String? status,
    String? priority,
    String? purchasedBy,
    DateTime? purchasedAt,
    String? categoryId,
    String? imageUrl,
    DateTime? createdAt,
  }) {
    return ProvisionItemModel(
      id: id ?? this.id,
      householdId: householdId ?? this.householdId,
      name: name ?? this.name,
      quantity: quantity ?? this.quantity,
      unit: unit ?? this.unit,
      plannedPrice: plannedPrice ?? this.plannedPrice,
      actualPrice: actualPrice ?? this.actualPrice,
      status: status ?? this.status,
      priority: priority ?? this.priority,
      purchasedBy: purchasedBy ?? this.purchasedBy,
      purchasedAt: purchasedAt ?? this.purchasedAt,
      categoryId: categoryId ?? this.categoryId,
      imageUrl: imageUrl ?? this.imageUrl,
      createdAt: createdAt ?? this.createdAt,
    );
  }
}
