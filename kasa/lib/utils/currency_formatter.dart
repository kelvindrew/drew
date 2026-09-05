import 'package:intl/intl.dart';

class CurrencyFormatter {
  static final Map<String, NumberFormat> _formats = {
    'USD': NumberFormat.currency(symbol: '\$', decimalDigits: 2),
    'CDF': NumberFormat.currency(symbol: 'FC', decimalDigits: 0),
    'EUR': NumberFormat.currency(symbol: '€', decimalDigits: 2),
    'GBP': NumberFormat.currency(symbol: '£', decimalDigits: 2),
  };

  static String format(double amount, String currency) {
    final format = _formats[currency];
    if (format != null) return format.format(amount);
    return '$amount $currency';
  }

  static String formatWithConversion(double amount, String fromCurrency, String toCurrency, double exchangeRate) {
    if (fromCurrency == toCurrency) return format(amount, fromCurrency);
    
    final converted = fromCurrency == 'USD' 
        ? amount * exchangeRate 
        : amount / exchangeRate;
    
    return '${format(amount, fromCurrency)} ≈ ${format(converted, toCurrency)}';
  }

  static double convert(double amount, String fromCurrency, String toCurrency, double exchangeRate) {
    if (fromCurrency == toCurrency) return amount;
    return fromCurrency == 'USD' 
        ? amount * exchangeRate 
        : amount / exchangeRate;
  }

  static String formatCompact(double amount) {
    if (amount >= 1000000) return '${(amount / 1000000).toStringAsFixed(1)}M';
    if (amount >= 1000) return '${(amount / 1000).toStringAsFixed(1)}K';
    return amount.toStringAsFixed(0);
  }
}
