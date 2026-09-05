import 'package:flutter_riverpod/flutter_riverpod.dart';
import '../models/provision_item_model.dart';
import 'household_provider.dart';
import 'firestore_service_provider.dart';
import 'category_provider.dart';

final provisionItemsProvider = StreamProvider<List<ProvisionItemModel>>((ref) {
  final household = ref.watch(householdProvider).valueOrNull;
  if (household == null) return Stream.value([]);
  return ref.watch(firestoreServiceProvider).provisionItemsStream(household.id);
});

final provisionBudgetProvider = Provider<double>((ref) {
  final categories = ref.watch(activeCategoriesProvider);
  final provisionCat = categories.where((c) => c.name == 'Provision').toList();
  if (provisionCat.isEmpty) return 0;
  return provisionCat.first.plannedAmount;
});

final provisionSpentProvider = Provider<double>((ref) {
  final items = ref.watch(provisionItemsProvider).valueOrNull ?? [];
  return ref.watch(firestoreServiceProvider).calculateProvisionSpent(items);
});

final provisionRemainingProvider = Provider<double>((ref) {
  final budget = ref.watch(provisionBudgetProvider);
  final spent = ref.watch(provisionSpentProvider);
  return budget - spent;
});

final plannedUnpurchasedProvider = Provider<double>((ref) {
  final items = ref.watch(provisionItemsProvider).valueOrNull ?? [];
  return ref.watch(firestoreServiceProvider).calculatePlannedUnpurchased(items);
});

final provisionForecastProvider = Provider<double>((ref) {
  final remaining = ref.watch(provisionRemainingProvider);
  final planned = ref.watch(plannedUnpurchasedProvider);
  return remaining - planned;
});

final shoppingListProvider = Provider<List<ProvisionItemModel>>((ref) {
  final items = ref.watch(provisionItemsProvider).valueOrNull ?? [];
  return items.where((i) => i.isPlanned).toList();
});
