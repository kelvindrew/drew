import 'package:flutter/material.dart';
import 'package:flutter_riverpod/flutter_riverpod.dart';
import 'package:google_fonts/google_fonts.dart';
import 'package:fl_chart/fl_chart.dart';
import '../../app/theme/app_theme.dart';
import '../../providers/budget_provider.dart';
import '../../providers/expense_provider.dart';
import '../../providers/category_provider.dart';
import '../../utils/currency_formatter.dart';
import '../../utils/date_formatter.dart';

class StatisticsScreen extends ConsumerWidget {
  const StatisticsScreen({super.key});

  @override
  Widget build(BuildContext context, WidgetRef ref) {
    final colors = AppTheme.of(context);
    final budget = ref.watch(budgetProvider).valueOrNull;
    final totalExpenses = ref.watch(totalExpensesProvider);
    final remaining = ref.watch(budgetRemainingProvider);
    final categories = ref.watch(activeCategoriesProvider);
    final expenses = ref.watch(expensesProvider).valueOrNull ?? [];

    return Scaffold(
      body: SafeArea(
        child: SingleChildScrollView(
          padding: const EdgeInsets.all(20),
          child: Column(
            crossAxisAlignment: CrossAxisAlignment.start,
            children: [
              Text(
                'Statistiques',
                style: GoogleFonts.inter(
                  fontSize: 28,
                  fontWeight: FontWeight.w700,
                  color: colors.textPrimary,
                ),
              ),
              const SizedBox(height: 4),
              Text(
                DateFormatter.currentMonth(),
                style: GoogleFonts.inter(
                  fontSize: 14,
                  color: colors.textSecondary,
                ),
              ),

              const SizedBox(height: 20),

              // Summary Cards
              Row(
                children: [
                  _StatCard(
                    title: 'Budget',
                    value: CurrencyFormatter.format(budget?.totalBudget ?? 0, 'USD'),
                    color: colors.primary,
                    icon: Icons.account_balance_wallet_outlined,
                  ),
                  const SizedBox(width: 12),
                  _StatCard(
                    title: 'Dépensé',
                    value: CurrencyFormatter.format(totalExpenses, 'USD'),
                    color: colors.warning,
                    icon: Icons.trending_up,
                  ),
                ],
              ),
              const SizedBox(height: 12),
              Row(
                children: [
                  _StatCard(
                    title: 'Restant',
                    value: CurrencyFormatter.format(remaining, 'USD'),
                    color: remaining >= 0 ? colors.success : colors.error,
                    icon: Icons.savings_outlined,
                  ),
                  const SizedBox(width: 12),
                  _StatCard(
                    title: 'Dépenses',
                    value: '${expenses.length}',
                    color: colors.info,
                    icon: Icons.receipt_long_outlined,
                  ),
                ],
              ),

              const SizedBox(height: 28),

              // Pie Chart - Expenses by category
              Text(
                'Dépenses par catégorie',
                style: GoogleFonts.inter(
                  fontSize: 18,
                  fontWeight: FontWeight.w600,
                  color: colors.textPrimary,
                ),
              ),
              const SizedBox(height: 16),

              if (categories.isEmpty || totalExpenses == 0)
                Container(
                  width: double.infinity,
                  padding: const EdgeInsets.all(32),
                  decoration: BoxDecoration(
                    color: colors.surfaceVariant,
                    borderRadius: BorderRadius.circular(AppTheme.radiusLg),
                  ),
                  child: Column(
                    children: [
                      const Text('📊', style: TextStyle(fontSize: 40)),
                      const SizedBox(height: 12),
                      Text(
                        'Pas encore de données',
                        style: GoogleFonts.inter(
                          fontSize: 16,
                          color: colors.textSecondary,
                        ),
                      ),
                    ],
                  ),
                )
              else
                Container(
                  height: 240,
                  padding: const EdgeInsets.all(16),
                  decoration: BoxDecoration(
                    color: colors.surface,
                    borderRadius: BorderRadius.circular(AppTheme.radiusLg),
                    border: Border.all(color: colors.border),
                  ),
                  child: Row(
                    children: [
                      // Pie chart
                      Expanded(
                        flex: 2,
                        child: PieChart(
                          PieChartData(
                            sections: _buildPieSections(categories, expenses, colors),
                            centerSpaceRadius: 40,
                            sectionsSpace: 2,
                          ),
                        ),
                      ),
                      const SizedBox(width: 16),
                      // Legend
                      Expanded(
                        flex: 3,
                        child: Column(
                          mainAxisAlignment: MainAxisAlignment.center,
                          crossAxisAlignment: CrossAxisAlignment.start,
                          children: categories.take(5).map((cat) {
                            final spent = expenses
                                .where((e) => e.categoryId == cat.id)
                                .fold(0.0, (sum, e) => sum + e.amount);
                            final color = _getColor(categories.indexOf(cat), colors);
                            return Padding(
                              padding: const EdgeInsets.symmetric(vertical: 3),
                              child: Row(
                                children: [
                                  Container(
                                    width: 10,
                                    height: 10,
                                    decoration: BoxDecoration(
                                      color: color,
                                      borderRadius: BorderRadius.circular(3),
                                    ),
                                  ),
                                  const SizedBox(width: 8),
                                  Expanded(
                                    child: Text(
                                      cat.name,
                                      style: GoogleFonts.inter(
                                        fontSize: 12,
                                        color: colors.textSecondary,
                                      ),
                                    ),
                                  ),
                                  Text(
                                    CurrencyFormatter.formatCompact(spent),
                                    style: GoogleFonts.inter(
                                      fontSize: 12,
                                      fontWeight: FontWeight.w600,
                                      color: colors.textPrimary,
                                    ),
                                  ),
                                ],
                              ),
                            );
                          }).toList(),
                        ),
                      ),
                    ],
                  ),
                ),

              const SizedBox(height: 28),

              // Bar Chart - Planned vs Actual
              Text(
                'Prévu vs Réel',
                style: GoogleFonts.inter(
                  fontSize: 18,
                  fontWeight: FontWeight.w600,
                  color: colors.textPrimary,
                ),
              ),
              const SizedBox(height: 16),

              if (categories.isNotEmpty)
                Container(
                  height: 200,
                  padding: const EdgeInsets.all(16),
                  decoration: BoxDecoration(
                    color: colors.surface,
                    borderRadius: BorderRadius.circular(AppTheme.radiusLg),
                    border: Border.all(color: colors.border),
                  ),
                  child: BarChart(
                    BarChartData(
                      alignment: BarChartAlignment.spaceAround,
                      maxY: _getMaxY(categories, expenses),
                      barGroups: _buildBarGroups(categories, expenses, colors),
                      titlesData: FlTitlesData(
                        show: true,
                        bottomTitles: AxisTitles(
                          sideTitles: SideTitles(
                            showTitles: true,
                            getTitlesWidget: (value, meta) {
                              final index = value.toInt();
                              if (index < categories.length) {
                                return Padding(
                                  padding: const EdgeInsets.only(top: 8),
                                  child: Text(
                                    categories[index].icon,
                                    style: const TextStyle(fontSize: 16),
                                  ),
                                );
                              }
                              return const Text('');
                            },
                          ),
                        ),
                        leftTitles: const AxisTitles(sideTitles: SideTitles(showTitles: false)),
                        topTitles: const AxisTitles(sideTitles: SideTitles(showTitles: false)),
                        rightTitles: const AxisTitles(sideTitles: SideTitles(showTitles: false)),
                      ),
                      borderData: FlBorderData(show: false),
                      gridData: const FlGridData(show: false),
                    ),
                  ),
                ),

              const SizedBox(height: 28),

              // Category breakdown
              Text(
                'Détail par catégorie',
                style: GoogleFonts.inter(
                  fontSize: 18,
                  fontWeight: FontWeight.w600,
                  color: colors.textPrimary,
                ),
              ),
              const SizedBox(height: 12),

              ...categories.map((cat) {
                final spent = expenses
                    .where((e) => e.categoryId == cat.id)
                    .fold(0.0, (sum, e) => sum + e.amount);
                final percentage = cat.plannedAmount > 0
                    ? (spent / cat.plannedAmount).clamp(0.0, 1.0)
                    : 0.0;

                return Container(
                  margin: const EdgeInsets.only(bottom: 8),
                  padding: const EdgeInsets.all(14),
                  decoration: BoxDecoration(
                    color: colors.surface,
                    borderRadius: BorderRadius.circular(AppTheme.radiusMd),
                    border: Border.all(color: colors.border),
                  ),
                  child: Row(
                    children: [
                      Text(cat.icon, style: const TextStyle(fontSize: 20)),
                      const SizedBox(width: 12),
                      Expanded(
                        child: Column(
                          crossAxisAlignment: CrossAxisAlignment.start,
                          children: [
                            Text(
                              cat.name,
                              style: GoogleFonts.inter(
                                fontSize: 14,
                                fontWeight: FontWeight.w500,
                                color: colors.textPrimary,
                              ),
                            ),
                            const SizedBox(height: 4),
                            Row(
                              children: [
                                Text(
                                  'Prévu: ${CurrencyFormatter.formatCompact(cat.plannedAmount)}',
                                  style: GoogleFonts.inter(
                                    fontSize: 12,
                                    color: colors.textTertiary,
                                  ),
                                ),
                                const SizedBox(width: 12),
                                Text(
                                  'Réel: ${CurrencyFormatter.formatCompact(spent)}',
                                  style: GoogleFonts.inter(
                                    fontSize: 12,
                                    fontWeight: FontWeight.w500,
                                    color: colors.textPrimary,
                                  ),
                                ),
                              ],
                            ),
                          ],
                        ),
                      ),
                      Text(
                        '${(percentage * 100).toInt()}%',
                        style: GoogleFonts.inter(
                          fontSize: 14,
                          fontWeight: FontWeight.w600,
                          color: percentage >= 1.0
                              ? colors.error
                              : percentage >= 0.7
                                  ? colors.warning
                                  : colors.primary,
                        ),
                      ),
                    ],
                  ),
                );
              }),

              const SizedBox(height: 20),
            ],
          ),
        ),
      ),
    );
  }

