import 'package:flutter/material.dart';
import 'package:google_fonts/google_fonts.dart';

class AppTheme {
  // Couleurs Ultra Premium (Inspiration Dark Mode / Luxe)
  static const Color backgroundDark = Color(0xFF0F0F13);
  static const Color surfaceDark = Color(0xFF1C1C21);
  static const Color accentGold = Color(0xFFD4AF37);
  static const Color accentNeonGreen = Color(0xFF00E676);
  static const Color textPrimary = Color(0xFFF5F5F5);
  static const Color textSecondary = Color(0xFFA0A0A5);

  static ThemeData get darkTheme {
    return ThemeData(
      brightness: Brightness.dark,
      scaffoldBackgroundColor: backgroundDark,
      primaryColor: accentGold,
      colorScheme: const ColorScheme.dark(
        primary: accentGold,
        secondary: accentNeonGreen,
        surface: surfaceDark,
        background: backgroundDark,
      ),
      textTheme: GoogleFonts.outfitTextTheme(ThemeData.dark().textTheme).copyWith(
        displayLarge: GoogleFonts.outfit(color: textPrimary, fontWeight: FontWeight.bold),
        bodyLarge: GoogleFonts.outfit(color: textPrimary),
        bodyMedium: GoogleFonts.outfit(color: textSecondary),
      ),
      cardTheme: CardTheme(
        color: surfaceDark,
        elevation: 8,
        shape: RoundedRectangleBorder(borderRadius: BorderRadius.circular(20)),
      ),
      appBarTheme: const AppBarTheme(
        backgroundColor: Colors.transparent,
        elevation: 0,
        centerTitle: true,
      ),
    );
  }
}
