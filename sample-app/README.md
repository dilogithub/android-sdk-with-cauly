# Dilo Android SDK 연동 가이드

* 본 문서의 내용은 Sample App을 기반으로 작성하였습니다. 해당 App의 코드를 함께 참고하시기를 권고합니다

## 목차

* [개정 이력](#개정-이력)

1. [시작하기](#1-시작하기)
    * [Dilo SDK 추가](#dilo-sdk-추가)
    * [AndroidManifest.xml 속성 지정](#androidmanifestxml-속성-지정)
    

2. [광고 설정](#2-광고-설정)
    * [Companion 광고를 위한 레이아웃 설정 (옵션)](#i-companion-광고를-위한-레이아웃-설정-옵션)
    * [광고 Skip기능 제공을 위한 Button 할당 (옵션)](#ii-광고-skip기능-제공을-위한-button-할당-옵션)
    * [Class `RequestParam`](#iii-class-requestparam)
    

3. [광고 요청](#3-광고-요청)
    * [Class `AdManager`](#i-class-admanager)
    * [광고 요청 예시](#ii-광고-요청-예시)


4. [광고 액션 수신](#4-광고-액션-수신)
    * [광고 액션](#광고-액션)
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

### 0.5.4 - 2021/05/31

#### 추가

* `RequestParam`에 **Flag**를 지정하는 메소드가 추가되었습니다
    - `setFlags(int flags)`, `addFlags(int flags)`, `removeFlags(int flags)`
    - 현재 SDK 기능 및 앞으로 추가될 기능들의 사용 여부를 지정하는 데 사용됩니다
    - [여기에서](#iii-class-requestparam) `RequestParam.FLAG_`로 시작하는 Flag들을 참고하시기 바랍니다


* Notification에 광고 진행률을 보여주는 프로그레스 바 추가
    - 해당 기능은 기본으로 **disable** 상태이며 사용하기 위해 `RequestParam.Builder`의 Flag 지정 메소드를 사용하여 `RequestParam.FLAG_USE_PROGRESSBAR_IN_NOTIFICATION` Flag를 지정하시기 바랍니다
    - 자세한 내용은 [Notification에 대한 동작](#iv-notification에-대한-동작)을 참고바랍니다

#### 수정

* Notification 사용자 일시중지/재개 변경 버튼 활성화 기능 변경
    - `RequestParam.usePauseInNotification(boolean)`이 ***Deprecated*** 되었습니다
    - 더 이상 해당 메소드는 기능이 작동하지 않습니다 (0.6 버전에서 삭제될 예정입니다)
    - 해당 기능은 기본으로 **disable** 상태이며 사용하기 위해 `RequestParam.Builder`의 Flag 지정 메소드를 사용하여 `RequestParam.FLAG_USE_PAUSE_IN_NOTIFICATION` Flag를 지정하시기 바랍니다
    

* `DiloUtil`에 정의된 Intent에서 Extra 데이터를 가져오기 위한 KEY 상수 이름이 수정되었습니다
    - DiloUtil.INTENT_KEY_로 시작하는 상수는 호환성을 위해 현재 버전에서는 유지되며 0.6 버전에서 삭제될 예정입니다
    
|기존|변경|
|----|----|
|INTENT_KEY_PROGRESS|EXTRA_PROGRESS|
|INTENT_KEY_ERROR|EXTRA_ERROR|
|INTENT_KEY_AD_INFO|EXTRA_AD_INFO|
|INTENT_KEY_MESSAGE|EXTRA_MESSAGE|


* 광고 액션 변경 사항
    - 3초 내에 같은 epiCode로 광고를 요청하면 `ON_ERROR`를 Broadcast한 후 요청을 무시합니다
    - SKIP에 대한 액션 전달
        * 변경 전 : SKIP 시 `ON_AD_SKIPPED` 액션 전달
        * 변경 후 : SKIP 시 `ON_AD_SKIPPED` 액션 전달 후 바로 `ON_AD_COMPLETED`(광고 재생 완료) 액션 전달
    

* Companion 리로드 기능
    - 사용자가 닫기 버튼을 눌러 Companion을 닫았거나, Skip 버튼을 눌러 건너뛰었거나, SDK가 광고 재생을 마쳤을 경우 `AdManager.reloadCompanion()` 호출 시 Companion을 리로드 하지 않고 무시합니다

---

### 0.5.3 - 2021/05/12

#### 추가

* 광고 액션에 `ON_COMPANION_CLOSED` 추가
    - 이 액션은 사용자가 Companion 닫기 버튼(ViewGroup)을 눌렀을 때 발생합니다

#### 수정

* 가이드 내 용어 변경
    - 매체사 -> App

---

### 0.5.2 - 2021/05/10

#### 추가

* RequestParam 클래스
    - enum `AdPositionType` 추가
    - RequestParam 클래스에 ***필수*** 값 추가
        - `adPositionType`, `channelName`, `episodeName`, `creatorName`, `creatorId`
* 광고 액션에 `ON_MESSAGE` 추가
    - SDK에서 App에 개발 시 참고할만한 메시지를 전송할 때 발생합니다
    - 개발 도중에는 이 액션을 수신하는 것을 권고합니다
* `DiloError` 클래스에 새로운 에러 유형 `REQUEST`추가

#### 수정

* RequestParam 클래스
    - `epicode`, `bundleId` **URL 인코딩** 처리
    - ***필수*** 값 Validation 처리 (Validation 실패 시 광고 액션 `ON_ERROR`에서 메시지 확인 가능)
* `DiloError` 클래스 패키지 이동 kr.co.dilo.sdk -> kr.co.dilo.sdk.model
* 광고가 재생중일 때 `AdManager.start()` 중복 호출 처리
* 컴패니언 노출 개선

---

### 0.5.1 - 2021/04/15

#### 수정

* Skip 버튼 null 설정 시 오류 처리

---

### 0.5 - 2021/04/01

최초 작성

---

## [1. 시작하기](#목차)

### [Dilo SDK 추가](#목차)

* 최상위 level `build.gradle`에 maven repository 추가

```
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

```
dependencies {    
    ...
    implementation 'kr.co.dilo:dilo-sdk:0.5.4'
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

* App에서 Companion(Image)을 포함하는 광고 노출을 원하는 경우 Companion이 노출될 레이아웃(kr.co.dilo.sdk.AdView)을 선언합니다.
* Companion 닫기 버튼을 제공할 경우, 해당 레이아웃(ViewGroup)을 포함하여 할당합니다.

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

```java
import androidx.annotation.Nullable;

class RequestParam {
    static class Builder {
        ///////////////////////
        // 필수 사항
        ///////////////////////
        // 누락 또는 유효하지 않은 값 입력 시 ACTION_ON_ERROR를 Broadcast
        ///////////////////////

        /**
         * 번들 ID(패키지 이름)를 설정합니다
         *      ※ 광고요청 전 DILO 시스템에 등록되어야 합니다
         *      ※ 등록되어있지 않으면 항상 NO-FILL 응답을 리턴합니다    
         *      ※ 테스트 시에는 "com.queen.sampleapp"를 지정하고 NO-FILL 응답 시 문의 메일 부탁드립니다
         *      ※ URL 인코딩되어있지 않아야 합니다 (딜로 SDK 내부에서 처리합니다)
         */
        public Builder bundleId(@NonNull String bundleId);

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
        public Builder epiCode(@NonNull String epiCode);

        /**
         * 에피소드의 채널 이름을 설정합니다
         * 광고 요청한 에피소드가 속해있는 채널 이름
         * 추후 리포트에 반영되는 값으로 채널 이름이 없을 경우 리포트에 보여질 임의값으로 설정합니다
         *     ex) "매일 아침 9시 세계의 라디오 세상", "앱 시작 광고" ...
         */
        public Builder channelName(@NonNull String channelName);

        /**
         * 에피소드 타이틀을 설정합니다
         * 추후 리포트에 반영되는 값으로 에피소드 타이틀이 없을 경우 리포트에 보여질 임의값 설정
         *     ex) "전세계 확진자 1억 명 임박…되돌아본 코로나19 1년 6개월", 
         */
        public Builder episodeName(@NonNull String episodeName);

        /**
         * 에피소드의 창작자 ID(중복되지 않는 고유 식별값)를 설정합니다
         * 매체사에서 창작자를 식별할 수 있는 값
         *     ex) "user#0001", "12391", "user@dilo.co.kr", "F8CA-9283-AFB1C" ...
         * 중복될 경우 같은 창작자로 인식하여 리포트가 집계됩니다
         *     ※ 만약 닉네임같이 변경 가능한 값을 설정할 경우, 변경 시 기존과 다른 창작자로 인식하여 리포트가 다르게 집계됩니다
         *
         */
        public Builder creatorId(@NonNull String creatorId);

        /**
         * 광고 요청한 에피소드의 창작자 이름을 설정합니다
         * 추후 리포트에 반영되는 값으로 창작자 이름이 없을 경우 리포트에 보여질 임의값 설정
         */
        public Builder creatorName(@NonNull String creatorName);

        /**
         * 광고 요청 길이를 설정합니다 (초)
         */
        public Builder drs(@IntRange(from = 0) int duration);

        /**
         * 광고 상품 유형을 설정합니다
         * 아래 enum ProductType 참고
         */
        public Builder productType(@NonNull ProductType productType);

        /**
         * 광고 채우기 유형을 설정합니다
         * 아래 enum FillType 참고
         */
        public Builder fillType(@NonNull FillType fillType);

        /**
         * 광고가 컨텐츠에서 삽입된 위치 타입을 설정합니다
         * 아래 enum AdPositionType 참고
         * 광고 요청 시점에따라 설정하여야합니다
         *      PRE : 컨텐츠(에피소드) 재생 전
         *      MID : 컨텐츠(에피소드) 50% 재생 후
         *     POST : 컨텐츠(에피소드) 재생 후
         */
        public public Builder adPositionType(@NonNull AdPositionType adPositionType);

        /**
         * Notification에 보여질 아이콘을 설정합니다
         */
        public Builder iconResourceId(@DrawableRes int iconResourceId);

        ///////////////////////
        // 선택 사항
        ///////////////////////

        /**
         * Companion이 할당된 사이즈를 설정합니다
         * 설정하지 않으면 자동으로 계산된 사이즈가 들어갑니다
         *     ※ 수동으로 설정할 경우 0보다 큰 값이 들어가야합니다 (단위 : pixel)
         */
        public Builder companionSize(@IntRange(from = 1) int width, @IntRange(from = 1) int height);

        /**
         * Companion이 보여질 뷰를 설정합니다
         *     ※ 컴패니언이 있는 광고 (ProductType = DILO_PLUS || DILO_PLUS_ONLY) 요청에
         *       이 항목을 설정하지 않으면 컴패니언 없는 광고(DILO)가 나갈 수 있습니다
         */
        public Builder companionAdView(@Nullable AdView companionAdView);

        /**
         * 광고 Close 버튼 뷰를 설정합니다
         */
        public Builder closeButton(@Nullable ViewGroup closeButton);

        /**
         * 광고 Skip 버튼을 설정합니다
         */
        public Builder skipButton(@Nullable Button skipButton);

        /**
         * Notification에서 사용자가 광고를 일시중지/재개 할 수 있는 기능을 제공합니다 (일시중지/재개 버튼이 표현됨)
         *   ※ 0.5.4버전부터 이 메소드는 더이상 사용되지 않습니다
         *     기존의 usePauseInNotification(true) 기능을 사용하려면 
         *     Flag 지정 메소드로 RequestParam.FLAG_USE_PAUSE_IN_NOTIFICATION 를 지정하십시오
         */
        @Deprecated
        public Builder usePauseInNotification(boolean usePauseInNotification);

        /**
         * Notification 클릭 시 수행할 PendingIntent를 설정합니다
         *     ※ App의 컨텐츠 재생 화면 (광고 재생 화면)으로 설정하는 것을 권고드립니다
         */
        public Builder notificationContentIntent(@Nullable PendingIntent intent);

        /**
         * Notification의 타이틀 문구를 설정합니다
         */
        public Builder notificationContentTitle(@Nullable String notificationContentTitle);

        /**
         * Notification의 텍스트 문구를 설정합니다
         */
        public Builder notificationContentText(@Nullable String notificationContentText);

        /**
         * 플래그를 지정합니다
         */
        public Builder setFlags(@Flags int flags);

        /**
         * 플래그를 추가합니다
         */
        public Builder addFlags(@Flags int flags);

        /**
         * 플래그를 삭제합니다
         */
        public Builder removeFlags(@Flags int flags);
    }

    /**
     * 광고 상품 유형
     */
    enum ProductType {
        /**
         * Audio 광고
         */
        DILO("dilo"),
        /**
         * Audio 또는 Audio + Companion 광고
         */
        DILO_PLUS("dilo_plus"),
        /**
         * Audio + Companion 광고 (Companion이 무조건 포함)
         */
        DILO_PLUS_ONLY("dilo_plus_only")
    }

    /**
     * 광고 채우기 유형
     */
    enum FillType {
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

    /**
     * 광고 요청 시점 유형
     */
    public enum AdPositionType {
        /**
         * 컨텐츠 재생 시작 직전 지점
         */
        PRE("pre"),
        /**
         * 컨텐츠 재생 50% 지점
         */
        MID("mid"),
        /**
         * 컨텐츠 재생 완료 직후 지점
         */
        POST("post");
    }

    /**
     * Notification에서 일시중지 / 재개 사용 플래그
     */
    public static final int FLAG_USE_PAUSE_IN_NOTIFICATION;
    /**
     * Notification에서 프로그래스 바 사용 플래그
     */
    public static final int FLAG_USE_PROGRESSBAR_IN_NOTIFICATION;
}
```

## [3. 광고 요청](#목차)

### [i. Class `AdManager`](#목차)

* 광고 요청 및 제어에 대한 전반적인 사항은 `AdManager` 클래스를 통해 수행합니다

> 동일한 메소드를 동시에 여러번 호출하는 것은 의도하지 않은 동작을 초래할 수 있습니다

```java
class AdManager {
    /**
     * AdManager를 초기화합니다 (Constructor)
     * @param context 컨텍스트
     */
    public AdManager(@NonNull Context context);

    /**
     * 광고가 재생중인지 여부를 반환합니다
     * @return 광고 재생 여부
     */
    public boolean isPlaying();

    /**
     * 광고를 시작합니다
     *
     * ※ loadAd()로 광고를 요청한 다음
     *     DiloUtil.ACTION_ON_AD_READY 에서 호출하여야합니다
     */
    public void start();

    /**
     * 광고를 일시중지 또는 재개합니다
     */
    public void playOrPause();

    /**
     * 광고를 Skip 합니다
     *     ※ 광고가 Skip 가능하지 않은 시점에 호출 시 무시됩니다
     *     ※ FillType.MULTI 광고이면 현재 재생중인 하나의 광고만 Skip됩니다
     */
    public void skip();

    /**
     * 광고를 종료하고 리소스를 해제합니다
     */
    public void release();

    /**
     * 컴패니언을 리로드합니다
     * @param companionAdView Companion 뷰
     * @param closeButton 닫기 버튼
     *     ※ 현재 재생중인 광고의 ProductType이 hybrid가 아니거나
     *       서비스가 종료되었을 경우 리로드 시 흰 화면이 보여집니다
     */
    public void reloadCompanion(@NonNull AdView companionAdView, @Nullable ViewGroup closeButton);

    /**
     * 광고를 요청합니다
     * @param requestParam 요청 파라미터
     */
    public void loadAd(@NonNull RequestParam requestParam);
}
```

### [ii. 광고 요청 예시](#목차)

* App에서 원하는 광고 형태를 `RequestParam.Builder` 클래스를 통해 `RequestParam`에 설정한 후 `AdManager`
  의 `loadAd()`에 전달하여 광고를 요청합니다

```java
class MyActivity extends AppCompatActivity {

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        AdManager adManager = new AdManager(this);

        // 30초를 채우는 n개의 audio(Companion 없는)광고 요청
        requestParamBuilder =
            new RequestParam.Builder(this)
                // 필수 항목
                .bundleId("com.queen.sampleapp")                // 패키지 설정
                .epiCode("test_live")                           // 에피소드 코드 설정
                .productType(RequestParam.ProductType.DILO)     // Audio 광고
                .fillType(RequestParam.FillType.MULTI)          // n개의 광고
                .drs(30)                                        // 30초
                .adPositionType(RequestParam.AdPositionType.PRE)// 광고 재생 시점 설정 (컨텐츠 재생 전)
                .channelName("딜로")                            // 채널 이름 설정
                .episodeName("오디오 광고는 딜로")              // 에피소드 이름 설정
                .creatorId("tester666")                         // 크리에이터 ID (식별자) 설정
                .creatorName("테스터")                          // 크리에이터 이름 설정
                .iconResourceId(R.drawable.notification_icon);  // Notification 아이콘 설정

        adManager.loadAd(requestParamBuilder.build());

    }

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        AdManager adManager = new AdManager(this);

        // 랜덤 시간 1개의 광고를 요청
        requestParamBuilder =
            new RequestParam.Builder(this)
                // 필수 항목
                .bundleId("com.queen.sampleapp")                      // 패키지 설정
                .epiCode("test_live")                                 // 에피소드 코드 설정
                .productType(RequestParam.ProductType.DILO_PLUS_ONLY) // Audio + Companion 광고
                .fillType(RequestParam.FillType.SINGLE_ANY)           // 광고 시간 랜덤(6, 10, 15, 20초 중) 1개 광고
                .drs(30)                                              // RequestParam.FillType.SINGLE_ANY 시 duration은 무시됩니다
                .adPositionType(RequestParam.AdPositionType.POST)     // 광고 재생 시점 설정 (재생 후)
                .channelName("딜로")                                  // 채널 이름 설정
                .episodeName("오디오 광고는 딜로")                    // 에피소드 이름 설정
                .creatorId("tester666")                               // 크리에이터 ID (식별자) 설정
                .creatorName("테스터")                                // 크리에이터 이름 설정
                .iconResourceId(R.drawable.notification_icon)         // Notification 아이콘 설정
                // 선택 항목
                .companionAdView(companionAdView)                     // Companion View 설정 (Companion이 있는 광고가 나가려면 필수)
                .closeButton(companionCloseButton)                    // 닫기 버튼 설정
                .skipButton(skipButton)                               // Skip 버튼 설정
                .notificationContentIntent(notificationIntent)        // Notification Click PendingIntent 설정
                .setFlags(RequestParam.FLAG_USE_PAUSE_IN_NOTIFICATION // Notification 사용자 일시정지/재개 기능, 프로그레스 바 표시 설정
                    | RequestParam.FLAG_USE_PROGRESSBAR_IN_NOTIFICATION)                         
                .notificationContentTitle("크리에이터를 후원하는 광고 재생중입니다") // Notification Title 설정 (상단 텍스트)
                .notificationContentText("ABC 뉴스");                                // Notification Text 설정 (하단 텍스트)

        adManager.loadAd(requestParamBuilder.build());
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
RELOAD_COMPANION|컴패니언 리로드| | Companion이 있는 광고에서 Companion이 노출됨(또는 노출해야 함)<br><br>**※ 비고** : Companion 광고를 노출/숨김 처리 하는 것은<br>`AdManager`를 초기화 하고 광고를 요청한 뷰에서는<br>자동으로 처리되지만,<br>Task Kill 등으로 뷰가 완전히 사라졌을 경우에는<br>` AdManager`를 다시 초기화 후에<br>BroadcastReceiver에서 이 액션을 받아 `AdManager`의<br>`reloadCompanion(AdView, ViewGroup)`을 호출하여<br>리로드하여야합니다
ON_COMPANION_CLOSED|사용자가 Companion을 닫음| |사용자가 닫기 버튼을 눌러 Companion을 닫음
ON_SKIP_ENABLED|광고 스킵 가능| |스킵 가능한 광고의 경우 스킵 가능한 시점 도달
ON_AD_SKIPPED|광고 스킵| | 사용자가 Skip 버튼을 눌러 광고를 Skip 또는<br>App에서`AdManager`의 `skip()` 메소드 호출
ON_NO_FILL|광고 없음| |요청에 맞는 조건의 광고가 없음
ON_AD_READY|광고 재생<br>준비 완료| | 광고가 로드되어 재생 준비가 완료됨
ON_AD_START|광고 재생 시작| [AdInfo](#i-class-adinfo)|광고 재생이 시작됨
ON_TIME_UPDATE|광고 진행 사항<br>업데이트| [Progress](#ii-class-progress)| 광고 진행사항이 업데이트 됨<br><br>**※ 비고** : 이 액션은 광고가 재생중일 때 200ms마다 호출됩니다
ON_AD_COMPLETED|광고 재생 완료| |하나의 광고가 재생 완료될 때마다 호출
ON_ALL_AD_COMPLETED|모든 광고<br>재생 완료| |모든 광고가 재생 완료되면 한 번 호출
ON_PAUSE|광고 일시 중지| |App에서 광고 재생 중 `AdManager`의 `playOrPause()` 호출<br>또는 사용자가 Notification에서 일시 중지 버튼 누름
ON_RESUME|광고 재개| |App에서 광고 일시 중지 중 `AdManager`의 `playOrPause()` 호출<br>또는 사용자가 Notification에서 재개 버튼 누름
ON_ERROR|에러 발생| [DiloError](#iii-class-diloerror)| 광고 요청/로드 또는 재생에 문제가 발생
ON_MESSAGE|메시지 전송|String| SDK에서 App으로 메시지를 전달
ON_SVC_DESTROYED|서비스 종료| | 딜로 SDK 서비스 종료

※ 유의 사항

1. `ON_AD_READY` 액션 발생 시 `AdManager`의 `start()` 메소드를 호출해야 광고가 시작되므로 이 액션은 항상 등록하시기
   바랍니다
2. `ON_NO_FILL`, `ON_ERROR ACTION` 발생 시 다른 액션이 발생하지 않고 SDK가 종료되어 App의 컨텐츠를 재생해야하므로 이 액션은
   항상 등록하시기 바랍니다
3. `ON_ALL_AD_COMPLETED` 또는 `ON_SVC_DESTROYED` 발생 시 App의 컨텐츠를 재생해야하므로 둘 중 한 액션은 항상 등록하시기
   바랍니다
4. `ON_MESSAGE` 액션 발생 시 App으로 메시지를 전달하므로 개발하는 동안에는 등록하시기를 권고드립니다, 메시지만을 전달하므로 이 액션 수신 시 광고 제어 메소드(AdManager의 메소드 play(), release() 등)를 호출하지 마시기 바랍니다
5. Companion 닫기 버튼(ViewGroup)의 클릭 시 이벤트 설정은 setOnClickListener가 아닌 `ON_COMPANION_CLOSED` 액션을 수신하여 처리하시기
   바랍니다. 리스너의 설정은 무시됩니다

### [광고 액션 수신 예제](#목차)

```java
class MyActivity extends AppCompatActivity {

    private AdManager adManager;

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        registerReceiver(diloActionReceiver, DiloUtil.DILO_INTENT_FILTER);

        adManager = new AdManager(this);
        // 광고 요청 생략
    }

    @Override
    public void onDestroy() {
        unregisterReceiver(diloActionReceiver);
        super.onDestroy();
    }

    BroadcastReceiver diloActionReceiver = new BroadcastReceiver() {

        @Override
        public void onReceive(Context context, Intent intent) {
            if (intent != null) {
                String action = intent.getAction();
                if (action != null) {
                    switch (action) {
                        // 컴패니언 리로드 액션
                        case DiloUtil.ACTION_RELOAD_COMPANION:
                            if (adManager != null) {
                                adWrapper.setVisibility(View.VISIBLE);
                                adManager.reloadCompanion(companionAdView, companionCloseButton);
                            }
                            break;

                        // 사용자의 컴패니언 닫기 액션
                        case DiloUtil.ACTION_ON_COMPANION_CLOSED:
                            log("사용자가 컴패니언을 닫았습니다");
                            break;

                        // 광고 준비 완료 액션
                        case DiloUtil.ACTION_ON_AD_READY:
                            log("광고 준비 완료");
                            // 광고 시작
                            adManager.start();
                            log("광고 재생");
                            break;

                        // 광고 플레이 시작 액션
                        case DiloUtil.ACTION_ON_AD_START:
                            AdInfo adInfo = (AdInfo) intent.getSerializableExtra(DiloUtil.EXTRA_AD_INFO);
                            log("========================================");
                            log("광고 정보");
                            log(String.format("타입     : %s", adInfo.type));
                            log(String.format("광고주   : %s", adInfo.advertiserName));
                            log(String.format("광고명   : %s", adInfo.title));
                            log(String.format("길이     : %d초", adInfo.duration));
                            log(String.format("광고 수  : %d/%d", adInfo.currentOffset, adInfo.totalCount));
                            log(String.format("컴패니언 : %s", adInfo.hasCompanion ? "있음" : "없음"));
                            log(String.format("스킵 %s", adInfo.skipOffset != 0 ? "가능 " + DiloSampleAppUtil.secondsToTimeString(adInfo.skipOffset) : "불가능"));
                            log("========================================");
                            log("재생이 시작되었습니다");
                            break;

                        // 광고 재생 완료 액션 (각각의 광고 재생 완료마다 호출)
                        case DiloUtil.ACTION_ON_AD_COMPLETED:
                            log("재생이 완료되었습니다");
                            break;

                        // 모든 광고 재생 완료 액션 (가장 마지막 광고 재생 완료 시 한 번 호출)
                        case DiloUtil.ACTION_ON_ALL_AD_COMPLETED:
                            log("모든 광고 재생이 완료되었습니다");
                            break;

                        // 광고 일시 중지 액션
                        case DiloUtil.ACTION_ON_PAUSE:
                            log("일시중지");
                            break;

                        // 광고 재개 액션
                        case DiloUtil.ACTION_ON_RESUME:
                            log("재개");
                            break;

                        // 요청한 조건에 맞는 광고 없음 액션
                        case DiloUtil.ACTION_ON_NO_FILL:
                            log("광고가 없습니다 (No Fill)");
                            adWrapper.setVisibility(View.INVISIBLE);
                            adInfoWrapper.setVisibility(View.INVISIBLE);
                            playContent();
                            break;

                        // 에러 발생 액션
                        case DiloUtil.ACTION_ON_ERROR:
                            DiloError error = (DiloError) intent.getSerializableExtra(DiloUtil.EXTRA_ERROR);
                            log(String.format("광고 요청 중 에러가 발생하였습니다\n\t타입: %s, 에러: %s, 상세: %s", error.type, error.error, error.detail));
                            playContent();
                            break;

                        // 광고 진행 사항 업데이트 액션
                        case DiloUtil.ACTION_ON_TIME_UPDATE:
                            Progress progress = (Progress) intent.getSerializableExtra(DiloUtil.EXTRA_PROGRESS);

                            int percent = (int) (progress.seconds * 100 / progress.duration);
                            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.N) {
                                progressBar.setProgress(percent, false);
                            } else {
                                progressBar.setProgress(percent);
                            }
                            break;

                        // 사용자 광고 스킵 액션
                        case DiloUtil.ACTION_ON_AD_SKIPPED:
                            log("사용자가 광고를 건너뛰었습니다");
                            break;

                        // SDK로부터 메시지 수신
                        case DiloUtil.ACTION_ON_MESSAGE:
                            String msg = intent.getStringExtra(DiloUtil.EXTRA_MESSAGE);
                            log(msg);

                        case DiloUtil.ACTION_ON_SVC_DESTROYED:
                            log("딜로 SDK 서비스 종료");
                            break;
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

`BroadcastReceiver`의 `onReceive`의 `intent.getAction()==DiloUtil.ACTION_ON_AD_START`(광고
시작됨) 에서<br>
`DiloUtil.EXTRA_AD_INFO` Key로 가져온 후 Cast<br>

```java
class MyActivity extends AppCompatActivity {

    @Override
    public void onReceive(Context context, Intent intent) {
        switch (intent.getAction()) {
            case DiloUtil.ACTION_ON_AD_START:
                AdInfo adInfo = (AdInfo) intent.getSerializableExtra(DiloUtil.EXTRA_AD_INFO);
                // ...
                break;
        }
    }
}
```

```java
/**
 * App에 전달하는 광고 정보 클래스
 */
class AdInfo implements Serializable {
    /**
     * 타입 (audio : companion 없는 광고 | hybrid : companion 있는 광고)
     */
    public String type;
    /**
     * 광고주 이름
     */
    public String advertiserName;
    /**
     * 광고명
     */
    public String title;
    /**
     * 총 광고 갯수
     */
    public int totalCount;
    /**
     * 현재 광고 오프셋 (1부터)
     */
    public int currentOffset;
    /**
     * 광고 컨텐츠 시간 (초)
     */
    public long duration;
    /**
     * 스킵 오프셋 (초)
     *      ※ 스킵 불가능 : 0
     */
    public long skipOffset;
    /**
     * 컴패니언 유무
     */
    public boolean hasCompanion;
}
```

### [ii. Class `Progress`](#목차)

광고 진행 정보 클래스

`BroadcastReceiver`의 `onReceive`의 `intent.getAction()==DiloUtil.ACTION_ON_TIME_UPDATE`(
광고 진행사항 업데이트) 에서<br>
`DiloUtil.EXTRA_PROGRESS` Key로 가져온 후 Cast

```java
class MyActivity extends AppCompatActivity {

    @Override
    public void onReceive(Context context, Intent intent) {
        switch (intent.getAction()) {
            case DiloUtil.ACTION_ON_TIME_UPDATE:
                Progress progress = (Progress) intent.getSerializableExtra(DiloUtil.EXTRA_PROGRESS);
                // ...
                break;
        }
    }
}
```

```java
/**
 * App에 전달할 광고 진행 정보 클래스
 */
class Progress implements Serializable {
    /**
     * 현재 광고 오프셋 (1부터)
     */
    public int current;
    /**
     * 총 광고 갯수
     */
    public int total;
    /**
     * 현재 재생 시간 (초)
     */
    public double seconds;
    /**
     * 총 재생 시간 (초)
     */
    public double duration;
}
```

### [iii. Class `DiloError`](#목차)

오류 정보 클래스

`BroadcastReceiver`의 `onReceive`의 `intent.getAction()==DiloUtil.ACTION_ON_ERROR`(오류 발생)
에서<br>
`DiloUtil.EXTRA_ERROR` Key로 가져온 후 Cast<br>

```java
class MyActivity extends AppCompatActivity {

    @Override
    public void onReceive(Context context, Intent intent) {
        switch (intent.getAction()) {
            case DiloUtil.ACTION_ON_ERROR:
                DiloError error = (DiloError) intent.getSerializableExtra(DiloUtil.EXTRA_ERROR);
                // ...
                break;
        }
    }
}
```

```java
/**
 * App에 전달할 오류 클래스
 */
public class DiloError extends Exception {

    @Retention(RetentionPolicy.SOURCE)
    @StringDef({REQUEST, MEDIA, NETWORK})
    public @interface ErrorType {
    }

    /**
     * 에러 유형 REQUEST
     *  딜로 광고 요청 에러가 반환됩니다
     */
    public final static String REQUEST = "REQUEST";
    /**
     * 에러 유형 MEDIA
     *  error, detail에 MediaPlayer의 에러가 반환됩니다
     */
    public final static String MEDIA = "MEDIA";
    /**
     * 에러 유형 NETWORK
     *  error, detail에 Volley의 에러가 반환됩니다
     */
    public final static String NETWORK = "NETWORK";

    /**
     * 에러 유형
     */
    @ErrorType
    public String type;

    /**
     * 에러
     */
    public String error;

    /**
     * 상세
     */
    public String detail;
}
```

### [iv. Class `DiloUtil`](#목차)

> 유틸성 변수/메소드 정의 클래스

```java
class DiloUtil {
    // 액션 정의
    /**
     * 컴패니언 리로드 액션
     */
    public static final String ACTION_RELOAD_COMPANION;
    /**
     * 사용자의 컴패니언 닫기 액션
     */
    public static final String ACTION_ON_COMPANION_CLOSED;
    /**
     * 광고 스킵 가능 액션
     */
    public static final String ACTION_ON_SKIP_ENABLED;
    /**
     * 사용자의 광고 스킵 액션
     */
    public static final String ACTION_ON_AD_SKIPPED;
    /**
     * 광고 재생 완료 액션 (각각의 광고 재생 완료마다 호출)
     */
    public static final String ACTION_ON_AD_COMPLETED;
    /**
     * 모든 광고 재생 완료 액션 (가장 마지막 광고 재생 완료 시 한 번 호출)
     */
    public static final String ACTION_ON_ALL_AD_COMPLETED;
    /**
     * 광고 재생 준비 완료 액션
     */
    public static final String ACTION_ON_AD_READY;
    /**
     * 요청한 조건에 맞는 광고 없음 액션
     */
    public static final String ACTION_ON_NO_FILL;
    /**
     * 광고 재생 시작 액션
     */
    public static final String ACTION_ON_AD_START;
    /**
     * 광고 진행 사항 업데이트 액션
     */
    public static final String ACTION_ON_TIME_UPDATE;
    /**
     * 광고 일시 중지 액션
     */
    public static final String ACTION_ON_PAUSE;
    /**
     * 광고 재개 액션
     */
    public static final String ACTION_ON_RESUME;
    /**
     * 에러 발생 액션
     */
    public static final String ACTION_ON_ERROR;
    /**
     * 메시지 발생 액션
     */
    public static final String ACTION_ON_MESSAGE;
    /**
     * 딜로 서비스 종료 액션
     */
    public static final String ACTION_ON_SVC_DESTROYED;

    /**
     * 위의 액션들을 모두 등록해놓은 인텐트 필터
     */
    public static final IntentFilter DILO_INTENT_FILTER;

    // 데이터 가져오기위한 키 정의
    /**
     * 광고 진행 정보
     *     Progress progress = (Progress) intent.getSerializableExtra(DiloUtil.EXTRA_PROGRESS);
     */
    public static final String EXTRA_PROGRESS;
    /**
     * 에러
     *     DiloError error = (DiloError) intent.getSerializableExtra(DiloUtil.EXTRA_ERROR);
     *     Log.e("App", "Error 발생 " + error);
     */
    public static final String EXTRA_ERROR;
    /**
     * 광고 정보
     *     AdInfo adInfo = (AdInfo) intent.getSerializableExtra(DiloUtil.EXTRA_AD_INFO);
     */
    public static final String EXTRA_AD_INFO;
    /**
     * 로그 정보
     *     String msg = intent.getStringExtra(DiloUtil.EXTRA_MESSAGE);
     */
    public static final String EXTRA_MESSAGE;

    /**
     * 딜로 SDK 버전을 가져오는 메소드
     * @return 딜로 SDK 버전
     *  eg) "0.5"
     */
    public static String getSDKVersion();
}
```

## [6. Dilo SDK 동작](#목차)

### [i. Companion에 대한 동작](#목차)

1. Companion이 있는 광고 재생 시 자동으로 Companion View와 닫기 버튼을 **Visible** 처리합니다
2. Companion이 있는 광고가 끝나고 Companion이 없는(Audio만 재생되는) 광고 재생 시 자동으로 Companion View와 닫기 버튼을 **Gone** 처리합니다
3. Skip 가능한 광고의 경우 Skip 가능 시점에만 Skip 버튼을 **Visible** 처리합니다

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
* 광고 종료 또는 에러 발생 시 Audio Focus를 반환합니다
* Audio Focus에 관하여 아래 표와 같이 동작합니다

Audio Focus<br>(prefix:AudioManager.AUDIOFOCUS_)|상태|예시|Dilo SDK 동작
---|---|---|---
LOSS|완전히 잃었을 때|다른 앱에서 오디오/비디오 재생|광고가 중지됩니다
LOSS_TRANSIENT|잠시 잃었을 때|통화|광고가 일시중지된 후 통화가 끝나면 다시 재생합니다
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

### [7. 기타](#목차)
i. App 프로세스 종료 시 유의사항

> 만약 완전한 앱 종료를 위해 아래와 유사한 프로세스 종료 코드가 있다면
> ```java
> public class MainActivity {
>
>     @Override
>     protected void onDestroy() {
>       super.onDestroy();
>
>       adManager.release();
>
>       // 종료코드
>       ActivityCompat.finishAffinity(this);
>       System.runFinalizersOnExit(true);
>       System.exit(0);
>     }
> }
> ```
> 딜로 SDK 명시적 release() 후 500~1000 ms 지연 시간 뒤에 종료코드를 실행하도록 작성하시기 바랍니다<br>
> 딜로 SDK가 종료되는 도중 프로세스가 종료되어 Notification이 사라지지 않을 수 있습니다
>
> ```java
> public class MainActivity {
>
>    @Override
>    protected void onDestroy() {
>       super.onDestroy();
> 
>       adManager.release();
> 
>       new Handler(Looper.getMainLooper())
>           .postDelayed(() -> {
>               // 종료코드
>               ActivityCompat.finishAffinity(this);
>               System.runFinalizersOnExit(true);
>               System.exit(0);
>           }, 1000);
>     } 
> }
> ```

## [문의](#목차)

> Dilo SDK 탑재 및 서비스 이용에 관한 문의는 [dilo@dilo.co.kr](mailto:dilo@dilo.co.kr)로 문의 주시기 바랍니다
