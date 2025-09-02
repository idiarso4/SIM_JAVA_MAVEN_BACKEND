import 'dart:convert';
import 'package:http/http.dart' as http;
import '../config/api_config.dart';
import '../models/student_model.dart';
import '../services/auth_service.dart';

class StudentService {
  final http.Client _client = http.Client();
  final AuthService _authService = AuthService();

  Future<List<Student>> getStudents() async {
    try {
      final token = await _authService.getToken();
      final response = await _client.get(
        Uri.parse('${ApiConfig.baseUrl}${ApiConfig.studentsEndpoint}'),
        headers: {
          'Content-Type': 'application/json',
          'Authorization': 'Bearer $token',
        },
      );

      if (response.statusCode == 200) {
        final List<dynamic> jsonList = jsonDecode(response.body);
        return jsonList.map((json) => Student.fromJson(json)).toList();
      } else {
        throw Exception('Failed to load students: ${response.statusCode}');
      }
    } catch (e) {
      throw Exception('Network error: $e');
    }
  }

  Future<Student> createStudent(Student student) async {
    try {
      final token = await _authService.getToken();
      final response = await _client.post(
        Uri.parse('${ApiConfig.baseUrl}${ApiConfig.studentsEndpoint}'),
        headers: {
          'Content-Type': 'application/json',
          'Authorization': 'Bearer $token',
        },
        body: jsonEncode(student.toJson()),
      );

      if (response.statusCode == 201) {
        final jsonResponse = jsonDecode(response.body);
        return Student.fromJson(jsonResponse);
      } else {
        throw Exception('Failed to create student: ${response.statusCode}');
      }
    } catch (e) {
      throw Exception('Network error: $e');
    }
  }

  Future<Student> updateStudent(Student student) async {
    try {
      final token = await _authService.getToken();
      final response = await _client.put(
        Uri.parse('${ApiConfig.baseUrl}${ApiConfig.studentsEndpoint}/${student.id}'),
        headers: {
          'Content-Type': 'application/json',
          'Authorization': 'Bearer $token',
        },
        body: jsonEncode(student.toJson()),
      );

      if (response.statusCode == 200) {
        final jsonResponse = jsonDecode(response.body);
        return Student.fromJson(jsonResponse);
      } else {
        throw Exception('Failed to update student: ${response.statusCode}');
      }
    } catch (e) {
      throw Exception('Network error: $e');
    }
  }

  Future<void> deleteStudent(int id) async {
    try {
      final token = await _authService.getToken();
      final response = await _client.delete(
        Uri.parse('${ApiConfig.baseUrl}${ApiConfig.studentsEndpoint}/$id'),
        headers: {
          'Authorization': 'Bearer $token',
        },
      );

      if (response.statusCode != 200) {
        throw Exception('Failed to delete student: ${response.statusCode}');
      }
    } catch (e) {
      throw Exception('Network error: $e');
    }
  }
}