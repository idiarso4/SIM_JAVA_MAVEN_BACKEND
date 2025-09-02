import 'package:flutter/material.dart';
import 'src/screens/login_screen.dart';
import 'src/screens/dashboard_screen.dart';
import 'src/screens/students_screen.dart';
import 'src/screens/add_student_screen.dart';
import 'src/screens/edit_student_screen.dart';
import 'src/services/auth_service.dart';
import 'src/models/student_model.dart';

void main() {
  runApp(const MyApp());
}

class MyApp extends StatelessWidget {
  const MyApp({super.key});

  @override
  Widget build(BuildContext context) {
    return MaterialApp(
      title: 'SIM App',
      theme: ThemeData(
        colorScheme: ColorScheme.fromSeed(seedColor: Colors.indigo),
        useMaterial3: true,
      ),
      debugShowCheckedModeBanner: false,
      initialRoute: '/login',
      routes: {
        '/login': (context) => const LoginScreen(),
        '/dashboard': (context) => const DashboardScreen(),
        '/students': (context) => const StudentsScreen(),
        '/students/add': (context) => const AddStudentScreen(),
        '/students/edit': (context) => EditStudentScreen(
              student: ModalRoute.of(context)!.settings.arguments as Student,
            ),
      },
    );
  }
}