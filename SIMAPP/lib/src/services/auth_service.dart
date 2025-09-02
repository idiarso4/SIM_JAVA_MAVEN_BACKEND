import 'dart:convert';
import 'package:http/http.dart' as http;
import 'package:flutter_secure_storage/flutter_secure_storage.dart';
import '../config/api_config.dart';
import '../models/auth_model.dart';

class AuthService {
  final http.Client _client = http.Client();
  final FlutterSecureStorage _storage = const FlutterSecureStorage();

  // Mock login - always returns success for demonstration
  Future<LoginResponse> login(LoginRequest request) async {
    try {
      // Simulasi delay jaringan
      await Future.delayed(const Duration(seconds: 1));
      
      // Validasi sederhana
      if (request.email.isEmpty || request.password.isEmpty) {
        return LoginResponse(
          token: '',
          message: 'Email and password are required',
          success: false,
        );
      }
      
      // Untuk demonstrasi, kita terima semua login dengan credential yang tidak kosong
      // Token ini adalah JWT mock
      final mockToken = 'eyJhbGciOiJIUzI1NiIsInR5cCI6IkpXVCJ9.eyJzdWIiOiIxMjM0NTY3ODkwIiwibmFtZSI6Ik1vY2sgVXNlciIsImlhdCI6MTUxNjIzOTAyMn0.6k-4Mow6J9YBxG6dUpdTr4kD2NPOuNg5lU0vR8nLn8I';
      
      return LoginResponse(
        token: mockToken,
        message: 'Login successful',
        success: true,
      );
    } catch (e, stackTrace) {
      print('Login error: $e');
      print('Stack trace: $stackTrace');
      return LoginResponse(
        token: '',
        message: 'Login failed: $e',
        success: false,
      );
    }
  }

  Future<void> saveToken(String token) async {
    await _storage.write(key: 'auth_token', value: token);
    print('Token saved: $token');
  }

  Future<String?> getToken() async {
    final token = await _storage.read(key: 'auth_token');
    print('Token retrieved: $token');
    return token;
  }

  Future<void> logout() async {
    await _storage.delete(key: 'auth_token');
    print('Token deleted');
  }
}