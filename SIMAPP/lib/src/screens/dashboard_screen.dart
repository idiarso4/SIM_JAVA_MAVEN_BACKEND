import 'dart:convert';
import 'package:flutter/material.dart';
import 'package:provider/provider.dart';
import '../api/api_client.dart';
import '../auth/auth_repository.dart';
import '../auth/token_store.dart';

class DashboardScreen extends StatefulWidget {
  const DashboardScreen({super.key});

  @override
  State<DashboardScreen> createState() => _DashboardScreenState();
}

class _DashboardScreenState extends State<DashboardScreen> {
  late final ApiClient _client;
  Map<String, dynamic>? _stats;
  bool _loading = false;
  String? _error;

  @override
  void initState() {
    super.initState();
    _client = ApiClient(TokenStore());
    _loadStats();
  }

  Future<void> _loadStats() async {
    setState(() {
      _loading = true;
      _error = null;
    });
    try {
      final res = await _client.get('/dashboard/stats');
      if (res.statusCode == 200) {
        _stats = jsonDecode(res.body) as Map<String, dynamic>;
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
      appBar: AppBar(
        title: const Text('SIM - Dashboard'),
        actions: [
          IconButton(
            icon: const Icon(Icons.logout),
            onPressed: () async {
              await context.read<AuthRepository>().logout();
            },
          )
        ],
      ),
      body: RefreshIndicator(
        onRefresh: _loadStats,
        child: _loading
            ? const Center(child: CircularProgressIndicator())
            : _error != null
                ? ListView(children: [
                    Padding(
                      padding: const EdgeInsets.all(16),
                      child: Text('Error: $_error'),
                    )
                  ])
                : ListView(
                    padding: const EdgeInsets.all(16),
                    children: [
                      _StatCard(title: 'Total Students', value: '${_stats?['totalStudents'] ?? '-'}'),
                      _StatCard(title: 'Total Users', value: '${_stats?['totalUsers'] ?? '-'}'),
                      _StatCard(title: 'Active Classes', value: '${_stats?['activeClasses'] ?? '-'}'),
                      _StatCard(title: 'Pending Tasks', value: '${_stats?['pendingTasks'] ?? '-'}'),
                      const SizedBox(height: 12),
                      FilledButton.icon(
                        onPressed: _loadStats,
                        icon: const Icon(Icons.refresh),
                        label: const Text('Reload'),
                      ),
                      const SizedBox(height: 8),
                      FilledButton.tonalIcon(
                        onPressed: () => Navigator.of(context).pushNamed('/students'),
                        icon: const Icon(Icons.school),
                        label: const Text('View Students'),
                      ),
                    ],
                  ),
      ),
    );
  }
}

class _StatCard extends StatelessWidget {
  const _StatCard({required this.title, required this.value});
  final String title;
  final String value;

  @override
  Widget build(BuildContext context) {
    return Card(
      child: ListTile(
        title: Text(title),
        trailing: Text(value, style: Theme.of(context).textTheme.headlineSmall),
      ),
    );
  }
}


