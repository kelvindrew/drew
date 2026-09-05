import 'package:flutter_riverpod/flutter_riverpod.dart';
import '../models/category_model.dart';
import 'household_provider.dart';
import 'firestore_service_provider.dart';

final categoriesProvider = StreamProvider<List<CategoryModel>>((ref) {
  final household = ref.watch(householdProvider).valueOrNull;
  if (household == null) return Stream.value([]);
  return ref.watch(firestoreServiceProvider).categoriesStream(household.id);
});

final activeCategoriesProvider = Provider<List<CategoryModel>>((ref) {
  final categories = ref.watch(categoriesProvider).valueOrNull ?? [];
  return categories.where((c) => c.active).toList();
});
