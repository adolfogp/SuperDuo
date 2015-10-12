This file lists the changes made to the original code provided by Udacity,
as part of the third project assignment for [Udacity's Android Developer Nanodegree]
(https://www.udacity.com).

* Added ZXing Android Embedded for the barcode scanning functionality.
* Removed unused class CameraPreview.
* Reorganized package structure and renamed classes to better follow conventions.
* Decoupled Fragment and Activity by removing callback communication and
  replacing it with EventBus.
* Replaced task DownloadImage with the Picasso library.
* Moved the app's icon from drawable to the mipmap folders to better support
  scaling.
* Added documentation for classes and packages.
* Added domain model objects to improve the object oriented design of the
  solution.
* Broke up large methods into smaller ones.
* Replaced ActionBarActivity (deprecated) with AppCompatActivity in MainActivity.
* Changed app navigation since the methods it used were deprecated, opting
  to use common navigation patterns as [suggested by the documentation]
  (http://developer.android.com/reference/android/support/v7/app/ActionBar.html#setNavigationMode%28int%29)
* Removed the navigation drawer and settings activity since they do not apply
  to the new navigation pattern.
* Added activities to present the different fragments when running on a phone
  (previously there was only one activity and many transactions on fragments).
* Fixed Loader registration. The Loader for the list of books dit not work
  properly, since it was not registered at onActivityCreated.
