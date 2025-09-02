class Student {
  final int id;
  final String name;
  final String email;
  final String nim;
  final String major;
  final String faculty;

  Student({
    required this.id,
    required this.name,
    required this.email,
    required this.nim,
    required this.major,
    required this.faculty,
  });

  factory Student.fromJson(Map<String, dynamic> json) {
    return Student(
      id: json['id'] ?? 0,
      name: json['name'] ?? '',
      email: json['email'] ?? '',
      nim: json['nim'] ?? '',
      major: json['major'] ?? '',
      faculty: json['faculty'] ?? '',
    );
  }

  Map<String, dynamic> toJson() {
    return {
      'id': id,
      'name': name,
      'email': email,
      'nim': nim,
      'major': major,
      'faculty': faculty,
    };
  }
}