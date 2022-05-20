# EAsyjourney (A Cab Booking App)
This app is built using mock APIs and is similar to UBER app. <br>
The App follows the basic design structure of MVP - (Model View Presenter).
It provides the following functionalities -> <br>
1. <b> PERMISSIONS </b> -> Requires GPS enabled for the app to work efficiently.
2. <b> ACCESS CURRENT LOCATION of USER </b> -> The user is able to see his current location when the app is launched.
3. <b> DISPLAY AVAILABLE CABS AROUND </b> -> All the cabs which are available at that instant to time will be displayed with <b> Car Markers </b>
4. <b> GOOGLE SEARCH AUTOCOMPLETE </b> -> Google Places API with a valid billing account for "Google Maps Platform" enables the app to access any location available on Google Maps.
5. <b> REQUEST CAB </b> -> After chosing required destination, the user can request for an available cab. If the cab gets booked, he will receive a confirmation message for the same.
6. <b> KEEPS THE USER UPDATED for CAB's LOCATION </b> ->  The "Presenter" class requests for cab's location with the help of mock APIs and receive updated cordinates (latitude and longitude) which it sends to the "MapsActivity" class to update the cab marker on Google Maps.
7. <b> ANIMATING CAMERA </b> -> The app will always set it's focus point on the current cordinates (latitude and longitude), by setting the camera position which ensures a smooth and comfortable user experience.
8. <b> UPDATES CAB MARKER </b > using <b> POLYLINE and INTERPOLATOR </b> -> PolyLine creates the path between source point and destination point using interpolator (A method of ValueAnimator) which divides 2 consecutive cordinates into small fractions and then the marker keep on shifting to these small fractions continuously, which provides an animating effect. The marker will rotate depending on the allignment with the polyLine.
9. <b> TRIP BEGINS AFTER PICKING UP </b> the <b> USER </b> -> After successful PickUp, the trip for destination point is initiated, and the user can keep a watch on the cab's current location all along the way.
10. <b> NEXT RIDE </b> -> After successfull completetion of trip, the user can now request for another cab, for his next destination. 

## SCREENSHOTS
