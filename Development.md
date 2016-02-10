# Development tools #

### The following tools are required to build the application: ###

  * Google Android SDK v20
  * Google Android NDK v8

### The following tools are optional: ###

  * Eclipse 3.7 / 3.8 / 4.2
  * Google ADT plugin for Eclipse v20 including NDK plugin
  * Apache Ant v1.8

# Starting book viewer activity from other Android applications #

The following fields should be filled to call EBookDroid viewer activity:
  * action - **android.intent.action.VIEW**
  * uri - path to the book's file
  * packageName - **org.ebookdroid**
  * className - **org.ebookdroid.ui.viewer.ViewerActivity**

The following extra parameters are supported to customize book viewer:
|**Name**|**Type**|**Values**|**Description**|
|:-------|:-------|:---------|:--------------|
|persistent|Boolean |true/false|if true, all book preference changes (including position and zoom level) should be stored in DB|
|pageIndex|String  |numberic string|The page number (started from 0) to be shown|
|offsetX |String  |floating string|The horizontal offset on defined page: [0, 1]|
|offsetY |String  |floating string|The vertical offset on defined page: [0, 1]|
|viewMode|String  | One of the following strings:<ul><li>VERTICALL_SCROLL</li><li>HORIZONTAL_SCROLL</li><li>SINGLE_PAGE</li></ul>| Defines the book view mode|
|animationType|String  | One of the following strings:<ul><li>NONE CURLER</li><li>CURLER_DYNAMIC</li><li>CURLER_NATURAL</li><li>SLIDER</li><li>FADER</li><li>SQUEEZER</li></ul>| Defines the animation type for single page mode|
|pageAlign|String  | One of the following strings:<ul><li>WIDTH</li><li>HEIGHT</li><li>AUTO</li></ul>| Defines the page align type for single page mode|
|splitPages|Boolean |true/false|if true, wide book pages will be splitted on two ones|
|cropPages|Boolean |true/false|if true, wide book pages will be cropped|
|nightMode|Boolean |true/false|if true, the night mode will be switched on|