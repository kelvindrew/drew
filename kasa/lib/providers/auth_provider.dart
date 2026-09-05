import 'package:flutter_riverpod/flutter_riverpod.dart';
import 'package:firebase_auth/firebase_auth.dart';
import '../models/user_model.dart';
import '../services/auth_service.dart';

final authServiceProvider = Provider<AuthService>((ref) => AuthService());

final authStateProvider = StreamProvider<User?>((ref) {
  return ref.watch(authServiceProvider).authStateChanges;
});

final userProfileProvider = StreamProvider<UserModel?>((ref) {
  final authUser = ref.watch(authStateProvider).valueOrNull;
  if (authUser == null) return Stream.value(null);
  return ref.watch(authServiceProvider).userStream(authUser.uid);
});

final householdIdProvider = Provider<String?>((ref) {
  final profile = ref.watch(userProfileProvider).valueOrNull;
  return profile?.householdId.isNotEmpty == true ? profile!.householdId : null;
});
