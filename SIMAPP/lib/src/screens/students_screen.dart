import 'dart:convert';
import 'package:flutter/material.dart';
import '../api/api_client.dart';
import '../auth/token_store.dart';
import '../models/student.dart';

class StudentsScreen extends StatefulWidget {
  const StudentsScreen({super.key});

  @override
  State<StudentsScreen> createState() => _StudentsScreenState();
}

class _StudentsScreenState extends State<StudentsScreen> {
  late final ApiClient _client;
  bool _loading = true;
  String? _error;
  List<Student> _students = const [];

  @override
  void initState() {
    super.initState();
    _client = ApiClient(TokenStore());
    _load();
  }

  Future<void> _load() async {
    setState(() {
      _loading = true;
      _error = null;
    });
    try {
      final res = await _client.get('/students?size=100&sortBy=namaLengkap');
      if (res.statusCode == 200) {
        final body = jsonDecode(res.body) as Map<String, dynamic>;
        final list = (body['content'] as List?) ?? [];
        _students = list.map((e) => Student.fromBackend(e as Map<String, dynamic>)).toList();
      } else {
        _error = 'Failed (${res.statusCode})';
      }
    } catch (e) {
      _error = e.toString();
    } finally {
      if (mounted) setState(() => _loading = false);
    }
  }

  @override
  Widget build(BuildContext context) {
    return Scaffold(
      appBar: AppBar(title: const Text('Students')),
      body: _loading
          ? const Center(child: CircularProgressIndicator())
          : _error != null
              ? Center(child: Text(_error!))
              : RefreshIndicator(
                  onRefresh: _load,
                  child: ListView.separated(
                    itemCount: _students.length,
                    separatorBuilder: (_, __) => const Divider(height: 1),
                    itemBuilder: (_, i) {
                      final s = _students[i];
                      return ListTile(
                        title: Text(s.name),
                        subtitle: Text([
                          if (s.className != null) 'Class: ${s.className}',
                          if (s.status != null) 'Status: ${s.status}',
                        ].join(' • ')),
                        trailing: Text(s.id),
                      );
                    },
                  ),
                ),
    );
  }
}


