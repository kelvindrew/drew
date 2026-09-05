import 'package:firebase_auth/firebase_auth.dart';
import 'package:cloud_firestore/cloud_firestore.dart';
import '../models/user_model.dart';

class AuthService {
  final FirebaseAuth _auth = FirebaseAuth.instance;
  final FirebaseFirestore _firestore = FirebaseFirestore.instance;

  User? get currentUser => _auth.currentUser;
  Stream<User?> get authStateChanges => _auth.authStateChanges();

  Future<UserModel?> signInAnonymously() async {
    try {
      final result = await _auth.signInAnonymously();
      return await getUserOrCreate(result.user!.uid);
    } catch (e) {
      rethrow;
    }
  }

  Future<UserModel?> getUserOrCreate(String uid) async {
    final doc = await _firestore.collection('users').doc(uid).get();
    if (doc.exists) {
      return UserModel.fromFirestore(doc);
    }
    return null;
  }

  Stream<UserModel?> userStream(String uid) {
    return _firestore.collection('users').doc(uid).snapshots().map(
      (doc) => doc.exists ? UserModel.fromFirestore(doc) : null,
    );
  }

  Future<void> createUser(String uid, String name) async {
    final user = UserModel(
      id: uid,
      name: name,
      createdAt: DateTime.now(),
    );
    await _firestore.collection('users').doc(uid).set(user.toFirestore());
  }

  Future<void> updateUserName(String uid, String name) async {
    await _firestore.collection('users').doc(uid).update({'name': name});
  }

  Future<void> updateUserHousehold(String uid, String householdId) async {
    await _firestore.collection('users').doc(uid).update({'householdId': householdId});
  }

  Future<void> signOut() async {
    await _auth.signOut();
  }
}
