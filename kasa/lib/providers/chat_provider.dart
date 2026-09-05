import 'package:flutter_riverpod/flutter_riverpod.dart';
import '../models/message_model.dart';
import '../models/poll_model.dart';
import 'household_provider.dart';
import 'firestore_service_provider.dart';

final messagesProvider = StreamProvider<List<MessageModel>>((ref) {
  final household = ref.watch(householdProvider).valueOrNull;
  if (household == null) return Stream.value([]);
  return ref.watch(firestoreServiceProvider).messagesStream(household.id);
});

final pollsProvider = StreamProvider<List<PollModel>>((ref) {
  final household = ref.watch(householdProvider).valueOrNull;
  if (household == null) return Stream.value([]);
  return ref.watch(firestoreServiceProvider).pollsStream(household.id);
});
