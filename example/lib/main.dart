import 'package:flutter/material.dart';
import 'dart:async';

import 'package:flutter_location/location.dart';

void main() => runApp(MyApp());

class MyApp extends StatefulWidget {
  @override
  _MyAppState createState() => _MyAppState();
}

class _MyAppState extends State<MyApp> {
  double _latitude;
  double _longitude;
  Stream<Map<String, double>> _stream;
  StreamSubscription<Map<String, double>> _subscription;

  @override
  void initState() {
    super.initState();
    initLocation();
  }



  Future<void> initLocation() async {
//   Map<String, double> result = await Location().getLastLocation();
//    _latitude = result["latitude"];
//    _longitude = result["longitude"];

    _stream = Location().requestLocationUpdates(LocationRequestOption(priority: LOCATION_PRIORITY_BALANCED_POWER_ACCURACY, interval: 10000, distance: 100.0));
    _subscription = _stream.listen((Map<String, double> location) {
      _latitude = location["latitude"];
      _longitude = location["longitude"];
    },
    onError: (error) {
      print(error);
    });
  }

  _onPressed() {
    if (_subscription != null) {
      _subscription.cancel();
      _subscription = null;
    } else {
      _subscription = _stream.listen((Map<String, double> location) {
        _latitude = location["latitude"];
        _longitude = location["longitude"];
      },
          onError: (error) {
            print(error);
      });
    }
      //todo ?
//    if(_subscription?.isPaused ?? false) {
//      _subscription.resume();
//    } else {
//      _subscription.pause();
//    }
  }

  @override
  Widget build(BuildContext context) {
    return MaterialApp(
      home: Scaffold(
        appBar: AppBar(
          title: const Text('Plugin example app'),
        ),
        body: Center(
          child: Text('latitude: $_latitude, longitude: $_longitude'),
        ),
        floatingActionButton: FloatingActionButton(onPressed: _onPressed),
      ),
    );
  }
}
