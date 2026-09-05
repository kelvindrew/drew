import 'package:flutter/material.dart';
import 'package:flutter_riverpod/flutter_riverpod.dart';
import 'package:go_router/go_router.dart';
import 'package:google_fonts/google_fonts.dart';
import '../../app/theme/app_theme.dart';
import '../../models/message_model.dart';
import '../../models/poll_model.dart';
import '../../providers/chat_provider.dart';
import '../../providers/auth_provider.dart';
import '../../providers/household_provider.dart';
import '../../providers/firestore_service_provider.dart';
import '../../utils/date_formatter.dart';

class ChatScreen extends ConsumerStatefulWidget {
  const ChatScreen({super.key});

  @override
  ConsumerState<ChatScreen> createState() => _ChatScreenState();
}

class _ChatScreenState extends ConsumerState<ChatScreen> {
  final _messageController = TextEditingController();
  final _scrollController = ScrollController();
  bool _isSending = false;

  @override
  void dispose() {
    _messageController.dispose();
    _scrollController.dispose();
    super.dispose();
  }

  Future<void> _sendMessage() async {
    final colors = AppTheme.of(context);
    final text = _messageController.text.trim();
    if (text.isEmpty) return;

    final household = ref.read(householdProvider).valueOrNull;
    final user = ref.read(userProfileProvider).valueOrNull;
    if (household == null || user == null) return;

    setState(() => _isSending = true);

    try {
      final message = MessageModel(
        id: '',
        householdId: household.id,
        userId: user.id,
        text: text,
        createdAt: DateTime.now(),
      );

      await ref.read(firestoreServiceProvider).sendMessage(message);
      _messageController.clear();

      if (_scrollController.hasClients) {
        _scrollController.animateTo(
          0,
          duration: const Duration(milliseconds: 300),
          curve: Curves.easeOut,
        );
      }
    } catch (e) {
      if (mounted) {
        ScaffoldMessenger.of(context).showSnackBar(
          SnackBar(content: Text('Erreur: $e'), backgroundColor: colors.error),
        );
      }
    } finally {
      if (mounted) setState(() => _isSending = false);
    }
  }

