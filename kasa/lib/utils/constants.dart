class AppConstants {
  static const String appName = 'KASA';
  static const String slogan = 'Notre maison, notre budget.';
  
  static const String defaultCurrency = 'USD';
  static const String defaultSecondaryCurrency = 'CDF';
  static const double defaultExchangeRate = 2250.0;
  
  static const String currencyUSD = 'USD';
  static const String currencyCDF = 'CDF';
  
  static const List<Map<String, String>> currencies = [
    {'code': 'USD', 'name': 'Dollar américain', 'symbol': '\$', 'flag': '🇺🇸'},
    {'code': 'CDF', 'name': 'Franc congolais', 'symbol': 'FC', 'flag': '🇨🇩'},
    {'code': 'EUR', 'name': 'Euro', 'symbol': '€', 'flag': '🇪🇺'},
    {'code': 'GBP', 'name': 'Livre sterling', 'symbol': '£', 'flag': '🇬🇧'},
  ];
  
  static const List<Map<String, String>> categoryIcons = [
    {'icon': '🏠', 'name': 'Loyer'},
    {'icon': '🍚', 'name': 'Provision'},
    {'icon': '🌐', 'name': 'Internet'},
    {'icon': '🧹', 'name': 'Ménage'},
    {'icon': '💡', 'name': 'Électricité'},
    {'icon': '🚰', 'name': 'Eau'},
    {'icon': '🛠️', 'name': 'Entretien'},
    {'icon': '📦', 'name': 'Autres'},
    {'icon': '🛒', 'name': 'Courses'},
    {'icon': '🍖', 'name': 'Nourriture'},
    {'icon': '📺', 'name': 'Divertissement'},
    {'icon': '🚗', 'name': 'Transport'},
    {'icon': '💊', 'name': 'Santé'},
    {'icon': '📱', 'name': 'Téléphone'},
    {'icon': '🎓', 'name': 'Éducation'},
    {'icon': '👔', 'name': 'Vêtements'},
  ];
}
