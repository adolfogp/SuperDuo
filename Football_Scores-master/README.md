This file lists the changes made to the original code provided by Udacity and
issues identified as part of the third project assignment for [Udacity's Android Developer Nanodegree]
(https://www.udacity.com).

* Removed unused strings from `strings.xml`.
* Added `translatable="false"` attributes to untranslatable strings, like the
  API key.
* Added content description for the share button.
* Renamed `myFetchService` to `FetchService` and `scoresAdapter` to
  `ScoresAdapter` since the former names did not follow Java class naming
  conventions. Also changed the name of some variables, but many more do not
  follow Java or Android naming conventions. The project does not follow code
  style guidelines.
* The application uses deprecated classes such as
  `android.support.v7.app.ActionBarActivity` and `android.text.format.Time`.