  @override
  Widget build(BuildContext context) {
    final colors = AppTheme.of(context);
    final messages = ref.watch(messagesProvider).valueOrNull ?? [];
    final polls = ref.watch(pollsProvider).valueOrNull ?? [];
    final user = ref.watch(userProfileProvider).valueOrNull;

    return Scaffold(
      appBar: AppBar(
        title: Row(
          children: [
            Container(
              width: 36,
              height: 36,
              decoration: BoxDecoration(
                color: colors.primaryLight,
                borderRadius: BorderRadius.circular(18),
              ),
              child: const Center(child: Text('💬', style: TextStyle(fontSize: 18))),
            ),
            const SizedBox(width: 10),
            Column(
              crossAxisAlignment: CrossAxisAlignment.start,
              children: [
                Text(
                  'Maison',
                  style: GoogleFonts.inter(
                    fontSize: 16,
                    fontWeight: FontWeight.w600,
                    color: colors.textPrimary,
                  ),
                ),
                Text(
                  'Espace de discussion',
                  style: GoogleFonts.inter(
                    fontSize: 11,
                    color: colors.textTertiary,
                  ),
                ),
              ],
            ),
          ],
        ),
      ),
      body: Column(
        children: [
          // Active Polls section (if any)
          if (polls.isNotEmpty)
            Container(
              padding: const EdgeInsets.symmetric(horizontal: 16, vertical: 8),
              color: colors.surfaceVariant.withValues(alpha: 0.5),
              child: Column(
                crossAxisAlignment: CrossAxisAlignment.start,
                children: [
                  Row(
                    children: [
                      const Text('📊', style: TextStyle(fontSize: 16)),
                      const SizedBox(width: 6),
                      Text(
                        'Sondage en cours (${polls.length})',
                        style: GoogleFonts.inter(
                          fontSize: 13,
                          fontWeight: FontWeight.w600,
                          color: colors.primary,
                        ),
                      ),
                    ],
                  ),
                  const SizedBox(height: 6),
                  ...polls.map((poll) => _PollCard(poll: poll, currentUserId: user?.id ?? '')),
                ],
              ),
            ),

          // Messages list
          Expanded(
            child: messages.isEmpty
                ? Center(
                    child: Column(
                      mainAxisAlignment: MainAxisAlignment.center,
                      children: [
                        const Text('💬', style: TextStyle(fontSize: 64)),
                        const SizedBox(height: 16),
                        Text(
                          'Aucun message',
                          style: GoogleFonts.inter(
                            fontSize: 20,
                            fontWeight: FontWeight.w600,
                            color: colors.textPrimary,
                          ),
                        ),
                        const SizedBox(height: 8),
                        Text(
                          'Commencez la conversation !',
                          style: GoogleFonts.inter(
                            fontSize: 14,
                            color: colors.textSecondary,
                          ),
                        ),
                      ],
                    ),
                  )
                : ListView.builder(
                    controller: _scrollController,
                    reverse: true,
                    padding: const EdgeInsets.symmetric(horizontal: 20, vertical: 12),
                    itemCount: messages.length,
                    itemBuilder: (context, index) {
                      final message = messages[index];
                      final isMe = message.userId == user?.id;
                      final showAvatar = index == 0 ||
                          messages[index - 1].userId != message.userId;

                      return _MessageBubble(
                        message: message,
                        isMe: isMe,
                        showAvatar: showAvatar,
                      );
                    },
                  ),
          ),

          // Input bar
          Container(
            padding: EdgeInsets.only(
              left: 16,
              right: 8,
              top: 8,
              bottom: MediaQuery.of(context).padding.bottom + 8,
            ),
            decoration: BoxDecoration(
              color: colors.surface,
              boxShadow: [
                BoxShadow(
                  color: Colors.black.withValues(alpha: 0.05),
                  blurRadius: 10,
                  offset: const Offset(0, -2),
                ),
              ],
            ),
            child: Row(
              children: [
                // Action button
                IconButton(
                  onPressed: _showActions,
                  icon: const Icon(Icons.add_circle_outline, size: 26),
                  color: colors.primary,
                ),
                const SizedBox(width: 4),
                // Text field
                Expanded(
                  child: Container(
                    decoration: BoxDecoration(
                      color: colors.surfaceVariant,
                      borderRadius: BorderRadius.circular(24),
                    ),
                    child: TextField(
                      controller: _messageController,
                      maxLines: null,
                      textInputAction: TextInputAction.send,
                      onSubmitted: (_) => _sendMessage(),
                      decoration: InputDecoration(
                        hintText: 'Écrire un message...',
                        border: OutlineInputBorder(
                          borderRadius: BorderRadius.circular(24),
                          borderSide: BorderSide.none,
                        ),
                        contentPadding: const EdgeInsets.symmetric(
                          horizontal: 16,
                          vertical: 10,
                        ),
                      ),
                    ),
                  ),
                ),
                const SizedBox(width: 4),
                // Send button
                IconButton(
                  onPressed: _isSending ? null : _sendMessage,
                  icon: _isSending
                      ? const SizedBox(
                          width: 20,
                          height: 20,
                          child: CircularProgressIndicator(strokeWidth: 2),
                        )
                      : const Icon(Icons.send, size: 22),
                  color: colors.primary,
                ),
              ],
            ),
          ),
        ],
      ),
    );
  }

  void _showActions() {
    final colors = AppTheme.of(context);
    showModalBottomSheet(
      context: context,
      builder: (context) => SafeArea(
        child: Padding(
          padding: const EdgeInsets.symmetric(vertical: 16),
          child: Column(
            mainAxisSize: MainAxisSize.min,
            children: [
              Container(
                width: 40,
                height: 4,
                decoration: BoxDecoration(
                  color: colors.border,
                  borderRadius: BorderRadius.circular(2),
                ),
              ),
              const SizedBox(height: 16),
              ListTile(
                leading: Icon(Icons.shopping_cart_outlined, color: colors.primary),
                title: const Text('Ajouter un achat'),
                onTap: () {
                  Navigator.pop(context);
                  context.push('/home/provision');
                },
              ),
              ListTile(
                leading: Icon(Icons.credit_card_outlined, color: colors.primary),
                title: const Text('Ajouter une dépense'),
                onTap: () {
                  Navigator.pop(context);
                  context.push('/home/expenses/add');
                },
              ),
              ListTile(
                leading: Icon(Icons.list_alt, color: colors.primary),
                title: const Text('Ajouter à la liste de courses'),
                onTap: () {
                  Navigator.pop(context);
                  context.push('/home/provision/list');
                },
              ),
              ListTile(
                leading: Icon(Icons.bar_chart, color: colors.primary),
                title: const Text('Voir le budget'),
                onTap: () {
                  Navigator.pop(context);
                  context.go('/home');
                },
              ),
              ListTile(
                leading: Icon(Icons.how_to_vote, color: colors.primary),
                title: const Text('Créer un sondage'),
                onTap: () {
                  Navigator.pop(context);
                  _showCreatePollDialog();
                },
              ),
            ],
          ),
        ),
      ),
    );
  }

