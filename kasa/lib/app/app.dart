import 'package:flutter/material.dart';
import 'package:flutter_riverpod/flutter_riverpod.dart';
import 'router.dart';
import 'theme/app_theme.dart';
import 'theme/app_theme_dark.dart';
import '../providers/theme_provider.dart';

class KasaApp extends ConsumerWidget {
  const KasaApp({super.key});

  @override
  Widget build(BuildContext context, WidgetRef ref) {
    final router = ref.watch(routerProvider);
    final themeMode = ref.watch(themeProvider);

    return MaterialApp.router(
      title: 'KASA',
      debugShowCheckedModeBanner: false,
      theme: AppTheme.lightTheme,
      darkTheme: AppThemeDark.darkTheme,
      themeMode: themeMode,
      routerConfig: router,
    );
  }
}
