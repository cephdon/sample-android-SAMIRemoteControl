Android SAMI Remote Control
===================================

The sample app demonstrates how to retrieve devices status from SAMI,
and send them commands.

Introduction
------------

The blog post [Making the Perfect Remote Control in Five Steps](https://blog.samsungsami.io/mobile/development/2015/03/31/making-the-perfect-remote-control-in-five-steps.html) at http://blog.samsungsami.io/ describes what the app does and how it is implemented.

Prerequisites
-------------

 * SAMI Android SDK https://github.com/samsungsamiio/sami-android
 * Android SDK v21
 * Android Build Tools v20.0.0
 * Android Studio 1.0.1

Setup and Installation
----------------------

1. Create an Application in devportal.samsungsami.io:
  * The Redirect URI is set to 'android-app://redirect'.
  * Choose "Client credentials, auth code, implicit" for OAuth 2.0 flow.
  * Under "PERMISSIONS", check "Read" for "Profile".
  * Add "READ" and "WRITE" permissions to the types of devices that you want to control. Click "Add Device Type" button.  Choose, for example, "zz_OLD_philips_00000000001", "Jawbone", and "Kwikset Lock" as the device type.
2. Download and build SAMI's [Java/Android SDK libraries.](/sami/native-SDKs/android-SDK.html) The library JAR files are generated under the `target` and `target/lib` directories of the SDK Maven project.
3. Copy all library JAR files to `app/libs` of SAMIRemoteControlExample.
4. Import `SAMIRemoteControlExample` as Non-Android studio project.
5. Use the client ID (obtained when registering the app in the Developer Portal) to replace `YOUR CLIENT APP ID` in `SamiHelper.java`.

Now build the project, and deploy the apk to an Android phone.

More about SAMI
---------------

If you are not familiar with SAMI we have extensive documentation at http://developer.samsungsami.io

The full SAMI API specification with examples can be found at http://developer.samsungsami.io/sami/api-spec.html

We blog about advanced sample applications at https://blog.samsungsami.io/

To create and manage your services and devices on SAMI visit developer portal at http://devportal.samsungsami.io

License and Copyright
---------------------

Licensed under the Apache License. See LICENSE.

Copyright (c) 2015 Samsung Electronics Co., Ltd.
