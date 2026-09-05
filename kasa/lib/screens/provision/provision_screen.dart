import 'package:flutter/material.dart';
import 'package:flutter_riverpod/flutter_riverpod.dart';
import 'package:go_router/go_router.dart';
import 'package:google_fonts/google_fonts.dart';
import 'package:percent_indicator/percent_indicator.dart';
import '../../app/theme/app_theme.dart';
import '../../models/provision_item_model.dart';
import '../../providers/provision_provider.dart';
import '../../providers/firestore_service_provider.dart';
import '../../providers/household_provider.dart';
import '../../utils/currency_formatter.dart';

class ProvisionScreen extends ConsumerWidget {
  const ProvisionScreen({super.key});

  @override
  Widget build(BuildContext context, WidgetRef ref) {
    final colors = AppTheme.of(context);
    final budget = ref.watch(provisionBudgetProvider);
    final spent = ref.watch(provisionSpentProvider);
    final remaining = ref.watch(provisionRemainingProvider);
    final planned = ref.watch(plannedUnpurchasedProvider);
    final forecast = ref.watch(provisionForecastProvider);
    final items = ref.watch(provisionItemsProvider).valueOrNull ?? [];
    final shoppingList = ref.watch(shoppingListProvider);

    final percentage = budget > 0 ? (spent / budget).clamp(0.0, 1.0) : 0.0;

    return Scaffold(
      appBar: AppBar(
        title: const Text('🍚 Provision'),
        actions: [
          IconButton(
            onPressed: () => _showAddItemDialog(context, ref),
            icon: const Icon(Icons.add),
            tooltip: 'Ajouter un article',
          ),
          TextButton.icon(
            onPressed: () => context.push('/home/provision/list'),
            icon: const Icon(Icons.shopping_cart_outlined, size: 18),
            label: Text('${shoppingList.length}'),
          ),
        ],
      ),
      floatingActionButton: FloatingActionButton.extended(
        onPressed: () => _showAddItemDialog(context, ref),
        backgroundColor: colors.primary,
        icon: const Icon(Icons.add, color: Colors.white),
        label: const Text('Ajouter un article', style: TextStyle(color: Colors.white)),
      ),
      body: SingleChildScrollView(
        padding: const EdgeInsets.all(20),
        child: Column(
          crossAxisAlignment: CrossAxisAlignment.start,
          children: [
            // Provision Budget Card
            Container(
              width: double.infinity,
              padding: const EdgeInsets.all(20),
              decoration: BoxDecoration(
                gradient: LinearGradient(
                  colors: [colors.primary, colors.primaryDark],
                  begin: Alignment.topLeft,
                  end: Alignment.bottomRight,
                ),
                borderRadius: BorderRadius.circular(AppTheme.radiusLg),
              ),
              child: Column(
                crossAxisAlignment: CrossAxisAlignment.start,
                children: [
                  Text(
                    'Budget Provision',
                    style: GoogleFonts.inter(
                      fontSize: 14,
                      color: Colors.white.withValues(alpha: 0.8),
                    ),
                  ),
                  const SizedBox(height: 8),
                  Text(
                    CurrencyFormatter.format(budget, 'USD'),
                    style: GoogleFonts.inter(
                      fontSize: 32,
                      fontWeight: FontWeight.w700,
                      color: Colors.white,
                    ),
                  ),
                  const SizedBox(height: 16),
                  LinearPercentIndicator(
                    lineHeight: 6,
                    percent: percentage,
                    backgroundColor: Colors.white.withValues(alpha: 0.2),
                    progressColor: Colors.white,
                    barRadius: const Radius.circular(3),
                    padding: EdgeInsets.zero,
                  ),
                  const SizedBox(height: 12),
                  Row(
                    mainAxisAlignment: MainAxisAlignment.spaceBetween,
                    children: [
                      _ProvisionStat(label: 'Dépensé', value: CurrencyFormatter.format(spent, 'USD')),
                      _ProvisionStat(label: 'Restant', value: CurrencyFormatter.format(remaining, 'USD')),
                    ],
                  ),
                ],
              ),
            ),

            const SizedBox(height: 20),

            // Forecast Section
            Container(
              width: double.infinity,
              padding: const EdgeInsets.all(16),
              decoration: BoxDecoration(
                color: colors.infoLight,
                borderRadius: BorderRadius.circular(AppTheme.radiusLg),
              ),
              child: Column(
                crossAxisAlignment: CrossAxisAlignment.start,
                children: [
                  Row(
                    children: [
                      const Text('🔮', style: TextStyle(fontSize: 20)),
                      const SizedBox(width: 8),
                      Text(
                        'Prévision',
                        style: GoogleFonts.inter(
                          fontSize: 16,
                          fontWeight: FontWeight.w600,
                          color: colors.info,
                        ),
                      ),
                    ],
                  ),
                  const SizedBox(height: 12),
                  _ForecastRow(label: 'Déjà dépensé', value: CurrencyFormatter.format(spent, 'USD')),
                  const SizedBox(height: 8),
                  _ForecastRow(label: 'Achats encore prévus', value: CurrencyFormatter.format(planned, 'USD')),
                  const Divider(height: 20),
                  _ForecastRow(
                    label: 'Solde estimé après achats',
                    value: CurrencyFormatter.format(forecast, 'USD'),
                    isBold: true,
                    color: forecast >= 0 ? colors.success : colors.error,
                  ),
                  const SizedBox(height: 8),
                  Text(
                    forecast >= 0
                        ? '✅ Si tous les achats prévus sont effectués au prix prévu, il restera environ ${CurrencyFormatter.format(forecast, "USD")}.'
                        : '⚠️ Attention, le budget ne sera pas suffisant pour tous les achats prévus.',
                    style: GoogleFonts.inter(
                      fontSize: 13,
                      color: colors.textSecondary,
                      height: 1.4,
                    ),
                  ),
                ],
              ),
            ),

            const SizedBox(height: 24),

            // Quick Shopping List
            Row(
              mainAxisAlignment: MainAxisAlignment.spaceBetween,
              children: [
                Text(
                  'Articles à acheter',
                  style: GoogleFonts.inter(
                    fontSize: 18,
                    fontWeight: FontWeight.w600,
                    color: colors.textPrimary,
                  ),
                ),
                TextButton(
                  onPressed: () => context.push('/home/provision/list'),
                  child: const Text('Voir tout'),
                ),
              ],
            ),

            if (shoppingList.isEmpty)
              Container(
                width: double.infinity,
                padding: const EdgeInsets.all(32),
                decoration: BoxDecoration(
                  color: colors.surfaceVariant,
                  borderRadius: BorderRadius.circular(AppTheme.radiusLg),
                ),
                child: Column(
                  children: [
                    const Text('🛒', style: TextStyle(fontSize: 40)),
                    const SizedBox(height: 12),
                    Text(
                      'Tout est acheté !',
                      style: GoogleFonts.inter(
                        fontSize: 16,
                        fontWeight: FontWeight.w500,
                        color: colors.textPrimary,
                      ),
                    ),
                  ],
                ),
              )
            else
              ...shoppingList.take(5).map((item) => Container(
                margin: const EdgeInsets.only(bottom: 8),
                padding: const EdgeInsets.all(12),
                decoration: BoxDecoration(
                  color: colors.surface,
                  borderRadius: BorderRadius.circular(AppTheme.radiusMd),
                  border: Border.all(color: colors.border),
                ),
                child: Row(
                  children: [
                    Text(item.name, style: const TextStyle(fontSize: 16)),
                    const Spacer(),
                    Text(
                      CurrencyFormatter.format(item.plannedPrice, 'USD'),
                      style: GoogleFonts.inter(
                        fontSize: 14,
                        fontWeight: FontWeight.w500,
                        color: colors.textPrimary,
                      ),
                    ),
                  ],
                ),
              )),

            const SizedBox(height: 24),

            // All Items
            Text(
              'Tous les articles',
              style: GoogleFonts.inter(
                fontSize: 18,
                fontWeight: FontWeight.w600,
                color: colors.textPrimary,
              ),
            ),
            const SizedBox(height: 12),

            ...items.map((item) => Container(
              margin: const EdgeInsets.only(bottom: 8),
              padding: const EdgeInsets.all(12),
              decoration: BoxDecoration(
                color: colors.surface,
                borderRadius: BorderRadius.circular(AppTheme.radiusMd),
                border: Border.all(color: colors.border),
              ),
              child: Row(
                children: [
                  Container(
                    width: 36,
                    height: 36,
                    decoration: BoxDecoration(
                      color: item.isPurchased ? colors.successLight : colors.warningLight,
                      borderRadius: BorderRadius.circular(8),
                    ),
                    child: Center(
                      child: item.isPurchased
                          ? Icon(Icons.check, size: 18, color: colors.success)
                          : Icon(Icons.schedule, size: 18, color: colors.warning),
                    ),
                  ),
                  const SizedBox(width: 12),
                  Expanded(
                    child: Column(
                      crossAxisAlignment: CrossAxisAlignment.start,
                      children: [
                        Text(
                          item.name,
                          style: GoogleFonts.inter(
                            fontSize: 14,
                            fontWeight: FontWeight.w500,
                            color: colors.textPrimary,
                          ),
                        ),
                        if (item.isPurchased && item.actualPrice != null) ...[
                          const SizedBox(height: 2),
                          Text(
                            'Payé: ${CurrencyFormatter.format(item.actualPrice!, "USD")}',
                            style: GoogleFonts.inter(fontSize: 12, color: colors.textSecondary),
                          ),
                        ],
                      ],
                    ),
                  ),
                  Text(
                    CurrencyFormatter.format(item.plannedPrice, 'USD'),
                    style: GoogleFonts.inter(
                      fontSize: 14,
                      fontWeight: FontWeight.w500,
                      color: colors.textPrimary,
                    ),
                  ),
                ],
              ),
            )),
          ],
        ),
      ),
    );
  }
}

