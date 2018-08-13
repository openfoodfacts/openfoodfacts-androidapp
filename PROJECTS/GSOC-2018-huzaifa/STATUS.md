# Status updates for the Open Food Facts project

## Week starting August 06th

### Highlights

* Added native editing.

### Completed this week

* Added salt field and auto compute salt value from sodium.
* Added the app's version number in the comment field when adding or editing a product.
* Added nutrient's modifier.
* Get the language specific name and ingredients on changing the product language while editing.

## Week starting July 30th

### Completed this week

* Instead of opening a custom tab, modified the edit button in the app to open product addition layout with all the non empty fields of the product already filled.
* Added option to take new images for the product by long pressing the the preview image.
* Fixed few bugs, enhancing the overall performance of the product addition layout.
* Display auto-suggestions in the app's language.
* Suffix the language code for `product_name` and `ingredients_text` fields. #1806


### Working on this week

* Fix bugs and make improvements in the native editing feature.
* Incorporate changes suggested by the mentor.

### To be worked on next week

* Continue the development of native editing.

## Week starting July 23rd

### Highlights

* Added thorough product addition and implemented OCR for ingredients. Product addition works offline too.

### Completed this week

* Added tests for `OfflineSavedProduct` class.
* New UI for the list of products saved offline.
* Added checks when uploading products saved offline
  * Checks for existing values of product name, ingredients, quantity and link on the server for the barcode being uploaded.
  * If any of these value exists locally as well as on server then the user is shown a comparison dialog to choose between his version or the version present on the server.
* During the addition of a product saved offline, check and reupload the images if not uploaded previously.
* Improvements in scanning.
* Added nova group image in the quickView as well as the product view.

### Working on this week

* Fix issues faced by users (if any) related to product addition.

### To be worked on next week

* Add native editing in the app.

## Week starting July 16th

### Completed this week

* Added offline product addition.
* Auto save entered details if the product exists in the offline mode on pressing back button.
* Added "extract ingredients" button if ingredients image is selected and ingredients text is empty when opening an offline saved product.
* Display the product in the quickView when offline, and the product is already stored in the OfflineSavedProduct db.
* Added option to zoom the preview image.
* Modified the Open Food Facts Base URL to production server.
* Fix bugs and incorporated changes suggested by mentor.

### Working on this week

* Fix bugs and make improvements in the product addition taking suggestions and feedback from the OFF community.

### To be worked on next week

* Continue the development of product addition.

## Week starting July 09th

### Completed this week

* Replace nutrition facts tab with a tab to add additional pictures for Open Beauty Facts and Open Products Facts.
* Added button to upload more photos.
* Added autocomplete for EMB code by getting suggestions from the server.
* Added confirmation dialog before discarding the product addition.
* Added search button for the product link field.
* Added field "Period of time after opening" for OBF with autocomplete suggestions from the server.
* Added QR code scanner to scan product URLs for the product link field.

### Working on this week

* Add offline mode in the product addition.

### To be worked on next week

* Continue the development of product addition specifically the offline mode and incorporate the changes suggested by mentor.

## Week starting July 02nd

### Completed this week

* Incorporated changes suggested by mentor.
* Added OCR for ingredients.
* Added autocomplete for traces (allergens).
* Overwrite the most recently uploaded image as the display image.
* Set the language of the image uploaded as the product language chosen and not as the default phone language or the app language.
* When a product is added successfully the quickView is filled with the product data and the FAB is turned green with a thumb up icon.
* Add chip input for categories.

### Working on this week

* Modify product addition to work for all the flavors of the app.

### To be worked on next week

* Continue the development of product addition and incorporate the changes suggested by mentor.

## Week starting June 25th

### Completed this week

* Changes done in the WIP PR #1712
* Cleared cached images after product was uploaded.
* Added autocomplete suggestions for categories, labels and countries.
* Added basic checks in the product addition such as:
  * pH shouldn't exceed 14.
  * Alcohol % shouldn't exceed 100%.
  * Sum of sugar and starch should be less than carbohydrate.

### Working on this week

* Add OCR for ingredients.

### To be worked on next week

* Continue the development of product addition and incorporate the changes suggested by mentor.

## Week starting June 18th

### Completed this week

* Added thorough product addition on the test server.
* Quicker feedback if scan was successful. #1735

### Working on this week

* Fix other issues from the issues tracker.

### To be worked on next week

* Enable offline mode in the thorough product addition.

## Week starting June 11th

### Completed this week

* Added new product addition UI.
* Fixed travis CI failing issue #1699
* Fixed issue #1658

### Working on this week

* Implement the added product addition UI.
* Fix other issues from the issues tracker.

### To be worked on next week

* Add thorough product addition on test server using the newly designed UI.

## Week starting June 04th

### Completed this week

* Fixed issue #1680
* Fixed issue #1676
* Fixed issue #1670
* Worked on the sliding up capabilities however it was not smooth and was not approved during the discussion. Tried using this [library](https://github.com/laenger/ViewPagerBottomSheet), however faced issues on scrolling the viewpager's fragments. Also found a nullpointerexception issue when implementing this library and tried communicating with the owner of this library who is still trying to figure out the issue. Also tried implementing different solutions for the sliding part however couldn't find any strong approach which could be of any help in this use case.

### Working on this week

* Discuss and finalize product addition layout.
* Fix other issues from the issues tracker.

### To be worked on next week

* Implement the product addition UI.

## Week starting May 28th

### Highlights

* Added continuous scan in the app.

### Completed this week

* Added continuous scan feature.
* Fixed issue #1646
* Fixed issue #1628

### Working on this week

* Minor UI changes in the new immersive layout for the scan fragment.
* Add sliding up capabilities.

### To be worked on next week

* Test features added till now.
* Incorporate any changes suggested in the new UI.

## Week starting May 21st

### Highlights

* First weekly status update for the Open Food Facts project

### Completed this week

* Created README and STATUS page for the project.
* Created and publish first weekly status.
* Fixed issue #1590
* Fixed issue #1592
* Fixed issue #1593

### Working on this week

* Create a new immersive layout for the scan fragment.
* Add continuous scan feature.

### To be worked on next week

* Test features added till now.
* Add sliding up capabilities.

## Week starting May 14th

### Completed this week
* Created a project board for GSoC 2018.
* Created issues related to the proposal with complete details including the UI/UX if required.
* Posted these issues in the android slack channel too for getting suggestions from others.

### To be worked on next week
* Create README and STATUS page for the project.
* Create and publish first weekly status.
