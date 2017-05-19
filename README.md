# Activity Tracker

This app will track your walking/moving paths on map. You can save your activity and you can retrieve back the saved entries.

## Getting Started

To run this project, you need Android Studio

### Prerequisites

Before building the project, please enter/update the Google Maps API KEY (Geo API KEY – com.google.android.geo.API_KEY) in AndroidManifest.xml File

<meta-data
android:name="com.google.android.maps.v2.API_KEY"
android:value="Your key"/>


## Running the Application
### Prerequisites:
	For better performance, App needs High Availability of location services and any source of data connection Wi-Fi/4g


1. Once open the application the home screen will show the current location on the map.
2. You can start your tracking by clicking on “Tracking” button from the bottom of the Home Screen
3. Your app starts tracking your activity on map, and map will be updated with the path in your direction
4. Whenever you want stop tracking toggle the “Tracking” button.
5. Save pop-up will open and ask you to enter a intensifier for your track activity.
6. You can retrieve you saved tracks from Menu --> Saved Tracks will show you saved tracks list.
7. Saved Tracks will show the following details.
a. Identifier (Name) 
b. Start Time
c. End Time
8. Select any from the list and it will show the activity track/path on map.


## License

This project is licensed under the MIT License - see the [LICENSE.md](LICENSE.md) file for details


