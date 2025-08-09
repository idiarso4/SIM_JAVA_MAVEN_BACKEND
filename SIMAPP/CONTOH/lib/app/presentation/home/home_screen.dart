import 'dart:io';

import 'package:skansapung_presensi/app/module/entity/attendance.dart';
import 'package:skansapung_presensi/app/presentation/detail_attendance/detail_attendance_screen.dart';
import 'package:skansapung_presensi/app/presentation/face_recognition/face_recognition_screen.dart';
import 'package:skansapung_presensi/app/presentation/home/home_notifier.dart';
import 'package:skansapung_presensi/app/presentation/login/login_screen.dart';
import 'package:skansapung_presensi/app/presentation/map/map_screen.dart';
import 'package:skansapung_presensi/core/helper/date_time_helper.dart';
import 'package:skansapung_presensi/core/helper/dialog_helper.dart';
import 'package:skansapung_presensi/core/helper/global_helper.dart';
import 'package:skansapung_presensi/core/helper/shared_preferences_helper.dart';
import 'package:skansapung_presensi/core/widget/app_widget.dart';
import 'package:flutter/material.dart';
import 'package:flutter/src/widgets/framework.dart';
import 'package:flutter/widgets.dart';
import 'package:retrofit/http.dart';

class HomeScreen extends AppWidget<HomeNotifier, void, void> {
  @override
  Widget bodyBuild(BuildContext context) {
    return SafeArea(
      child: RefreshIndicator(
        onRefresh: () => notifier.init(),
        child: SingleChildScrollView(
          child: Column(
            children: [
              _headerLayout(context),
              _todayLayout(context),
              _thisMonthLayout(context)
            ],
          ),
        ),
      ),
    );
  }

  _headerLayout(BuildContext context) {
    return Container(
      padding: EdgeInsets.fromLTRB(20, 30, 20, 20),
      decoration: BoxDecoration(
        color: Colors.white,
        borderRadius: BorderRadius.only(
          bottomLeft: Radius.circular(20),
          bottomRight: Radius.circular(20),
        ),
        boxShadow: [
          BoxShadow(
            color: Colors.grey.withOpacity(0.1),
            spreadRadius: 1,
            blurRadius: 5,
            offset: Offset(0, 3),
          ),
        ],
      ),
      child: Row(
        children: [
          CircleAvatar(
            child: Icon(
              Icons.person,
              size: 40,
              color: Colors.white,
            ),
            backgroundColor: Color(0xFF4A90E2),
            radius: 30,
          ),
          SizedBox(
            width: 15,
          ),
          Expanded(
            child: Column(
              crossAxisAlignment: CrossAxisAlignment.start,
              children: [
                Text(
                  notifier.name,
                  style: TextStyle(
                    fontSize: 20,
                    fontWeight: FontWeight.bold,
                    color: Colors.black87,
                  ),
                ),
                SizedBox(
                  height: 5,
                ),
                (notifier.isLeaves)
                    ? SizedBox()
                    : Row(
                        children: [
                          Expanded(
                              child: Row(
                            children: [
                              Icon(Icons.location_city, 
                                   color: Colors.grey[600],
                                   size: 20),
                              SizedBox(width: 5),
                              Text(
                                notifier.schedule?.office.name ?? '',
                                style: TextStyle(
                                  color: Colors.grey[600],
                                  fontSize: 14,
                                ),
                              ),
                            ],
                          )),
                          Expanded(
                              child: Row(
                            children: [
                              Icon(Icons.access_time,
                                   color: Colors.grey[600],
                                   size: 20),
                              SizedBox(width: 5),
                              Text(
                                notifier.schedule?.shift.name ?? '',
                                style: TextStyle(
                                  color: Colors.grey[600],
                                  fontSize: 14,
                                ),
                              )
                            ],
                          ))
                        ],
                      )
              ],
            ),
          ),
          SizedBox(width: 10),
          IconButton(
              onPressed: () => _onPressEditNotification(context),
              icon: Icon(Icons.edit_notifications, color: Colors.grey[600])),
          IconButton(
              onPressed: () => _onPressLogout(context),
              icon: Icon(Icons.logout, color: Colors.grey[600]))
        ],
      ),
    );
  }

