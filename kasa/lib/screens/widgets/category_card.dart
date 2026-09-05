import 'package:flutter/material.dart';
import 'package:google_fonts/google_fonts.dart';
import 'package:percent_indicator/percent_indicator.dart';
import '../../app/theme/app_theme.dart';
import '../../models/category_model.dart';
import '../../providers/expense_provider.dart';
import 'package:flutter_riverpod/flutter_riverpod.dart';
import '../../utils/currency_formatter.dart';

class CategoryCard extends ConsumerWidget {
  final CategoryModel category;
  const CategoryCard({super.key, required this.category});

  @override
  Widget build(BuildContext context, WidgetRef ref) {
    final colors = AppTheme.of(context);
    final expenses = ref.watch(expensesProvider).valueOrNull ?? [];
    final spent = expenses
        .where((e) => e.categoryId == category.id)
        .fold(0.0, (sum, e) => sum + e.amount);
    final remaining = category.plannedAmount - spent;
    final percentage = category.plannedAmount > 0
        ? (spent / category.plannedAmount).clamp(0.0, 1.0)
        : 0.0;

    Color statusColor;
    if (percentage >= 1.0) {
      statusColor = colors.error;
    } else if (percentage >= 0.7) {
      statusColor = colors.warning;
    } else {
      statusColor = colors.primary;
    }

    return Container(
      padding: const EdgeInsets.all(14),
      decoration: BoxDecoration(
        color: colors.surface,
        borderRadius: BorderRadius.circular(AppTheme.radiusLg),
        border: Border.all(color: colors.border),
      ),
      child: Column(
        crossAxisAlignment: CrossAxisAlignment.start,
        mainAxisAlignment: MainAxisAlignment.spaceBetween,
        children: [
          Row(
            children: [
              Text(category.icon, style: const TextStyle(fontSize: 24)),
              const Spacer(),
              if (!category.active)
                Container(
                  padding: const EdgeInsets.symmetric(horizontal: 6, vertical: 2),
                  decoration: BoxDecoration(
                    color: colors.surfaceVariant,
                    borderRadius: BorderRadius.circular(8),
                  ),
                  child: Text(
                    'Inactif',
                    style: GoogleFonts.inter(fontSize: 10, color: colors.textTertiary),
                  ),
                ),
            ],
          ),
          Text(
            category.name,
            style: GoogleFonts.inter(
              fontSize: 14,
              fontWeight: FontWeight.w600,
              color: colors.textPrimary,
            ),
            maxLines: 1,
            overflow: TextOverflow.ellipsis,
          ),
          Column(
            crossAxisAlignment: CrossAxisAlignment.start,
            children: [
              Row(
                mainAxisAlignment: MainAxisAlignment.spaceBetween,
                children: [
                  Text(
                    'Prévu',
                    style: GoogleFonts.inter(fontSize: 11, color: colors.textTertiary),
                  ),
                  Text(
                    CurrencyFormatter.formatCompact(category.plannedAmount),
                    style: GoogleFonts.inter(
                      fontSize: 12,
                      fontWeight: FontWeight.w500,
                      color: colors.textSecondary,
                    ),
                  ),
                ],
              ),
              const SizedBox(height: 4),
              Row(
                mainAxisAlignment: MainAxisAlignment.spaceBetween,
                children: [
                  Text(
                    'Restant',
                    style: GoogleFonts.inter(fontSize: 11, color: colors.textTertiary),
                  ),
                  Text(
                    CurrencyFormatter.formatCompact(remaining),
                    style: GoogleFonts.inter(
                      fontSize: 12,
                      fontWeight: FontWeight.w600,
                      color: statusColor,
                    ),
                  ),
                ],
              ),
            ],
          ),
          LinearPercentIndicator(
            lineHeight: 4,
            percent: percentage,
            backgroundColor: colors.surfaceVariant,
            progressColor: statusColor,
            barRadius: const Radius.circular(2),
            padding: EdgeInsets.zero,
            animation: true,
            animationDuration: 400,
          ),
        ],
      ),
    );
  }
}
