# SIM App - Flutter Client

A Flutter-based mobile application for the School Information Management System (SIM).

## Features

- Student Management (CRUD operations)
- Authentication (Login/Logout)
- Dashboard with navigation to different modules
- Attendance tracking (coming soon)
- Face recognition (coming soon)
- School map (coming soon)

## Setup Instructions

1. Make sure the backend server is running on `http://localhost:8080`
2. For Android emulator, the app will connect to `http://10.0.2.2:8080` (host PC)
3. Install dependencies:
   ```
   flutter pub get
   ```
4. Run the app:
   ```
   flutter run
   ```

## Dependencies

- `http` - For API communication
- `flutter_secure_storage` - For secure token storage
- `provider` - For state management

## Architecture

The app follows a clean architecture pattern with the following directories:

- `/src/api` - API client configuration
- `/src/auth` - Authentication logic
- `/src/config` - Configuration files
- `/src/models` - Data models
- `/src/screens` - UI screens
- `/src/services` - Business logic and API services
- `/src/utils` - Utility functions
- `/src/widgets` - Custom widgets

## API Integration

The app integrates with the SIM backend API:

- Authentication endpoint: `/api/v1/auth/login`
- Students endpoint: `/api/v1/students`

## Screenshots

![Login Screen](screenshots/login.png)
![Dashboard](screenshots/dashboard.png)
![Students List](screenshots/students.png)
![Add Student](screenshots/add_student.png)

## Development

To contribute to this project:

1. Fork the repository
2. Create a feature branch
3. Make your changes
4. Submit a pull request

## License

This project is licensed under the MIT License.