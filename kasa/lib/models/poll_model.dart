import 'package:cloud_firestore/cloud_firestore.dart';

class PollOption {
  final String text;
  final List<String> voters;

  PollOption({required this.text, this.voters = const []});

  factory PollOption.fromMap(Map<String, dynamic> map) {
    return PollOption(
      text: map['text'] ?? '',
      voters: List<String>.from(map['voters'] ?? []),
    );
  }

  Map<String, dynamic> toMap() {
    return {
      'text': text,
      'voters': voters,
    };
  }

  int get voteCount => voters.length;
}

class PollModel {
  final String id;
  final String householdId;
  final String creatorId;
  final String question;
  final List<PollOption> options;
  final bool active;
  final DateTime createdAt;

  PollModel({
    required this.id,
    required this.householdId,
    required this.creatorId,
    required this.question,
    required this.options,
    this.active = true,
    required this.createdAt,
  });

  factory PollModel.fromFirestore(DocumentSnapshot doc) {
    final data = doc.data() as Map<String, dynamic>;
    final optionsData = data['options'] as List<dynamic>? ?? [];
    final options = optionsData.map((o) => PollOption.fromMap(o as Map<String, dynamic>)).toList();

    return PollModel(
      id: doc.id,
      householdId: data['householdId'] ?? '',
      creatorId: data['creatorId'] ?? '',
      question: data['question'] ?? '',
      options: options,
      active: data['active'] ?? true,
      createdAt: (data['createdAt'] as Timestamp?)?.toDate() ?? DateTime.now(),
    );
  }

  Map<String, dynamic> toFirestore() {
    return {
      'householdId': householdId,
      'creatorId': creatorId,
      'question': question,
      'options': options.map((o) => o.toMap()).toList(),
      'active': active,
      'createdAt': Timestamp.fromDate(createdAt),
    };
  }

  int get totalVotes => options.fold(0, (sum, o) => sum + o.voteCount);

  PollOption? get leadingOption {
    if (options.isEmpty) return null;
    return options.reduce((a, b) => a.voteCount > b.voteCount ? a : b);
  }

  PollModel copyWith({
    String? id,
    String? householdId,
    String? creatorId,
    String? question,
    List<PollOption>? options,
    bool? active,
    DateTime? createdAt,
  }) {
    return PollModel(
      id: id ?? this.id,
      householdId: householdId ?? this.householdId,
      creatorId: creatorId ?? this.creatorId,
      question: question ?? this.question,
      options: options ?? this.options,
      active: active ?? this.active,
      createdAt: createdAt ?? this.createdAt,
    );
  }
}
