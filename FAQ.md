## Q: First of all, what is EBookDroid and what can I do with it? ##
## A: ##
Well, first and foremost you can read books or, more generally, documents of many sorts: PDF, DJVU, EPUB, MOBI, FB2, CBR, CBZ, and even RTF and XPS.
The most prominent feature of EBookDroid is its customizability: you can tune both interface and reading modes to suit your reading habits. And most importantly, you can set layout and viewing preferences on a per book basis.

Most (but not all!) of the settings are accessible via the side menus you can access by swiping from the left or right borders toward the center of the screen.
The menu on the left has (mostly) system and file-system options; the menu on the right has (mostly) viewing and appearance options. To access the menus just swipe horizontally near the blue glowing areas (and yes, you can disable the blue glow, if you wish so!).

The rest of the settings are available in a multi-page menu that can be accessed from the left lateral menu (entry "All Settings...").
In there you can also find the option to remove the spiderwebs and other decorations, if they are too cheesy for you.

## Q: What else can I do with EBookDroid? ##
## A: ##
You can read books with facing pages in landscape mode.

You can split landscape double pages into two portrait pages.

You can read from right to left.

You can fix skewed pages, automatically.

You can remove empty or useless borders via cropping (either Automatic or Manual).

You can zoom in and out of columns of text (requires configuration).

You can browse the table of contents (outline) of the book, if the book has one. And you can create a custom ToC with bookmarks if the book does not have one.

You can add bookmarks and in-text notes. You can also create a notebook with extended page-indexed annotations that can be used as a 'reading diary' or be exported as an external EPUB file for further processing.

You can highlight and draw on the page.

You can search the document, copy and paste text or look-up a dictionary or web-search engine for a given word.

You can flip between multiple open books.

You can set the brightness and background color of the pages. You can go easy on your eyes with Night Mode and Dark Room Mode.

## Q: What can I NOT do with EBookDroid (at least for the time being)? ##
## A: ##
You can't reflow text. Sorry, this is by design. This means that you cannot increase font size to make the text bigger without the page growing bigger than the screen.
You can zoom to columns, if you wish.

You can't embed your notes directly into the document (for example a PDF file).
EBookDroid philosophy is not to alter any document.
Possibly an external tool can be developed to incorporate the notes exported into the EPUB file in the original document file. But do not hold you breath while waiting for it.

You can't brew coffee.
But you can still read a book on coffee brewing.

## Q: What? No reflow??? ##
## A: ##
Yes, no reflow.

For several technical reasons, EBookDroid is a 'page-oriented' document viewer. Many of its unique features require the document's content to be cast in a set of immutable pages. Not only that, but EBookDroid developers believe a document should be rendered as faithfully as possible to the way the original creator designed it.
All that said, EBookDroid provide several features to make the best of the scarce screen size of portable devices (read on).

## Q: Why are the command sequences described in the FAQ different on my version of EBookDroid? ##
## A: ##
EBookDroid is constantly being updated and enhanced. Sometimes the features described in this FAQ might not be up to date with those in the latest version of the program. A certain degree of mental flexibility is required to adapt the procedures herein described to the actual version of EBookDroid currently on your device. Most of the the times some options have changed name with something equivalent; some other times certain options might have been removed, or simply moved to another menu or another panel. Reading the ChangeLog embedded in the app and placed on the Wiki might be of some help.

## Q: Do I need to download some plug-in to view my PDF or DJVU documents? ##
## A: ##
No, all plug-ins are _already integrated_ into EBookDroid. All you have to do is to acknowledge installation and the fact that you want to use the plug-in to read your documents. The reason for this is simply that starting with version 2.0 some of the code is now used as separate modules in form of a plug-in. Again, you do not have to download anything: all the code is already present within the EBookDroid app.

## Q: I do not see any of my EPUB, RTF, MOBI or XXX files. Why? ##
## A: ##
You have to enable the correspondent file type associations in General settings..., File Association.... Just tap in the missing checkboxes, restart EBookDroid and you will see your EPUB and RTF files. Or any other new supported format that you wish to enable.

## Q: My comic books are just jpg images in a folder. How can I read them in EBookDroid? ##
## A: ##
CBR and CBZ file are just rar- and zip-compressed folders of images. You can turn your folder of pictures into such a file by simply compressing them (with a RAR o ZIP compressing utility) and then changing the file suffix from ".rar" or ".zip" to ".cbr" or ".cbz". It's that easy. Then you can have EBookDroid read them.

