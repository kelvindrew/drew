import 'package:flutter/material.dart';
import 'package:flutter_riverpod/flutter_riverpod.dart';
import 'package:google_fonts/google_fonts.dart';
import '../../app/theme/app_theme.dart';
import '../../models/expense_model.dart';
import '../../models/message_model.dart';
import '../../models/provision_item_model.dart';
import '../../providers/category_provider.dart';
import '../../providers/household_provider.dart';
import '../../providers/provision_provider.dart';
import '../../providers/auth_provider.dart';
import '../../providers/firestore_service_provider.dart';
import '../../utils/currency_formatter.dart';

class ShoppingListScreen extends ConsumerStatefulWidget {
  const ShoppingListScreen({super.key});

  @override
  ConsumerState<ShoppingListScreen> createState() => _ShoppingListScreenState();
}

class _ShoppingListScreenState extends ConsumerState<ShoppingListScreen> {
  @override
  Widget build(BuildContext context) {
    final colors = AppTheme.of(context);
    final shoppingList = ref.watch(shoppingListProvider);

    return Scaffold(
      appBar: AppBar(
        title: const Text('🛒 Liste de courses'),
      ),
      body: shoppingList.isEmpty
          ? Center(
              child: Column(
                mainAxisAlignment: MainAxisAlignment.center,
                children: [
                  const Text('✅', style: TextStyle(fontSize: 64)),
                  const SizedBox(height: 16),
                  Text(
                    'Tout est acheté !',
                    style: GoogleFonts.inter(
                      fontSize: 20,
                      fontWeight: FontWeight.w600,
                      color: colors.textPrimary,
                    ),
                  ),
                  const SizedBox(height: 8),
                  Text(
                    'Aucun article en attente.',
                    style: GoogleFonts.inter(
                      fontSize: 14,
                      color: colors.textSecondary,
                    ),
                  ),
                ],
              ),
            )
          : ListView.builder(
              padding: const EdgeInsets.all(20),
              itemCount: shoppingList.length,
              itemBuilder: (context, index) {
                final item = shoppingList[index];
                return _ShoppingItemCard(
                  item: item,
                  onPurchase: () => _showPurchaseDialog(item),
                );
              },
            ),
    );
  }

  void _showPurchaseDialog(ProvisionItemModel item) {
    final colors = AppTheme.of(context);
    final priceController = TextEditingController(text: item.plannedPrice.toStringAsFixed(0));
    
    showDialog(
      context: context,
      builder: (context) => AlertDialog(
        title: Text(item.name),
        content: Column(
          mainAxisSize: MainAxisSize.min,
          crossAxisAlignment: CrossAxisAlignment.start,
          children: [
            Text(
              'Prix prévu: ${CurrencyFormatter.format(item.plannedPrice, "USD")}',
              style: GoogleFonts.inter(fontSize: 14, color: colors.textSecondary),
            ),
            const SizedBox(height: 16),
            TextField(
              controller: priceController,
              keyboardType: TextInputType.number,
              decoration: const InputDecoration(
                labelText: 'Prix réellement payé',
                prefixIcon: Icon(Icons.attach_money),
              ),
            ),
            const SizedBox(height: 8),
            Text(
              'Acheté par: ${ref.read(userProfileProvider).valueOrNull?.name ?? "Vous"}',
              style: GoogleFonts.inter(fontSize: 13, color: colors.textTertiary),
            ),
          ],
        ),
        actions: [
          TextButton(
            onPressed: () => Navigator.pop(context),
            child: const Text('Annuler'),
          ),
          ElevatedButton(
            onPressed: () {
              final price = double.tryParse(priceController.text) ?? item.plannedPrice;
              _confirmPurchase(item, price);
              Navigator.pop(context);
            },
            child: const Text('Confirmer l\'achat'),
          ),
        ],
      ),
    );
  }

