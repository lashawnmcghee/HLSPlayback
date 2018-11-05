# HLSPlayback
An Android demonstration app for playing and caching two selectable HLS streams.

The HLS Playback is a demo application which:
Can play one of two HLS video streams from the web, and will also
offer the user the ability to cache the video for offline viewing.

There are only 2 activities, the first of which present a choice of two streams
as URLs. Clicking on one of the two will begin its playback.

On the playback screen you will notice either a download icon (which the down arrow)
or a delete icon (trash can) will allows the user to download the stream into file
cache.  This will allow the video to be played offline.

You may test this functionality by:
1. Disable your mobile data and ensure your WiFi is connected.
2. Begin viewing a video by selecting the URL.
3. Click the download (you will see a notification with progress in your
   notifications bar)
4. When the download is complete, you may turn off your Wifi connection
5. Click the back button to get back to the initial choice screen and select the
   same link in step 2.
6. Your video should play and you can seek around to ensure it is complete.
7. Optional: If you click the other link, it will not play if you have not
   previously downloaded it into cache.  As well, if you delete the one that works from
   cache, you will no longer be able to play it either.
   
Download and playback errors are reported to the screen via toast messages as they
may come from the foreground or background listeners.

You may completely delete cache at any time by going into your Android app settings 
and deleting the app data.

The streams used came from [Bitmovin][] which provides 17 Free MPEG-DASH example
and HLS m3u8 sample test streams.

[Bitmovin]: https://bitmovin.com/mpeg-dash-hls-examples-sample-streams/ 

### Locally ###

Cloning the repository and depending on the modules locally is required when
using some ExoPlayer extension modules. It's also a suitable approach if you
want to make local changes to ExoPlayer, or if you want to use a development
branch.

First, clone the repository into a local directory and checkout the desired
branch:

```sh
git clone https://github.com/lashawnmcghee/HLSPlayback
```

#### Using Android Studio ####

To develop HLSPlayback using Android Studio, simply open the ExoPlayer project in
the root directory of the repository.

### Licenses ###

This demo makes use of the great library and media player provided by
[ExoPlayer].

[ExoPlayer]: https://github.com/google/ExoPlayer

ExoPlayer is Licensed under the Apache License, Version 2.0.

Copyright (C) 2017 The Android Open Source Project

Licensed under the Apache License, Version 2.0 (the "License");
you may not use this file except in compliance with the License.
You may obtain a copy of the License at

      http://www.apache.org/licenses/LICENSE-2.0
      
Unless required by applicable law or agreed to in writing, software
distributed under the License is distributed on an "AS IS" BASIS,
WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
See the License for the specific language governing permissions and
limitations under the License.

