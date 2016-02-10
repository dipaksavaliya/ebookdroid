# EBookDroid v1.2 changelog #

## New features ##

### Optimization of decoding and drawing process ###
  * The decoding queue is resorted depending on current view position before top element is taken
  * The position & zoom event handlers optimization to restrict number of affected pages/page nodes.
  * New memory usage mode: "Render page in document resolution" for new devices with Android 3+
  * Native bitmap memory is directly used if possible (Android 2.2+)
  * Full recycling of book resources on "Back" key pressed.

### Bookmarks support added ###
  * For current page bookmark can be added by corresponding menu item.
  * For any other page bookmark can be added inside the "Goto page" dialog
    * use seek bar to set the required page number
    * click the "Bookmarks" button below to open "Add bookmark dialog"
  * After that bookmark will be available in scrollable list below the "Bookmarks" button
  * Short click on a bookmark in this list move the progress indicator in seek bar and update page number edit control
  * Long click on a bookmark allow to delete this bookmark
  * Long click on the "Bookmarks" allow to delete all bookmarks for the opened book.

### Impoved book formats support ###
  * DJVU, PDF: native bitmap memory support added
  * DJVU: decoding into 16 bit color space supppord added

### Configuration and settings ###
  * "Load recent book on start" preference added into UI settings
  * "Zoom by double tap" preference added into Touch&Scrolling settings
  * New memory usage mode: "Render page in document resolution" for new devices with Android 3+
  * Max slice image size (in KB) added for "Low memory" mode.
  * Values of text/list preferences are shown in summary string

### Other changes ###
  * Current book settings dialog is additionally available from main menu
  * Interface changes in file browser/recent library activities
    * Optimization of folder scanning process
    * Book settings dialog is opened on long click on a book icon/row


## Bug fixes ##

  * Various issues are fixed