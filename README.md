# A01DL (PKRemote) : The Android application for downloading pictures from various digital cameras via WiFi.

This document is written in Japanese.

--------------------------------

## A01DL : PKRemote Android用Wifiカメラ撮影画像ダウンローダー**

### 概要

A01DLは、(機種によっては少し制約がありますが)いろんなカメラに対応した、Wifi経由で撮影画像を転送するためのAndroidアプリケーションです。

-----------

### 機能一覧

* カレンダー表示
* カメラ内画像一覧表示（すべて、撮影日別、カメラ内フォルダ別）
* 画像を複数選択して一括で取得
* 画像を１枚表示して取得（RAWファイル、動画ファイルの取得も可）
* カメラで撮影した画像を自動転送

各社の通信仕様が異なるため、残念ながら全ての機能を全てのカメラで動作させることはできていません。
カレンダー表示はアプリを起動した日になる、、RAWファイルや動画ファイルが取得できない、などの制限がある機種があります。

-----------

### 制御対象カメラ

* RICOH [GR II](http://www.ricoh-imaging.co.jp/japan/products/gr-2/), [GR III](http://www.ricoh-imaging.co.jp/japan/products/gr-3/)
* RICOH [THETA](https://store.ricoh360.com/)
* [PENTAX DSLR (K-1, KP, K-70 等)](https://api.ricoh/docs/camera-wireless-sdk-android/)
* Olympus製カメラ (OM-D, PEN, TG-6 等)
* [OLYMPUS AIR A01](https://jp.omsystem.com/cms/product/opc/a01/index.html)
* SONY製カメラ（レンズスタイルのDSC-QX1シリーズは除く、ILCE-QX1は使用可能です。）
* Canon製カメラ ([iNSPiC REC](https://faq.canon.jp/app/answers/detail/a_id/101025/)は除く)
* Fujifilm製カメラ
* Panasonic製カメラ
* Nikon製カメラ (Wireless Mobile Utility対応機、SnapBridge対応機は、Wifi直接接続をサポートした機種のみ使用可能です)
* [JK Imaging製 KODAK PIXPRO WPZ2](https://kodakpixpro.com/AsiaOceania/jp/cameras/sportcamera/wpz2/)
* [VisionKids HappiCAMU T3](https://www.visionkids.com/product-page/happicamu-t3)
* [myFirst Camera Insta Wi](https://jp.myfirst.tech/products/myfirst-camera-insta-wi)

### 操作説明

- [操作説明](https://github.com/MRSa/GokigenOSDN_documents/blob/main/Applications/A01DL/Readme.md)

-----------

### OlympusCameraKitについて

A01DL は、OlympusCameraKit を使用して[OLYMPUS AIR A01](https://jp.omsystem.com/cms/product/opc/a01/index.html)と通信を行います。そのため、以下の「SDKダウンロード許諾契約書」の条件に従います。

- [EULA_OlympusCameraKit_ForDevelopers_jp.pdf](https://github.com/MRSa/gokigen/blob/5ec908fdbe16c4de9e37fe90d70edc9352b6f948/osdn-svn/Documentations/miscellaneous/EULA_OlympusCameraKit_ForDevelopers_jp.pdf)
- [EULA_OlympusCameraKit_ForDevelopers_en.pdf](https://github.com/MRSa/GokigenOSDN_documents/blob/main/miscellaneous/EULA_OlympusCameraKit_ForDevelopers_en.pdf)

-----------
