import 'dart:convert';
import 'package:http/http.dart' as http;
import '../config/config.dart';
import '../auth/token_store.dart';

class ApiClient {
  ApiClient(this._tokenStore);

  final TokenStore _tokenStore;

  Future<http.Response> get(String path, {Map<String, String>? headers}) async {
    return _request('GET', path, headers: headers);
  }

  Future<http.Response> post(String path, {Object? body, Map<String, String>? headers}) async {
    return _request('POST', path, body: body, headers: headers);
  }

  Future<http.Response> _request(String method, String path, {Object? body, Map<String, String>? headers}) async {
    final uri = Uri.parse('${AppConfig.baseUrl}$path');
    final token = await _tokenStore.readToken();
    final mergedHeaders = <String, String>{
      'Accept': 'application/json',
      'Content-Type': 'application/json',
      if (token != null) 'Authorization': 'Bearer $token',
      ...?headers,
    };

    switch (method) {
      case 'GET':
        return http.get(uri, headers: mergedHeaders);
      case 'POST':
        return http.post(uri, headers: mergedHeaders, body: body is String ? body : jsonEncode(body));
      default:
        throw UnsupportedError('Unsupported method: $method');
    }
  }
}