Linux users already know how to do that. Under Windows, popular compressing utilities are 7zip (free and open source), WinZip and WinRar (not so free and not open source).

## Q: How do I access my books and documents? ##
## A: ##
You leave your books and documents exactly where they are - there is no need to import or copy them in special folders. Just specify the path were you keep them stored (for example on your external SD card) and they will be available in the graphical user interface (the bookshelf).
Each folder and subfolder is graphically translated into a separate bookshelf that you can browse by swiping down (if there are more books than can be displayed in a single screen).
You can browse between bookshelves (that is folders) by swiping left and right.
By default, the "Recent" bookshelf is shown when EBookDroid is first opened.

If you prefer browsing directory trees, you can do so by accessing the Folder View under the EBookDroid icon button (it's on the top left corner: the backward E and B that look like two books - or a droid with a green dome...).

In either view, whenever you reach a book you want to read, you can open it by - guess what - tapping on it.
It will later show up in the Recent bookshelf.

## Q: How can I tell EBookDroid where to look for my books? ##
## A: ##
While in bookshelf view tap the app icon (the "EB" on the top left); a list of options will appear. Choose "All Settings" to enter the settings page (yes, not everything is set via the lateral menus!).
Scroll down to "Library and Files" and tap on it.
Tap on "Auto Scan Directories" and type the path to the directory you want EBookDroid to look for your books (default is the internal SD card of your device)

## Q: What can I do from within the Bookshelves? ##
## A: ##
In the "Recent" bookshelf you can remove the book link (not the file, just its presence in this particular shelf), its cached data or its specific settings.

In all other shelves you can executes file operations (to rename, delete or copy the corresponding files - needless to say, you'd better be careful with "delete").

## Q: How can I  have EBookDroid automatically open a certain type of file using an external file manager? ##
## A: ##
EBookDroid uses MIME types to handle different file types. Unfortunately, Android's and many other file managers might not support all MIME types. When this happens, you will not be able to open said files directly from the file manager. Some file managers allow you to add new MIME types and associate them with the corresponding file extensions. Refer to your file manager manual to get further instructions on how to do that. All MIME types supported by EBookDroid are listed [here](MimeTypes.md).

## Q: Why can I turn pages only in vertical? ##
## A: ##
By default EBookDroid has two tapping areas at the top and bottom of the screen that allow you to turn pages up and down. This is the most natural way to navigate a document in small devices like mobile phones and smartphones.
You can add your own tapping areas by means of the "Configure Taps" menu. You can have both vertical and horizontal tapping areas.

## Q: Can I customize the interface? ##
## A: ##
You can customize how EBookDroid reacts to gestures and key pressing.
By default EBookDroid comes with a few commands coded in:

  * Close
  * Page forward
  * Page backward
  * Settings

You can either redefine the tactile interface from scratch, or simply add new commands.
The Settings... Configure Touch menu shows a grid of touch sensitive zones representing the whole screen.
You can select an area by dragging a selection on the screen - the area will change color (to a different hue, it's not important which color is used) to reflect the selection - and then you can add up to three different commands for the selected zone

  * one for Tap
  * one for Double Tap
  * one for Two-fingers Tap
> (the long tap on the whole page is reserved for the Edit Tools)

(You do not have to associate a command for each one of these three gestures, pick the ones you want and just leave those you are not interested in blank)
Colors are semi-transparent because zones are allowed to overlap.

For example, to add a left to right page browsing (as opposed to the up-down by default), just select the leftmost column and then choose the "Page back" command from the "Tap" list. After that, you can select the rightmost column and associate the "Page forward" command to the tap List.

If you want to add a "Double Tap to Column" behavior to EBookDroid, just select the whole screen area and then associate the "Zoom to column" command to the "Double Tap" gesture.
Next time you'll double tap on a column of text, EBookDroid will automatically zoom in to the column width. Double tap again and you are back to your original magnification level.

## Q: What else can I customize of the interface? ##
## A: ##
You can hide the title and or the status bar (to gain a full page view of your document).

You can hide the blue lateral glow of the lateral menus (to make sure nothing stands between the nazgu--- the reader and its book)

You can choose if and where to show small indicators for page number and zoom level.

You can hide the spiderwebs and winter decorations.

More to come...


## Q: How can I maximize viewable area in Ebookdroid? ##
## A: ##
EBookDroid offers several features to make the most out of the - usually scarce - screen real estate of tablets and smart phones. Apart from the ordinary zoom level (manually selectable either by pinching in and out or by means of a slider at the bottom of the screen), the following methods can be used to magnify relevant parts of a document:

  * Split pages
  * Automatic crop margins
  * Manually crop margins (select and lock viewable area)
  * Zoom to a selected column of text

These methods are not necessarily mutually exclusive as it is for example possible to split a landscape page into two portrait pages, each one with its own margins automatically cropped and it will still be possible to zoom in and out of single columns of text with a simple gesture. Please note that all these features can be applied on a per book basis, as EBookDroid will remember each book's specific settings.

### Split pages ###
Some documents show two pages side by side in a single landscape view. EBookDroid can show half landscape page at the time in portrait mode, effectively splitting a single large landscape page into two subsequent portrait pages. This functionality is easily accessed by checking "**Split pages**" in the More... View... menu (the actual sequence of commands could vary from version to version). It can also be set as a default behavior for all books, although you'll rarely need to do so.

### Crop margins (automatic) ###
Most documents (books, whitepapers, tutorials) share a page layout characterized by wide empty margins around the page's content. While this is usually desirable in a printed document, it represents a waste of screen real estate on handheld devices such as smartphones and tablets. By checking the option "**Crop margins**" in the View... menu, you can have EBookDroid automatically strip those margins away for you.

This option can be set as a default for all books (by checking it in the General Settings menu) or on a per book basis (by accessing it from the View... menu or the Book Settings menu).

When (automatic) Crop Margins is enabled, you can zoom in by pinching in and return to the cropped version of the page by simply zooming out.

### Crop margins (manual) ###
Sometimes it is useful to be able to manually select a specific portion of page to be shown on screen and lock that particular view. This is the case when the document has a structure that would fool the algorithm used by the automatic Crop Margin function, or when it is preferable to a have a fixed sized (albeit enhanced) viewable area, or simply when the relevant content is only part of the complete content on the page.

For example, some whitepapers carry disclaimers or website addresses on the side of the page: showing a full page with intact margins or a page automatically cropped to include the side text would result in unnecessary waste of screen area. EBookDroid allows you to select only what you want to read, and it does so with unique flexibility.

The Manual Crop menu works in every visualization mode but _it can only be set in **single page** mode_. It can be accessed via the View... menu by selecting "**Manual Crop**". If you are in Single Page mode, you will be presented with a customizable rectangular selection on top of the current (single) page.  By dragging handles at the opposite corners of the rectangular selection, you will be able to define the portion of the page that will be shown on the screen (viewable area). Once you are satisfied with your selection, _double tap_ inside the area. This will bring up a context menu with the following relevant options:

  * Crop current page only
  * Crop all pages
  * Crop all even (odd) pages
  * Crop even and odd pages, symmetrically

EBookDroid can crop all pages with the same area pattern, but it can also crop even and odd pages with vertically symmetric area patterns, or with two different patterns. This will allow you to maximize content magnification in virtually every document.


For example, some textbooks have the main body of text always on the right of each page and a wide margin that can carry pictures or comments always on the left (or the other way around). Being the relevant content area the same for every page, these book are best viewed by selecting "**Crop all pages**" after having highlighted the area of interest.


Other textbooks have a symmetric layout, with the main body of text placed on the left for even pages and on the right for odd pages (or the other way around). These books are best served by selecting the area of interest on one page (it does not matter if it's even or odd) and then choosing "**Crop all even (odd) pages symmetrically**". The area for the specular pages will be automatically set by EBookDroid. You can use this feature to remove the holes from the sheets of a ringbinder, for example.


Other documents present a certain type of content on one page and a different kind of content on the next one (for example, a picture on the left page and text/comment on the right page).
EBookDroid's flexible Manual Crop feature allow you to select two distinct viewable areas for even and odd pages. Just repeat the Manual Crop procedure twice for two subsequent pages: each time, select the desired viewable area, double tap inside it and choose "**Crop all even (odd) pages**". Again, you do not need to worry which pages are odd and which are even; you just need to repeat the procedure for two (adjacent) pages.

Once you have set the desired viewable area with Manual Crop, you can scroll, change page, zoom in and out and be sure to be able to return to the same view by pinching out. The area selected represents now the new page size for your document (to be specific: for one, all, all even or odd pages according to your choice).

You can remove manual crop settings via the same menu you use to set them: View... , "Manual crop", double click on the selected area and then choose one of the options:

  * Remove crop settings from current page(s)
  * Remove all crop setting

The first option will remove any (manual) crop settings associated with the current page or set of pages. This means that it can be used to remove crop settings from a single page, from all pages, from all even (or odd, but not both) pages, according to what had previously been chosen. To remove all (manual) crop settings from the selected document just choose "Remove all crop settings". This won't affect the automatic crop setting the document might have.

(Superseded) - The last option in Manual Crop settings,

  * Return to area selection

simply takes you back to the area selection screen. You can exit the Manual Crop submenu by using the back button on your device.

### Zoom to column ###
Zoom to Column is a feature that allows you to fit the width of a selected column to the screen. This is useful for reading multiple column documents, like newspapers, magazines, scientific papers, some textbooks and whitepapers. There is no menu to directly access this feature, whose action can be associated to a particular gesture (a tap, a long tap or a double tap, whatever suits you) in the "**Tap Configuration**" menu.

For example, to add a "Double Tap to Column" behavior to EBookDroid, go into the Configure Tap menu, and select the whole screen area by dragging one corner of the screen to the opposite corner. Then associate the "Zoom to column" command to the "Double Tap" gesture.

Once you have done this, whenever you double tap on a column of text, EBookDroid will automatically zoom in to the column's width. Repeating the gesture on the magnified column will take you back at the previous level of magnification (be it full page, a custom level or one imposed via automatic or manual crop settings).

## Q: When I try to select the area for Manual Crop nothing happens. Why? (superseded) ##
## A: ##
Although the viewable area selection affects all viewing modes, setting the area for Manual Crop requires your document to be displayed in Single Page mode. Please turn on Single Page mode before trying to set Manual Crop's viewable area, otherwise you will not be able to see the resizable rectangular selection superposed to the current (single) page.

## Q: Can I change brightness, color and appearance of the document? ##
## A: ##
You can change the brightness at a global level, setting a value as a percent of the default device brightness. If white pages are too bright to read, you can set the value at 80 or 70, it will look better in dim light.

You can even change the page background color - at the moment this is a global setting.

You can also invert colors, in Night mode. It will help you go easy on your eyes when you are reading at night, with the lights off.
A special setting will let you choose between gray-scale images (it adds to a nice blackboard effect for math books) or colored images.

And you can change the contrast, brightness and saturation of the displayed page. This is very helpful with old xeroxes that might need some tweaking for enhance their readability.

## Q: Where are the Edit Tools? ##
## A: ##
The new Edit Tools introduced with EBookDroid 2.0 are experimental features under development. For this reason they are disabled by default. In order to use them, you need to enable each one of them in the menu Edit Tools under General Settings...
Once one or more Edit Tools have been enable, a long tap on the page will make the Edit toolbar appear at the top of the screen. Choose a tool and then an action to perform with that tool. When you are done, tap on the check sign on the left of the toolbar to apply your action.

## Q: Why are some Edit Tools and functions disabled by default? ##
## A: ##
Several features are still in development and are marked as experimental. They will work flawlessly on many tablets and smartphone, but they might cause problems to users with certain hardware/software combinations. To avoid crippling EBookDroid for these unfortunate users, the app is shipped with these features disabled so that a user can immediately know when enabling it will result in some kind of malfunctioning.

## Q: What??? EBookDroid highlights word by word? ##
## A: ##
You can enable "Smart highlighting" to have a continuous highlighting without spaces between words. This is still an experimental feature and for this reason it is disabled by default.

## Q: What kind of annotations can I add to my ebooks? ##
## A: ##
You can add bookmarks. They are shown as small ribbon-like icons.

You can add in-text notes. They are shown as asterisks.

You can add extensive notes that will be collected in a notebook associated with the book. When a page of the book has a notebook page associated to it, a small page icon is shown in the top right corner of the page (if not cropped).

You can highlight text.

You can draw simple geometric figures (like rectangular and circular boxes).

You can add free-hand drawings by using your fingers or an S-Pen (on certain Samsung devices).

## Q: What happens to these annotations? ##
## A: ##
All annotations for a given book are saved internally by EBbookDroid in a dedicated database for fast retrieval, but can be exported in a user-selected folder for backup purposes. The annotations are NOT embedded in the document, as EBookDroid's philosophy is to leave the documents as they are. You can save the annotations for a given book and then restore them if you need to do so.

## Q: What is the Notebook function and how do I use it? ##
## A: ##
Starting with version 2.0, EBookDroid provides users with many ways to annotate their documents. The Notebook function can associate text with every page of the book. This allows the user to write extensive comments, considerations, ideas, or simply to summarize what he or she has read so far. All notebook pages can be exported as an epub document that can be read by any application and moved to other platforms for further processing.

### To add a Notebook page: ###
Long tap on the page, choose "Notes" from the tools bar, then "Notebook".
You can now write down your thoughts and associate the text to the page you are reading. A notebook entry can have a title and a content; the content can have some basic formatting (bold, italics, underline).
These notes will be saved internally by EBookDroid and will be accessible for reading and editing by using the Notebook feature on that same page.
You can add a notebook page for every page of your book.
The whole notebook will be accessible via the Go to... menu, by tapping on the notebook icon.


### To export the Notebook: ###
Notebook pages are stored internally by EBookDroid for maximum efficiency. If you want to access all notes at once from another application, you have to export them using the Export function (available on the Text toolbar). EBookDroid will generate an epub file containing all your notebook pages neatly ordered by page number.The epub file will be placed in the same folder as the original file with the name booktitle\_notes.epub (where booktitle is the actual name of the file of your document). You can then use it at your leisure, for example by moving it on your computer, renaming it as a zip file and extract the notes on selected pages.
If you add more entries to your notebook, you must remember to export it again in order for them to show up in the epub container.

## Q: Why should I need a Notebook feature? ##
## A: ##
The Notebook feature (actually a personal log associated with the document) provides the user with much more flexibility with respect to in-text annotations (also available in EBookDroid 2.0+). Here are a few examples of how you could use it.

You are an IT professional and you are reading a programming textbook while commuting on the subway. While you read, you write code that exemplifies what you believe you have just learned. At the end of the day you end up with several programs, most likely one per page. Transfer the booktitle\_notes.epub file to your computer, rename it as a zip file and extract the pages where your code snippets are located.

You are a student and have to write an essay. It would be easier to do it if you could write the essential points as long as you read. Do that at every page you want. When you finish reading, take the booktitle\_notes.epub file, import it into your word processor, massage it a bit, print it out and hand it in.
You could use notebooks to summarize lengthy textbooks while you read. Take notes about the most interesting parts on every page you feel inclined to add something. When you finish reading the book, you will have a nice version of it in your own words, chapter after chapter, page after page.

You are a journalist and you have to review a book. While you are reading it, you can add your considerations on every page you find something worth noting. At the end of the book, you export your diary as booktitle\_notes.epub and move it to your computer. Here you can open it and transfer it into your word processor to create a review with all your annotations ordered by page.

## Q: Why does EBookDroid need to access the Internet? Do you collect data when I am not looking? ##
## A: ##
EBookDroid requires Internet access in order to provide OPDS functionality. When EBookDroid was open source you could have analyzed the code to verify that. Later versions are now distributed with another license and the code is no longer available for scrutiny, so you'll have to either trust the developers or analyze all data exchanged by the app to make sure nothing shady is going on under the hood. If you still think that EBookDroid is "too good to be free", you can support the developers by buying the paid version (PDF and DJVU Reader, look it up on Google Play) that, for your peace of mind, features no Internet connection (but lacks the OPDS functionality).

## Q: On my device, EBookDroid is too slow for my tastes: can I speed it up a bit? ##
## A: ##
The first thing to do is getting a version of EBookDroid optimized for your device's particular architecture. Please refer to the download page on this website for versions optimized for different architectures (like for Arm A8 or A9 with Neon support).

If you are using an optimized version of EBookDroid and your browsing still feel sluggish, you can try fiddling with Advanced Settings. These are settings that affect performance but need to be balanced on a per device basis to avoid having the opposite effect to that intended.
For example: you can make EBookDroid load a certain number of pages (default is zero) into memory. The more pages you load into memory, the faster it will be moving from one to the next. But too many pages will drain your memory (sometimes unnecessarily, for example if you do not need to fast-browse and you take some time to read each page). Some users believe that setting to 3 the "Number of pages to load into memory" is a good compromise. Your mileage may vary.

Not all devices tolerate all these advanced settings, therefore some experimenting is needed to strike the correct balance of performance and stability.
For example, you can dedicate more than one thread to page rendering, but on certain devices this could have a detrimental effect on some services or other applications making the system overall slower.

## Q: How do I obtain the program's log file? ##
## A: ##
### 1. On the PC, using Android SDK ###
Connect your device to a PC running Android SDK and issue the command _"adb logcat"_.
### 2. On the device ###
There are many apps on the Market that allow you to see and save the log file directly on the device.
For example:
  * https://market.android.com/details?id=com.nolanlawson.logcat
  * https://market.android.com/details?id=org.jtb.alogcat