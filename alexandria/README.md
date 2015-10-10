This file lists the changes made to the original code provided by Udacity,
as part of the third project assignment for [Udacity's Android Developer Nanodegree]
(https://www.udacity.com).

* Added ZXing Android Embedded for the barcode scanning functionality.
* Removed unused class CameraPreview.
* Reorganized package structure and renamed classes to better follow conventions.
* Decoupled Fragment and Activity by removing callback communication and
  replacing it with EventBus.
* Replaced task DownloadImage with the Picasso library.
