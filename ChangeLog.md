### Since version 2, ###

  * The EBookDroid become a closed source application.
  * All 3d-party GPL code are deleted or moved to separate programs.
  * Only Android 4+ devices supported.
  * Legacy fonts are added back in package and selected by default.

# EBookDroid v2.0.7 changelog #

## Decoding features ##
  * EPUB parsing speed improved.

## Book viewer features ##
  * Bookmarks, notes and notebooks re-styled

## Preference changes ##
  * "All Settings/Supported Tools/SPen starts drawing" preference is added.


# EBookDroid v2.0.6 changelog #

## Common changes ##
  * Assembled APKs and source code for PDF and DJVU standalone decoders are available on Google Drive: http://goo.gl/SKKb8c
  * Tatar localization added.
  * Width of navigation tabs in action bar is restricted

### Book viewer features ###
  * Manual crop view is re-styled and works in all view modes.

## Bug fixes ##
  * Attempt to fix black screen on return from settings
  * Attempt to fix action bar padding
  * Exit confirmation fixed.
  * Menu button processing fixed
  * Various crashes fixed


# EBookDroid v2.0.4 changelog #

## Bug fixes ##
  * Indetermined progress indicator removed from title bar
  * Menu button processing fixed
  * Various crashes fixed


# EBookDroid v2.0.3 changelog #

## Common changes ##
  * Progress indicator is shown instead of application icon now.
  * Overflow sub-menu is opened on Menu button press.

## Decoding features ##
  * SSE optimizations in JPEG decoding are available now.

## Preference changes ##
  * "Quick zoom scale factor" preference is added.

## Bug fixes ##
  * Various fixes

# EBookDroid v2.0.2 changelog #

## Common changes ##
  * NDK v9 and GCC 4.8 are used to compile native code
  * The JPEG-turbo library is updated and configured to switch on/off SIMD optimizations in runtime.

## Preference changes ##
  * "Decode process nice level" preference added.
  * "Use SIMD optimizations" preference added.

### Decoding features ###
  * Forced line splitting added for EPUB, FB2 and RTF.
  * New icon added to show bookmarks/notes in the night mode.

## Bug fixes ##
  * Various fixes


# EBookDroid v2.0.1 changelog #

## Common changes ##
  * Removed reference to standard Android Holo theme
  * JPEG library replaced by JPEG-turbo
  * Menu re-styling for phones and small tablets.

## Preference changes ##
  * "Confirm exit" preference added.

## Bug fixes ##
  * Fixed XPS opening.
  * Fixed AES encoded PDF opening.
  * Fixed some errors reported on Google Play
  * Fixed processing of interlaced GIFs
  * Fixed processing of EPUB entities
  * Various fixes


# EBookDroid v2.0.0 changelog #

## New features ##
### Common features ###
  * Multi-document support including instant switching between opened documents and service views.
    * The following service views are reachable on application icon click:
      * Library
      * Local files
      * Local network
      * OPDS catalogs
      * Application settings
    * The following navigation styles are available:
      * Drop down list in title bar
      * Tabs in title bar
    * The following navigation mode can be applied to service views:
      * All service views are always shown as independent items
      * Only top service view is shown even any book is opened
      * Only top service view is shown if no book opened and hidden otherwise
  * Network shares browser

### Decoding features ###
  * The following separate native programs are used for document rendering:
    * mupdf - a lightweight PDF and XPS viewer, based on MuPDF library.
    * djvu - a lightweight DJVU viewer based on DjVuLibre library.
    * cbx - a lightweight Comicbook viewer based on libPng, libjpeg, GIFLIB, Unrar and zLib libraries
  * CBZ/CBR auto-detection added.
  * Experimental EPUB and RTF support added.
  * Internal structure of text documents (FB2, EPUB and RTF) are cached on disk after first rendering.

### Book viewer features ###
  * Advanced navigation panel added to replace GoToPage and Outline dialogs.
  * The following extra tools are added:
    * Text selection and external dictionary support.
    * Text highlighting
    * Free-hand annotations.
    * Text notes and page notebooks.
  * Export notes, highlights and annotations into EPUB file.

## Other changes ##
  * FAQ page added into the About dialog.
  * Languages without actual translation are removed.
  * Embedded fonts returned back.

## Bug fixes ##
  * Lot of old bugs fixed.
  * Lot of new bugs added.