class _ProvisionStat extends StatelessWidget {
  final String label;
  final String value;
  const _ProvisionStat({required this.label, required this.value});

  @override
  Widget build(BuildContext context) {
    return Column(
      crossAxisAlignment: CrossAxisAlignment.start,
      children: [
        Text(
          label,
          style: GoogleFonts.inter(
            fontSize: 12,
            color: Colors.white.withValues(alpha: 0.7),
          ),
        ),
        Text(
          value,
          style: GoogleFonts.inter(
            fontSize: 16,
            fontWeight: FontWeight.w600,
            color: Colors.white,
          ),
        ),
      ],
    );
  }
}

class _ForecastRow extends StatelessWidget {
  final String label;
  final String value;
  final bool isBold;
  final Color? color;

  const _ForecastRow({
    required this.label,
    required this.value,
    this.isBold = false,
    this.color,
  });

  @override
  Widget build(BuildContext context) {
    final colors = AppTheme.of(context);
    return Row(
      mainAxisAlignment: MainAxisAlignment.spaceBetween,
      children: [
        Text(
          label,
          style: GoogleFonts.inter(
            fontSize: 14,
            color: colors.textSecondary,
          ),
        ),
        Text(
          value,
          style: GoogleFonts.inter(
            fontSize: 14,
            fontWeight: isBold ? FontWeight.w700 : FontWeight.w500,
            color: color ?? colors.textPrimary,
          ),
        ),
      ],
    );
  }
}

