import 'dart:convert';
import 'package:http/http.dart' as http;
import 'package:flutter_secure_storage/flutter_secure_storage.dart';
import '../config/api_config.dart';
import '../models/auth_model.dart';

class AuthService {
  final http.Client _client = http.Client();
  final FlutterSecureStorage _storage = const FlutterSecureStorage();

  Future<LoginResponse> login(LoginRequest request) async {
    try {
      final response = await _client.post(
        Uri.parse('${ApiConfig.baseUrl}${ApiConfig.loginEndpoint}'),
        headers: {
          'Content-Type': 'application/json',
        },
        body: jsonEncode({
          'identifier': request.email,
          'password': request.password,
        }),
      );

      if (response.statusCode == 200) {
        final jsonResponse = jsonDecode(response.body);
        return LoginResponse(
          token: jsonResponse['token'] ?? '',
          message: 'Login successful',
          success: true,
        );
      } else {
        final jsonResponse = jsonDecode(response.body);
        return LoginResponse(
          token: '',
          message: jsonResponse['message'] ?? 'Login failed',
          success: false,
        );
      }
    } catch (e) {
      // Log error for debugging
      // In production, you might want to use a proper logging framework
      return LoginResponse(
        token: '',
        message: 'Network error: $e',
        success: false,
      );
    }
  }

  Future<void> saveToken(String token) async {
    await _storage.write(key: 'auth_token', value: token);
  }

  Future<String?> getToken() async {
    return await _storage.read(key: 'auth_token');
  }

  Future<void> logout() async {
    try {
      final token = await getToken();
      if (token != null) {
        await _client.post(
          Uri.parse('${ApiConfig.baseUrl}/auth/logout'),
          headers: {
            'Authorization': 'Bearer $token',
          },
        );
      }
    } catch (e) {
      // Log error for debugging
      // In production, you might want to use a proper logging framework
    } finally {
      await _storage.delete(key: 'auth_token');
    }
  }
}