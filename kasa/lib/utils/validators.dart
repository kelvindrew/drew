class Validators {
  static String? required(String? value, [String fieldName = 'Ce champ']) {
    if (value == null || value.trim().isEmpty) {
      return '$fieldName est obligatoire';
    }
    return null;
  }

  static String? name(String? value) {
    if (value == null || value.trim().isEmpty) {
      return 'Le nom est obligatoire';
    }
    if (value.trim().length < 2) {
      return 'Le nom doit contenir au moins 2 caractères';
    }
    return null;
  }

  static String? email(String? value) {
    if (value == null || value.trim().isEmpty) {
      return 'L\'email est obligatoire';
    }
    final regex = RegExp(r'^[\w-\.]+@([\w-]+\.)+[\w-]{2,4}$');
    if (!regex.hasMatch(value.trim())) {
      return 'Email invalide';
    }
    return null;
  }

  static String? amount(String? value) {
    if (value == null || value.trim().isEmpty) {
      return 'Le montant est obligatoire';
    }
    final parsed = double.tryParse(value.trim().replaceAll(',', '.'));
    if (parsed == null || parsed < 0) {
      return 'Montant invalide';
    }
    return null;
  }

  static String? inviteCode(String? value) {
    if (value == null || value.trim().isEmpty) {
      return 'Le code d\'invitation est obligatoire';
    }
    final clean = value.trim().toUpperCase().replaceAll('KASA-', '');
    if (clean.length < 4 || clean.length > 8) {
      return 'Code invalide (4-8 caractères)';
    }
    return null;
  }

  static String cleanInviteCode(String code) {
    return code.trim().toUpperCase().replaceAll('KASA-', '');
  }
}
