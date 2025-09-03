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
        final jsonResponse = jsonDecode(response.body);
        
        // Handle paginated response
        if (jsonResponse is Map<String, dynamic> && jsonResponse.containsKey('content')) {
          final List<dynamic> jsonList = jsonResponse['content'];
          return jsonList.map((json) => Student.fromJson(json)).toList();
        } 
        // Handle direct array response
        else if (jsonResponse is List) {
          return jsonResponse.map((json) => Student.fromJson(json)).toList();
        } else {
          throw Exception('Unexpected response format');
        }
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
      
      // Create request body matching backend CreateStudentRequest
      final requestBody = {
        'nis': student.nim,
        'namaLengkap': student.name,
        'alamat': student.address,
        'noHpOrtu': student.phone,
        'tahunMasuk': DateTime.now().year,
        'status': 'ACTIVE',
      };
      
      final response = await _client.post(
        Uri.parse('${ApiConfig.baseUrl}${ApiConfig.studentsEndpoint}'),
        headers: {
          'Content-Type': 'application/json',
          'Authorization': 'Bearer $token',
        },
        body: jsonEncode(requestBody),
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
      
      // Create request body matching backend UpdateStudentRequest
      final requestBody = {
        'nis': student.nim,
        'namaLengkap': student.name,
        'alamat': student.address,
        'noHpOrtu': student.phone,
        'status': student.status ?? 'ACTIVE',
      };
      
      final response = await _client.put(
        Uri.parse('${ApiConfig.baseUrl}${ApiConfig.studentsEndpoint}/${student.id}'),
        headers: {
          'Content-Type': 'application/json',
          'Authorization': 'Bearer $token',
        },
        body: jsonEncode(requestBody),
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

      if (response.statusCode != 200 && response.statusCode != 204) {
        throw Exception('Failed to delete student: ${response.statusCode}');
      }
    } catch (e) {
      throw Exception('Network error: $e');
    }
  }
}