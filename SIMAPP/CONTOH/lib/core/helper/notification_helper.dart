import 'dart:io';

import 'package:flutter_local_notifications/flutter_local_notifications.dart';
import 'package:timezone/timezone.dart';
import 'package:timezone/data/latest.dart';
import 'package:flutter_timezone/flutter_timezone.dart';

class NotificationHelper {
  static FlutterLocalNotificationsPlugin flutterLocalNotificationsPlugin =
      FlutterLocalNotificationsPlugin();

  static Future<void> configurateLocalTimeZone() async {
    initializeTimeZones();
    final String currentTimeZone = await FlutterTimezone.getLocalTimezone();
    setLocalLocation(getLocation(currentTimeZone));
  }

  static initNotification() async {
    await configurateLocalTimeZone();

    AndroidInitializationSettings androidSetting =
        AndroidInitializationSettings('@mipmap/ic_launcher');

    DarwinInitializationSettings iosSetting = DarwinInitializationSettings(
        requestSoundPermission: true,
        requestBadgePermission: true,
        requestAlertPermission: true);
    InitializationSettings setting =
        InitializationSettings(android: androidSetting, iOS: iosSetting);
    await flutterLocalNotificationsPlugin.initialize(setting);
  }

  static TZDateTime convertTime(
      int year, int month, int day, int hour, int minutes) {
    return TZDateTime(local, year, month, day, hour, minutes);
  }

  static scheduleNotification(
      {required int id,
      required String title,
      required String body,
      required int hour,
      required int minutes}) async {
    final now = DateTime.now();
    await flutterLocalNotificationsPlugin.zonedSchedule(
        id,
        title,
        body,
        convertTime(now.year, now.month, now.day, hour, minutes),
        NotificationDetails(
            android: AndroidNotificationDetails('1', 'skansapung_presensi',
                importance: Importance.max, priority: Priority.high),
            iOS: DarwinNotificationDetails()),
        uiLocalNotificationDateInterpretation:
            UILocalNotificationDateInterpretation.absoluteTime,
        matchDateTimeComponents: DateTimeComponents.time);
  }

  static cancelAll() async {
    await flutterLocalNotificationsPlugin.cancelAll();
  }

  static Future<bool> requestPermission() async {
    bool isGranted = false;
    if (Platform.isIOS) {
      isGranted = await flutterLocalNotificationsPlugin
              .resolvePlatformSpecificImplementation<
                  IOSFlutterLocalNotificationsPlugin>()
              ?.requestPermissions(alert: true, sound: true, badge: true) ??
          false;
    } else if (Platform.isAndroid) {
      final AndroidFlutterLocalNotificationsPlugin?
          androidFlutterLocalNotificationsPlugin =
          flutterLocalNotificationsPlugin.resolvePlatformSpecificImplementation<
              AndroidFlutterLocalNotificationsPlugin>();

      isGranted = await androidFlutterLocalNotificationsPlugin
              ?.requestNotificationsPermission() ??
          false;
    }

    return isGranted;
  }

  static Future<bool> isPermissionGranted() async {
    bool isGranted = false;
    if (Platform.isIOS) {
      final permission = await flutterLocalNotificationsPlugin
          .resolvePlatformSpecificImplementation<
              IOSFlutterLocalNotificationsPlugin>()
          ?.checkPermissions();

      isGranted = permission?.isEnabled ?? false;
    } else if (Platform.isAndroid) {
      isGranted = await flutterLocalNotificationsPlugin
              .resolvePlatformSpecificImplementation<
                  AndroidFlutterLocalNotificationsPlugin>()
              ?.areNotificationsEnabled() ??
          false;
    }

    return isGranted;
  }
}
