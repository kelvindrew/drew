import 'package:flutter/material.dart';
import 'package:flutter/services.dart';
import 'package:flutter_riverpod/flutter_riverpod.dart';
import 'package:go_router/go_router.dart';
import 'package:google_fonts/google_fonts.dart';
import '../../app/theme/app_theme.dart';
import '../../models/household_model.dart';
import '../../providers/auth_provider.dart';
import '../../providers/firestore_service_provider.dart';
import '../../utils/constants.dart';
import '../../utils/validators.dart';

class CreateHouseholdScreen extends ConsumerStatefulWidget {
  final String userName;
  const CreateHouseholdScreen({super.key, required this.userName});

  @override
  ConsumerState<CreateHouseholdScreen> createState() => _CreateHouseholdScreenState();
}

class _CreateHouseholdScreenState extends ConsumerState<CreateHouseholdScreen> {
  final _formKey = GlobalKey<FormState>();
  final _nameController = TextEditingController();
  final _budgetController = TextEditingController();
  final _exchangeRateController = TextEditingController(text: '2250');
  
  String _currency = 'USD';
  String _secondaryCurrency = 'CDF';
  bool _isLoading = false;
  HouseholdModel? _createdHousehold;

  @override
  void dispose() {
    _nameController.dispose();
    _budgetController.dispose();
    _exchangeRateController.dispose();
    super.dispose();
  }

  Future<void> _createHousehold() async {
    final colors = AppTheme.of(context);
    if (!_formKey.currentState!.validate()) return;
    
    setState(() => _isLoading = true);
    
    try {
      final authUser = ref.read(authStateProvider).valueOrNull;
      if (authUser == null) return;

      final household = await ref.read(firestoreServiceProvider).createHousehold(
        name: _nameController.text.trim(),
        ownerId: authUser.uid,
        currency: _currency,
        secondaryCurrency: _secondaryCurrency,
        exchangeRate: double.tryParse(_exchangeRateController.text) ?? 2250,
      );

      final budget = double.tryParse(_budgetController.text) ?? 0;
      if (budget > 0) {
        await ref.read(firestoreServiceProvider).updateBudget(household.id, budget);
      }

      setState(() => _createdHousehold = household);
    } catch (e) {
      if (mounted) {
        ScaffoldMessenger.of(context).showSnackBar(
          SnackBar(content: Text('Erreur: $e'), backgroundColor: colors.error),
        );
      }
    } finally {
      if (mounted) setState(() => _isLoading = false);
    }
  }

  void _copyCode() {
    final colors = AppTheme.of(context);
    if (_createdHousehold != null) {
      Clipboard.setData(ClipboardData(text: _createdHousehold!.formattedInviteCode));
      ScaffoldMessenger.of(context).showSnackBar(
        SnackBar(
          content: Text('Code copié: ${_createdHousehold!.formattedInviteCode}'),
          backgroundColor: colors.primary,
        ),
      );
    }
  }

  @override
  Widget build(BuildContext context) {
    if (_createdHousehold != null) {
      return _buildSuccessScreen();
    }
    return _buildFormScreen();
  }

