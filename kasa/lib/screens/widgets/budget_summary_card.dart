import 'package:flutter/material.dart';
import 'package:google_fonts/google_fonts.dart';
import 'package:percent_indicator/percent_indicator.dart';
import '../../app/theme/app_theme.dart';
import '../../utils/currency_formatter.dart';

class BudgetSummaryCard extends StatelessWidget {
  final double totalBudget;
  final double spent;
  final double remaining;
  final double percentage;
  final String currency;

  const BudgetSummaryCard({
    super.key,
    required this.totalBudget,
    required this.spent,
    required this.remaining,
    required this.percentage,
    required this.currency,
  });

  Color _statusColor(AppColors colors) {
    if (percentage >= 1.0) return colors.error;
    if (percentage >= 0.7) return colors.warning;
    return colors.primary;
  }

  @override
  Widget build(BuildContext context) {
    final colors = AppTheme.of(context);
    final statusColor = _statusColor(colors);

    return Container(
      padding: const EdgeInsets.all(20),
      decoration: BoxDecoration(
        color: colors.surface,
        borderRadius: BorderRadius.circular(AppTheme.radiusLg),
        border: Border.all(color: colors.border),
      ),
      child: Column(
        crossAxisAlignment: CrossAxisAlignment.start,
        children: [
          Row(
            mainAxisAlignment: MainAxisAlignment.spaceBetween,
            children: [
              Text(
                'Budget du mois',
                style: GoogleFonts.inter(
                  fontSize: 14,
                  color: colors.textSecondary,
                ),
              ),
              Container(
                padding: const EdgeInsets.symmetric(horizontal: 10, vertical: 4),
                decoration: BoxDecoration(
                  color: statusColor.withValues(alpha: 0.1),
                  borderRadius: BorderRadius.circular(20),
                ),
                child: Text(
                  percentage >= 1.0
                      ? 'Dépassé'
                      : percentage >= 0.7
                          ? 'Attention'
                          : 'Normal',
                  style: GoogleFonts.inter(
                    fontSize: 12,
                    fontWeight: FontWeight.w500,
                    color: statusColor,
                  ),
                ),
              ),
            ],
          ),
          const SizedBox(height: 12),
          Text(
            CurrencyFormatter.format(totalBudget, currency),
            style: GoogleFonts.inter(
              fontSize: 36,
              fontWeight: FontWeight.w700,
              color: colors.textPrimary,
            ),
          ),
          const SizedBox(height: 16),
          // Progress bar
          LinearPercentIndicator(
            lineHeight: 8,
            percent: percentage.clamp(0.0, 1.0),
            backgroundColor: colors.surfaceVariant,
            progressColor: statusColor,
            barRadius: const Radius.circular(4),
            padding: EdgeInsets.zero,
            animation: true,
            animationDuration: 600,
          ),
          const SizedBox(height: 16),
          Row(
            children: [
              _InfoChip(
                label: 'Dépensé',
                value: CurrencyFormatter.format(spent, currency),
                color: colors.textSecondary,
              ),
              const SizedBox(width: 12),
              _InfoChip(
                label: 'Disponible',
                value: CurrencyFormatter.format(remaining, currency),
                color: remaining >= 0 ? colors.primary : colors.error,
              ),
            ],
          ),
        ],
      ),
    );
  }
}

class _InfoChip extends StatelessWidget {
  final String label;
  final String value;
  final Color color;

  const _InfoChip({
    required this.label,
    required this.value,
    required this.color,
  });

  @override
  Widget build(BuildContext context) {
    final colors = AppTheme.of(context);
    return Expanded(
      child: Container(
        padding: const EdgeInsets.symmetric(horizontal: 12, vertical: 10),
        decoration: BoxDecoration(
          color: color.withValues(alpha: 0.06),
          borderRadius: BorderRadius.circular(AppTheme.radiusMd),
        ),
        child: Column(
          crossAxisAlignment: CrossAxisAlignment.start,
          children: [
            Text(
              label,
              style: GoogleFonts.inter(
                fontSize: 12,
                color: colors.textTertiary,
              ),
            ),
            const SizedBox(height: 2),
            Text(
              value,
              style: GoogleFonts.inter(
                fontSize: 15,
                fontWeight: FontWeight.w600,
                color: color,
              ),
            ),
          ],
        ),
      ),
    );
  }
}