  List<PieChartSectionData> _buildPieSections(
    List categories,
    List expenses,
    AppColors colors,
  ) {
    final colorList = [
      colors.primary,
      colors.warning,
      colors.info,
      colors.error,
      const Color(0xFF8B5CF6),
      const Color(0xFFEC4899),
    ];

    return categories.asMap().entries.map((entry) {
      final cat = entry.value;
      final spent = expenses
          .where((e) => e.categoryId == cat.id)
          .fold(0.0, (sum, e) => sum + e.amount);

      return PieChartSectionData(
        value: spent > 0 ? spent : 0.01,
        color: colorList[entry.key % colorList.length],
        radius: 50,
        title: '',
      );
    }).toList();
  }

  Color _getColor(int index, AppColors colors) {
    final colorList = [
      colors.primary,
      colors.warning,
      colors.info,
      colors.error,
      const Color(0xFF8B5CF6),
      const Color(0xFFEC4899),
    ];
    return colorList[index % colorList.length];
  }

  double _getMaxY(List categories, List expenses) {
    double max = 0;
    for (final cat in categories) {
      final spent = expenses
          .where((e) => e.categoryId == cat.id)
          .fold(0.0, (sum, e) => sum + e.amount);
      if (spent > max) max = spent;
      if (cat.plannedAmount > max) max = cat.plannedAmount;
    }
    return max > 0 ? max * 1.2 : 100;
  }