  void _showCreatePollDialog() {
    final colors = AppTheme.of(context);
    final questionController = TextEditingController();
    final optionsController = <TextEditingController>[
      TextEditingController(),
      TextEditingController(),
    ];

    showDialog(
      context: context,
      builder: (context) => StatefulBuilder(
        builder: (context, setDialogState) => AlertDialog(
          title: const Text('Créer un sondage'),
          content: SingleChildScrollView(
            child: Column(
              mainAxisSize: MainAxisSize.min,
              children: [
                TextField(
                  controller: questionController,
                  decoration: const InputDecoration(
                    hintText: 'Votre question ?',
                    labelText: 'Question',
                  ),
                ),
                const SizedBox(height: 16),
                ...List.generate(optionsController.length, (i) {
                  return Padding(
                    padding: const EdgeInsets.only(bottom: 8),
                    child: Row(
                      children: [
                        Expanded(
                          child: TextField(
                            controller: optionsController[i],
                            decoration: InputDecoration(
                              hintText: 'Option ${i + 1}',
                            ),
                          ),
                        ),
                        if (optionsController.length > 2)
                          IconButton(
                            icon: Icon(Icons.remove_circle_outline, color: colors.error),
                            onPressed: () {
                              setDialogState(() {
                                optionsController.removeAt(i);
                              });
                            },
                          ),
                      ],
                    ),
                  );
                }),
                TextButton.icon(
                  onPressed: () {
                    setDialogState(() {
                      optionsController.add(TextEditingController());
                    });
                  },
                  icon: const Icon(Icons.add),
                  label: const Text('Ajouter une option'),
                ),
              ],
            ),
          ),
          actions: [
            TextButton(
              onPressed: () => Navigator.pop(context),
              child: const Text('Annuler'),
            ),
            ElevatedButton(
              onPressed: () async {
                if (questionController.text.trim().isEmpty) return;
                final validOptions = optionsController
                    .map((c) => c.text.trim())
                    .where((t) => t.isNotEmpty)
                    .toList();
                if (validOptions.length < 2) return;

                final household = ref.read(householdProvider).valueOrNull;
                final user = ref.read(userProfileProvider).valueOrNull;
                if (household == null || user == null) return;

                final poll = PollModel(
                  id: '',
                  householdId: household.id,
                  creatorId: user.id,
                  question: questionController.text.trim(),
                  options: validOptions.map((opt) => PollOption(text: opt, voters: [])).toList(),
                  active: true,
                  createdAt: DateTime.now(),
                );

                await ref.read(firestoreServiceProvider).createPoll(poll);

                if (context.mounted) {
                  Navigator.pop(context);
                  ScaffoldMessenger.of(context).showSnackBar(
                    SnackBar(
                      content: const Text('Sondage créé !'),
                      backgroundColor: colors.success,
                    ),
                  );
                }
              },
              child: const Text('Créer'),
            ),
          ],
        ),
      ),
    );
  }
}

class _PollCard extends ConsumerWidget {
  final PollModel poll;
  final String currentUserId;

  const _PollCard({required this.poll, required this.currentUserId});

