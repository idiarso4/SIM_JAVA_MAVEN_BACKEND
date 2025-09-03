import 'package:flutter/foundation.dart';

class ApiConfig {
  static String get baseUrl {
    if (kIsWeb) {
      return 'http://localhost:8080/api/v1';
    } else {
      // For Android emulator
      return 'http://10.0.2.2:8080/api/v1';
    }
  }
  
  static const String loginEndpoint = '/auth/login';
  static const String studentsEndpoint = '/students';
}