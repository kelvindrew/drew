import 'package:cloud_firestore/cloud_firestore.dart';

class HouseholdModel {
  final String id;
  final String name;
  final String inviteCode;
  final String currency;
  final String secondaryCurrency;
  final double exchangeRate;
  final String ownerId;
  final List<String> memberIds;
  final DateTime createdAt;

  HouseholdModel({
    required this.id,
    required this.name,
    required this.inviteCode,
    this.currency = 'USD',
    this.secondaryCurrency = 'CDF',
    this.exchangeRate = 2250.0,
    required this.ownerId,
    this.memberIds = const [],
    required this.createdAt,
  });

  factory HouseholdModel.fromFirestore(DocumentSnapshot doc) {
    final data = doc.data() as Map<String, dynamic>;
    return HouseholdModel(
      id: doc.id,
      name: data['name'] ?? '',
      inviteCode: data['inviteCode'] ?? '',
      currency: data['currency'] ?? 'USD',
      secondaryCurrency: data['secondaryCurrency'] ?? 'CDF',
      exchangeRate: (data['exchangeRate'] ?? 2250).toDouble(),
      ownerId: data['ownerId'] ?? '',
      memberIds: List<String>.from(data['memberIds'] ?? []),
      createdAt: (data['createdAt'] as Timestamp?)?.toDate() ?? DateTime.now(),
    );
  }

  Map<String, dynamic> toFirestore() {
    return {
      'name': name,
      'inviteCode': inviteCode,
      'currency': currency,
      'secondaryCurrency': secondaryCurrency,
      'exchangeRate': exchangeRate,
      'ownerId': ownerId,
      'memberIds': memberIds,
      'createdAt': Timestamp.fromDate(createdAt),
    };
  }

  String get formattedInviteCode => 'KASA-$inviteCode';

  HouseholdModel copyWith({
    String? id,
    String? name,
    String? inviteCode,
    String? currency,
    String? secondaryCurrency,
    double? exchangeRate,
    String? ownerId,
    List<String>? memberIds,
    DateTime? createdAt,
  }) {
    return HouseholdModel(
      id: id ?? this.id,
      name: name ?? this.name,
      inviteCode: inviteCode ?? this.inviteCode,
      currency: currency ?? this.currency,
      secondaryCurrency: secondaryCurrency ?? this.secondaryCurrency,
      exchangeRate: exchangeRate ?? this.exchangeRate,
      ownerId: ownerId ?? this.ownerId,
      memberIds: memberIds ?? this.memberIds,
      createdAt: createdAt ?? this.createdAt,
    );
  }
}
