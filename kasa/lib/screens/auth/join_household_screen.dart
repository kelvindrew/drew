import 'package:flutter/material.dart';
import 'package:flutter_riverpod/flutter_riverpod.dart';
import 'package:go_router/go_router.dart';
import 'package:google_fonts/google_fonts.dart';
import '../../app/theme/app_theme.dart';
import '../../providers/auth_provider.dart';
import '../../providers/firestore_service_provider.dart';
import '../../utils/validators.dart';

class JoinHouseholdScreen extends ConsumerStatefulWidget {
  const JoinHouseholdScreen({super.key});

  @override
  ConsumerState<JoinHouseholdScreen> createState() => _JoinHouseholdScreenState();
}

class _JoinHouseholdScreenState extends ConsumerState<JoinHouseholdScreen> {
  final _controller = TextEditingController();
  final _formKey = GlobalKey<FormState>();
  bool _isLoading = false;

  @override
  void dispose() {
    _controller.dispose();
    super.dispose();
  }

  Future<void> _join() async {
    final colors = AppTheme.of(context);
    if (!_formKey.currentState!.validate()) return;
    
    setState(() => _isLoading = true);
    
    try {
      final authUser = ref.read(authStateProvider).valueOrNull;
      if (authUser == null) {
        // First sign in
        await ref.read(authServiceProvider).signInAnonymously();
      }
      
      final user = ref.read(authStateProvider).valueOrNull;
      if (user == null) return;

      final code = Validators.cleanInviteCode(_controller.text);
      final household = await ref.read(firestoreServiceProvider).joinHousehold(code, user.uid);
      
      if (household != null && mounted) {
        ScaffoldMessenger.of(context).showSnackBar(
          SnackBar(
            content: Text('Bienvenue dans ${household.name} !'),
            backgroundColor: colors.success,
          ),
        );
        context.go('/home');
      } else if (mounted) {
        ScaffoldMessenger.of(context).showSnackBar(
          SnackBar(
            content: const Text('Code invalide ou foyer introuvable.'),
            backgroundColor: colors.error,
          ),
        );
      }
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

  @override
  Widget build(BuildContext context) {
    final colors = AppTheme.of(context);
    return Scaffold(
      appBar: AppBar(
        leading: IconButton(
          icon: const Icon(Icons.arrow_back_ios),
          onPressed: () => context.pop(),
        ),
      ),
      body: SafeArea(
        child: Padding(
          padding: const EdgeInsets.symmetric(horizontal: AppTheme.spacingLg),
          child: Form(
            key: _formKey,
            child: Column(
              crossAxisAlignment: CrossAxisAlignment.start,
              children: [
                const SizedBox(height: AppTheme.spacingXl),
                
                Text(
                  'Rejoindre un foyer',
                  style: GoogleFonts.inter(
                    fontSize: 28,
                    fontWeight: FontWeight.w700,
                    color: colors.textPrimary,
                  ),
                ),
                const SizedBox(height: AppTheme.spacingSm),
                Text(
                  'Entrez le code d\'invitation partagé par un membre.',
                  style: GoogleFonts.inter(fontSize: 15, color: colors.textSecondary),
                ),
                
                const SizedBox(height: AppTheme.spacingXl),
                
                TextFormField(
                  controller: _controller,
                  validator: Validators.inviteCode,
                  textCapitalization: TextCapitalization.characters,
                  autofocus: true,
                  textAlign: TextAlign.center,
                  style: GoogleFonts.inter(
                    fontSize: 24,
                    fontWeight: FontWeight.w600,
                    letterSpacing: 4,
                  ),
                  decoration: const InputDecoration(
                    hintText: 'KASA-XXXX',
                    labelText: 'Code d\'invitation',
                    prefixIcon: Icon(Icons.vpn_key_outlined),
                  ),
                  onFieldSubmitted: (_) => _join(),
                ),
                
                const Spacer(),
                
                SizedBox(
                  width: double.infinity,
                  child: ElevatedButton(
                    onPressed: _isLoading ? null : _join,
                    child: _isLoading
                        ? const SizedBox(
                            height: 20,
                            width: 20,
                            child: CircularProgressIndicator(strokeWidth: 2, color: Colors.white),
                          )
                        : const Text('Rejoindre'),
                  ),
                ),
                
                const SizedBox(height: AppTheme.spacingLg),
              ],
            ),
          ),
        ),
      ),
    );
  }
}
