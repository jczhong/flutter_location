# flutter_location

A flutter location plugin.

## Getting Started

    import 'package:flutter_location/location.dart';
  
    Stream<Map<String, double>> stream = Location().requestLocationUpdates(LocationRequestOption(distance: 100.0));
    //or
    Map<String, double> location = await Location().getLastLocation();


## Notice

Support Android only.
