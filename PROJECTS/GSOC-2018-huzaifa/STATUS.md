# Status updates for the Open Food Facts project

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
