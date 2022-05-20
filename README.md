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
1. <b> Launcher Screen </b> with <b> available cabs around </b> <br>
PickUp Location is by default set to current location
<img src="https://user-images.githubusercontent.com/77202061/169549658-d35512f8-deae-4132-ae67-b784f535f5e6.jpeg" width="200" height="400" />
2. <b> Google Autocomplete Search Box </b>
<img src="https://user-images.githubusercontent.com/77202061/169550329-bd13b9ee-124c-42a7-97bc-64c6911a9e46.jpeg" width="200" height="400" />
3. <b> Setting Destination point </b>
<p float="left">
  <img src="https://user-images.githubusercontent.com/77202061/169550641-84cf2430-f864-4539-a27e-47bdfe796359.jpeg" width="200" height="400" />
  <img src="https://user-images.githubusercontent.com/77202061/169550943-256867dd-ebdd-4293-aaa1-f6d153918fc9.jpeg" width="200" height="400" />
</p>
4. <b> Requesting Cab, Cab Booked and Cab in Way </b>
<p float="left">
  <img src="https://user-images.githubusercontent.com/77202061/169551429-1deaaff7-d907-4d78-b1d6-d2c417a61934.jpeg" width="200" height="400" />
  <img src="https://user-images.githubusercontent.com/77202061/169551433-d03beaac-89c4-4391-a346-8ffe6c70a885.jpeg" width="200" height="400" />
  <img src="https://user-images.githubusercontent.com/77202061/169551783-c1c445f9-8401-48bc-9665-f3f90805844c.jpeg" width="200" height="400" />
</p>
5. <b> Cab about to arrive </b> and <b> Cab Arrived </b>
<p float="left">
  <img src="https://user-images.githubusercontent.com/77202061/169552510-3fe51489-5961-4a13-9f88-5025e840f7f7.jpeg" width="200" height="400" />
  <img src="https://user-images.githubusercontent.com/77202061/169552488-9207a847-a7ad-4539-b888-fd1bc4c376ab.jpeg" width="200" height="400" />
</p>
6. <b> Trip starts </b> and <b> Cab in way </b>
<p float="left">
  <img src="https://user-images.githubusercontent.com/77202061/169553609-f0cd1638-b144-4ca4-9a95-c7ac2c9c4fbe.jpeg" width="200" height="400" />
  <img src="https://user-images.githubusercontent.com/77202061/169553468-0fe1501f-a0f1-42cf-a7bd-380982910157.jpeg" width="200" height="400" />
</p>
7. <b> Trip ends </b> 
<img src="https://user-images.githubusercontent.com/77202061/169554394-1d5467bb-d25d-4f95-bd50-8245d604b262.jpeg" width="200" height="400" />
8. <b> Take Next Ride </b> - Will reset the screen
<img src="https://user-images.githubusercontent.com/77202061/169554622-f017dcb7-76fd-448e-9912-622a533b23ca.jpeg" width="200" height="400" />

# Hope you had a Comfortable and Smooth Ride !! :)