  Widget _buildFormScreen() {
    final colors = AppTheme.of(context);
    return Scaffold(
      appBar: AppBar(
        leading: IconButton(
          icon: const Icon(Icons.arrow_back_ios),
          onPressed: () => context.pop(),
        ),
      ),
      body: SafeArea(
        child: Form(
          key: _formKey,
          child: ListView(
            padding: const EdgeInsets.symmetric(horizontal: AppTheme.spacingLg),
            children: [
              const SizedBox(height: AppTheme.spacingLg),
              
              Text(
                'Créer un foyer',
                style: GoogleFonts.inter(
                  fontSize: 28,
                  fontWeight: FontWeight.w700,
                  color: colors.textPrimary,
                ),
              ),
              const SizedBox(height: AppTheme.spacingSm),
              Text(
                'Configurez votre foyer pour commencer.',
                style: GoogleFonts.inter(fontSize: 15, color: colors.textSecondary),
              ),
              
              const SizedBox(height: AppTheme.spacingXl),
              
              // Household Name
              TextFormField(
                controller: _nameController,
                validator: (v) => Validators.required(v, 'Le nom du foyer'),
                textCapitalization: TextCapitalization.words,
                decoration: const InputDecoration(
                  hintText: 'Notre Maison',
                  labelText: 'Nom du foyer',
                  prefixIcon: Icon(Icons.home_outlined),
                ),
              ),
              
              const SizedBox(height: AppTheme.spacingMd),
              
              // Budget
              TextFormField(
                controller: _budgetController,
                keyboardType: TextInputType.number,
                validator: Validators.amount,
                decoration: InputDecoration(
                  hintText: '600',
                  labelText: 'Budget mensuel',
                  prefixIcon: const Icon(Icons.attach_money),
                  suffixText: _currency,
                ),
              ),
              
              const SizedBox(height: AppTheme.spacingMd),
              
              // Primary Currency
              DropdownButtonFormField<String>(
                initialValue: _currency,
                decoration: const InputDecoration(
                  labelText: 'Devise principale',
                  prefixIcon: Icon(Icons.monetization_on_outlined),
                ),
                items: AppConstants.currencies.map((c) {
                  return DropdownMenuItem(
                    value: c['code'],
                    child: Text('${c['flag']} ${c['code']} - ${c['name']}'),
                  );
                }).toList(),
                onChanged: (v) => setState(() => _currency = v ?? 'USD'),
              ),
              
              const SizedBox(height: AppTheme.spacingMd),
              
              // Secondary Currency
              DropdownButtonFormField<String>(
                initialValue: _secondaryCurrency,
                decoration: const InputDecoration(
                  labelText: 'Devise secondaire',
                  prefixIcon: Icon(Icons.monetization_on_outlined),
                ),
                items: AppConstants.currencies.map((c) {
                  return DropdownMenuItem(
                    value: c['code'],
                    child: Text('${c['flag']} ${c['code']} - ${c['name']}'),
                  );
                }).toList(),
                onChanged: (v) => setState(() => _secondaryCurrency = v ?? 'CDF'),
              ),
              
              const SizedBox(height: AppTheme.spacingMd),
              
              // Exchange Rate
              TextFormField(
                controller: _exchangeRateController,
                keyboardType: TextInputType.number,
                decoration: InputDecoration(
                  hintText: '2250',
                  labelText: 'Taux de change (1 $_currency = ? $_secondaryCurrency)',
                  prefixIcon: const Icon(Icons.currency_exchange),
                ),
              ),
              
              const SizedBox(height: AppTheme.spacingXl),
              
              // Create Button
              SizedBox(
                width: double.infinity,
                child: ElevatedButton(
                  onPressed: _isLoading ? null : _createHousehold,
                  child: _isLoading
                      ? const SizedBox(
                          height: 20,
                          width: 20,
                          child: CircularProgressIndicator(strokeWidth: 2, color: Colors.white),
                        )
                      : const Text('Créer le foyer'),
                ),
              ),
              
              const SizedBox(height: AppTheme.spacingLg),
            ],
          ),
        ),
      ),
    );
  }

  Widget _buildSuccessScreen() {
    final colors = AppTheme.of(context);
    return Scaffold(
      body: SafeArea(
        child: Padding(
          padding: const EdgeInsets.symmetric(horizontal: AppTheme.spacingLg),
          child: Column(
            children: [
              const Spacer(flex: 2),
              
              Container(
                width: 80,
                height: 80,
                decoration: BoxDecoration(
                  color: colors.successLight,
                  borderRadius: BorderRadius.circular(AppTheme.radiusXl),
                ),
                child: Icon(Icons.check, size: 40, color: colors.success),
              ),
              
              const SizedBox(height: AppTheme.spacingLg),
              
              Text(
                'Foyer créé !',
                style: GoogleFonts.inter(
                  fontSize: 28,
                  fontWeight: FontWeight.w700,
                  color: colors.textPrimary,
                ),
              ),
              
              const SizedBox(height: AppTheme.spacingSm),
              
              Text(
                _createdHousehold!.name,
                style: GoogleFonts.inter(
                  fontSize: 18,
                  color: colors.primary,
                  fontWeight: FontWeight.w500,
                ),
              ),
              
              const SizedBox(height: AppTheme.spacingXl),
              
              // Invite Code Card
              Container(
                width: double.infinity,
                padding: const EdgeInsets.all(AppTheme.spacingLg),
                decoration: BoxDecoration(
                  color: colors.primaryContainer,
                  borderRadius: BorderRadius.circular(AppTheme.radiusLg),
                  border: Border.all(color: colors.primary.withValues(alpha: 0.2)),
                ),
                child: Column(
                  children: [
                    Text(
                      'Code d\'invitation',
                      style: GoogleFonts.inter(
                        fontSize: 14,
                        color: colors.textSecondary,
                      ),
                    ),
                    const SizedBox(height: AppTheme.spacingSm),
                    Text(
                      _createdHousehold!.formattedInviteCode,
                      style: GoogleFonts.inter(
                        fontSize: 32,
                        fontWeight: FontWeight.w700,
                        color: colors.primary,
                        letterSpacing: 4,
                      ),
                    ),
                    const SizedBox(height: AppTheme.spacingMd),
                    OutlinedButton.icon(
                      onPressed: _copyCode,
                      icon: const Icon(Icons.copy, size: 18),
                      label: const Text('Copier le code'),
                    ),
                  ],
                ),
              ),
              
              const SizedBox(height: AppTheme.spacingMd),
              
              Text(
                'Partagez ce code avec les membres de votre foyer pour qu\'ils rejoignent.',
                textAlign: TextAlign.center,
                style: GoogleFonts.inter(
                  fontSize: 14,
                  color: colors.textSecondary,
                ),
              ),
              
              const Spacer(flex: 3),
              
              SizedBox(
                width: double.infinity,
                child: ElevatedButton(
                  onPressed: () => context.go('/home'),
                  child: const Text('Commencer'),
                ),
              ),
              
              const SizedBox(height: AppTheme.spacingLg),
            ],
          ),
        ),
      ),
    );
  }
}