  _todayLayout(BuildContext context) {
    return Container(
      margin: EdgeInsets.fromLTRB(20, 10, 20, 10),
      padding: EdgeInsets.all(20),
      decoration: BoxDecoration(
        borderRadius: BorderRadius.circular(20),
        gradient: LinearGradient(
          begin: Alignment.topLeft,
          end: Alignment.bottomRight,
          colors: [
            Color(0xFF4A90E2),
            Color(0xFF357ABD),
          ],
        ),
        boxShadow: [
          BoxShadow(
            color: Color(0xFF4A90E2).withOpacity(0.3),
            spreadRadius: 1,
            blurRadius: 10,
            offset: Offset(0, 4),
          ),
        ],
      ),
      child: Column(
        children: [
          Row(
            children: [
              Container(
                padding: EdgeInsets.symmetric(horizontal: 12, vertical: 8),
                decoration: BoxDecoration(
                  borderRadius: BorderRadius.circular(10),
                  color: Colors.white.withOpacity(0.9),
                ),
                child: Row(
                  children: [
                    Icon(Icons.today, color: Color(0xFF4A90E2), size: 20),
                    SizedBox(width: 8),
                    Text(
                      DateTimeHelper.formatDateTime(
                        dateTime: DateTime.now(),
                        format: 'EEE, dd MMM yyyy'
                      ),
                      style: TextStyle(
                        color: Colors.black87,
                        fontWeight: FontWeight.w500,
                      ),
                    ),
                  ],
                ),
              ),
              Expanded(child: SizedBox()),
              (notifier.isLeaves)
                  ? SizedBox()
                  : Container(
                      padding: EdgeInsets.symmetric(horizontal: 12, vertical: 8),
                      decoration: BoxDecoration(
                        borderRadius: BorderRadius.circular(10),
                        color: Colors.white.withOpacity(0.9),
                      ),
                      child: Text(
                        (notifier.schedule?.isWfa ?? false) ? 'WFA' : 'WFO',
                        style: TextStyle(
                          color: Color(0xFF4A90E2),
                          fontWeight: FontWeight.w600,
                        ),
                      ))
            ],
          ),
          SizedBox(height: 20),
          Row(
            children: [
              _timeTodayLayout(context, 'Datang',
                  notifier.attendanceToday?.startTime ?? '-'),
              _timeTodayLayout(
                  context, 'Pulang', notifier.attendanceToday?.endTime ?? '-')
            ],
          ),
          SizedBox(height: 20),
          (notifier.isLeaves)
              ? Text(
                  'Anda hari ini sedang cuti',
                  style: TextStyle(
                    fontSize: 18,
                    fontWeight: FontWeight.bold,
                    color: Colors.white,
                  ),
                )
              : Container(
                  width: double.maxFinite,
                  child: ElevatedButton(
                    onPressed: () => _onPressCreateAttendance(context),
                    child: Text(
                      'Buat Kehadiran',
                      style: TextStyle(
                        fontSize: 16,
                        fontWeight: FontWeight.w600,
                      ),
                    ),
                    style: ElevatedButton.styleFrom(
                      backgroundColor: Colors.white,
                      foregroundColor: Color(0xFF4A90E2),
                      padding: EdgeInsets.symmetric(vertical: 12),
                      shape: RoundedRectangleBorder(
                        borderRadius: BorderRadius.circular(10),
                      ),
                      elevation: 0,
                    ),
                  ),
                )
        ],
      ),
    );
  }

