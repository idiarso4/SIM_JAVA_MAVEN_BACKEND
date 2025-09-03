import 'package:flutter/material.dart';
import '../config/app_theme.dart';
import '../screens/dashboard_screen.dart';
import '../screens/students_screen.dart';
import '../screens/attendance_detail_screen.dart';
import '../screens/face_recognition_screen.dart';
import '../screens/map_screen.dart';

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

  final _titles = [
    'Dashboard',
    'Data Siswa',
    'Absensi',
    'Face Recognition',
    'Peta Sekolah',
  ];

  @override
  Widget build(BuildContext context) {
    return Scaffold(
      body: _pages[_index],
      bottomNavigationBar: Container(
        decoration: BoxDecoration(
          color: Colors.white,
          boxShadow: [
            BoxShadow(
              color: Colors.black.withOpacity(0.1),
              blurRadius: 10,
              offset: const Offset(0, -2),
            ),
          ],
        ),
        child: SafeArea(
          child: Padding(
            padding: const EdgeInsets.symmetric(horizontal: 8, vertical: 8),
            child: Row(
              mainAxisAlignment: MainAxisAlignment.spaceAround,
              children: [
                _buildNavItem(
                  index: 0,
                  icon: Icons.home_outlined,
                  selectedIcon: Icons.home,
                  label: 'Beranda',
                ),
                _buildNavItem(
                  index: 1,
                  icon: Icons.school_outlined,
                  selectedIcon: Icons.school,
                  label: 'Siswa',
                ),
                _buildNavItem(
                  index: 2,
                  icon: Icons.fact_check_outlined,
                  selectedIcon: Icons.fact_check,
                  label: 'Absensi',
                ),
                _buildNavItem(
                  index: 3,
                  icon: Icons.face_outlined,
                  selectedIcon: Icons.face,
                  label: 'Face ID',
                ),
                _buildNavItem(
                  index: 4,
                  icon: Icons.map_outlined,
                  selectedIcon: Icons.map,
                  label: 'Peta',
                ),
              ],
            ),
          ),
        ),
      ),
    );
  }

  Widget _buildNavItem({
    required int index,
    required IconData icon,
    required IconData selectedIcon,
    required String label,
  }) {
    final isSelected = _index == index;
    
    return GestureDetector(
      onTap: () => setState(() => _index = index),
      child: AnimatedContainer(
        duration: const Duration(milliseconds: 200),
        padding: const EdgeInsets.symmetric(horizontal: 12, vertical: 8),
        decoration: BoxDecoration(
          color: isSelected ? AppTheme.primaryYellow.withOpacity(0.1) : Colors.transparent,
          borderRadius: BorderRadius.circular(12),
        ),
        child: Column(
          mainAxisSize: MainAxisSize.min,
          children: [
            AnimatedSwitcher(
              duration: const Duration(milliseconds: 200),
              child: Icon(
                isSelected ? selectedIcon : icon,
                key: ValueKey(isSelected),
                color: isSelected ? AppTheme.primaryYellow : AppTheme.darkGrey,
                size: 24,
              ),
            ),
            const SizedBox(height: 4),
            Text(
              label,
              style: TextStyle(
                fontSize: 11,
                fontWeight: isSelected ? FontWeight.w600 : FontWeight.normal,
                color: isSelected ? AppTheme.primaryYellow : AppTheme.darkGrey,
              ),
            ),
          ],
        ),
      ),
    );
  }
}