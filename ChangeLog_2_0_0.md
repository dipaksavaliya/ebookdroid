# EBookDroid v2.0.0 changelog #

### Since version 2, ###
  * The EBookDroid become a closed source application.
  * All 3d-party GPL code are deleted or moved to separate programs.
  * Only Android 4+ devices supported.
  * Legacy fonts are added back in package and selected by default.

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