  _thisMonthLayout(BuildContext context) {
    return Container(
      constraints: BoxConstraints(
          minHeight: MediaQuery.of(context).size.height - kToolbarHeight),
      width: double.maxFinite,
      margin: EdgeInsets.only(top: 10),
      padding: EdgeInsets.all(20),
      decoration: BoxDecoration(
        borderRadius: BorderRadius.vertical(top: Radius.circular(20)),
        color: Colors.white,
        boxShadow: [
          BoxShadow(
            color: Colors.grey.withOpacity(0.1),
            spreadRadius: 1,
            blurRadius: 5,
            offset: Offset(0, -3),
          ),
        ],
      ),
      child: Column(
        crossAxisAlignment: CrossAxisAlignment.start,
        children: [
          Row(
            children: [
              Expanded(
                child: Text(
                  'Presensi Sebulan Terakhir',
                  style: TextStyle(
                    fontSize: 18,
                    fontWeight: FontWeight.bold,
                    color: Colors.black87,
                  ),
                ),
              ),
              ElevatedButton(
                  onPressed: () => _onPressSeeAll(context),
                  child: Text('Lihat Semua'),
                  style: ElevatedButton.styleFrom(
                    backgroundColor: Colors.white,
                    foregroundColor: Color(0xFF4A90E2),
                    padding: EdgeInsets.symmetric(vertical: 12),
                    shape: RoundedRectangleBorder(
                      borderRadius: BorderRadius.circular(10),
                    ),
                    elevation: 0,
                  ))
            ],
          ),
          SizedBox(height: 5),
          Container(
            height: 1,
            color: Colors.grey[300],
          ),
          SizedBox(height: 2),
          Row(
            children: [
              Expanded(
                  flex: 1,
                  child: Center(
                    child: Text(
                      'Tgl',
                      style: TextStyle(
                        fontSize: 14,
                        fontWeight: FontWeight.w500,
                        color: Colors.grey[600],
                      ),
                    ),
                  )),
              Expanded(
                  flex: 2,
                  child: Center(
                      child: Text(
                    'Datang',
                    style: TextStyle(
                      fontSize: 14,
                      fontWeight: FontWeight.w500,
                      color: Colors.grey[600],
                    ),
                  ))),
              Expanded(
                  flex: 2,
                  child: Center(
                      child: Text('Pulang',
                          style: TextStyle(
                            fontSize: 14,
                            fontWeight: FontWeight.w500,
                            color: Colors.grey[600],
                          ))))
            ],
          ),
          SizedBox(height: 2),
          Container(
            height: 2,
            color: Colors.grey[300],
          ),
          ListView.separated(
            physics: NeverScrollableScrollPhysics(),
            shrinkWrap: true,
            separatorBuilder: (context, index) => Container(
              margin: EdgeInsets.symmetric(vertical: 2),
              height: 1,
              color: Colors.grey[300],
            ),
            itemCount: notifier.listAttendanceThisMonth.length,
            itemBuilder: (context, index) {
              final item = notifier.listAttendanceThisMonth[
                  notifier.listAttendanceThisMonth.length - index - 1];
              return _itemThisMonth(context, item);
            },
          )
        ],
      ),
    );
  }

  _timeTodayLayout(BuildContext context, String label, String time) {
    return Expanded(
        child: Column(
      children: [
        Text(
          time,
          style: TextStyle(
            fontSize: 18,
            fontWeight: FontWeight.bold,
            color: Colors.white,
          ),
        ),
        Text(
          label,
          style: TextStyle(
            fontSize: 14,
            color: Colors.white,
          ),
        )
      ],
    ));
  }

  _itemThisMonth(BuildContext context, AttendanceEntity item) {
    return Container(
      margin: EdgeInsets.symmetric(vertical: 3),
      child: Row(
        children: [
          Expanded(
              flex: 1,
              child: Container(
                  padding: EdgeInsets.symmetric(vertical: 2, horizontal: 5),
                  decoration: BoxDecoration(
                      borderRadius: BorderRadius.circular(5),
                      color: Color(0xFF4A90E2)),
                  child: Text(
                    DateTimeHelper.formatDateTimeFromString(
                        dateTimeString: item.date!, formar: 'dd\nMMM'),
                    style: TextStyle(
                      fontSize: 14,
                      fontWeight: FontWeight.w500,
                      color: Colors.white,
                    ),
                    textAlign: TextAlign.center,
                  ))),
          Expanded(
              flex: 2,
              child: Center(
                  child: Text(
                item.startTime,
                style: TextStyle(
                  fontSize: 14,
                  color: Colors.black87,
                ),
              ))),
          Expanded(
              flex: 2,
              child: Center(
                  child: Text(
                item.endTime,
                style: TextStyle(
                  fontSize: 14,
                  color: Colors.black87,
                ),
              )))
        ],
      ),
    );
  }

  _onPressCreateAttendance(BuildContext context) async {
    await Navigator.push(
        context,
        MaterialPageRoute(
          builder: (context) => FaceRecognitionScreen(),
        ));
    notifier.init();
  }

  _onPressEditNotification(BuildContext context) async {
    DialogHelper.showBottomDialog(
        context: context,
        title: "Edit waktu notifikasi",
        content: DropdownMenu<int>(
            initialSelection: notifier.timeNotification,
            onSelected: (value) => _onSaveEditNotification(context, value!),
            dropdownMenuEntries: notifier.listEditNotification));
  }

  _onPressLogout(BuildContext context) async {
    await SharedPreferencesHelper.logout();
    Navigator.pushAndRemoveUntil(
      context,
      MaterialPageRoute(
        builder: (context) => LoginScreen(),
      ),
      (route) => false,
    );
  }

  _onPressSeeAll(BuildContext context) {
    Navigator.push(
        context,
        MaterialPageRoute(
          builder: (context) => DetailAttendanceScreen(),
        ));
  }

  _onSaveEditNotification(BuildContext context, int param) {
    Navigator.pop(context);
    notifier.saveNotificationSetting(param);
  }
}
