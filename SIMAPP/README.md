# SIMAPP (Flutter Android App)

Flutter Android client for the SIM backend at `http://localhost:8080/api/v1`.

## Prerequisites
- Flutter SDK (stable)
- Android SDK / Android Studio (with an emulator or a physical device)
- Java backend running locally on port 8080

## Setup
1. Open a terminal in this folder:
   - `cd C:\xampp\htdocs\SIM2\SIMAPP`
2. Create missing platform folders (Android) for this Flutter project:
   - `flutter create . --platforms=android`
3. Get dependencies:
   - `flutter pub get`
4. Configure API base URL (if not localhost):
   - Edit `lib/config/config.dart` and change `baseUrl`.
5. Run the app:
   - `flutter run -d android`

## Features
- Login with backend (stores JWT)
- Dashboard calling `/dashboard/stats`
- Basic error handling

## Project structure
- `lib/config/config.dart` — base API config
- `lib/api/api_client.dart` — HTTP client with auth header
- `lib/auth/auth_repository.dart` — login/logout/token storage
- `lib/screens/login_screen.dart` — login UI
- `lib/screens/dashboard_screen.dart` — stats UI
- `lib/models/*` — data models
- `lib/main.dart` — app entry

## Notes
- Ensure the backend CORS allows `http://localhost` (already configured in the project).
- Update credentials to match your backend users.


