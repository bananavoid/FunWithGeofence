# FunWithGeofence


Application helps to see is the device in selected geofence area and connected to selected network.

By default geofence area defined around the current user location with 150m radius. If wifi connection is available,
this ssid will be defined as selected network.

User is welcome
- choose another center of geofence area by long tap on map
- set another network ssid clicking on "Set network name" from action bar menu
- change geofence area with seek bar.


The app was build with MVP arch and help of dagger 2 and rxjava.
Thanks to Dmitriy and his lib https://github.com/RxViper/RxViper for the classes that were used for MVP sugar.

Missed things:

- error handlings
- data retriving on orientation change
- TESTS
