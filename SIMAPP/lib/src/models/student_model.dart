class Student {
  final int id;
  final String name;
  final String email;
  final String nim;
  final String major;
  final String faculty;
  final String? phone;
  final String? address;
  final String? classRoom;
  final String? status;

  Student({
    required this.id,
    required this.name,
    required this.email,
    required this.nim,
    required this.major,
    required this.faculty,
    this.phone,
    this.address,
    this.classRoom,
    this.status,
  });

  factory Student.fromJson(Map<String, dynamic> json) {
    return Student(
      id: json['id'] ?? 0,
      name: json['name'] ?? '',
      email: json['email'] ?? '',
      nim: json['nim'] ?? '',
      major: json['major'] ?? '',
      faculty: json['faculty'] ?? '',
      phone: json['phone'],
      address: json['address'],
      classRoom: json['classRoom'],
      status: json['status'],
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
      'phone': phone,
      'address': address,
      'classRoom': classRoom,
      'status': status,
    };
  }
}