void _showAddItemDialog(BuildContext context, WidgetRef ref) {
  final colors = AppTheme.of(context);
  final nameController = TextEditingController();
  final quantityController = TextEditingController(text: '1');
  final priceController = TextEditingController();
  String selectedUnit = 'pièce';
  String selectedPriority = 'medium';
  final formKey = GlobalKey<FormState>();

  showDialog(
    context: context,
    builder: (context) => StatefulBuilder(
      builder: (context, setDialogState) => AlertDialog(
        title: const Text('Ajouter un article'),
        content: Form(
          key: formKey,
          child: SingleChildScrollView(
            child: Column(
              mainAxisSize: MainAxisSize.min,
              children: [
                TextFormField(
                  controller: nameController,
                  autofocus: true,
                  decoration: const InputDecoration(
                    labelText: 'Nom de l\'article',
                    hintText: 'Ex: Sac de riz 25kg',
                  ),
                  validator: (val) =>
                      val == null || val.trim().isEmpty ? 'Veuillez saisir un nom' : null,
                ),
                const SizedBox(height: 12),
                Row(
                  children: [
                    Expanded(
                      flex: 2,
                      child: TextFormField(
                        controller: quantityController,
                        keyboardType: TextInputType.number,
                        decoration: const InputDecoration(
                          labelText: 'Quantité',
                        ),
                        validator: (val) {
                          if (val == null || val.isEmpty) return 'Requis';
                          final n = int.tryParse(val);
                          return n == null || n <= 0 ? 'Invalide' : null;
                        },
                      ),
                    ),
                    const SizedBox(width: 8),
                    Expanded(
                      flex: 3,
                      child: DropdownButtonFormField<String>(
                        initialValue: selectedUnit,
                        decoration: const InputDecoration(labelText: 'Unité'),
                        items: const [
                          DropdownMenuItem(value: 'pièce', child: Text('Pièce')),
                          DropdownMenuItem(value: 'kg', child: Text('Kg')),
                          DropdownMenuItem(value: 'sac', child: Text('Sac')),
                          DropdownMenuItem(value: 'litre', child: Text('Litre')),
                          DropdownMenuItem(value: 'carton', child: Text('Carton')),
                          DropdownMenuItem(value: 'pack', child: Text('Pack')),
                        ],
                        onChanged: (val) {
                          if (val != null) {
                            setDialogState(() => selectedUnit = val);
                          }
                        },
                      ),
                    ),
                  ],
                ),
                const SizedBox(height: 12),
                TextFormField(
                  controller: priceController,
                  keyboardType: const TextInputType.numberWithOptions(decimal: true),
                  decoration: const InputDecoration(
                    labelText: 'Prix prévu (estimé)',
                    prefixIcon: Icon(Icons.attach_money),
                  ),
                  validator: (val) {
                    if (val == null || val.isEmpty) return 'Veuillez saisir un montant';
                    final n = double.tryParse(val.replaceAll(',', '.'));
                    return n == null || n <= 0 ? 'Montant invalide' : null;
                  },
                ),
                const SizedBox(height: 12),
                DropdownButtonFormField<String>(
                  initialValue: selectedPriority,
                  decoration: const InputDecoration(labelText: 'Priorité'),
                  items: const [
                    DropdownMenuItem(value: 'high', child: Text('🔴 Haute')),
                    DropdownMenuItem(value: 'medium', child: Text('🟡 Moyenne')),
                    DropdownMenuItem(value: 'low', child: Text('🟢 Basse')),
                  ],
                  onChanged: (val) {
                    if (val != null) {
                      setDialogState(() => selectedPriority = val);
                    }
                  },
                ),
              ],
            ),
          ),
        ),
        actions: [
          TextButton(
            onPressed: () => Navigator.pop(context),
            child: const Text('Annuler'),
          ),
          ElevatedButton(
            onPressed: () async {
              if (!formKey.currentState!.validate()) return;
              final household = ref.read(householdProvider).valueOrNull;
              if (household == null) return;

              final item = ProvisionItemModel(
                id: '',
                householdId: household.id,
                name: nameController.text.trim(),
                quantity: int.tryParse(quantityController.text) ?? 1,
                unit: selectedUnit,
                plannedPrice: double.parse(priceController.text.replaceAll(',', '.')),
                priority: selectedPriority,
                status: 'planned',
                createdAt: DateTime.now(),
              );

              await ref.read(firestoreServiceProvider).addProvisionItem(item);
              if (context.mounted) {
                Navigator.pop(context);
                ScaffoldMessenger.of(context).showSnackBar(
                  SnackBar(
                    content: Text('${item.name} ajouté aux provisions !'),
                    backgroundColor: colors.success,
                  ),
                );
              }
            },
            child: const Text('Ajouter'),
          ),
        ],
      ),
    ),
  );
}

