# VoxAudioBooks

## Introduction
VoxAudioBooks is an Android app built by Kotlin, target API 30. The App handles both online data and Room databases with the help of coroutines and LiveData. 

## MediaPlayerService
MediaPlayer service is running as foreground sevice adapt to the restriction of accessing to background since API 26. 
AutoFocus is adopted to make sure the app will act nicely in case there is another app obtains FocusControl.
PhoneStateListener() is set up so the app will be paused when there is an incoming call and resume when it ends.
Setup notification with NotificationCompbat and build as MediaStyle
Implemeting interface and mediaSession.CallBack to update infor from the UI to the media notification and vice versa

## Images of the app
### List of audio books

<kbd>
<img src="https://github.com/KateVu/VoxAudioBooks/blob/master/images/ListBook.png" width="200"></kbd>  <kbd><img src="https://github.com/KateVu/VoxAudioBooks/blob/master/images/ListBook_Dark.png" width="200"></kbd>

### Favourite screen

<img src="https://github.com/KateVu/VoxAudioBooks/blob/master/images/Favourite.png" width="200">

### Details screen and media notification

<img src="https://github.com/KateVu/VoxAudioBooks/blob/master/images/Detail.png" width="200"> <img src="https://github.com/KateVu/VoxAudioBooks/blob/master/images/Notification.png" width="200">

