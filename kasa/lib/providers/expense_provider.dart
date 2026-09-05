import 'package:flutter_riverpod/flutter_riverpod.dart';
import '../models/expense_model.dart';
import 'household_provider.dart';
import 'firestore_service_provider.dart';

final expensesProvider = StreamProvider<List<ExpenseModel>>((ref) {
  final household = ref.watch(householdProvider).valueOrNull;
  if (household == null) return Stream.value([]);
  return ref.watch(firestoreServiceProvider).expensesStream(household.id);
});
