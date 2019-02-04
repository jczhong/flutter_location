import 'dart:async';

import 'package:flutter/services.dart';

const int LOCATION_PRIORITY_BALANCED_POWER_ACCURACY = 0;
const int LOCATION_PRIORITY_HIGH_ACCURACY = 1;
const int LOCATION_PRIORITY_LOW_POWER = 2;
const int LOCATION_PRIORITY_NO_POWER = 3;

class LocationRequestOption {
  final int priority;
  final int interval;
  final double distance;

  LocationRequestOption({this.priority, this.interval, this.distance});

  dynamic _toJSON() {
    final Map<String, dynamic> json = <String, dynamic>{};

    void addIfPresent(String fieldName, dynamic value) {
      if (value != null) {
        json[fieldName] = value;
      }
    }

    addIfPresent("priority", priority);
    addIfPresent("interval", interval);
    addIfPresent("distance", distance);

    return json;
  }
}

class Location {
  static const MethodChannel _channel =
      const MethodChannel('com.z1c2.flutter_location/method');
  static const EventChannel _event =
      const EventChannel('com.z1c2.flutter_location/event');

  Future<Map<String, double>> getLastLocation() =>
      _channel.invokeMethod('getLastLocation').then(
            (result) => result.cast<String, double>(),
            onError: (e) => throw e,
          );

  Stream<Map<String, double>> requestLocationUpdates(
      [LocationRequestOption options]) {
    return _event
        .receiveBroadcastStream(options?._toJSON())
        .map<Map<String, double>>((event) => event.cast<String, double>());
  }
}
