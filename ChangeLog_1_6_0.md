# EBookDroid v1.6.0 changelog #

## New features ##
  * The actual version is targeted for devices with Android 2.2+
  * OpenGL rendering
  * Decoding & drawing through native memory
  * Added support for the following URI schemes: smb:// http:// https://

### Recents & Files ###
  * Recent & file browser items re-styled
  * For Android 3+ tablets added the "File browser" menu item is replaced with "Storage" sub-menu with the following items:
    * File system (/)
    * SD card (external storage - typically /sdcard)
    * Auto Scan dirs (optionally)
    * Removable media (optionally)

### Tap/keyboard actions ###
  * Force portrait/landscape actions added.

### Configuration and settings ###
  * Deprecated performance settings removed.
  * The new "Auto scan removable media" preference allow to automatically scan inserted removable media (flash drives, SD cards, etc).
  * The new "Show removable" preference allow to show removable media in the "Storage" menu.
  * The new "Show scan dirs" preference allow to show scan dirs in the "Storage" menu.
  * The new "Show notifications" preference allow to switch on/off notifications about added/removed files.

## Bug fixes ##
  * Fix for flickering activities on docked Asus and some other devices.
  * Fix cropping of inner nodes for splitted pages.
  * Fix keys processing if search field is visible.