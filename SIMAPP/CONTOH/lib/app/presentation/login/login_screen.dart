import 'package:skansapung_presensi/app/presentation/home/home_screen.dart';
import 'package:skansapung_presensi/app/presentation/login/login_notifier.dart';
import 'package:skansapung_presensi/core/helper/global_helper.dart';
import 'package:skansapung_presensi/core/widget/app_widget.dart';
import 'package:flutter/material.dart';
import 'package:flutter/src/widgets/framework.dart';

class LoginScreen extends AppWidget<LoginNotifier, void, void> {
  @override
  void checkVariableAfterUi(BuildContext context) {
    if (notifier.isLoged) {
      Navigator.pushReplacement(
          context,
          MaterialPageRoute(
            builder: (context) => HomeScreen(),
          ));
    }
  }

  @override
  Widget bodyBuild(BuildContext context) {
    return Scaffold(
      backgroundColor: Colors.grey[100],
      body: SafeArea(
        child: SingleChildScrollView(
          child: Padding(
            padding: const EdgeInsets.all(24.0),
            child: Center(
              child: Container(
                constraints: const BoxConstraints(maxWidth: 400),
                decoration: BoxDecoration(
                  color: Colors.white,
                  borderRadius: BorderRadius.circular(20),
                  boxShadow: [
                    BoxShadow(
                      color: Colors.grey.withOpacity(0.1),
                      spreadRadius: 5,
                      blurRadius: 7,
                      offset: const Offset(0, 3),
                    ),
                  ],
                ),
                padding: const EdgeInsets.all(32),
                child: Column(
                  mainAxisSize: MainAxisSize.min,
                  crossAxisAlignment: CrossAxisAlignment.stretch,
                  children: [
                    // Logo
                    Icon(
                      Icons.school,
                      size: 50,
                      color: Theme.of(context).primaryColor,
                    ),
                    const SizedBox(height: 16),
                    // App Name
                    Text(
                      'Absen\nSMKN 1 Punggelan',
                      style: Theme.of(context).textTheme.headlineSmall?.copyWith(
                            fontWeight: FontWeight.bold,
                            color: Colors.black87,
                          ),
                      textAlign: TextAlign.center,
                    ),
                    const SizedBox(height: 24),
                    // Sign in text
                    Text(
                      'Sign in',
                      style: Theme.of(context).textTheme.titleLarge?.copyWith(
                            fontWeight: FontWeight.w500,
                          ),
                    ),
                    const SizedBox(height: 24),
                    // Email Field
                    TextFormField(
                      controller: notifier.emailController,
                      decoration: InputDecoration(
                        hintText: 'Email',
                        filled: true,
                        fillColor: Colors.grey[50],
                        border: OutlineInputBorder(
                          borderRadius: BorderRadius.circular(12),
                          borderSide: BorderSide.none,
                        ),
                        enabledBorder: OutlineInputBorder(
                          borderRadius: BorderRadius.circular(12),
                          borderSide: BorderSide.none,
                        ),
                        focusedBorder: OutlineInputBorder(
                          borderRadius: BorderRadius.circular(12),
                          borderSide: BorderSide(color: Theme.of(context).primaryColor),
                        ),
                      ),
                      keyboardType: TextInputType.emailAddress,
                    ),
                    const SizedBox(height: 16),
                    // Password Field
                    TextFormField(
                      controller: notifier.passwordController,
                      obscureText: !notifier.isShowPassword,
                      decoration: InputDecoration(
                        hintText: 'Password',
                        filled: true,
                        fillColor: Colors.grey[50],
                        border: OutlineInputBorder(
                          borderRadius: BorderRadius.circular(12),
                          borderSide: BorderSide.none,
                        ),
                        enabledBorder: OutlineInputBorder(
                          borderRadius: BorderRadius.circular(12),
                          borderSide: BorderSide.none,
                        ),
                        focusedBorder: OutlineInputBorder(
                          borderRadius: BorderRadius.circular(12),
                          borderSide: BorderSide(color: Theme.of(context).primaryColor),
                        ),
                        suffixIcon: IconButton(
                          icon: Icon(
                            notifier.isShowPassword ? Icons.visibility_off : Icons.visibility,
                            color: Colors.grey,
                          ),
                          onPressed: _showHidePassword,
                        ),
                      ),
                    ),
                    const SizedBox(height: 16),
                    // Remember Me and Forgot Password
                    Row(
                      mainAxisAlignment: MainAxisAlignment.spaceBetween,
                      children: [
                        Row(
                          children: [
                            Switch(
                              value: notifier.rememberMe ?? false,
                              onChanged: (value) {
                                notifier.rememberMe = value;
                                notifier.notifyListeners();
                              },
                              activeColor: Theme.of(context).primaryColor,
                            ),
                            Text('Remember Me',
                                style: Theme.of(context).textTheme.bodyMedium),
                          ],
                        ),
                        TextButton(
                          onPressed: () {
                            // TODO: Implement forgot password
                          },
                          child: Text(
                            'Forgot password?',
                            style: TextStyle(
                              color: Colors.grey[600],
                            ),
                          ),
                        ),
                      ],
                    ),
                    const SizedBox(height: 24),
                    // Sign In Button
                    ElevatedButton(
                      onPressed: () => _onPressLogin(context),
                      style: ElevatedButton.styleFrom(
                        backgroundColor: Theme.of(context).primaryColor,
                        padding: const EdgeInsets.symmetric(vertical: 16),
                        shape: RoundedRectangleBorder(
                          borderRadius: BorderRadius.circular(12),
                        ),
                      ),
                      child: const Text(
                        'SIGN IN',
                        style: TextStyle(
                          fontSize: 16,
                          fontWeight: FontWeight.bold,
                        ),
                      ),
                    ),
                    const SizedBox(height: 8),
                    Text(
                      '*Click SIGN IN for guest login',
                      style: Theme.of(context).textTheme.bodySmall?.copyWith(
                            color: Colors.grey,
                          ),
                      textAlign: TextAlign.center,
                    ),
                    const SizedBox(height: 24),
                    // OR Divider
                    Row(
                      children: [
                        Expanded(child: Divider()),
                        Padding(
                          padding: const EdgeInsets.symmetric(horizontal: 16),
                          child: Text('OR',
                              style: TextStyle(color: Colors.grey[600])),
                        ),
                        Expanded(child: Divider()),
                      ],
                    ),
                    const SizedBox(height: 24),
                    // Social Login Buttons
                    OutlinedButton.icon(
                      icon: Icon(Icons.g_mobiledata, size: 24),
                      label: const Text('Login with Google'),
                      onPressed: () {
                        // TODO: Implement Google login
                      },
                      style: OutlinedButton.styleFrom(
                        padding: const EdgeInsets.symmetric(vertical: 12),
                        shape: RoundedRectangleBorder(
                          borderRadius: BorderRadius.circular(12),
                        ),
                      ),
                    ),
                    const SizedBox(height: 16),
                    OutlinedButton.icon(
                      icon: Icon(Icons.facebook, size: 24),
                      label: const Text('Login with Facebook'),
                      onPressed: () {
                        // TODO: Implement Facebook login
                      },
                      style: OutlinedButton.styleFrom(
                        padding: const EdgeInsets.symmetric(vertical: 12),
                        shape: RoundedRectangleBorder(
                          borderRadius: BorderRadius.circular(12),
                        ),
                      ),
                    ),
                    const SizedBox(height: 24),
                    // Sign Up Link
                    Row(
                      mainAxisAlignment: MainAxisAlignment.center,
                      children: [
                        Text(
                          "Don't have an account? ",
                          style: Theme.of(context).textTheme.bodyMedium,
                        ),
                        TextButton(
                          onPressed: () {
                            // TODO: Implement sign up navigation
                          },
                          child: Text(
                            'Sign Up',
                            style: TextStyle(
                              color: Theme.of(context).primaryColor,
                              fontWeight: FontWeight.bold,
                            ),
                          ),
                        ),
                      ],
                    ),
                  ],
                ),
              ),
            ),
          ),
        ),
      ),
    );
  }

  _showHidePassword() {
    notifier.isShowPassword = !notifier.isShowPassword;
    notifier.notifyListeners();
  }

  _onPressLogin(BuildContext context) {
    notifier.login();
  }
}
