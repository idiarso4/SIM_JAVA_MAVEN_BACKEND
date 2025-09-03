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
      name: json['namaLengkap'] ?? json['name'] ?? '',
      email: json['user']?['email'] ?? json['email'] ?? '',
      nim: json['nis'] ?? json['nim'] ?? '',
      major: json['classRoom']?['major']?['name'] ?? json['major'] ?? '',
      faculty: json['classRoom']?['major']?['department']?['name'] ?? json['faculty'] ?? '',
      phone: json['noHpOrtu'] ?? json['phone'],
      address: json['alamat'] ?? json['address'],
      classRoom: json['classRoom']?['name'] ?? json['classRoom'],
      status: json['status']?.toString() ?? 'ACTIVE',
    );
  }

  Map<String, dynamic> toJson() {
    return {
      'id': id,
      'namaLengkap': name,
      'nis': nim,
      'alamat': address,
      'noHpOrtu': phone,
      'status': status,
    };
  }
}