import 'dart:convert';
import 'package:flutter/foundation.dart';
import '../api/api_client.dart';
import 'token_store.dart';

class AuthRepository extends ChangeNotifier {
  AuthRepository() {
    _tokenStore = TokenStore();
    _client = ApiClient(_tokenStore);
  }

  late final TokenStore _tokenStore;
  late final ApiClient _client;
  bool isAuthenticated = false;
  bool isLoading = true;

  Future<void> loadToken() async {
    final token = await _tokenStore.readToken();
    isAuthenticated = token != null && token.isNotEmpty;
    isLoading = false;
    notifyListeners();
  }

  Future<String?> login(String identifier, String password) async {
    final res = await _client.post('/auth/login', body: {
      'identifier': identifier,
      'password': password,
    });

    if (res.statusCode == 200) {
      final data = jsonDecode(res.body) as Map<String, dynamic>;
      final token = data['token'] as String?;
      if (token != null && token.isNotEmpty) {
        await _tokenStore.writeToken(token);
        isAuthenticated = true;
        notifyListeners();
        return null;
      }
      return 'Invalid token from server';
    }
    return 'Login failed (${res.statusCode})';
  }

  Future<void> logout() async {
    try {
      await _client.post('/auth/logout');
    } catch (_) {}
    await _tokenStore.clear();
    isAuthenticated = false;
    notifyListeners();
  }
}


