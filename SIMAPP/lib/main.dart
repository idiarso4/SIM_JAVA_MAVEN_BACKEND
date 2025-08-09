import 'package:flutter/material.dart';
import 'package:provider/provider.dart';
import 'src/config/config.dart';
import 'src/auth/auth_repository.dart';
import 'src/screens/login_screen.dart';
import 'src/screens/home_shell.dart';
import 'src/screens/dashboard_screen.dart';
import 'src/screens/students_screen.dart';

void main() {
  WidgetsFlutterBinding.ensureInitialized();
  runApp(const SimApp());
}

class SimApp extends StatelessWidget {
  const SimApp({super.key});

  @override
  Widget build(BuildContext context) {
    return MultiProvider(
      providers: [
        ChangeNotifierProvider(create: (_) => AuthRepository()),
      ],
      child: MaterialApp(
        title: 'SIM',
        theme: ThemeData(
          colorScheme: ColorScheme.fromSeed(seedColor: Colors.indigo),
          useMaterial3: true,
        ),
        debugShowCheckedModeBanner: false,
        routes: {
          '/': (_) => const _Root(),
          '/students': (_) => const StudentsScreen(),
        },
        initialRoute: '/',
      ),
    );
  }
}

class _Root extends StatefulWidget {
  const _Root();

  @override
  State<_Root> createState() => _RootState();
}

class _RootState extends State<_Root> {
  @override
  void initState() {
    super.initState();
    context.read<AuthRepository>().loadToken();
  }

  @override
  Widget build(BuildContext context) {
    final auth = context.watch<AuthRepository>();
    if (auth.isLoading) {
      return const Scaffold(body: Center(child: CircularProgressIndicator()));
    }
    return auth.isAuthenticated ? const HomeShell() : const LoginScreen();
  }
}


