# awipe

Android wiping app to help you wipe free space on your device

Useful for old Android devices that do not have proper encryption or wiping mechanisms

[Download latest APK](https://github.com/ericswpark/awipe/releases/latest/download/app-release.apk)

## Screenshots

![main_window](img/main_window.png?raw=true) ![wipe_process](img/wipe_process.png?raw=true)

## Warning

awipe is still in beta. It may not securely write over every single byte on your device. I am not responsible for any sensitive data leaking as a result of using this tool.

## Usage

1. Wipe your device
2. Encrypt your device (if it isn't already encrypted)
3. Install awipe
4. Run awipe
5. Factory reset from the Settings app, or if the Settings app force-closes due to low available storage space, then reboot to recovery and format the data partition.
6. Give your device away or sell it

# Is awipe really necessary?

No, but also yes. On modern Android versions, the encryption key is discarded on factory reset, just like iOS. However, because manufacturers like to change Android and possibly make their distributions out of spec, it's possible that their wiping/encryption scheme has problems. In addition, older Android phones do not mandate encryption, or proper encryption at that.

awipe makes it really hard for data recovery since we are writing randomized, encrypted data over your previous data. Because awipe fills up all of the available storage, it is unlikely that wear-leveling cells in the memory chips will retain enough of your previous information to be useful, if any. However, just like the warning written above, awipe makes no guarantees on the safety of your data.
