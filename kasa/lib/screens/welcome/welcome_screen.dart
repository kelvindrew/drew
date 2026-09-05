import 'package:flutter/material.dart';
import 'package:flutter_animate/flutter_animate.dart';
import 'package:go_router/go_router.dart';
import 'package:google_fonts/google_fonts.dart';
import '../../app/theme/app_theme.dart';

class WelcomeScreen extends StatelessWidget {
  const WelcomeScreen({super.key});

  @override
  Widget build(BuildContext context) {
    final colors = AppTheme.of(context);
    return Scaffold(
      body: SafeArea(
        child: Padding(
          padding: const EdgeInsets.symmetric(horizontal: AppTheme.spacingLg),
          child: Column(
            children: [
              const Spacer(flex: 2),
              // Logo
              Container(
                width: 100,
                height: 100,
                decoration: BoxDecoration(
                  color: colors.primary,
                  borderRadius: BorderRadius.circular(AppTheme.radiusXl),
                  boxShadow: [
                    BoxShadow(
                      color: colors.primary.withValues(alpha: 0.3),
                      blurRadius: 30,
                      offset: const Offset(0, 10),
                    ),
                  ],
                ),
                child: Center(
                  child: Text(
                    'K',
                    style: GoogleFonts.inter(
                      fontSize: 48,
                      fontWeight: FontWeight.w700,
                      color: Colors.white,
                    ),
                  ),
                ),
              ).animate().scale(duration: 600.ms, curve: Curves.easeOutBack),
              
              const SizedBox(height: AppTheme.spacingLg),
              
              // App Name
              Text(
                'KASA',
                style: GoogleFonts.inter(
                  fontSize: 40,
                  fontWeight: FontWeight.w700,
                  color: colors.textPrimary,
                  letterSpacing: 4,
                ),
              ).animate().fadeIn(delay: 200.ms, duration: 600.ms),
              
              const SizedBox(height: AppTheme.spacingSm),
              
              // Slogan
              Text(
                'Notre maison, notre budget.',
                style: GoogleFonts.inter(
                  fontSize: 16,
                  color: colors.textSecondary,
                  fontWeight: FontWeight.w400,
                ),
              ).animate().fadeIn(delay: 400.ms, duration: 600.ms),
              
              const Spacer(flex: 3),
              
              // Description
              Text(
                'Gérez le budget de votre foyer en toute simplicité. Dépenses, provision, communication — tout au même endroit.',
                textAlign: TextAlign.center,
                style: GoogleFonts.inter(
                  fontSize: 15,
                  color: colors.textSecondary,
                  height: 1.5,
                ),
              ).animate().fadeIn(delay: 600.ms, duration: 600.ms),
              
              const Spacer(flex: 3),
              
              // Create Household Button
              SizedBox(
                width: double.infinity,
                child: ElevatedButton(
                  onPressed: () => context.push('/name'),
                  child: const Text('Créer un foyer'),
                ),
              ).animate().fadeIn(delay: 800.ms).slideY(begin: 0.2, duration: 400.ms),
              
              const SizedBox(height: AppTheme.spacingMd),
              
              // Join Household Button
              SizedBox(
                width: double.infinity,
                child: OutlinedButton(
                  onPressed: () => context.push('/join-household'),
                  child: const Text('Rejoindre un foyer'),
                ),
              ).animate().fadeIn(delay: 900.ms).slideY(begin: 0.2, duration: 400.ms),
              
              const Spacer(flex: 1),
            ],
          ),
        ),
      ),
    );
  }
}
