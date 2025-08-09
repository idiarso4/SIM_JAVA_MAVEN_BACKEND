import 'package:flutter_secure_storage/flutter_secure_storage.dart';
import '../config/config.dart';

class TokenStore {
  final FlutterSecureStorage _storage = const FlutterSecureStorage();

  Future<void> writeToken(String token) async {
    await _storage.write(key: AppConfig.tokenKey, value: token);
  }

  Future<String?> readToken() async {
    return _storage.read(key: AppConfig.tokenKey);
  }

  Future<void> clear() async {
    await _storage.delete(key: AppConfig.tokenKey);
  }
}


