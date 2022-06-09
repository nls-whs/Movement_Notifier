# Movement Notifier

_Android OS version_: 5.1.1 (Lollipop) <br/>
_Device tested_: Vuzix Blade <br/>
_Platform_: Android (built using Android Studio) <br/>

# Introduction
This prototype has been developed as part of [Next level Sports](https://hci.w-hs.de/research/projects/nextlevelsports/) project. The main goal of this app is to enable the user to suggest an exercise suitable for the everyday situation or environment if he has not exercised enough and thus help him to integrate sport and exercise into everyday life. This means that fixed times for training units do not have to be scheduled, but the suggested exercises can be carried out immediately on site. The app is developed for the smart glass, Vuzix Blade, which has most of the features that a smartphone also has.

## Features

Some of the features are: 

• **Notifications**: If the user has not moved enough in the previous hour, he would be notified via notification message in the form of text or exercise suggestion.

• **Environment determination**: In order to be able to suggest an exercise that suits the environment or everyday situation to the user, it must be possible to determine it. For this purpose, the TensorFlow Lite API for image classification and the ML Kit Image Labeling API are tested. These allow images to be classified and titled.

• **Exercise display**: After the environment has been recognized, a suitable exercise should be suggested to the user. These should be presented in text form. This means that the title of the exercise is displayed along with the associated explanation. For reasons of space, a picture is not used for a better understanding of the exercise.

• **Measuring physical activity**: Counting the number of steps was chosen as a benchmark for physical activity. This makes it relatively easy to record physical activity without having to wear other wearables. If the number of steps within the last hour is too low, the user is then notified for exercise.

![image](https://user-images.githubusercontent.com/104509917/172883990-240d36f6-da24-45df-9944-a6796274463d.png)


# Interaction

The two-axis touchpad of the Vuzix Blade is mainly used to interact with the app which is located on the right temple of the glasses, in order to keep the operation as trivial as possible. Two main types of interaction with the touchpad are used: Touch and Swipe commands. The first one is used to turn on the display, open apps or confirm actions. The second one is again preferably used for navigation. More details are given below: <br/>

<details><summary>Interaction Details</summary>


1. _One finger, single tap_: <br/>
		– Wake up device <br/>
		– Select focused menu item <br/>
		– Open received notification <br/>
2. _One Finger, Hold_: Opens the menu if it is not visible <br/>
3. _One finger, swipe forward and backward_: <br/>
		– Navigation through the menu items <br/>
		– Horizontal scrolling of lists <br/>
4. _One finger, swipe up and down_: Scroll through lists vertically <br/>
5. _Two fingers, single tap_: <br/>
		– If the menu is visible: closes the menu <br/>
		– If the menu is not visible: Serves as a back button <br/>
6. _Two Finger, Hold_: Closes the current app and navigates to the first app in the app rail <br/>
7. _Three Finger, Single Tap_: Turns off the display <br/>

</details>

# Installation instructions for Vuzix Blade
 
1. Connect the Vuzix Blade to the computer with a USB cable. 
2. Download the _"Vuzix View"_ application for the respective computer operating system.
3. Start _"Vuzix View"_ application. Allow connection in the glasses and connect to the Vuzix Blade via the "Connect" button. 
4. Place the APK file on the user interface of the _"Live-Vuzix View"_ application (NOT where you can connect to the glasses). 
5. App is installed (display is transferred).
6. After successful installation, the app can be launched and used. 

In advance, if glasses have been reset or Dev Mode is disabled, enable ADB Debugging on the Vuzix Blade for Vuzix View. The Blade, by default, will have ADB (Android Debug Bridge) debugging disabled for security and privacy reasons.

<details><summary>Enable ADB Debugging</summary>
 
1. Go to the _Settings_ App and navigate to **System > About** menu item.
2. Tap on _About_ will show Device information screen. On this Device info screen, perform One-Finger Swipe Forward 7 times.
3. Notice a prompt reporting the number of swipes will be displayed (_Note: Make sure your Blade is in Gesture mode when performing the One-Finger swipes._)
4. After the 7 swipes the user will be taken to _Dev Options_ button and you will notice that the Dev Options button will be added under the System menu.
5. This Dev Option has additional features like USB debugging, Keep Screen On and Mock Locations.

	_New Dev Option Menu_
6. Developer options - ADB options <br/>
https://www.vuzix.com/Developer/KnowledgeBase/Detail/1077#
(Dev Account required - can be created for free)
</details>

## APK file link
The file can be downloaded from https://w-hs.sciebo.de/s/7Y2hZ6JhRfzFBvd

# Documentation

ML Image Labelling API: https://developers.google.com/ml-kit/vision/image-labeling <br/>
Vuzix Blade UX Design Guidelines: http://files.vuzix.com/Content/Upload/Vuzix%20Blade%20UX%20Design%20Guidelines_v2.pdf <br/>


## Developer
Denis Dziacko


