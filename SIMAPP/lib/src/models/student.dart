class Student {
  final String id;
  final String name;
  final String? className;
  final String? email;
  final String? phone;
  final String? status;

  Student({
    required this.id,
    required this.name,
    this.className,
    this.email,
    this.phone,
    this.status,
  });

  factory Student.fromBackend(Map<String, dynamic> json) {
    return Student(
      id: (json['nis'] ?? json['id']).toString(),
      name: (json['namaLengkap'] ?? json['name'] ?? '').toString(),
      className: json['classRoom'] != null ? json['classRoom']['name']?.toString() : null,
      email: json['user'] != null ? json['user']['email']?.toString() : null,
      phone: json['noHpOrtu']?.toString(),
      status: json['status']?.toString(),
    );
  }
}


