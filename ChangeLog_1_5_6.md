# EBookDroid v1.5.6 changelog #

## New features ##

  * Added ability to open a book on existing bookmark from book's context menu in Recents or File Browser.
  * Splitting pages right-to-left by user choice
  * File browser opens on first directory from set of autoscan dirs.

### Improved book formats support ###
  * PDF: MuPDF library updated up to actual version.
  * FB2: `<code>` tag support
  * FB2: Fixed width of punctuation symbols under Android 4.x
  * FB2: Various fixed and optimizations

### Tap/keyboard actions ###
  * All options menu actions available to bind.
  * CR #54: new bindable action for scrolling to page corners

### Configuration and settings ###
  * Multi-pane preferences shows only on tablets and only in landscape.
  * "Rotation mode" preference is moved to Rendering settings page.
  * Navigation and history setting are moved into separated preference screen
  * New "Show bookmarks in menu" preference added into Navigation settings ( for Android 3+).
  * Added "First page offset" book preference.

### Other changes ###
  * CR #364: capitalize bookmark title
  * CR #334: GoToPage dialog landscape layout added
  * Replace task progress dialog with alert one.
  * Hebrew localization added.

## Bug fixes ##
  * Fix for #354
  * Fix for #359
  * Fix for #345
  * Fix for missing thumbnails problem
  * Fix NPEs reported in Market.