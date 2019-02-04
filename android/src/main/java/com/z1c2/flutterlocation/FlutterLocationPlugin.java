package com.z1c2.flutterlocation;

import android.app.Activity;
import android.location.Location;
import android.os.Looper;
import android.support.annotation.NonNull;
import android.util.Log;

import com.google.android.gms.location.FusedLocationProviderClient;
import com.google.android.gms.location.LocationCallback;
import com.google.android.gms.location.LocationRequest;
import com.google.android.gms.location.LocationResult;
import com.google.android.gms.location.LocationServices;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;

import java.util.HashMap;
import java.util.Map;

import io.flutter.plugin.common.EventChannel;
import io.flutter.plugin.common.EventChannel.EventSink;
import io.flutter.plugin.common.EventChannel.StreamHandler;
import io.flutter.plugin.common.MethodCall;
import io.flutter.plugin.common.MethodChannel;
import io.flutter.plugin.common.MethodChannel.MethodCallHandler;
import io.flutter.plugin.common.MethodChannel.Result;
import io.flutter.plugin.common.PluginRegistry.Registrar;

public class FlutterLocationPlugin implements MethodCallHandler, StreamHandler {
  private final static String TAG = "FlutterLocationPlugin";

  private final static String METHOD_CHANNEL = "com.z1c2.flutter_location/method";
  private final static String EVENT_CHANNEL ="com.z1c2.flutter_location/event";

  private final static int[] PRIORITY = {
          LocationRequest.PRIORITY_BALANCED_POWER_ACCURACY,
          LocationRequest.PRIORITY_HIGH_ACCURACY,
          LocationRequest.PRIORITY_LOW_POWER,
          LocationRequest.PRIORITY_NO_POWER
  };

  private EventSink mEvent = null;
  private FusedLocationProviderClient mFusedLocationClient;
  private LocationRequest mLocationRequest = null;

  private Location mPreviousLocation = null;
  private double mDistance = 0.0;

  private final LocationCallback mLocationCallback = new LocationCallback() {
    @Override
    public void onLocationResult(LocationResult locationResult) {
      super.onLocationResult(locationResult);

      Location location = locationResult.getLastLocation();
      if (mDistance != 0.0 && mPreviousLocation != null) {
        float distance = location.distanceTo(mPreviousLocation);
        if (distance < mDistance) {
          return;
        }
      }

      HashMap<String, Double> ret = new HashMap<>();
      ret.put("latitude", location.getLatitude());
      ret.put("longitude", location.getLongitude());
      if (mEvent != null) {
        mEvent.success(ret);
      }

      mPreviousLocation = location;
    }
  };

  private FlutterLocationPlugin(Activity activity) {
    mFusedLocationClient = LocationServices.getFusedLocationProviderClient(activity);
  }

  public static void registerWith(Registrar registrar) {
    FlutterLocationPlugin plugin = new FlutterLocationPlugin(registrar.activity());
    final MethodChannel methodChannel = new MethodChannel(registrar.messenger(), METHOD_CHANNEL);
    methodChannel.setMethodCallHandler(plugin);

    final EventChannel eventChannel = new EventChannel(registrar.messenger(), EVENT_CHANNEL);
    eventChannel.setStreamHandler(plugin);
  }

  @Override
  public void onMethodCall(MethodCall call, final Result result) {
    switch (call.method) {
      case "getLastLocation":
        mFusedLocationClient.getLastLocation().addOnSuccessListener(new OnSuccessListener<Location>() {
          @Override
          public void onSuccess(Location location) {
            if (location != null) {
              HashMap<String, Double> ret = new HashMap<>();
              ret.put("latitude", location.getLatitude());
              ret.put("longitude", location.getLongitude());
              result.success(ret);
            } else {
              result.error("ERROR", "Failed to get location.", null);
            }
          }
        }).addOnFailureListener(new OnFailureListener() {
          @Override
          public void onFailure(@NonNull Exception e) {
            Log.e(TAG, e.getMessage());
            result.error("ERROR", "No permission.", null);
          }
        });
        break;
      default:
        result.notImplemented();
    }
  }

  @Override
  public void onListen(Object arguments, EventSink eventSink) {
    if (arguments != null) {
      mLocationRequest = getLocationRequest(arguments);
    }
    mEvent = eventSink;
    LocationRequest request = mLocationRequest != null ? mLocationRequest : getDefaultLocationRequest();
    mFusedLocationClient.requestLocationUpdates(request, mLocationCallback, Looper.myLooper());
  }

  @Override
  public void onCancel(Object arguments) {
    mFusedLocationClient.removeLocationUpdates(mLocationCallback);
  }

  private LocationRequest getLocationRequest(Object argument) {
    final Map<?, ?> data = (Map<?, ?>) argument;
    LocationRequest request = LocationRequest.create();
    if (data.get("priority") != null) {
      request.setPriority(PRIORITY[ toInt(data.get("priority")) ]);
    }
    if (data.get("interval") != null) {
      request.setInterval(toLong(data.get("interval")));
    }
    if (data.get("distance") != null) {
      mDistance = toDouble(data.get("distance"));
    }

    return request;
  }

  private LocationRequest getDefaultLocationRequest() {
    return LocationRequest.create();
  }

  private static int toInt(Object o) {
    return ((Number) o).intValue();
  }

  private static long toLong(Object o) {
    return ((Number) o).longValue();
  }

  private static double toDouble(Object o) {
    return ((Number) o).doubleValue();
  }
}
