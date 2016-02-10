EBookDroid preferences

## User interface: User interface settings ##
  * Load recent: Auto loading last opened book on app start - OFF by default
  * Confirm closing: Ask close confirmation on Back pressed - OFF by default
  * Brightness: Set the overall image brightness (0–100) - 100 by default
  * Brightness in night mode: Apply brightness setting only in night mode - OFF by default
  * Keep screen On: Keep screen always on - ON by default
  * Rotation mode: How to change rotation - possible values:
    * Force landscape
    * Force portrait
    * Automatic
  * Full screen: Hide status bar. - OFF by default
  * Show Title: Show Title. Need Document reopen!! - ON by default
  * Page in title: Show page number in title bar - ON by default
  * Position of page number toast: Position of page number toast - possible values:
    * Invisible
    * Left top corner
    * Right top corner
    * Left bottom corner
    * Bottom of the screeen
    * Right bottom corner
  * Position of zoom toast: Position of zoom toast - possible values:
    * Invisible
    * Left top corner
    * Right top corner
    * Left bottom corner
    * Bottom of the screeen
    * Right bottom corner
  * Show animation icon: Show animation or dragging icon if animation or dragging enabled - ON by default
## Touch and Scrolling: Touch and scrolling preferences ##
  * Enable taps: Use Tap Configuration to define tap regions and actions - ON by default
  * Scroll height: Set height for scroll - 50 by default
  * Touch processing delay: Touch processing delay (in milliseconds) - 50 by default
  * Animate scrolling: Animate scrolling in scroll modes - ON by default
## Performance settings: Performance settings ##
  * Pages in memory: Number of pages to store in memory - 0 by default
### Low level application preferences (Applied after document reload): Low level application preferences (Applied after document reload) ###
    * Document viewer implementation: Defines viewer class - possible values:
      * Based on android.view.View
      * Based on android.view.SurfaceView
    * Decode thread priority: Defines decode thread priority - possible values:
      * Lowest
      * Lower
      * Normal
      * Higher
      * Highest
    * Surface draw thread priority: Defines surface draw thread priority - possible values:
      * Lowest
      * Lower
      * Normal
      * Higher
      * Highest
    * Use native graphics: Use Android 2+ native graphics library - ON by default
    * Hardware acceleration enabled: Set if Hardware acceleration can be set by viewer - OFF by default
    * Texture bitmap size: Defines size of texture parts - possible values:
      * 64 `*` 64
      * 128 `*` 128
      * 256 `*` 256
      * 512 `*` 512
      * 1024 `*` 1024
    * Texture filtering: Enable bilinear texture filtering - OFF by default
    * Reuse texture bitmaps: Reuse existing texture bitmaps when page part is decoded again - ON by default
    * Bitmap allocation hack: Use bitmap allocation tracking hack - OFF by default
    * Early bitmap recycling: Bitmaps are manually recycled when they are removed from pool - OFF by default
    * Reload page during zoom: Reload page during zoom if zoom changed more than 20% - OFF by default
## Rendering: Rendering settings ##
  * Split pages: Split landscape pages into two portrait - OFF by default
  * Crop pages: Crop margins of pages (experimantal) - OFF by default
  * Page view mode: Page view mode - possible values:
    * Vertical scroll
    * Horizontal scroll
    * Single page
### Single page settings: Settings actual for single page mode ###
    * Page align mode: How to align pages - possible values:
      * By width
      * By height
      * Automatic
    * Animation type: Select type of transition animation - possible values:
      * None
      * Simple curler
      * Dynamic curler
      * Natural curler
      * Slider
      * Fade in
      * Squeeze
### Post–processing effects: Post–processing effects ###
    * Night mode: Use white symbols on black background - OFF by default
    * Contrast: Contrast correction level (0 – 1000, 100 by default) - 100 by default
    * Exposure: Exposure correction level (0 – 200, 100 by default) - 100 by default
    * Auto levels: Use auto levels correction - OFF by default
### Format specific: Settings for each individual file format ###
#### Format specific: Settings for each individual file format ####
##### DJVU: DJVU format specific settings #####
      * Rendering mode: Page rendering mode - possible values:
        * Color
        * Black
        * Color only
        * Mask only
        * Background only
        * Foreground only
##### PDF: PDF format specific settings #####
      * Use custom DPI: Define custom DPI values below - OFF by default
      * X DPI: X DPI - 120 by default
      * Y DPI: Y DPI - 120 by default
##### FB2: FB2 format specific settings #####
      * Font size: Base font size - possible values:
        * Tiny
        * Small
        * Normal
        * Large
        * Huge
      * Enable Hyphenation: Experimental Hyphenation implementation - OFF by default
## Book settings: Current book settings ##
  * Split pages: Split landscape pages into two portrait - OFF by default
  * Crop pages: Crop margins of pages (experimantal) - OFF by default
  * Page view mode: Page view mode - possible values:
    * Vertical scroll
    * Horizontal scroll
    * Single page
### Single page settings: Settings actual for single page mode ###
    * Page align mode: How to align pages - possible values:
      * By width
      * By height
      * Automatic
    * Animation type: Select type of transition animation - possible values:
      * None
      * Simple curler
      * Dynamic curler
      * Natural curler
      * Slider
      * Fade in
      * Squeeze
### Post–processing effects: Post–processing effects ###
    * Night mode: Use white symbols on black background - OFF by default
    * Contrast: Contrast correction level (0 – 1000, 100 by default) - 100 by default
    * Exposure: Exposure correction level (0 – 200, 100 by default) - 100 by default
    * Auto levels: Use auto levels correction - OFF by default
## File Browser: File browser and file types settings ##
  * Use bookcase view: Use bookcase view instead of lists - ON by default
  * Auto Scan directory: Directory for automatic files search - /sdcard by default
### Files Types: Files Types for Show in library ###
    * .djvu: Show DjVu files - ON by default
    * .djv: Show DjVu files - ON by default
    * .pdf: Show PDF files - ON by default
    * .xps: Show XPS files - ON by default
    * .oxps: Show OXPS files - ON by default
    * .cbz: Show CBZ files - ON by default
    * .cbr: Show CBR files - ON by default
    * .fb2: Show FB2 files - ON by default
    * .fb2.zip: Show FB2.ZIP files - ON by default