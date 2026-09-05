import 'package:flutter_riverpod/flutter_riverpod.dart';
import '../models/household_model.dart';
import 'auth_provider.dart';
import 'firestore_service_provider.dart';

final householdProvider = StreamProvider<HouseholdModel?>((ref) {
  final userProfile = ref.watch(userProfileProvider).valueOrNull;
  if (userProfile == null || userProfile.householdId.isEmpty) {
    return Stream.value(null);
  }
  return ref.watch(firestoreServiceProvider).householdStream(userProfile.householdId);
});
