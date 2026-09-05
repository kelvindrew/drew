import 'package:go_router/go_router.dart';
import 'package:flutter_riverpod/flutter_riverpod.dart';
import '../screens/welcome/welcome_screen.dart';
import '../screens/auth/name_screen.dart';
import '../screens/auth/create_household_screen.dart';
import '../screens/auth/join_household_screen.dart';
import '../screens/home/home_screen.dart';
import '../screens/provision/provision_screen.dart';
import '../screens/provision/shopping_list_screen.dart';
import '../screens/expenses/expenses_screen.dart';
import '../screens/expenses/add_expense_screen.dart';
import '../screens/chat/chat_screen.dart';
import '../screens/statistics/statistics_screen.dart';
import '../screens/budget/budget_screen.dart';
import '../screens/members/members_screen.dart';
import '../screens/settings/settings_screen.dart';
import '../providers/auth_provider.dart';

final routerProvider = Provider<GoRouter>((ref) {
  final authState = ref.watch(authStateProvider);
  
  return GoRouter(
    initialLocation: '/',
    redirect: (context, state) {
      final isLoggedIn = authState.valueOrNull != null;
      final isOnAuth = state.matchedLocation == '/' ||
          state.matchedLocation == '/name' ||
          state.matchedLocation == '/create-household' ||
          state.matchedLocation == '/join-household';
      
      if (!isLoggedIn && !isOnAuth) return '/';
      return null;
    },
    routes: [
      GoRoute(path: '/', builder: (context, state) => const WelcomeScreen()),
      GoRoute(path: '/name', builder: (context, state) => const NameScreen()),
      GoRoute(
        path: '/create-household',
        builder: (context, state) {
          final userName = state.extra as String? ?? '';
          return CreateHouseholdScreen(userName: userName);
        },
      ),
      GoRoute(path: '/join-household', builder: (context, state) => const JoinHouseholdScreen()),
      
      // Shell with bottom navigation
      ShellRoute(
        builder: (context, state, child) => HomeScreen(child: child),
        routes: [
          GoRoute(path: '/home', builder: (context, state) => const BudgetScreen()),
          GoRoute(path: '/home/provision', builder: (context, state) => const ProvisionScreen()),
          GoRoute(path: '/home/provision/list', builder: (context, state) => const ShoppingListScreen()),
          GoRoute(path: '/home/expenses', builder: (context, state) => const ExpensesScreen()),
          GoRoute(path: '/home/chat', builder: (context, state) => const ChatScreen()),
          GoRoute(path: '/home/statistics', builder: (context, state) => const StatisticsScreen()),
        ],
      ),
      
      GoRoute(path: '/home/expenses/add', builder: (context, state) => const AddExpenseScreen()),
      GoRoute(path: '/home/members', builder: (context, state) => const MembersScreen()),
      GoRoute(path: '/home/settings', builder: (context, state) => const SettingsScreen()),
    ],
  );
});
