# Dilo Android SDK 연동 가이드

* 본 문서의 내용은 Sample App을 기반으로 작성하였습니다. 해당 App의 코드(특히 `ContentActivity`)를 함께 참고하시기 바랍니다

## 목차

* [개정 이력](#개정-이력)

1. [시작하기](#1-시작하기)
    * [Dilo SDK 추가](#dilo-sdk-추가)
    * [AndroidManifest.xml 속성 지정](#androidmanifestxml-속성-지정)


2. [광고 설정](#2-광고-설정)
    * [Companion 광고를 위한 레이아웃 설정 (옵션)](#i-companion-광고를-위한-레이아웃-설정-옵션)
    * [광고 Skip기능 제공을 위한 Button 할당 (옵션)](#ii-광고-skip기능-제공을-위한-button-할당-옵션)
    * [Class `RequestParam`](#iii-class-requestparam)
    * [필수 파라미터 관련 예시](#iv-필수-파라미터-관련-예시)


3. [광고 요청](#3-광고-요청)
    * [Class `AdManager`](#i-class-admanager)
    * [광고 요청 예시](#ii-광고-요청-예시)


4. [광고 액션 수신](#4-광고-액션-수신)
    * [광고 액션](#광고-액션)
    * [광고 액션 수신 시퀀스 다이어그램](#광고-액션-수신-시퀀스-다이어그램)
    * [광고 액션 수신 예제](#광고-액션-수신-예제)


5. [데이터 클래스 명세](#5-데이터-클래스-명세)
    * [Class `AdInfo`](#i-class-adinfo)
    * [Class `Progress`](#ii-class-progress)
    * [Class `DiloError`](#iii-class-diloerror)
    * [Class `DiloUtil`](#iv-class-diloutil)


6. [딜로 SDK 동작](#6-Dilo-SDK-동작)
    * [Companion에 대한 동작](#i-companion에-대한-동작)
    * [Tracking에 대한 동작](#ii-tracking에-대한-동작)
    * [Audio Focus에 대한 동작](#iii-audio-focus에-대한-동작)
    * [Notification에 대한 동작](#iv-notification에-대한-동작)
    * [Audio 재생에 대한 동작](#v-audio-재생에-대한-동작)


7. [기타](#7-기타)

---

## [개정 이력](#목차)

### 0.6.2 - 2021/08/11

#### 수정

* SDK
    * 광고 재생 시 MediaSession에 광고 메타데이터 정보 업데이트 추가
        1. `RequestParam.Builder`에 앨범 아트 URI를 전달받는 메소드 추가
            * `albumArtUri(albumArtUri: String?)`<br><br>
            
        2. `MediaBrowserServiceCompat`을 구현한 `DiloMediaBrowserService` 작성
            DiloMediaBrowserService의 MediaSession에 아래 정보가 설정
              - MediaMetadataCompat.METADATA_KEY_ARTIST : RequestParam.Builder().notificationContentTitle
              - MediaMetadataCompat.METADATA_KEY_TITLE : RequestParam.Builder().notificationContentText
              - MediaMetadataCompat.METADATA_KEY_ALBUM_ART_URI : RequestParam.Builder().albumArtUri
              - MediaMetadataCompat.METADATA_KEY_ALBUM_ART : 앨범 아트 Bitmap
              - MediaMetadataCompat.METADATA_KEY_DURATION : 광고 길이


* 샘플앱
    - '설정'에 앨범 URI를 입력받는 EditText 추가

---

<details>
<summary>0.6.1 - 2021/07/29</summary>

#### 수정

* SDK
    - 안정성 개선

</details>

---

<details>
<summary>0.6 - 2021/06/22</summary>

#### 수정

* 공통 (SDK, 샘플앱, 가이드)
    - 더 나은 지원을 위해 언어가 `JAVA`에서 `Kotlin`으로 변경되었습니다


* SDK
    - `JAVA`에서의 public 접근 호환을 위하여 아래 클래스들은 `JAVA`로 유지됩니다
        * `Progress`  `AdInfo` `DiloError` `DiloUtil`
    - 간헐적으로 광고주 이름 및 광고명의 인코딩 문제가 발생하는 현상이 수정되었습니다
    - 0.5.4 버전에서의 ***Deprecated*** 메소드 및 상수가 삭제되었습니다
        + 메소드 : `RequestParam.usePauseInNotification(boolean)`
        + 상수 : `DiloUtil.INTENT_KEY_PROGRESS` `DiloUtil.INTENT_KEY_ERROR` `DiloUtil.INTENT_KEY_AD_INFO` `DiloUtil.INTENT_KEY_MESSAGE`


* 가이드
    - [2-iv. 필수 파라미터 관련 예시](#iv-필수-파라미터-관련-예시)가 추가되었습니다

</details>

---

<details>
<summary>0.5.4 - 2021/06/04</summary>

#### 추가

* `RequestParam` 클래스에 **Flag**를 지정하는 메소드가 추가되었습니다
    - `setFlags(int flags)`, `addFlags(int flags)`, `removeFlags(int flags)`
    - 현재 SDK 기능 및 앞으로 추가될 기능들의 사용 여부를 지정하는 데 사용됩니다
    - [`RequestParam` 클래스](#iii-class-requestparam)에서 `RequestParam.FLAG_`로 시작하는 Flag들을 참고하시기 바랍니다


* `DiloError` 클래스에 `code` 추가
    - 상세 에러 코드로 처리할 수 있도록 에러코드 추가
    - 자세한 코드는 [`DiloError` 클래스](#iii-class-diloerror)를 참고하시기 바랍니다


* Notification에 광고 진행률을 보여주는 프로그레스 바 추가
    - 해당 기능은 기본으로 **disable** 상태이며 사용하기 위해 `RequestParam.Builder`의 Flag 지정 메소드를
      사용하여 `RequestParam.FLAG_USE_PROGRESSBAR_IN_NOTIFICATION` Flag를 지정하시기 바랍니다
    - 자세한 내용은 [Notification에 대한 동작](#iv-notification에-대한-동작)을 참고하시기 바랍니다

#### 수정

* Notification 사용자 일시중지/재개 변경 버튼 활성화 기능 변경
    - 해당 기능을 사용/미사용하는데 사용하였던 `RequestParam.usePauseInNotification(boolean)` 메소드는 ***Deprecated*** 되었습니다
        - 더 이상 위 메소드는 기능이 작동하지 않습니다 (다음 릴리즈 버전에서 삭제될 예정입니다)
    - 일시중지/재개 버튼 사용 여부는 기본으로 **disable** 상태이며 사용하기 위해 `RequestParam.Builder`의 Flag 지정 메소드를
      사용하여 `RequestParam.FLAG_USE_PAUSE_IN_NOTIFICATION` Flag를 지정하시기 바랍니다


* `DiloUtil`에 정의된 Intent에서 Extra 데이터를 가져오기 위한 KEY 상수 이름이 수정되었습니다
    - DiloUtil.INTENT_KEY_로 시작하는 상수는 호환성을 위해 현재 버전에서는 유지되며 다음 릴리즈 버전에서 삭제될 예정입니다

기존|변경
----|----
INTENT_KEY_PROGRESS|EXTRA_PROGRESS
INTENT_KEY_ERROR|EXTRA_ERROR
INTENT_KEY_AD_INFO|EXTRA_AD_INFO
INTENT_KEY_MESSAGE|EXTRA_MESSAGE

* 광고 액션 변경 사항
    - 광고 요청 후 3초 내에 같은 epiCode로 광고를 요청하면 `ON_ERROR` 액션을 Broadcast한 후 요청을 무시합니다
    - SKIP에 대한 액션 전달
        * 변경 전 : SKIP 시 `ON_AD_SKIPPED` 액션 전달
        * 변경 후 : SKIP 시 `ON_AD_SKIPPED` 액션 전달 후 바로 `ON_AD_COMPLETED`(광고 재생 완료) 액션 전달
    

* 데이터 클래스 변경 사항
    - `AdInfo` `DiloError` `Progress` 클래스의 implements 클래스가 변경되었습니다 `Serializable` -> `Parcelable`
    - 따라서 `Intent.getSerializableExtra()` 대신 `Intent.getParcelableExtra()` 를 사용해서 데이터를 가져와야합니다


* Companion
    1. 리로드 기능
        - 사용자가 닫기 버튼을 눌러 Companion을 닫았거나, Skip 버튼을 눌러 건너뛰었거나, SDK가 광고 재생을 마쳤을 경우 `AdManager.reloadCompanion()` 호출 시
          Companion을 리로드 하지 않고 무시합니다
    2. 로드 기능
        - Companion이 있는 광고 유형이어도 Companion View인 `kr.co.dilo.sdk.AdView`가 사용자에게 보이지 않는 상황(getWindowVisibility() !=
          View.VISIBLE)이 되면 더이상 Companion을 로드하지 않습니다

</details>

---

<details>
<summary>0.5.3 - 2021/05/12</summary>

#### 추가

* 광고 액션에 `ON_COMPANION_CLOSED` 추가
    - 이 액션은 사용자가 Companion 닫기 버튼(ViewGroup)을 눌렀을 때 발생합니다

#### 수정

* 가이드 내 용어 변경
    - 매체사 -> App

</details>

---

<details>

<summary>0.5.2 - 2021/05/10</summary>

#### 추가

* RequestParam 클래스
    - enum `AdPositionType` 추가
    - RequestParam 클래스에 ***필수*** 값 추가
        - `adPositionType`, `channelName`, `episodeName`, `creatorName`, `creatorId`
* 광고 액션에 `ON_MESSAGE` 추가
    - App 개발 시 SDK에서 참고할만한 메시지를 전송할 때 발생합니다
    - 개발 도중에는 이 액션을 수신하는 것을 권고합니다
* `DiloError` 클래스에 새로운 에러 유형 `REQUEST`추가

#### 수정

* RequestParam 클래스
    - `epicode`, `bundleId` **URL 인코딩** 처리
    - ***필수*** 값 Validation 처리 (Validation 실패 시 광고 액션 `ON_ERROR`에서 메시지 확인 가능)
* `DiloError` 클래스 패키지 이동 kr.co.dilo.sdk -> kr.co.dilo.sdk.model
* 광고가 재생중일 때 `AdManager.start()` 중복 호출 처리
* Companion 노출 개선

</details>

---

<details>
<summary>0.5.1 - 2021/04/15</summary>

#### 수정

* Skip 버튼 null 설정 시 오류 처리

</details>

---

<details>
<summary>0.5 - 2021/04/01</summary>

#### 최초 작성

</details>

---

## [1. 시작하기](#목차)

### [Dilo SDK 추가](#목차)

* 최상위 level `build.gradle`에 maven repository 추가

```javascript
allprojects {
    repositories {
        google()
        mavenCentral()

        // DILO Maven Repository 접근 정보
        maven {
            url "s3://maven.dilo.co.kr/release"
            credentials(AwsCredentials) {
                accessKey "AKIAWDHIQRYZCM64U2NM"
                secretKey "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"
            }
        }

    }
}
```

* App level `build.gradle`에 디펜던시 추가

```javascript
dependencies {    
    ...
    implementation 'kr.co.dilo:dilo-sdk:0.6.2'
}
```

### [AndroidManifest.xml 속성 지정](#목차)

* 필수 퍼미션 추가

```xml

<manifest
        xmlns:android="http://schemas.android.com/apk/res/android"
        package="kr.co.dilo.sample.app"
>
    ...
    <!-- 필요 권한 설정 -->
    <uses-permission android:name="android.permission.INTERNET"/>
    <uses-permission android:name="android.permission.ACCESS_NETWORK_STATE"/>

    <!-- 네트워크 보안 설정 (targetSdkVersion 28 이상) -->
    <!-- 광고 노출 및 클릭이 정상적으로 동작하기 위해서 cleartext 네트워크 설정 필요 -->
    <application android:usesCleartextTraffic="true"/>
</manifest>
```

## [2. 광고 설정](#목차)

### [i. Companion 광고를 위한 레이아웃 설정 (옵션)](#목차)

* App에서 Companion(Image)을 포함하는 광고 노출을 원하는 경우 Companion이 노출될 레이아웃(`kr.co.dilo.sdk.AdView`)을 선언합니다.
* Companion 닫기 버튼을 제공할 경우, 해당 레이아웃(`ViewGroup`)을 포함하여 할당합니다.

```xml
<!--
     eg) Companion을 보여줄 레이아웃을 'kr.co.dilo.sdk.AdView'로 추가
         Companion 부모 레이아웃의 크기를 1000px * 1000px로 설정
         닫기 버튼을 할당하기 위한 RelativeLayout을 오른쪽 위에 추가
-->
<FrameLayout
        android:layout_width="1000px"
        android:layout_height="1000px">

    <kr.co.dilo.sdk.AdView
            android:id="@+id/companion_ad_view"
            android:layout_width="match_parent"
            android:layout_height="match_parent"
    />

    <RelativeLayout
            android:id="@+id/companion_close_button"
            android:layout_width="25dp"
            android:layout_height="25dp"
            android:layout_marginRight="10dp"
            android:layout_marginTop="15dp"
            android:layout_gravity="right|top"
            android:orientation="vertical"
            android:background="@drawable/close_button"
    >
    </RelativeLayout>
</FrameLayout>
```

### [ii. 광고 Skip기능 제공을 위한 Button 할당 (옵션)](#목차)

* App에서 광고 Skip기능을 제공할 경우 SKIP 버튼(Button)을 선언합니다.
* Dilo SDK는 Skip 가능한 시점에만 해당 버튼을 Visible 처리합니다.

> 닫기 버튼의 위치는 Companion 우측 상단에 구현하는 것을 권고합니다
>
> 참고로 Dilo의 opt-out 버튼은 항상 우측 하단에 위치합니다
>
> <p align="center">
>     <img src="https://user-images.githubusercontent.com/73524723/113123506-9ed89d80-924f-11eb-8598-d933e0744d74.png">
> </p>

```xml
<!-- eg) Skip 버튼을 App 내 원하는 곳에 위치하여 레이아웃 설정 -->
<Button
        android:id="@+id/skip_button"
        android:layout_width="wrap_content"
        android:layout_height="30dp"
        android:textColor="@android:color/white"
        android:background="@drawable/skip_button"
        android:layout_marginLeft="0dp"
        android:paddingLeft="5dp"
        android:paddingRight="5dp"
        android:textSize="10sp"
        android:visibility="invisible"
/>
```

### [iii. Class `RequestParam`](#목차)

> 광고 요청에 필요한 클래스 및 열거 명세입니다<br>
> class `RequestParam`, `RequestParam.Builder` <br>
> enum `RequestParam.ProductType`, `RequestParam.FillType`, `RequestParam.AdPositionType`

> RequestParam.Builder를 통해 필요한 광고 요청을 세팅할 수 있습니다

```kotlin
class RequestParam {
    class Builder {
        ///////////////////////
        // 필수 사항
        ///////////////////////
        // 누락 또는 유효하지 않은 값 입력 시 ACTION_ON_ERROR 액션이 전달됩니다
        ///////////////////////

        /**
         * 번들 ID(패키지 이름)를 설정합니다
         *      ※ 광고요청 전 DILO 시스템에 등록되어야 합니다
         *      ※ 등록되어있지 않으면 항상 NO-FILL 응답을 리턴합니다    
         *      ※ 테스트 시에는 "com.queen.sampleapp"를 지정하고 NO-FILL 응답 시 문의 메일 부탁드립니다
         *      ※ URL 인코딩되어있지 않아야 합니다 (딜로 SDK 내부에서 처리합니다)
         */
        fun bundleId(bundleId: String): Builder

        /**
         * 에피소드 코드를 설정합니다
         *      ※ 광고요청 전 DILO 시스템에 등록되어야 합니다
         *      ※ 등록되어있지 않으면 항상 NO-FILL 응답을 리턴합니다
         *      ※ 테스트 시에는 "test_live"를 지정하고 NO-FILL 응답 시 문의 메일 부탁드립니다
         *      ※ URL 인코딩되어있지 않아야 합니다 (딜로 SDK 내부에서 처리합니다)
         *          ex) https://test.com/audio/episode_20210410.mp3
         *      ※ 컨텐츠 재생 시점이 아닌 지점의 광고 삽입의 에피소드 코드는 문의 메일 부탁드립니다
         *          ex) 앱 첫 실행 시점, 앱 종료 직전, 특정 메뉴 터치 시점 등 
         */
        fun epiCode(epiCode: String): Builder

        /**
         * 에피소드의 채널 이름을 설정합니다
         * 광고 요청한 에피소드가 속해있는 채널 이름
         * 추후 리포트에 반영되는 값으로 채널 이름이 없을 경우 리포트에 보여질 임의값으로 설정합니다
         *     ex) "매일 아침 9시 세계의 라디오 세상", "앱 시작 광고" ...
         */
        fun channelName(channelName: String): Builder

        /**
         * 에피소드 타이틀을 설정합니다
         * 추후 리포트에 반영되는 값으로 에피소드 타이틀이 없을 경우 리포트에 보여질 임의값 설정
         *     ex) "전세계 확진자 1억 명 임박…되돌아본 코로나19 1년 6개월", 
         */
        fun episodeName(episodeName: String): Builder

        /**
         * 에피소드의 창작자 ID(중복되지 않는 고유 식별값)를 설정합니다
         * 매체사에서 창작자를 식별할 수 있는 값
         *     ex) "user#0001", "12391", "F8CA-9283-AFB1C" ...
         * 중복될 경우 같은 창작자로 인식하여 리포트가 집계됩니다
         *     ※ 만약 닉네임같이 변경 가능한 값을 설정할 경우, 변경 시 기존과 다른 창작자로 인식하여 리포트가 다르게 집계됩니다
         *     ※ 이 항목에 개인정보가 포함되지 않도록 유의하시기 바랍니다
         *        Email, 전화번호 등 개인정보가 포함되는 경우에는 적절한 암호/해시화(BASE64, MD5, SHA1 등) 처리를 하시는 것을 권고합니다
         */
        fun creatorId(creatorId: String): Builder

        /**
         * 광고 요청한 에피소드의 창작자 이름을 설정합니다
         * 추후 리포트에 반영되는 값으로 창작자 이름이 없을 경우 리포트에 보여질 임의값 설정
         *     ex) BJ홍길동
         */
        fun creatorName(creatorName: String): Builder

        fun drs(@IntRange(from = 0) duration: Int): Builder // 광고 요청 길이를 설정합니다 (초)

        /**
         * 광고 상품 유형을 설정합니다
         * 아래 enum ProductType 참고
         */
        fun productType(productType: ProductType): Builder

        /**
         * 광고 채우기 유형을 설정합니다
         * 아래 enum FillType 참고
         */
        fun fillType(fillType: FillType): Builder

        /**
         * 광고가 컨텐츠에서 삽입된 위치 타입을 설정합니다
         * 아래 enum AdPositionType 참고
         * 광고 요청 시점에따라 설정하여야합니다
         */
        fun adPositionType(adPositionType: AdPositionType): Builder

        fun iconResourceId(@DrawableRes iconResourceId: Int): Builder // Notification에 보여질 아이콘을 설정합니다

        ///////////////////////
        // 선택 사항
        ///////////////////////

        /**
         * Companion이 할당된 사이즈를 설정합니다
         * 설정하지 않으면 자동으로 계산된 사이즈가 들어갑니다
         *     ※ 수동으로 설정할 경우 0보다 큰 값이 들어가야합니다 (단위 : pixel)
         */
        fun companionSize(@IntRange(from = 1) width: Int, @IntRange(from = 1) height: Int): Builder

        /**
         * Companion이 보여질 뷰를 설정합니다
         *     ※ 컴패니언이 있는 광고 (ProductType = DILO_PLUS || DILO_PLUS_ONLY) 요청에
         *       이 항목을 설정하지 않으면 컴패니언 없는 광고(DILO)가 나갈 수 있습니다
         */
        fun companionAdView(companionAdView: AdView): Builder

        fun closeButton(closeButton: ViewGroup?): Builder // 광고 Close 버튼 뷰를 설정합니다
        fun skipButton(skipButton: Button?): Builder      // 광고 Skip 버튼을 설정합니다

        /**
         * Notification 클릭 시 수행할 PendingIntent를 설정합니다
         *     ※ App의 컨텐츠 재생 화면 (광고 재생 화면)으로 설정하는 것을 권고합니다
         */
        fun notificationContentIntent(intent: PendingIntent?): Builder
        fun notificationContentTitle(notificationContentTitle: String?): Builder // Notification의 타이틀 문구를 설정합니다
        fun notificationContentText(notificationContentText: String?): Builder   // Notification의 텍스트 문구를 설정합니다

        /**
         * 앨범 아트 URI
         *     ※ 설정하지 않으면 딜로 이미지가 들어갑니다
         */
        fun albumArtUri(albumArtUri: String?): Builder
        
        fun setFlags(@Flags flags: Int): Builder    // 플래그를 지정합니다
        fun addFlags(@Flags flags: Int): Builder    // 플래그를 추가합니다
        fun removeFlags(@Flags flags: Int): Builder // 플래그를 삭제합니다
    }

    /**
     * 광고 상품 유형
     */
    enum class ProductType {
        DILO("dilo"),                    // Audio 광고
        DILO_PLUS("dilo_plus"),          // Audio 또는 Audio + Companion 광고
        DILO_PLUS_ONLY("dilo_plus_only") // Audio + Companion 광고 (Companion이 무조건 포함)
    }

    /**
     * 광고 채우기 유형
     */
    enum class FillType {
        /**
         * 1개의 광고 요청 타입
         *      ※ Duration(drs) 은 6, 10, 15, 20 중 하나이어야 합니다.
         *       (다른 값으로 요청하면 "광고 없음(NoFill)" 처리됩니다)
         */
        SINGLE("single"),
        /**
         * 1개의 광고 요청 타입 (6, 10, 15, 20 초 광고중 랜덤)
         *      ※ Duration 은 무시됩니다
         */
        SINGLE_ANY("single_any"),
        /**
         * Duration 만큼 채우는 n 개의 광고 요청 타입
         *      ※ padding 기능 설정 여부에 따라 5 초 이하의 오차발생 가능합니다.
         */
        MULTI("multi")
    }

    // 광고 요청 시점 유형
    enum class AdPositionType {
        PRE("pre"),   // 컨텐츠 재생 시작 직전 지점
        MID("mid"),   // 컨텐츠 재생 50% 지점
        POST("post"); // 컨텐츠 재생 완료 직후 지점
    }

    companion object {
        const val FLAG_USE_PAUSE_IN_NOTIFICATION       = 0x00000001 // Notification에서 일시중지 / 재개 사용 플래그
        const val FLAG_USE_PROGRESSBAR_IN_NOTIFICATION = 0x00000002 // Notification에서 프로그래스 바 사용 플래그
    }
}
```

### [iv. 필수 파라미터 관련 예시](#목차)

필수 파라미터 `채널 이름` `에피소드 이름` `크리에이터 ID` `크리에이터 이름` 중 정보가 일부 없는 경우에는 아래와 같이 전달해 주시기 바랍니다

* 전달 정보 중 **개인정보**가 포함되는 경우 적절한 암호/해시화(BASE64, MD5, SHA1 등) 처리를 해서 전달해주시기 바랍니다

<table>
    <thead>
        <tr>
            <th><sub>전달 불가 파라미터</sub></th>
            <th><sub>대체 값</sub></th>
            <th><sub>채널 이름</sub></th>
            <th><sub>에피소드 이름</sub></th>
            <th><sub>크리에이터 ID</sub></th>
            <th><sub>크리에이터 이름</sub></th>
        </tr>
    </thead>
    <tbody>
        <tr>
            <td><sub>크리에이터 ID</sub></td>
            <td><sub>대체하거나 누락 불가</sub></td>
            <td align="center"><sub>O</sub></td>
            <td align="center"><sub>O</sub></td>
            <td align="center"><sub>O</sub></td>
            <td align="center"><sub>O</sub></td>
        </tr>
        <tr>
            <td><sub>크리에이터 이름</sub></td>
            <td><sub>크리에이터 ID</sub></td>
            <td align="center"><sub>O</sub></td>
            <td align="center"><sub>O</sub></td>
            <td align="center"><sub>O</sub></td>
            <td align="center"><sub>X</sub></td>
        </tr>
        <tr>
            <td><sub>채널 이름</sub></td>
            <td><sub>크리에이터 이름</sub></td>
            <td align="center"><sub>X</sub></td>
            <td align="center"><sub>O</sub></td>
            <td align="center"><sub>O</sub></td>
            <td align="center"><sub>O</sub></td>
        </tr>
        <tr>
            <td><sub>에피소드 이름</sub></td>
            <td><sub>채널 이름</sub></td>
            <td align="center"><sub>O</sub></td>
            <td align="center"><sub>X</sub></td>
            <td align="center"><sub>O</sub></td>
            <td align="center"><sub>O</sub></td>
        </tr>
        <tr>
            <td><sub>채널 이름<br>에피소드 이름</sub></td>
            <td><sub>크리에이터 이름</sub></td>
            <td align="center"><sub>X</sub></td>
            <td align="center"><sub>X</sub></td>
            <td align="center"><sub>O</sub></td>
            <td align="center"><sub>O</sub></td>
        </tr>
        <tr>
            <td colspan=10>컨텐츠(에피소드 재생)와 관련 없는 광고 요청 시 아래와 같이 전달해 주시기 바랍니다</td>
        </tr>
        <tr>
            <td><sub>크리에이터 ID, 크리에이터 이름</sub></td>
            <td><sub>Bundle ID</sub></td>
            <td align="center" rowspan=2><sub>O</sub></td>
            <td align="center" rowspan=2><sub>O</sub></td>
            <td align="center" rowspan=2><sub>X</sub></td>
            <td align="center" rowspan=2><sub>X</sub></td>
        </tr>
        <tr>
            <td><sub>채널 이름, 에피소드 이름</sub></td>
            <td><sub>동작 이름</sub></td>
        </tr>
    </tbody>
</table>

> 예시

<table>
    <thead>
        <tr>
            <th><sub>전달 불가 파라미터</sub></th>
            <th><sub>대체 값</sub></th>
            <th><sub>채널 이름</sub></th>
            <th><sub>에피소드 이름</sub></th>
            <th><sub>크리에이터 ID</sub></th>
            <th><sub>크리에이터 이름</sub></th>
        </tr>
    </thead>
    <tbody>
        <tr>
            <td><sub>크리에이터 ID</sub></td>
            <td><sub>대체하거나 누락 불가</sub></td>
            <td><sub>딜로 상담소</sub></td>
            <td><sub>비밀 상담 1회</sub></td>
            <td><sub><code>dilo1234</code></sub></td>
            <td><sub>BJ딜로</sub></td>
        </tr>
        <tr>
            <td><sub>크리에이터 이름</sub></td>
            <td><sub>크리에이터 ID</sub></td>
            <td><sub>딜로 상담소</sub></td>
            <td><sub>비밀 상담 1회</sub></td>
            <td><sub>dilo1234</sub></td>
            <td><sub><code>dilo1234</code></sub></td>
        </tr>
        <tr>
            <td><sub>채널 이름</sub></td>
            <td><sub>크리에이터 이름</sub></td>
            <td><sub><code>BJ딜로</code></sub></td>
            <td><sub>비밀 상담 1회</sub></td>
            <td><sub>dilo1234</sub></td>
            <td><sub>BJ딜로</sub></td>
        </tr>
        <tr>
            <td><sub>에피소드 이름</sub></td>
            <td><sub>채널 이름</sub></td>
            <td><sub>딜로 상담소</sub></td>
            <td><sub><code>딜로 상담소</code></sub></td>
            <td><sub>dilo1234</sub></td>
            <td><sub>BJ딜로</sub></td>
        </tr>
        <tr>
            <td><sub>채널 이름<br>에피소드 이름</sub></td>
            <td><sub>크리에이터 이름</sub></td>
            <td><sub><code>BJ딜로</code></sub></td>
            <td><sub><code>BJ딜로</code></sub></td>
            <td><sub>dilo1234</sub></td>
            <td><sub>BJ딜로</sub></td>
        </tr>
        <tr>
            <td colspan=10>컨텐츠(에피소드 재생)와 관련 없는 광고 요청 시 아래와 같이 전달해 주시기 바랍니다</td>
        </tr>
        <tr>
            <td><sub>크리에이터 ID,<br>크리에이터 이름</sub></td>
            <td><sub>Bundle ID</sub></td>
            <td rowspan=2><sub><code>앱실행</code></sub></td>
            <td rowspan=2><sub><code>앱실행</code></sub></td>
            <td rowspan=2><sub><code>kr.co.dilo.sample.app</code></sub></td>
            <td rowspan=2><sub><code>kr.co.dilo.sample.app</code></sub></td>
        </tr>
        <tr>
            <td><sub>채널 이름,<br>에피소드 이름</sub></td>
            <td><sub>동작 이름</sub></td>
        </tr>
    </tbody>
</table>



## [3. 광고 요청](#목차)

### [i. Class `AdManager`](#목차)

* 광고 요청 및 제어에 대한 전반적인 사항은 `AdManager` 클래스를 통해 수행합니다

> 동일한 메소드를 동시에 여러번 호출하는 것은 의도하지 않은 동작을 초래할 수 있습니다

```kotlin
class AdManager(private val context: Context) {
    
    val isPlaying: Boolean  // 광고가 재생중인지 여부를 반환합니다
        get() {}

    /**
     * 광고를 시작합니다
     *
     * ※ loadAd()로 광고를 요청한 다음
     *     DiloUtil.ACTION_ON_AD_READY 에서 호출하여야합니다
     */
    fun start()

    fun playOrPause() // 광고를 일시중지 또는 재개합니다

    /**
     * 광고를 Skip 합니다
     *     ※ 광고가 Skip 가능하지 않은 시점에 호출 시 무시됩니다
     *     ※ FillType.MULTI 광고이면 현재 재생중인 하나의 광고만 Skip됩니다
     */
    fun skip()

    fun release() // 광고를 종료하고 리소스를 해제합니다

    /**
     * 컴패니언을 리로드합니다
     * @param companionAdView Companion 뷰
     * @param closeButton 닫기 버튼
     *     ※ 현재 재생중인 광고의 ProductType이 hybrid가 아니거나
     *       서비스가 종료되었을 경우, companionAdView의 WindowVisibility가 
     *       View.VISIABLE이 아닌 경우 리로드 시 무시됩니다
     * @return 컴패니언 리로드 성공 여부      
     */
    fun reloadCompanion(companionAdView: AdView, closeButton: ViewGroup?): Boolean

    fun loadAd(requestParam: RequestParam) // 광고를 요청합니다
}
```

### [ii. 광고 요청 예시](#목차)

* App에서 원하는 광고 형태를 `RequestParam.Builder` 클래스를 통해 `RequestParam`에 설정한 후 `AdManager`
  의 `loadAd()`에 전달하여 광고를 요청합니다

```kotlin
class MyActivity : AppCompatActivity {

    override fun onCreate(savedInstanceState: Bundle?) {
        val adManager: AdManager = AdManager(this)

        // 30초를 채우는 n개의 audio(Companion 없는)광고 요청
        val requestParamBuilder: RequestParam.Builder = RequestParam.Builder(this).apply {
            // 필수 항목
            bundleId("com.queen.sampleapp")                // 패키지 설정
            epiCode("test_live")                           // 에피소드 코드 설정
            productType(RequestParam.ProductType.DILO)     // Audio 광고
            fillType(RequestParam.FillType.MULTI)          // n개의 광고
            drs(30)                                        // 30초
            adPositionType(RequestParam.AdPositionType.PRE)// 광고 재생 시점 설정 (컨텐츠 재생 전)
            channelName("딜로")                            // 채널 이름 설정
            episodeName("오디오 광고는 딜로")               // 에피소드 이름 설정
            creatorId("tester666")                         // 크리에이터 ID (식별자) 설정
            creatorName("테스터")                          // 크리에이터 이름 설정
            iconResourceId(R.drawable.notification_icon)   // Notification 아이콘 설정

            adManager.loadAd(requestParamBuilder.build())
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        val adManager: AdManager = AdManager(this)

        // 랜덤 시간 1개의 광고를 요청
        val requestParamBuilder: RequestParam.Builder = RequestParam.Builder(this).apply {
            // 필수 항목
            bundleId("com.queen.sampleapp")                      // 패키지 설정
            epiCode("test_live")                                 // 에피소드 코드 설정
            productType(RequestParam.ProductType.DILO_PLUS_ONLY) // Audio + Companion 광고
            fillType(RequestParam.FillType.SINGLE_ANY)           // 광고 시간 랜덤(6, 10, 15, 20초 중) 1개 광고
            drs(30)                                              // RequestParam.FillType.SINGLE_ANY 시 duration은 무시됩니다
            adPositionType(RequestParam.AdPositionType.POST)     // 광고 재생 시점 설정 (재생 후)
            channelName("딜로")                                  // 채널 이름 설정
            episodeName("오디오 광고는 딜로")                     // 에피소드 이름 설정
            creatorId("tester666")                               // 크리에이터 ID (식별자) 설정
            creatorName("테스터")                                // 크리에이터 이름 설정
            iconResourceId(R.drawable.notification_icon)         // Notification 아이콘 설정
            // 선택 항목
            companionAdView(companionAdView)                     // Companion View 설정 (Companion이 있는 광고가 나가려면 필수)
            closeButton(companionCloseButton)                    // 닫기 버튼 설정
            skipButton(skipButton)                               // Skip 버튼 설정
            notificationContentIntent(notificationIntent)        // Notification Click PendingIntent 설정
            // Notification 사용자 일시정지/재개 기능, 프로그레스 바 표시 설정
            setFlags(RequestParam.FLAG_USE_PAUSE_IN_NOTIFICATION or RequestParam.FLAG_USE_PROGRESSBAR_IN_NOTIFICATION)                           
            notificationContentTitle("크리에이터를 후원하는 광고 재생중입니다") // Notification Title 설정 (상단 텍스트)
            notificationContentText("ABC 뉴스")                              // Notification Text 설정 (하단 텍스트)

            adManager.loadAd(requestParamBuilder.build())
        }
    }
}
```

## [4. 광고 액션 수신](#목차)

* Dilo SDK에서 보내는 광고에 대한 액션 수신은 `BroadcastReceiver`를 통해 가능합니다
* 아래의 모든 액션은 `DiloUtil.DILO_INTENT_FILTER`에 등록되어 있으니 registerReceiver시 IntentFilter로 등록하거나 `
  DiloUtil.ACTION_`으로 시작하는 아래 액션에서 필요한 액션만 등록해서 사용하시면 됩니다
* 액션 목록은 아래와 같습니다

### [광고 액션](#목차)

액션<br>(prefix:DiloUtil.ACTION_)|설명|전달<br>데이터 클래스|비고
---|---|:---:|---
RELOAD_COMPANION|컴패니언 리로드| | Companion이 있는 광고에서 Companion이 노출됨(또는 노출해야 함)<br><br>**※비고** : Companion 광고를 노출/숨김 처리 하는 것은<br>`AdManager`를 초기화 하고 광고를 요청한 뷰에서는<br>자동으로 처리되지만,<br>Task Kill 등으로 뷰가 완전히 사라졌을 경우에는<br>`AdManager`를 다시 초기화 후에<br>BroadcastReceiver에서 이 액션을 받아 `AdManager`의<br>`reloadCompanion(AdView, ViewGroup)`을 호출하여<br>리로드하여야합니다
ON_COMPANION_CLOSED|사용자가 Companion을 닫음| |사용자가 닫기 버튼을 눌러 Companion을 닫음
ON_SKIP_ENABLED|광고 스킵 가능| |스킵 가능한 광고의 경우 스킵 가능한 시점 도달
ON_AD_SKIPPED|광고 스킵| | 사용자가 Skip 버튼을 눌러 광고를 Skip 또는<br>App에서`AdManager`의 `skip()` 메소드 호출
ON_NO_FILL|광고 없음| |요청에 맞는 조건의 광고가 없음
ON_AD_READY|광고 재생<br>준비 완료| | 광고가 로드되어 재생 준비가 완료됨
ON_AD_START|광고 재생 시작| [AdInfo](#i-class-adinfo)|광고 재생이 시작됨
ON_TIME_UPDATE|광고 진행 사항<br>업데이트| [Progress](#ii-class-progress)| 광고 진행사항이 업데이트 됨<br><br>**※비고** : 이 액션은 광고가 재생중일 때 200ms마다 호출됩니다
ON_AD_COMPLETED|광고 재생 완료| |하나의 광고가 재생 완료될 때마다 호출
ON_ALL_AD_COMPLETED|모든 광고<br>재생 완료| |모든 광고가 재생 완료되면 한 번 호출
ON_PAUSE|광고 일시 중지| |App에서 광고 재생 중 `AdManager`의 `playOrPause()` 호출<br>또는 사용자가 Notification에서 일시 중지 버튼 누름
ON_RESUME|광고 재개| |App에서 광고 일시 중지 중 `AdManager`의 `playOrPause()` 호출<br>또는 사용자가 Notification에서 재개 버튼 누름
ON_ERROR|에러 발생| [DiloError](#iii-class-diloerror)| 광고 요청/로드 또는 재생에 문제가 발생
ON_MESSAGE|메시지 전송|String| SDK에서 App으로 메시지를 전달
ON_SVC_DESTROYED|서비스 종료| | 딜로 SDK 서비스 종료

> ※ 유의 사항
> 1. `ON_AD_READY` 액션 발생 시 `AdManager`의 `start()` 메소드를 호출해야 광고가 시작되므로 이 액션은 항상 등록하시기 바랍니다
> 2. `ON_NO_FILL`, `ON_ERROR` 발생 시 다른 액션이 발생하지 않고 SDK가 종료되어 App의 컨텐츠를 재생해야하므로 이 액션은 항상 등록하시기 바랍니다
> 3. `ON_ALL_AD_COMPLETED` 또는 `ON_SVC_DESTROYED` 발생 시 App의 컨텐츠를 재생해야하므로 둘 중 한 액션은 항상 등록하시기 바랍니다
> 4. `ON_MESSAGE` 액션 발생 시 App으로 메시지를 전달하므로 개발하는 동안에는 등록하시기를 권고합니다, 메시지만을 전달하므로 이 액션 수신 시 광고 제어 메소드(AdManager의 메소드 play(), release() 등)를 호출하지 마시기 바랍니다
> 5. Companion 닫기 버튼(ViewGroup)의 클릭 시 이벤트 설정은 setOnClickListener가 아닌 `ON_COMPANION_CLOSED` 액션을 수신하여 처리하시기 바랍니다. 리스너의 설정은
   무시됩니다

### [광고 액션 수신 시퀀스 다이어그램](#목차)

일반적인 액션 시퀀스 다이어그램은 아래와 같습니다
> 광고 없음 / 에러
> <p align="center">
>     <img src="https://user-images.githubusercontent.com/73524723/120272718-7c630f00-c2e8-11eb-9be6-909552ed769d.png">
> </p>

> 일시중지/재개를 하는 경우
> <p align="center">
>     <img src="https://user-images.githubusercontent.com/73524723/120272725-7cfba580-c2e8-11eb-9652-72edfb4589d1.png">
> </p>

> 광고를 SKIP하는 경우
> <p align="center">
>     <img src="https://user-images.githubusercontent.com/73524723/120272728-7d943c00-c2e8-11eb-801c-3f0bc61edd4c.png">
> </p>

### [광고 액션 수신 예제](#목차)

```kotlin
class MyActivity : AppCompatActivity {

    private val adManager: adManager

    override fun onCreate(savedInstanceBundle: bundle?) {
        registerReceiver(diloActionReceiver, DiloUtil.DILO_INTENT_FILTER)

        adManager = AdManager(this)
        // 광고 요청 생략
    }

    override fun onDestroy() {
        unregisterReceiver(diloActionReceiver)
        super.onDestroy()
    }

    val diloActionReceiver: BroadcastReceiver = object : BroadcastReceiver() {

        override fun onReceive(context: Context, intent: Intent) {
            val action: String? = intent.action

            if (action != null) {
                when (action) {
                    // 컴패니언 리로드 액션
                    DiloUtil.ACTION_RELOAD_COMPANION ->
                        if (adManager?.reloadCompanion(companionAdView!!, companionCloseButton) == true) {
                            adWrapper?.visibility = View.VISIBLE
                        }

                    // 사용자의 컴패니언 닫기 액션
                    DiloUtil.ACTION_ON_COMPANION_CLOSED -> log("사용자가 컴패니언을 닫았습니다")

                    // 광고 준비 완료 액션
                    DiloUtil.ACTION_ON_AD_READY -> {
                        log("광고 준비 완료")
                        adManager?.start()
                        log("광고 재생")
                    }

                    // 광고 플레이 시작 액션
                    DiloUtil.ACTION_ON_AD_START -> {
                        skipOffset = 0L
                        val adInfo: AdInfo? = intent.getParcelableExtra(DiloUtil.EXTRA_AD_INFO)
                        
                        // [광고주] 광고이름
                        adTitle?.text = ("[${adInfo?.advertiserName}] ${adInfo?.title}")
                        adCount?.visibility = View.VISIBLE

                        log("========================================")
                        log("광고 정보")
                        log("타입     : ${adInfo?.type}")
                        log("광고주   : ${adInfo?.advertiserName}")
                        log("광고명   : ${adInfo?.title}")
                        log("길이     : ${adInfo?.duration}초")
                        log("광고 수  : ${adInfo?.currentOffset}/${adInfo?.totalCount}")
                        log("컴패니언 : ${if (adInfo?.hasCompanion == true) "있음" else "없음"}")
                        log("스킵 " + if (adInfo?.skipOffset != 0L) "가능 ${adInfo?.skipOffset?.toDouble().toTimeString()}" else "불가능")
                        log("========================================")
                        log("재생이 시작되었습니다")
                        skipOffset = adInfo?.skipOffset ?: 0L
                        if (adInfo?.skipOffset == 0L) {
                            skipButton?.text = "스킵 불가능"
                            skipButton?.visibility = View.VISIBLE
                        }
                        adWrapper?.visibility = View.VISIBLE
                    }

                    // 광고 재생 완료 액션 (각각의 광고 재생 완료마다 호출)
                    DiloUtil.ACTION_ON_AD_COMPLETED -> log("재생이 완료되었습니다")

                    // 모든 광고 재생 완료 액션 (가장 마지막 광고 재생 완료 시 한 번 호출)
                    DiloUtil.ACTION_ON_ALL_AD_COMPLETED -> log("모든 광고 재생이 완료되었습니다")

                    // 광고 일시 중지 액션
                    DiloUtil.ACTION_ON_PAUSE -> log("일시중지")

                    // 광고 재개 액션
                    DiloUtil.ACTION_ON_RESUME -> log("재개")

                    // 요청한 조건에 맞는 광고 없음 액션
                    DiloUtil.ACTION_ON_NO_FILL -> {
                        log("광고가 없습니다 (No Fill)")
                        adWrapper?.visibility = View.INVISIBLE
                        playContent()
                    }

                    // 스킵 가능한 광고일 때 스킵 가능 시점 도달 액션
                    DiloUtil.ACTION_ON_SKIP_ENABLED -> {
                        log("스킵 가능 시점 도달")
                        if (companionAdView?.visibility == View.VISIBLE) {
                            skipButton?.visibility = View.VISIBLE
                        }
                    }

                    // 에러 발생 액션
                    DiloUtil.ACTION_ON_ERROR -> {
                        val error: DiloError? = intent.getParcelableExtra(DiloUtil.EXTRA_ERROR)
                        log("광고 요청 중 에러가 발생하였습니다\n\t타입: ${error?.type}, 코드 :${error?.code}, 에러: ${error?.error}, 상세: ${error?.detail}")
                        if (error?.code != DiloError.CODE_TOO_MANY_REQUEST) {
                            playContent()
                        }
                    }

                    // 광고 진행 사항 업데이트 액션
                    DiloUtil.ACTION_ON_TIME_UPDATE -> {
                        val progress: Progress? = intent.getParcelableExtra(DiloUtil.EXTRA_PROGRESS)

                        val percent: Int = progress?.run {seconds * 100 / duration}?.toInt() ?: 0

                        progressBar?.progress = percent

                        currentTime?.text = progress?.seconds.toTimeString()
                        totalTime?.text = progress?.duration.toTimeString()
                        adCount?.text = ("광고 [${progress?.current}/${progress?.total}]")

                        if (skipOffset != 0L) {
                            var msg: String? = "건너뛰기"
                            val sec: Int = progress?.seconds?.toInt() ?: 0
                            if (skipOffset - sec > 0) {
                                msg = "${skipOffset - sec}초 후 건너뛰기"
                                skipButton?.visibility = View.VISIBLE
                            }
                            skipButton?.text = msg
                        }
                    }

                    // 사용자 광고 스킵 액션
                    DiloUtil.ACTION_ON_AD_SKIPPED -> log("사용자가 광고를 건너뛰었습니다")

                    // SDK로부터 메시지 수신
                    DiloUtil.ACTION_ON_MESSAGE -> {
                        val msg: String? = intent.getStringExtra(DiloUtil.EXTRA_MESSAGE)
                        log(msg!!)
                    }

                    // SDK 서비스 종료 액션
                    DiloUtil.ACTION_ON_SVC_DESTROYED -> {
                        log("딜로 SDK 서비스 종료")
                        skipButton?.visibility = View.GONE
                        adCount?.visibility = View.GONE
                        playContent()
                    }
                }
            }
        }
    };


}
```

## [5. 데이터 클래스 명세](#목차)

### [i. Class `AdInfo`](#목차)

광고 정보 클래스

```java
/**
 * App에 전달하는 광고 정보 클래스
 */
class AdInfo {
    public String type;           // 타입 (audio : companion 없는 광고 | hybrid : companion 있는 광고)
    public String advertiserName; // 광고주 이름
    public String title;          // 광고 명
    public int totalCount;        // 총 광고 갯수
    public int currentOffset;     // 현재 광고 오프셋 (1부터)
    public long duration;         // 광고 길이 (초)
    public long skipOffset;       // 스킵 오프셋 (초) ※ 스킵 불가능 : 0
    public boolean hasCompanion;  // companion 유무
}
```

`BroadcastReceiver.onReceive()`의 인텐트 액션이 `DiloUtil.ACTION_ON_AD_START`(광고 시작됨) 일때 `DiloUtil.EXTRA_AD_INFO` Name으로 가져오기

```kotlin
class MyActivity : AppCompatActivity {

    override fun onReceive(context: Context, intent: Intent) {
        when (intent.action) {
            DiloUtil.ACTION_ON_AD_START -> {
                val adInfo: AdInfo? = intent.getParcelableExtra(DiloUtil.EXTRA_AD_INFO)
                // ...
            }
        }
    }
}
```

### [ii. Class `Progress`](#목차)

광고 진행 정보 클래스

```java
/**
 * App에 전달할 광고 진행 정보 클래스
 */
class Progress {
    public int current;     // 현재 광고 오프셋 (1부터)
    public int total;       // 총 광고 갯수
    public double seconds;  // 현재 재생 시간 (초)
    public double duration; // 총 재생 시간 (초)
}
```

`BroadcastReceiver.onReceive()`의 인텐트 액션이 `DiloUtil.ACTION_ON_TIME_UPDATE`(광고 진행사항 업데이트) 일때 `DiloUtil.EXTRA_PROGRESS` Name으로 가져오기

```kotlin
class MyActivity : AppCompatActivity {

    override fun onReceive(context: Context, intent: Intent) {
        when(intent.action) {
            DiloUtil.ACTION_ON_TIME_UPDATE -> {
            val progress: Progress? = intent.getParcelableExtra(DiloUtil.EXTRA_PROGRESS)
            // ...
            }
        }
    }
}
```

### [iii. Class `DiloError`](#목차)

오류 정보 클래스

```java
/**
 * App에 전달할 오류 클래스
 */
public class DiloError {

    @ErrorType public String type;   // 에러 유형
    @ErrorCode public int code;      // 에러 코드
    public String error;             // 에러 메시지
    public String detail;            // 에러 메시지 상세
    
    // 에러 유형
    @StringDef({
            TYPE_REQUEST, // 딜로 광고 요청 에러 반환
            TYPE_MEDIA,   // error, detail에 Media Player 에러가 반환
            TYPE_NETWORK  // error, detail에 Volley 에러 반환
    })
    public @interface ErrorType {}
    
    // 에러 코드
    @IntDef({
            CODE_TOO_MANY_REQUEST, // 광고 요청 후 같은 Epi Code로 3초 동안 10회 이내에 추가 요청이 왔을 경우 반환
            CODE_MISSING_REQUIRED_PARAM_BUNDLE_ID,        // Bundle ID 누락
            CODE_MISSING_REQUIRED_PARAM_EPI_CODE,         // Epi Code 누락
            CODE_MISSING_REQUIRED_PARAM_PRODUCT_TYPE,     // Product Type 누락
            CODE_MISSING_REQUIRED_PARAM_FILL_TYPE,        // Fill Type 누락
            CODE_MISSING_REQUIRED_PARAM_AD_POSITION_TYPE, // Ad Position Type 누락
            CODE_MISSING_REQUIRED_PARAM_ICON_RESOURCE_ID, // Icon Resource ID (Notification Icon) 누락
            CODE_MISSING_REQUIRED_PARAM_DURATION,         // Duration 누락 또는 유효하지 않은 값(음수)
            CODE_MISSING_REQUIRED_PARAM_CHANNEL_NAME,     // Channel Name 누락
            CODE_MISSING_REQUIRED_PARAM_EPISODE_NAME,     // Episode Name 누락
            CODE_MISSING_REQUIRED_PARAM_CREATOR_NAME,     // Creator Name 누락
            CODE_MISSING_REQUIRED_PARAM_CREATOR_ID,       // Creator Id 누락
            CODE_MEDIA_ERROR // Media Player 오류
    })
    public @interface ErrorCode {}
}
```

`BroadcastReceiver.onReceive()`의 인텐트 액션이 `DiloUtil.ACTION_ON_ERROR`(오류 발생) 일때 `DiloUtil.EXTRA_ERROR` Name으로 가져오기

```kotlin
class MyActivity : AppCompatActivity {

    override fun onReceive(context: Context, intent: Intent) {
        when(intent.action) {
            DiloUtil.ACTION_ON_ERROR -> {
                val error: DiloError? = intent.getParcelableExtra(DiloUtil.EXTRA_ERROR)
                // ...
            }
        }
    }
}
```

### [iv. Class `DiloUtil`](#목차)

> 유틸성 변수/메소드 정의 클래스

```java
class DiloUtil {
    /**
     * 액션 정의
     */
    
    public static final String ACTION_RELOAD_COMPANION;    // 컴패니언 리로드 액션
    public static final String ACTION_ON_COMPANION_CLOSED; // 사용자의 컴패니언 닫기 액션
    public static final String ACTION_ON_SKIP_ENABLED;     // 광고 스킵 가능 액션
    public static final String ACTION_ON_AD_SKIPPED;       // 사용자의 광고 스킵 액션
    public static final String ACTION_ON_AD_COMPLETED;     // 광고 재생 완료 액션 (각각의 광고 재생 완료마다 호출)
    public static final String ACTION_ON_ALL_AD_COMPLETED; // 모든 광고 재생 완료 액션 (가장 마지막 광고 재생 완료 시 한 번 호출)
    public static final String ACTION_ON_AD_READY;         // 광고 재생 준비 완료 액션
    public static final String ACTION_ON_NO_FILL;          // 요청한 조건에 맞는 광고 없음 액션
    public static final String ACTION_ON_AD_START;         // 광고 재생 시작 액션
    public static final String ACTION_ON_TIME_UPDATE;      // 광고 진행 사항 업데이트 액션
    public static final String ACTION_ON_PAUSE;            // 광고 일시 중지 액션
    public static final String ACTION_ON_RESUME;           // 광고 재개 액션
    public static final String ACTION_ON_ERROR;            // 에러 발생 액션
    public static final String ACTION_ON_MESSAGE;          // 메시지 발생 액션
    public static final String ACTION_ON_SVC_DESTROYED;    // 딜로 서비스 종료 액션
    
    public static final IntentFilter DILO_INTENT_FILTER;   // 위의 액션들을 모두 등록해놓은 인텐트 필터

    /**
     * 데이터 가져오기위한 키 정의 
     */
    
    public static final String EXTRA_PROGRESS; // 광고 진행 정보
    public static final String EXTRA_ERROR;    // 에러
    public static final String EXTRA_AD_INFO;  // 광고 정보
    public static final String EXTRA_MESSAGE;  // 로그 정보

    public static String DILO_SDK_VERSION;     // 딜로 SDK 버전 eg) "0.6"
    public static String DILO_SDK_BUILD_DATE;  // 딜로 SDK 빌드 시간 eg) 2021/07/11 13:44:13
    public static String DILO_SDK_BUILD_TYPE;  // 딜로 SDK 빌드 타입 eg) release
}
```

## [6. Dilo SDK 동작](#목차)

### [i. Companion에 대한 동작](#목차)

1. Companion이 있는 광고 재생 시 자동으로 Companion View와 닫기 버튼을 **Visible** 처리합니다
2. Companion이 있는 광고가 끝나고 Companion이 없는(Audio만 재생되는) 광고 재생 시 자동으로 Companion View와 닫기 버튼을 **INVISIBLE** 처리합니다
3. Skip 가능한 광고의 경우 Skip 가능 시점에만 Skip 버튼을 **VISIBLE** 처리합니다

> ※ View를 처리하는 위의 경우 Companion View가 있는 Activity/Fragment가 Destory되면 자동으로 처리하지 못하니 BroadcastReceiver의 ACTION_RELOAD_COMPANION, ACTION_ON_SKIP_ENABLED 액션을 통해 처리해야합니다

4. 사용자가 Companion 클릭 시 Landing에 대한 처리가 **자동**으로 이루어집니다
5. 사용자가 Companion 내의 opt-out 클릭 시에 대한 처리가 **자동**으로 이루어집니다

### [ii. Tracking에 대한 동작](#목차)

* Dilo SDK에서는 아래와 같은 이벤트에 대하여 자동으로 Tracking합니다

이벤트|설명
---|---
START|광고가 시작되었을 때
IMPRESSION|광고가 시작되고 유효 시간이 지났을 때
FIRST_QUARTILE|광고가 1/4 재생되었을 때
MID_POINT|광고가 1/2 재생되었을 때
THIRD_QUARTILE|광고가 3/4 재생되었을 때
COMPLETE|광고가 끝까지 재생되었을 때
PROGRESS|광고에서 특정 시간이 지났을 때
SKIP|광고를 Skip 했을 때
ERROR|Error 가 발생했을 때
VIEW THROUGH|Companion이 노출되었을 때
CLICK|Companion을 클릭했을 때

### [iii. Audio Focus에 대한 동작](#목차)

* 광고 요청 시 Dilo SDK에서는 `AudioManager.AUDIOFOCUS_GAIN`으로 Audio Focus를 요청합니다
* 광고 종료 또는 에러 발생, 서비스 종료 시 Audio Focus를 반환합니다
* Audio Focus에 관하여 아래 표와 같이 동작합니다

Audio Focus<br>(prefix:AudioManager.AUDIOFOCUS_)|상태|예시|Dilo SDK 동작
---|---|---|---
LOSS|완전히 잃었을 때|다른 앱에서 오디오/비디오 재생|광고가 중지됩니다
LOSS_TRANSIENT|잠시 잃었을 때|통화|광고가 일시중지된 후<br>Audio Focus를 획득하면<br>다시 재생합니다
LOSS_TRANSIENT_CAN_DUCK|볼륨 감소 요청이 있었을 때| |볼륨을 반으로 줄여 재생합니다
GAIN|최초 포커스를 얻거나 다시 얻었을 때| |이전 볼륨으로 재생합니다

### [iv. Notification에 대한 동작](#목차)

> `RequestParam`의 `FLAG_USE_PAUSE_IN_NOTIFICATION` (사용자 일시 중지 허용) 플래그 지정 따른 Notification 동작에 대한 설명입니다

플래그 미 지정 시 (기본)

* Notification에 일시중지/재개 버튼이 **사라**집니다

<p align="center">
   <img src="https://user-images.githubusercontent.com/73524723/120162984-f2596e80-c233-11eb-815e-798fc9333fab.jpg" width=50%>
</p>

플래그 지정 시

* 사용자가 Notification에서 버튼을 눌러 Dilo광고를 **일시중지/재개 할 수** 있습니다


1. 광고가 재생중이어서 일시 중지할 수 있는 상태

<p align="center">
    <img src="https://user-images.githubusercontent.com/73524723/120162988-f2f20500-c233-11eb-8615-47a4429f550d.jpg" width=50%>
</p>

2. 광고가 일시 중지중이어서 재생할 수 있는 상태

<p align="center">
   <img src="https://user-images.githubusercontent.com/73524723/120162990-f38a9b80-c233-11eb-90f9-da165648acb5.jpg" width=50%>
</p>

> `RequestParam`의 `FLAG_USE_PROGRESSBAR_IN_NOTIFICATION` (광고 진행 프로그레스 바 표시) 플래그 지정 따른 Notification 동작에 대한 설명입니다

플래그 미 지정 시 (기본)

* Notification에 프로그레스 바가 **사라**집니다

<p align="center">
    <img src="https://user-images.githubusercontent.com/73524723/120162984-f2596e80-c233-11eb-815e-798fc9333fab.jpg" width=50%>
</p>

플래그 지정 시

* Notification에 프로그레스 바가 **표시**됩니다

<p align="center">
   <img src="https://user-images.githubusercontent.com/73524723/120159975-ba046100-c230-11eb-8004-b2a0d47bddef.jpg" width=50%>
</p>

### [v. Audio 재생에 대한 동작](#목차)

Dilo SDK에서는 광고 오디오 음원을 Service로 재생하여 App이 종료되어도 광고가 재생될 수 있도록 구현하였습니다

* Service는 광고가 모두 종료된 후 Destroy됩니다

하지만 아래와 같은 상황에서 Dilo SDK Service(광고 재생)가 Android 시스템에 의해 강제 종료될 수 있습니다
>
> 1. App의 "배터리 사용 관리" 설정의 백그라운드에서 실행이 꺼져있음
>
> <p align="center">
>     <img src="https://user-images.githubusercontent.com/73524723/113123501-9da77080-924f-11eb-8bcc-1e35f1ea15e6.jpg" width=40%>
> </p>
>
> 2. App의 "배터리 사용 관리" 설정의 배터리 사용량 최적화가 켜져있음
>
>
> <p align="center">
>     <img src="https://user-images.githubusercontent.com/73524723/113123504-9e400700-924f-11eb-9ec6-84e0a52dbacf.jpg" width="40%">
> </p>
>

## [7. 기타](#목차)

i. App 프로세스 종료 시 유의사항

> 만약 완전한 앱 종료를 위해 아래와 유사한 프로세스 종료 코드가 있다면
> ```kotlin
> class MainActivity {
>
>     override fun onDestroy() {
>       super.onDestroy()
>
>       adManager?.release()
>
>       // 종료코드
>       ActivityCompat.finishAffinity(this)
>       exitProcess(0) // kotlin
>       System.exit(0) // java, kotlin
>       Process.killProcess(Process.myPid()) // java, kotlin
> 
>     }
> }
> ```
> 딜로 SDK 명시적 release() 후 500~1000 ms 지연 시간 뒤에 종료코드를 실행하도록 작성하시기 바랍니다<br>
> 딜로 SDK가 종료되는 도중 프로세스가 종료되어 딜로 광고 Notification이 사라지지 않을 수 있습니다
>
> ```kotlin
> class MainActivity {
>
>    override fun onDestroy() {
>       super.onDestroy()
> 
>       adManager?.release()
> 
>       Handler(Looper.getMainLooper())
>           .postDelayed({
>               // 종료코드
>               ActivityCompat.finishAffinity(this)
>               exitProcess(0) // kotlin
>               System.exit(0) // java, kotlin
>               Process.killProcess(Process.myPid()) // java, kotlin
>           }, 1000)
>     } 
> }
> ```



## [문의](#목차)

> Dilo SDK 탑재 및 서비스 이용에 관한 문의는 [dilo@dilo.co.kr](mailto:dilo@dilo.co.kr)로 문의 주시기 바랍니다
