import 'package:cloud_firestore/cloud_firestore.dart';

class CategoryModel {
  final String id;
  final String householdId;
  final String name;
  final String icon;
  final double plannedAmount;
  final bool active;
  final int order;

  CategoryModel({
    required this.id,
    required this.householdId,
    required this.name,
    this.icon = '📦',
    this.plannedAmount = 0,
    this.active = true,
    this.order = 0,
  });

  factory CategoryModel.fromFirestore(DocumentSnapshot doc) {
    final data = doc.data() as Map<String, dynamic>;
    return CategoryModel(
      id: doc.id,
      householdId: data['householdId'] ?? '',
      name: data['name'] ?? '',
      icon: data['icon'] ?? '📦',
      plannedAmount: (data['plannedAmount'] ?? 0).toDouble(),
      active: data['active'] ?? true,
      order: data['order'] ?? 0,
    );
  }

  Map<String, dynamic> toFirestore() {
    return {
      'householdId': householdId,
      'name': name,
      'icon': icon,
      'plannedAmount': plannedAmount,
      'active': active,
      'order': order,
    };
  }

  CategoryModel copyWith({
    String? id,
    String? householdId,
    String? name,
    String? icon,
    double? plannedAmount,
    bool? active,
    int? order,
  }) {
    return CategoryModel(
      id: id ?? this.id,
      householdId: householdId ?? this.householdId,
      name: name ?? this.name,
      icon: icon ?? this.icon,
      plannedAmount: plannedAmount ?? this.plannedAmount,
      active: active ?? this.active,
      order: order ?? this.order,
    );
  }

  static List<CategoryModel> defaultCategories(String householdId) {
    return [
      CategoryModel(id: '', householdId: householdId, name: 'Loyer', icon: '🏠', plannedAmount: 0, order: 0),
      CategoryModel(id: '', householdId: householdId, name: 'Provision', icon: '🍚', plannedAmount: 0, order: 1),
      CategoryModel(id: '', householdId: householdId, name: 'Connexion Internet', icon: '🌐', plannedAmount: 0, order: 2),
      CategoryModel(id: '', householdId: householdId, name: 'Femme de ménage', icon: '🧹', plannedAmount: 0, order: 3),
      CategoryModel(id: '', householdId: householdId, name: 'Électricité', icon: '💡', plannedAmount: 0, order: 4),
      CategoryModel(id: '', householdId: householdId, name: 'Eau', icon: '🚰', plannedAmount: 0, order: 5),
      CategoryModel(id: '', householdId: householdId, name: 'Entretien', icon: '🛠️', plannedAmount: 0, order: 6),
      CategoryModel(id: '', householdId: householdId, name: 'Autres', icon: '📦', plannedAmount: 0, order: 7),
    ];
  }
}
