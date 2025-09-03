import 'package:flutter/material.dart';
import 'src/config/app_theme.dart';
import 'src/screens/login_screen.dart';
import 'src/screens/home_shell.dart';
import 'src/screens/add_student_screen.dart';
import 'src/screens/edit_student_screen.dart';
import 'src/models/student_model.dart';

void main() {
  runApp(const MyApp());
}

class MyApp extends StatelessWidget {
  const MyApp({super.key});

  @override
  Widget build(BuildContext context) {
    return MaterialApp(
      title: 'SIM Vokasi',
      theme: AppTheme.lightTheme,
      debugShowCheckedModeBanner: false,
      initialRoute: '/login',
      routes: {
        '/login': (context) => const LoginScreen(),
        '/dashboard': (context) => const HomeShell(),
        '/students/add': (context) => const AddStudentScreen(),
        '/students/edit': (context) => EditStudentScreen(
              student: ModalRoute.of(context)!.settings.arguments as Student,
            ),
      },
    );
  }
}