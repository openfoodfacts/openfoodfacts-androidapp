This roadmap is fully collaborative.

Note: Nutrition table bugs can be solved by fixing: Use the HTML fallback for the nutrition table, and make the native optional until we fix it. https://github.com/openfoodfacts/openfoodfacts-androidapp/issues/2474

Showstopper bugs
Lists:
- Lists are not properly shown on the product page : https://github.com/openfoodfacts/openfoodfacts-androidapp/issues/2923

Product page:
- Remove old palm oil field : https://github.com/openfoodfacts/openfoodfacts-androidapp/issues/2914
- Infinite refresh loop: https://github.com/openfoodfacts/openfoodfacts-androidapp/issues/2443

History:
- Red crosses shown instead of product images: https://github.com/openfoodfacts/openfoodfacts-androidapp/issues/2895

Edit mode:
- Fix units for the edit mode (bug to create)
- The android app sends "chamorro" language for Swiss users: https://github.com/openfoodfacts/openfoodfacts-androidapp/issues/2789
- The app displays a calorie less that the value in the edit mode: https://github.com/openfoodfacts/openfoodfacts-androidapp/issues/2743
- Fix camera crash related to Easy Image : https://github.com/openfoodfacts/openfoodfacts-androidapp/issues/2736

Scan mode:
- Add a button on new product addition card: https://github.com/openfoodfacts/openfoodfacts-androidapp/issues/2859
- Disable dragging up on unknown product: https://github.com/openfoodfacts/openfoodfacts-androidapp/issues/2680

Nutrition table : 
- Cocoa percentage is displayed as g: https://github.com/openfoodfacts/openfoodfacts-androidapp/issues/2178

Enhancements:
- Product image stretched on upload: https://github.com/openfoodfacts/openfoodfacts-androidapp/issues/1393
- Show NOVA and Nutri-Score in search results and history: https://github.com/openfoodfacts/openfoodfacts-androidapp/issues/2458
- Don't return a result if the scanned barcode matches the server-side blocklist:  https://github.com/openfoodfacts/openfoodfacts-androidapp/issues/2629
- I should not have to wait for the product to upload (esp. when I have no or little network) https://github.com/openfoodfacts/openfoodfacts-androidapp/issues/2677
- Add in-app changelog: https://github.com/openfoodfacts/openfoodfacts-androidapp/issues/2870
- Revamp the onboarding with illustations/animations/new features
- Add % of daily values for each nutrient of a product
- Add a graph of your nutrient intake
- Honoring the server-side barcode blocklist to avoid scan errors
- Remove bugs on product page after product editing
- Add additive function in the additive card: https://github.com/openfoodfacts/openfoodfacts-androidapp/issues/2233
- Move "Take image" buttons at very start of product addition https://github.com/openfoodfacts/openfoodfacts-androidapp/issues/2860 and https://github.com/openfoodfacts/openfoodfacts-androidapp/issues/2350
- Add promo to make image selector discoverable
- Add hints to explain image selector buttons
- Make nutrition input numeric: https://github.com/openfoodfacts/openfoodfacts-androidapp/issues/2605
- Reformat Nutrition tab: https://github.com/openfoodfacts/openfoodfacts-androidapp/issues/2449
- Keep additives warnings in compare mode: https://github.com/openfoodfacts/openfoodfacts-androidapp/issues/2376
- Information related to Nutri-score and Nova should be available offline : https://github.com/openfoodfacts/openfoodfacts-androidapp/issues/2238
- Product name is not immediately shown on scan card after product addition: https://github.com/openfoodfacts/openfoodfacts-androidapp/issues/2194
- Improve suggestions by eliminating common special characters: https://github.com/openfoodfacts/openfoodfacts-androidapp/issues/2184
- Prepared nutrition facts are not displayed: https://github.com/openfoodfacts/openfoodfacts-androidapp/issues/2177 and https://github.com/openfoodfacts/openfoodfacts-androidapp/issues/1288 (close one of them as duplicate)
- Scanned products that do not exist in Open Food Facts do not appear in history: https://github.com/openfoodfacts/openfoodfacts-androidapp/issues/168

