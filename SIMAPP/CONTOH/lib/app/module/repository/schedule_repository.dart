import 'package:skansapung_presensi/app/module/entity/schedule.dart';
import 'package:skansapung_presensi/core/network/data_state.dart';

abstract class ScheduleRepository {
  Future<DataState<ScheduleEntity?>> get();
  Future<DataState> banned();
}
