import 'package:cloud_firestore/cloud_firestore.dart';

class MessageModel {
  final String id;
  final String householdId;
  final String userId;
  final String text;
  final String type;
  final String attachmentUrl;
  final bool pinned;
  final Map<String, List<String>> reactions;
  final DateTime createdAt;

  MessageModel({
    required this.id,
    required this.householdId,
    required this.userId,
    required this.text,
    this.type = 'text',
    this.attachmentUrl = '',
    this.pinned = false,
    this.reactions = const {},
    required this.createdAt,
  });

  factory MessageModel.fromFirestore(DocumentSnapshot doc) {
    final data = doc.data() as Map<String, dynamic>;
    final reactionsData = data['reactions'] as Map<String, dynamic>? ?? {};
    final reactions = <String, List<String>>{};
    reactionsData.forEach((key, value) {
      reactions[key] = List<String>.from(value ?? []);
    });

    return MessageModel(
      id: doc.id,
      householdId: data['householdId'] ?? '',
      userId: data['userId'] ?? '',
      text: data['text'] ?? '',
      type: data['type'] ?? 'text',
      attachmentUrl: data['attachmentUrl'] ?? '',
      pinned: data['pinned'] ?? false,
      reactions: reactions,
      createdAt: (data['createdAt'] as Timestamp?)?.toDate() ?? DateTime.now(),
    );
  }

  Map<String, dynamic> toFirestore() {
    return {
      'householdId': householdId,
      'userId': userId,
      'text': text,
      'type': type,
      'attachmentUrl': attachmentUrl,
      'pinned': pinned,
      'reactions': reactions.map((key, value) => MapEntry(key, value)),
      'createdAt': Timestamp.fromDate(createdAt),
    };
  }

  bool get isText => type == 'text';
  bool get isExpense => type == 'expense';
  bool get isSystem => type == 'system';
  bool get isPoll => type == 'poll';

  MessageModel copyWith({
    String? id,
    String? householdId,
    String? userId,
    String? text,
    String? type,
    String? attachmentUrl,
    bool? pinned,
    Map<String, List<String>>? reactions,
    DateTime? createdAt,
  }) {
    return MessageModel(
      id: id ?? this.id,
      householdId: householdId ?? this.householdId,
      userId: userId ?? this.userId,
      text: text ?? this.text,
      type: type ?? this.type,
      attachmentUrl: attachmentUrl ?? this.attachmentUrl,
      pinned: pinned ?? this.pinned,
      reactions: reactions ?? this.reactions,
      createdAt: createdAt ?? this.createdAt,
    );
  }
}