Major Enhancements:
- Allow adding a product to any project: https://github.com/openfoodfacts/openfoodfacts-androidapp/issues/1900
- Revamp the Product store
- Offline product scan (Pull request doing half the feature ready): https://github.com/openfoodfacts/openfoodfacts-androidapp/issues/2069, https://github.com/openfoodfacts/openfoodfacts-androidapp/issues/37 and https://github.com/openfoodfacts/openfoodfacts-androidapp/issues/30
- Product contribution experience that's less intimidating and includes our machine learning advances to offer suggestions
- Store all history products for offline viewing: https://github.com/openfoodfacts/openfoodfacts-androidapp/issues/875 and duplicate: https://github.com/openfoodfacts/openfoodfacts-androidapp/issues/211
- Merge Open Food Facts and Open Beauty Facts
- Products recommandations (with better Nutri-Scores, NOVAs…): https://github.com/openfoodfacts/openfoodfacts-androidapp/issues/825 and https://github.com/openfoodfacts/openfoodfacts-androidapp/issues/605
- Add a personal achievements page: https://github.com/openfoodfacts/openfoodfacts-androidapp/issues/2873
- Various proposals for product addition redesign: https://github.com/openfoodfacts/openfoodfacts-androidapp/issues/2230 , https://github.com/openfoodfacts/openfoodfacts-androidapp/issues/499, https://github.com/openfoodfacts/openfoodfacts-androidapp/issues/269
- Add product recall notifications: https://github.com/openfoodfacts/openfoodfacts-androidapp/issues/349
- Daily Calorie counter: https://github.com/openfoodfacts/openfoodfacts-androidapp/issues/348
- Search result ranking: https://github.com/openfoodfacts/openfoodfacts-androidapp/issues/199
- Advanced search by additive, categories, ingredients: https://github.com/openfoodfacts/openfoodfacts-androidapp/issues/167
- Detect barcode at 90° angle: https://github.com/openfoodfacts/openfoodfacts-androidapp/issues/125
- Ability to add a product that doesn't have a barcode: https://github.com/openfoodfacts/openfoodfacts-androidapp/issues/123

Bugs:
- use Product instead of State in the bundle intent used in ProductFragment: https://github.com/openfoodfacts/openfoodfacts-androidapp/issues/152
- Share credentials between Open Beauty Facts and Open Food Facts: https://github.com/openfoodfacts/openfoodfacts-androidapp/issues/528
- Fully native Open Food Facts signup
- Honor the unit from the nutrient taxonomy: https://github.com/openfoodfacts/openfoodfacts-androidapp/issues/2867

PRs to validate:
- Open openfoodfacts.org links in the app: https://github.com/openfoodfacts/openfoodfacts-androidapp/issues/273

Other:
- Screenshot automation for F-Droid and the PlayStore: https://github.com/openfoodfacts/openfoodfacts-androidapp/issues?q=is%3Aissue+is%3Aopen+label%3Afastlane
- Show last edit date on product: https://github.com/openfoodfacts/openfoodfacts-androidapp/issues/356
- Check the PR :https://github.com/openfoodfacts/openfoodfacts-androidapp/issues/305
- Select products for offline view: https://github.com/openfoodfacts/openfoodfacts-androidapp/issues/303
- Include translated descriptions in F-Droid metadata: https://github.com/openfoodfacts/openfoodfacts-androidapp/issues/107

Milestones:

https://github.com/openfoodfacts/openfoodfacts-androidapp/milestones

Labels:

https://github.com/openfoodfacts/openfoodfacts-androidapp/labels



Offline Browsing
Offline Edit
Gamification
Look and feel
User management
Onboarding