  List<BarChartGroupData> _buildBarGroups(List categories, List expenses, AppColors colors) {
    return categories.asMap().entries.map((entry) {
      final cat = entry.value;
      final spent = expenses
          .where((e) => e.categoryId == cat.id)
          .fold(0.0, (sum, e) => sum + e.amount);

      return BarChartGroupData(
        x: entry.key,
        barRods: [
          BarChartRodData(
            toY: cat.plannedAmount,
            color: colors.primary.withValues(alpha: 0.3),
            width: 12,
            borderRadius: const BorderRadius.vertical(top: Radius.circular(4)),
          ),
          BarChartRodData(
            toY: spent,
            color: colors.primary,
            width: 12,
            borderRadius: const BorderRadius.vertical(top: Radius.circular(4)),
          ),
        ],
      );
    }).toList();
  }
}

class _StatCard extends StatelessWidget {
  final String title;
  final String value;
  final Color color;
  final IconData icon;

  const _StatCard({
    required this.title,
    required this.value,
    required this.color,
    required this.icon,
  });

  @override
  Widget build(BuildContext context) {
    final colors = AppTheme.of(context);
    return Expanded(
      child: Container(
        padding: const EdgeInsets.all(14),
        decoration: BoxDecoration(
          color: color.withValues(alpha: 0.06),
          borderRadius: BorderRadius.circular(AppTheme.radiusLg),
          border: Border.all(color: color.withValues(alpha: 0.1)),
        ),
        child: Column(
          crossAxisAlignment: CrossAxisAlignment.start,
          children: [
            Icon(icon, size: 20, color: color),
            const SizedBox(height: 8),
            Text(
              value,
              style: GoogleFonts.inter(
                fontSize: 18,
                fontWeight: FontWeight.w700,
                color: color,
              ),
            ),
            const SizedBox(height: 2),
            Text(
              title,
              style: GoogleFonts.inter(
                fontSize: 12,
                color: colors.textSecondary,
              ),
            ),
          ],
        ),
      ),
    );
  }
}
