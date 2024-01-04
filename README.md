# ConnectToIoT
An implementation of Android file transfer protocol over ftp and transfer to Cloud. I have used personal Hotspot here for connecting to the IoT devices and usual http protocol for upload to the destination cloud servers
# Automated_Data_Collector_for_IoT_devices_v3.1.0

## Description
This application works as a bridge for data transfer between the IoT devices fitted in the fleets and the destination cloud servers (viz. Optimine and Azure’s IoTHub). 
It is responsible for collecting files at regular interval, store it momentarily till it is uploaded successfully at the serverside.

[Installation](#installation)
[Generating Signed APK](#generating signed apk)
[Usage](#usage)
[Features](#features)
[Permissions](#permissions)
[Dependencies](#dependencies)
[Upgrading dependencies](#upgrading dependencies)
[Gradle Info](#gradle info)
[Technical Intricacies](#technical intricacies)
[Activity Walkthrough](#activity walkthrough)
[Foreground Services](#foreground services)
[Android Lifecycle Management](#android lifecycle management)
[Maintainers](#maintainers)
[Contributing](#contributing)
[License](#license)

## Installation
Clone this repository and import into Android Studio

git clone http://fitux88.fi.sandvik.com:8081/products/datamule.git

## Generating Signed APK
From Android Studio:

Build menu
Generate Signed APK...
Fill in the keystore information (you only need to do this once manually and then let Android Studio remember it)

## Usage

### Getting Started
Define the Java environment variable path in your local. Make sure it is configured same in your android studio path as well. Select Configuration [app] and run on an emulator having api level 31+.
Upon successful gradle build and installation, you will be taken to Battery Optimization page. Select the app from the list of optimised app and turn battery optimization Off.
Next, You will be taken to the write settings page, where you need to allow write settings for the app.

### App Usage
Upon successfully granting the runtime permissions, you will be taken to the main screen. Check if the personal hotspot has turned on successfully. The ftp server would start automatically following that. If not, go to the settings screen in the app and look for the 'data transfer mode' option. You can switch between 'Equipment only', 'server Only' and 'Equipment and Server mode'. To enable autoswitching between equipment and server modes, meaning to switch between personal hotspot and Wifi Client by reading the files received from machines..you need to choose 'equipment and server mode' 
You can also switch on the ftp toggle to manually turn on/off the ftp server.
Once the server is up and running, You receive a notification. 

### Connectivity with machines
Make sure the personal hotspot credentials are same as configured in the machines we are targeting to connect with.
Usually the factory setting is: ssid- 'Sandvik' and Password- '12345678'

After you have made the changes, you should see the list of connected machines in the main screen under the heading 'Connected Devices'.
To check for the files received from these connected machines, you can click on the 'log' button on the main screen and check for files.
To view the downloaded files in file explorer, you have an option in the side bar menu as 'mule logs'. Clicking on that takes you to the location for all downloaded files.

### Upload files to server
File upload to server needs one time entering of the iothub url in the 'Server Url' option in app's setting page.
You can also choose between optimine or IoTHub Servers from the settings menu.

# Features
 1. Autoswitching between hotspot and wifi client based on the volume of received files and established connection with the machines.
 2. Choose between Equipment and server mode to specifically direct the app to undergo one function at a time
 3. Easy access to mule logs from the app screen
 4. Self debugging capability of the app by creating descriptive logs about the app' usage.
 5. Status bar at the top of main screen to indicate the running state of essential services and health of the the mobile device, enabling users to take appropriate actions and avoid data loss
 6. Battery level indicator within app screen for user to decide whether to take the device underground or not.
 7. FTP Toggle indicator at the bottom of settings screen to inform users of the underlying events occuring at runtime while switching the ftp toggle.
 8. Pending files count lebel on the main screen to give an idea on the volume of received files.

# Permissions

## Install-time Permissions
1. Access coarse Location
2. Access fine Location
3. Change Network State
4. Change Wifi State
5. Access Wifi State
6. Internet
7. Post Notification
8. Read External Storage
9. Write External Storage
10. Access Network State
11. Wake Lock
12. Over ride wifi configuration
13. Manage External Storage
14. Foreground Services

## Runtime Permissions
1. Write Settings
2. Request Ignore battery optimization
3. Turn on Wifi (only for package that involves wifiDirect) 

# Dependencies
External Library Dependencies: 
ftplet-api-1.1.1 : API related to FTP (File Transfer Protocol) in Java. It might define interfaces or classes that  developers can use to create custom FTP server implementations or plugins.
ftpserver-core-1.1.1 :  This is a core component of an FTP server library in Java. It provides the essential functionality for creating and managing FTP servers. Developers can use it to build custom FTP server applications.
log4j-2.21.1 : Log4j is a widely used logging framework for Java. Version 2.21.1 is a specific release that allows developers to incorporate logging capabilities into their Java applications, making it easier to monitor and troubleshoot their software.
mina-core-2.0.16 : MINA (Multipurpose Infrastructure for Network Applications) is a network application framework in Java. The core library, version 2.0.16, offers tools and components for developing network-based applications, including support for building both client and server applications.
slf4j-api-2.0.3 : This is the core SLF4J API, version 2.0.3. It defines the common logging interfaces used by SLF4J and allows developers to bind to various logging implementations (such as Log4j, Logback, or Java's built-in logging) at runtime.
dexmaker-2.28.3 : Dexmaker is a library used for generating .dex files on Android. These files contain executable code for Android applications. Developers often use Dexmaker to create dynamic proxies and mock objects for unit testing on the Android platform.
azure-storage-android:0.7.0@aar : This is the Azure Storage SDK for Android, version 0.7.0. It enables Android developers to interact with Azure cloud storage services, including Azure Blob Storage, Azure Table Storage, and Azure Queue Storage, for storing and retrieving data in the cloud.
apache-httpmime:4.5.6 : This library is part of the Apache HttpClient suite and is specifically focused on handling MIME (Multipurpose Internet Mail Extensions) entities in HTTP requests and responses. It's often used for working with multipart/form-data and handling file uploads in HTTP communication.
commons-net:3.3 : Commons Net is a library that offers a collection of network protocol implementations in Java. Version 3.3 provides support for various network protocols, including FTP, SMTP, and more. It simplifies network communication tasks.

# Upgrading Dependencies
An useful addition for upgrading dependencies on the go (for non-android developers):
Keeping up with the latest dependencies is always a task for ensuring smooth functioning of application, and for such a project where we use multiple third party libraries, it is quite some task to go back and forth, searching for appropriate library updates and reconfiguring accordingly in code where ever required.
To make this process a bit more easier and friendly even for someone who is not aware of android development in specific, but is familiar with terminal commands, we have made a functionality for the developers and backend team.
The illustration for above functionality is as below:

We have used the Gradle dependecyUpdates plugin in the project level build.gradle file. You can look for the buildscript section:
     


 buildscript {
    repositories {
        jcenter()
        google()
    }
    dependencies {
        classpath "com.github.ben-manes:gradle-versions-plugin:0.39.0"
    }
}


Then, in the app level build.gradle file, check the top of the file for something like this:
   apply plugin: 'com.github.ben-manes.versions'

How to use this functionality?

Run the following Gradle task from the command line or Android Studio’s terminal to check for updates:
  ./gradlew dependencyUpdates
This will list all the dependencies in our project along with the latest available versions. You can then decide which dependencies you want to update.
To update a specific dependency, go back to the build.gradle file of your module and change the version to the latest version.
After making the changes, sync your project in Android Studio to apply the updates.
You may need to check project’s compatibility with the new versions, as updates may introduce breaking changes.
Finally, test the project thoroughly to ensure that the updates dependencies haven’t introduced any new issues.


# Gradle Info
Gradle version: v7.6.2
Configurations chosen for build: minimum sdk- 29, Target sdk – 33

# Technical Intricacies
Auto-switching between mobile hotspot and Wifi Client by smart sensing folders is the catch of this application, which ensures smooth data movements even when the app interface is not interacted with. However, user interactions are always considered in the foreground whenever the app is opened/ used manually. 
Mobile Hotspot ip in android is no more hard coded as it used to be in the previous AOSPs, but there is a way to get a static ip (without flashing ROM!). This is under test(re f. SandvikDataBearer-test ver2.0.1_t2).
For ensuring elongated functioning of the app and not getting killed by the Android Lifecycle calls, we run the FDM and FTP as background service and not in the foreground. 
We also use another application “Watchdog Mule” which sends momentary pulses to our application, ensuring even longer wake time for the app. (ref. SandvikDataBearerWatchdog-release v1.1.154). Note that it does not affect the functioning of the app but only ensures elongated timer.
The ConnectivityManager apis have changed over years and are expected to change further as well. The Apache Mina FTP server we using is pretty old, which needs to be considered to replace if any lag or slow down is experienced.
Significant Modules:
Apmanager: This module have classes responsible for providing apis to handle connectivity calls and        switch between personal hotspot and wifi client. Two most important methods are doEnableAccessPoint() and doDisableAccessPoint(). Earlier for pre-Oreo versions,we were using WifiConfiguration object to access the hotspot settings (as in class apmanager_pre_oreo) and for Oreo and later versions, we use ConnectivityManager api to access the startTethering/stopTethering methods(as in class apmanager_oreo).
Scope for a new access point class: As we experiment in the package SandvikDataBearer-test ver2.0.1_t2, we have a new accesspoint approch that handles the wifiDirect Hotspot instead of personal hotspot we use in our current release package. WifiDirect brings many benefits for our use case where we seek to get a static ip for the ftp server which is a limitation with personal hotspot as the new android AOSPs have it set to dynamic and is no longer hardCoded as it used to be in previous androids. 
However, our experiments suggest that WifiDirect Hotspot gateway ip stays static at 192.168.49.1 and can be used for achieving our static ip feature. Switching to this newer hotspot would also enhance the cpu performance and decrease load on the main thread of the application as we need not switch between wifiClient and hotspot here, but only the wifi stays on all the time. Third benefit is, ability to customize the SSID and password from app’s preference itself. 
However, it should be noted that every wifiDirect network created would start with a default name “Direct-hs-<your custom hotspot name>” so, the machines should be configured accordingly.

FTP: This module provides api for setting up the ftp server and establish the server over the hotspot network created. We are using the ftp client APACHE MINA FTP SERVER v1.2.0 for our app, but if we experience lag and server functionality issues, we need to consider replacing it with some newer FTP Client. This module has 4 classes viz: FsService, FTPNotifications, FTPServerCustomMessageResource and MediaUpdater. FsService and FTPNotification are the major classes. 
IoTHub Client:  This class defines the IoTHub urls and handles IoTuploads, url checks and defining ports for data transfer. There is no flaws identified in this module so we are using the previously written class as it is in the new infrastructure.
Settings: Android Preference Framework has changed. We have implemented new androidX Preferences framework to get the latest upgrades and be compatiblity with the newer devices. Any sub preferences within the preference page should now be coded as another fragment class and not as a preferenceCategory. Implementation of this new framework has helped resolve lagging of the ftp toggle switch and smoother transitions. Dynamic summary feature in this new api helps setting up summary for each preference.

# Activity Walkthrough
1.	 Starter Activity:
           The purpose of this activity is to determine which activity to start based on the Android version and then navigate to that activity. An Intent object named i is created, and it's initialized by calling the getIntentForStartingActivity() method. This method will help determine which activity to start based on the Android version. The startActivity(i) method is called to initiate the transition to the activity specified by the Intent object i. It will start the target activity.
The finish() method is called to close the current activity (in this case, StarterActivity) after starting the target activity.
Method called getIntentForStartingActivity() returns an Intent object, which will be used to determine and initiate the activity to start based on the Android version.
If the Android version is 7.1, the i Intent is configured to start the UnsupportedActivity class.
If the Android version is not 7.1, the i Intent is configured to start the MainActivity class.
 
2.	Unsupported Activity: 

This activity appears is designed to display a message when the application is launched on an unsupported Android version. 
A string variable msg is created, which is initialized by calling the getIntentMsg() method. This method extracts a message from the Intent extras if it exists.
Another string variable named errorMessage is created. It is set to the value of msg if msg is not null. If msg is null, it is set to a default message retrieved from the resources (strings.xml) with the key app_not_supported.
A TextView widget named msgTextView is obtained from the layout by its ID, which is defined in the XML layout.
In brief, the UnsupportedActivity is designed to display a message when the application is launched on an unsupported Android version. It receives a message via an Intent and displays it in a TextView on the activity's user interface. If no message is provided, it displays a default "app_not_supported" message defined in the app's resources.

3.	Main Activity:
The MainActivity class control and manage the main functionality of the Android application. It handles UI elements, permissions, services, and other essential aspects of the app. Additionally, it makes use of controllers and listeners to manage data and events within the application. 
It extends AppCompatActivity and implements the IFileStorageListener interface. This class represents the main activity of our application and is responsible for controlling various aspects of the application, including user interface elements and services.
Several static variables are declared. These include a Handler for handling runnables, a boolean variable isActive, and instances of MachineController and TransferLogController.
Various UI elements are declared, including text views, image views, and a floating action button. These elements are used for displaying information and interacting with the user.
A static variable defaultExpHandler is declared and initialized with the default uncaught exception handler for threads.
The code then proceeds to implement various methods, including onCreate, onBackPressed, onRequestPermissionsResult, and more. These methods are part of the Android activity lifecycle and are responsible for handling various events and user interactions within the application.

4.	XSettings Activity: 

This activity is designed for managing application settings.

The setSupportActionBar method is called to set the action bar of the activity using the settingsToolbar defined in the layout.

In Brief, the XSettingsActivity is an Android activity that manages application settings. It sets up the user interface, including an action bar with navigation, and displays a fragment (likely containing the settings options) in the fragment_container when the activity is created for the first time. Users can access and configure the application's settings through this activity.
  
Fragments involved with this activity- 

•	SettingsFragment
•	LoginFragment
•	AdvancedSettingFragment
•	DynamicMultiSelectListPreference

# Foreground Services
We use two services in our app for background task running. Viz, FDM and FTP

# Android Lifecycle Management
We need to be careful while calling the objects from FTP, FDM and Access Point modules. We call them in  
at various instances despite once called from onCreate() lifecycle method to ensure working of the services and connectivity management in scenarios where  the app instance is killed by user interference or sleep of the device. 
For this to happen, we call the crucial objects at other lifecycle methods like onPause() and onResume().

Note: Additionally, we take the wake lock permission  at device runtime to ensure service stays awake everytime and doesn’t get killed by android activity lifecycles. 


# Maintainers

This Project is maintained by Debojeet Bhattacharjee

# Contributing
Clone it
Create your feature branch (git checkout -b my-new-feature)
Commit your changes (git commit -m 'Add some feature')
Push your branch (git push origin my-new-feature)
Create a new Pull Request








                                 
