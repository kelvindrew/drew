import 'package:flutter_riverpod/flutter_riverpod.dart';
import '../models/monthly_budget_model.dart';
import 'household_provider.dart';
import 'firestore_service_provider.dart';
import 'expense_provider.dart';

final budgetProvider = StreamProvider<MonthlyBudgetModel?>((ref) {
  final household = ref.watch(householdProvider).valueOrNull;
  if (household == null) return Stream.value(null);
  return ref.watch(firestoreServiceProvider).budgetStream(household.id);
});

final totalExpensesProvider = Provider<double>((ref) {
  final expenses = ref.watch(expensesProvider).valueOrNull ?? [];
  return expenses.fold(0.0, (sum, e) => sum + e.amount);
});

final budgetRemainingProvider = Provider<double>((ref) {
  final budget = ref.watch(budgetProvider).valueOrNull;
  final totalExpenses = ref.watch(totalExpensesProvider);
  if (budget == null) return 0;
  return budget.totalBudget - totalExpenses;
});

final budgetPercentageProvider = Provider<double>((ref) {
  final budget = ref.watch(budgetProvider).valueOrNull;
  final totalExpenses = ref.watch(totalExpensesProvider);
  if (budget == null || budget.totalBudget == 0) return 0;
  return (totalExpenses / budget.totalBudget).clamp(0.0, 1.0);
});