  void _confirmPurchase(ProvisionItemModel item, double actualPrice) async {
    final colors = AppTheme.of(context);
    final household = ref.read(householdProvider).valueOrNull;
    final user = ref.read(userProfileProvider).valueOrNull;
    if (household == null || user == null) return;

    try {
      final firestore = ref.read(firestoreServiceProvider);

      // 1. Mark provision item as purchased
      await firestore.purchaseProvisionItem(
        householdId: household.id,
        itemId: item.id,
        actualPrice: actualPrice,
        purchasedBy: user.id,
      );

      // 2. Add linked expense under Provision category
      final categories = ref.read(activeCategoriesProvider);
      final provCategory = categories
          .where((c) => c.name.toLowerCase().contains('provision'))
          .firstOrNull;

      final expense = ExpenseModel(
        id: '',
        householdId: household.id,
        categoryId: provCategory?.id ?? (categories.isNotEmpty ? categories.first.id : ''),
        userId: user.id,
        amount: actualPrice,
        currency: household.currency,
        note: 'Achat provision: ${item.name} (${item.quantity} ${item.unit})',
        createdAt: DateTime.now(),
      );
      await firestore.addExpense(expense);

      // 3. Post system message to household chat
      final formattedPrice = CurrencyFormatter.format(actualPrice, household.currency);
      final message = MessageModel(
        id: '',
        householdId: household.id,
        userId: user.id,
        text: '🛒 ${user.name} a acheté "${item.name}" pour $formattedPrice',
        type: 'system',
        createdAt: DateTime.now(),
      );
      await firestore.sendMessage(message);

      if (mounted) {
        ScaffoldMessenger.of(context).showSnackBar(
          SnackBar(
            content: Text('${item.name} acheté ($formattedPrice) et enregistré !'),
            backgroundColor: colors.success,
          ),
        );
      }
    } catch (e) {
      if (mounted) {
        ScaffoldMessenger.of(context).showSnackBar(
          SnackBar(content: Text('Erreur: $e'), backgroundColor: colors.error),
        );
      }
    }
  }
}

class _ShoppingItemCard extends StatelessWidget {
  final ProvisionItemModel item;
  final VoidCallback onPurchase;

  const _ShoppingItemCard({required this.item, required this.onPurchase});

  @override
  Widget build(BuildContext context) {
    final colors = AppTheme.of(context);
    return Container(
      margin: const EdgeInsets.only(bottom: 12),
      padding: const EdgeInsets.all(16),
      decoration: BoxDecoration(
        color: colors.surface,
        borderRadius: BorderRadius.circular(AppTheme.radiusLg),
        border: Border.all(color: colors.border),
      ),
      child: Row(
        children: [
          Container(
            width: 44,
            height: 44,
            decoration: BoxDecoration(
              color: colors.primaryLight,
              borderRadius: BorderRadius.circular(12),
            ),
            child: Center(
              child: Text(
                item.priority == 'high' ? '🔴' : item.priority == 'medium' ? '🟡' : '🟢',
                style: const TextStyle(fontSize: 20),
              ),
            ),
          ),
          const SizedBox(width: 14),
          Expanded(
            child: Column(
              crossAxisAlignment: CrossAxisAlignment.start,
              children: [
                Text(
                  item.name,
                  style: GoogleFonts.inter(
                    fontSize: 15,
                    fontWeight: FontWeight.w600,
                    color: colors.textPrimary,
                  ),
                ),
                const SizedBox(height: 4),
                Text(
                  '${item.quantity} ${item.unit}',
                  style: GoogleFonts.inter(
                    fontSize: 13,
                    color: colors.textSecondary,
                  ),
                ),
              ],
            ),
          ),
          Column(
            crossAxisAlignment: CrossAxisAlignment.end,
            children: [
              Text(
                CurrencyFormatter.format(item.plannedPrice, 'USD'),
                style: GoogleFonts.inter(
                  fontSize: 15,
                  fontWeight: FontWeight.w600,
                  color: colors.primary,
                ),
              ),
              const SizedBox(height: 4),
              GestureDetector(
                onTap: onPurchase,
                child: Container(
                  padding: const EdgeInsets.symmetric(horizontal: 10, vertical: 4),
                  decoration: BoxDecoration(
                    color: colors.primary,
                    borderRadius: BorderRadius.circular(8),
                  ),
                  child: Text(
                    'Acheter',
                    style: GoogleFonts.inter(
                      fontSize: 12,
                      fontWeight: FontWeight.w500,
                      color: Colors.white,
                    ),
                  ),
                ),
              ),
            ],
          ),
        ],
      ),
    );
  }
}
