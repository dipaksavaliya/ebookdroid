# EBookDroid v1.3 changelog #

### New features ###
  * Hardware acceleration is returned back for Android 3+
  * Horizontal scroll view mode added
  * FB2 support added
  * Page cropping implemented
  * Bookshelf view (Android 1.6+) for recent books and library folders added
  * Natural curler (like as in FBReader) added
  * PDF rotation implemented.
  * Book thumbnails added
  * "Set as thumbnail" action is added into menu
  * About dialog added

### Optimization of decoding and drawing process ###
  * Bitmap pool added
  * Page bitmaps are splitted onto 128\*128 pieces to unify memory usage
  * Gray background is drawn if djvu page is failed to decode
  * Zooming processing improved

### Impoved book formats support ###
  * MuPDF library updated up to actual sources
  * DjVU library updated up to actual sources
  * XPS library updated up to actual sources
  * Update freetype to version 2.4.7

### Configuration and settings ###
  * Preferences based on fragments UI available for Android 3+
  * DjVU specific settings added

### Other changes ###
  * Animated transition is used (if defined) when scroll button pressed
  * New Year's Easter egg added
  * Clearing application data dialog added

## Bug fixes ##
  * Old bugs fixed
  * New bugs added