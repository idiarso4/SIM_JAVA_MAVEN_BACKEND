import 'package:flutter/material.dart';
import 'package:provider/provider.dart';
import '../auth/auth_repository.dart';
import 'dashboard_screen.dart';
import 'students_screen.dart';
import 'attendance_detail_screen.dart';
import 'face_recognition_screen.dart';
import 'map_screen.dart';

class HomeShell extends StatefulWidget {
  const HomeShell({super.key});

  @override
  State<HomeShell> createState() => _HomeShellState();
}

class _HomeShellState extends State<HomeShell> {
  int _index = 0;
  final _pages = const [
    DashboardScreen(),
    StudentsScreen(),
    AttendanceDetailScreen(),
    FaceRecognitionScreen(),
    MapScreen(),
  ];

  @override
  Widget build(BuildContext context) {
    return Scaffold(
      appBar: AppBar(
        title: const Text('SIM'),
        actions: [
          IconButton(
            icon: const Icon(Icons.logout),
            onPressed: () => context.read<AuthRepository>().logout(),
          )
        ],
      ),
      body: _pages[_index],
      bottomNavigationBar: NavigationBar(
        selectedIndex: _index,
        onDestinationSelected: (i) => setState(() => _index = i),
        destinations: const [
          NavigationDestination(icon: Icon(Icons.home_outlined), selectedIcon: Icon(Icons.home), label: 'Home'),
          NavigationDestination(icon: Icon(Icons.school_outlined), selectedIcon: Icon(Icons.school), label: 'Students'),
          NavigationDestination(icon: Icon(Icons.fact_check_outlined), selectedIcon: Icon(Icons.fact_check), label: 'Attendance'),
          NavigationDestination(icon: Icon(Icons.face_retouching_natural_outlined), selectedIcon: Icon(Icons.face), label: 'Face'),
          NavigationDestination(icon: Icon(Icons.map_outlined), selectedIcon: Icon(Icons.map), label: 'Map'),
        ],
      ),
    );
  }
}


