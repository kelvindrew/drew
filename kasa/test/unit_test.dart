import 'package:flutter_test/flutter_test.dart';
import 'package:intl/date_symbol_data_local.dart';
import 'package:kasa/models/provision_item_model.dart';
import 'package:kasa/models/household_model.dart';
import 'package:kasa/models/user_model.dart';
import 'package:kasa/models/message_model.dart';
import 'package:kasa/utils/currency_formatter.dart';
import 'package:kasa/utils/date_formatter.dart';

void main() {
  setUpAll(() async {
    await initializeDateFormatting('fr_FR', null);
  });

  group('CurrencyFormatter Tests', () {
    test('Format USD', () {
      expect(CurrencyFormatter.format(150.0, 'USD'), '\$150.00');
    });

    test('Format Compact', () {
      expect(CurrencyFormatter.formatCompact(1500.0), '1.5K');
    });
  });

  group('DateFormatter Tests', () {
    test('Format Date', () {
      final date = DateTime(2026, 8, 30);
      final formatted = DateFormatter.formatDate(date);
      expect(formatted.isNotEmpty, true);
    });
  });

  group('Models Serialization and Logic Tests', () {
    test('UserModel model creation', () {
      final user = UserModel(
        id: 'u1',
        name: 'Landry',
        email: 'landry@test.com',
        householdId: 'h1',
        role: 'admin',
        createdAt: DateTime.now(),
      );

      expect(user.isAdmin, true);
    });

    test('ProvisionItemModel calculations', () {
      final item = ProvisionItemModel(
        id: 'p1',
        householdId: 'h1',
        name: 'Sac de riz',
        quantity: 2,
        unit: 'sac',
        plannedPrice: 25.0,
        actualPrice: 30.0,
        priority: 'high',
        status: 'planned',
        createdAt: DateTime.now(),
      );

      expect(item.isPlanned, true);
      expect(item.variance, 5.0);
      expect(item.isOverBudget, true);
    });

    test('MessageModel helper getters', () {
      final textMsg = MessageModel(
        id: 'm1',
        householdId: 'h1',
        userId: 'u1',
        text: 'Bonjour',
        type: 'text',
        createdAt: DateTime.now(),
      );
      expect(textMsg.isText, true);

      final systemMsg = MessageModel(
        id: 'm2',
        householdId: 'h1',
        userId: 'u1',
        text: 'Achat validé',
        type: 'system',
        createdAt: DateTime.now(),
      );
      expect(systemMsg.isSystem, true);
    });

    test('HouseholdModel invite code formatting', () {
      final household = HouseholdModel(
        id: 'h1',
        name: 'Maison KASA',
        currency: 'USD',
        inviteCode: 'ABCDEF',
        ownerId: 'u1',
        memberIds: ['u1'],
        createdAt: DateTime.now(),
      );

      expect(household.formattedInviteCode, 'KASA-ABCDEF');
    });
  });
}