  @override
  Widget build(BuildContext context, WidgetRef ref) {
    final colors = AppTheme.of(context);
    final totalVotes = poll.totalVotes;

    return Container(
      margin: const EdgeInsets.only(bottom: 8),
      padding: const EdgeInsets.all(12),
      decoration: BoxDecoration(
        color: colors.surface,
        borderRadius: BorderRadius.circular(AppTheme.radiusMd),
        border: Border.all(color: colors.border),
      ),
      child: Column(
        crossAxisAlignment: CrossAxisAlignment.start,
        children: [
          Text(
            poll.question,
            style: GoogleFonts.inter(
              fontSize: 14,
              fontWeight: FontWeight.w600,
              color: colors.textPrimary,
            ),
          ),
          const SizedBox(height: 8),
          ...poll.options.asMap().entries.map((entry) {
            final index = entry.key;
            final option = entry.value;
            final hasVoted = option.voters.contains(currentUserId);
            final percentage = totalVotes > 0 ? (option.voteCount / totalVotes) : 0.0;

            return GestureDetector(
              onTap: () async {
                final household = ref.read(householdProvider).valueOrNull;
                if (household == null || currentUserId.isEmpty) return;
                await ref.read(firestoreServiceProvider).votePoll(
                  household.id,
                  poll.id,
                  index,
                  currentUserId,
                );
              },
              child: Container(
                margin: const EdgeInsets.only(bottom: 6),
                padding: const EdgeInsets.symmetric(horizontal: 10, vertical: 8),
                decoration: BoxDecoration(
                  color: hasVoted ? colors.primaryLight.withValues(alpha: 0.4) : colors.surfaceVariant,
                  borderRadius: BorderRadius.circular(8),
                  border: Border.all(
                    color: hasVoted ? colors.primary : colors.border,
                  ),
                ),
                child: Column(
                  crossAxisAlignment: CrossAxisAlignment.start,
                  children: [
                    Row(
                      mainAxisAlignment: MainAxisAlignment.spaceBetween,
                      children: [
                        Expanded(
                          child: Text(
                            option.text,
                            style: GoogleFonts.inter(
                              fontSize: 13,
                              fontWeight: hasVoted ? FontWeight.w600 : FontWeight.w400,
                              color: colors.textPrimary,
                            ),
                          ),
                        ),
                        if (hasVoted)
                          Icon(Icons.check_circle, size: 16, color: colors.primary),
                        const SizedBox(width: 6),
                        Text(
                          '${option.voteCount} (${(percentage * 100).toInt()}%)',
                          style: GoogleFonts.inter(
                            fontSize: 12,
                            color: colors.textSecondary,
                          ),
                        ),
                      ],
                    ),
                    const SizedBox(height: 4),
                    ClipRRect(
                      borderRadius: BorderRadius.circular(2),
                      child: LinearProgressIndicator(
                        value: percentage,
                        backgroundColor: Colors.grey.withValues(alpha: 0.2),
                        color: hasVoted ? colors.primary : colors.textSecondary,
                        minHeight: 4,
                      ),
                    ),
                  ],
                ),
              ),
            );
          }),
        ],
      ),
    );
  }
}


class _MessageBubble extends ConsumerWidget {
  final MessageModel message;
  final bool isMe;
  final bool showAvatar;

  const _MessageBubble({
    required this.message,
    required this.isMe,
    required this.showAvatar,
  });

  @override
  Widget build(BuildContext context, WidgetRef ref) {
    final colors = AppTheme.of(context);
    if (message.isSystem) {
      return Padding(
        padding: const EdgeInsets.symmetric(vertical: 8),
        child: Center(
          child: Container(
            padding: const EdgeInsets.symmetric(horizontal: 12, vertical: 6),
            decoration: BoxDecoration(
              color: colors.surfaceVariant,
              borderRadius: BorderRadius.circular(12),
            ),
            child: Text(
              message.text,
              style: GoogleFonts.inter(
                fontSize: 12,
                color: colors.textTertiary,
              ),
            ),
          ),
        ),
      );
    }

    return Align(
      alignment: isMe ? Alignment.centerRight : Alignment.centerLeft,
      child: Container(
        margin: EdgeInsets.only(
          top: showAvatar ? 12 : 2,
          left: isMe ? 60 : 0,
          right: isMe ? 0 : 60,
        ),
        child: Column(
          crossAxisAlignment: isMe ? CrossAxisAlignment.end : CrossAxisAlignment.start,
          children: [
            if (showAvatar && !isMe)
              Padding(
                padding: const EdgeInsets.only(bottom: 4),
                child: Text(
                  message.userId.substring(0, 8),
                  style: GoogleFonts.inter(
                    fontSize: 11,
                    fontWeight: FontWeight.w500,
                    color: colors.textTertiary,
                  ),
                ),
              ),
            Container(
              padding: const EdgeInsets.symmetric(horizontal: 14, vertical: 10),
              decoration: BoxDecoration(
                color: isMe ? colors.primary : colors.surface,
                borderRadius: BorderRadius.only(
                  topLeft: const Radius.circular(16),
                  topRight: const Radius.circular(16),
                  bottomLeft: Radius.circular(isMe ? 16 : 4),
                  bottomRight: Radius.circular(isMe ? 4 : 16),
                ),
                border: isMe ? null : Border.all(color: colors.border),
              ),
              child: Column(
                crossAxisAlignment: CrossAxisAlignment.start,
                children: [
                  Text(
                    message.text,
                    style: GoogleFonts.inter(
                      fontSize: 15,
                      color: isMe ? Colors.white : colors.textPrimary,
                    ),
                  ),
                  const SizedBox(height: 4),
                  Text(
                    DateFormatter.formatTime(message.createdAt),
                    style: GoogleFonts.inter(
                      fontSize: 10,
                      color: isMe ? Colors.white.withValues(alpha: 0.7) : colors.textTertiary,
                    ),
                  ),
                ],
              ),
            ),
          ],
        ),
      ),
    );
  }
}
