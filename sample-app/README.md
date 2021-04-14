# Dilo Android SDK 연동 가이드

version 0.0.1

* 본 문서의 내용은 Sample App을 기반으로 작성하였습니다. 해당 App의 코드를 함께 참고하시기를 권고합니다

## 목차

* [개정 이력](#개정-이력)

1. [시작하기](#1-시작하기)
    * [Dilo SDK 추가](#dilo-sdk-추가)
    * [AndroidManifest.xml 속성 지정](#androidmanifestxml-속성-지정)
2. [광고 설정](#2-광고-설정)
    * [Companion 광고를 위한 레이아웃 설정 (옵션)](#i-companion-광고를-위한-레이아웃-설정-옵션)<br>
    * [광고 Skip기능 제공을 위한 Button 할당 (옵션)](#ii-광고-skip기능-제공을-위한-button-할당-옵션)
    * [Class <code>RequestParam</code>](#iii-class-requestparam)
3. [광고 요청](#3-광고-요청)
    * [Class <code>AdManager</code>](#i-class-admanager)
    * [광고 요청 예시](#ii-광고-요청-예시)

4. [광고 액션 수신](#4-광고-액션-수신)
    * [광고 액션](#광고-액션)
    * [광고 액션 수신 예제](#광고-액션-수신-예제)
5. [데이터 클래스 명세](#5-데이터-클래스-명세)
    * [Class <code>AdInfo</code>](#i-class-adinfo)
    * [Class <code>Progress</code>](#ii-class-progress)
    * [Class <code>DiloError</code>](#iii-class-diloerror)
    * [Class <code>DiloUtil</code>](#iv-class-diloutil)

6. [딜로 SDK 동작](#6-Dilo-SDK-동작)
    * [Companion에 대한 동작](#i-companion에-대한-동작)
    * [Tracking에 대한 동작](#ii-tracking에-대한-동작)
    * [Audio Focus에 대한 동작](#iii-audio-focus에-대한-동작)
    * [Notification에 대한 동작](#iv-notification에-대한-동작)
    * [Audio 재생에 대한 동작](#v-audio-재생에-대한-동작)
   
---
## [개정 이력](#목차)

변경일|버전|수정 내용|작성자
:---:|---|---|:---:
2021/04/01|0.0.1|최초 작성|백재현

## [1. 시작하기](#목차)

### [Dilo SDK 추가](#목차)

* 최상위 level <code>build.gradle</code>에 maven repository 추가

```
allprojects {
    repositories {
        google()
        jcenter()

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

* App level <code>build.gradle</code>에 디펜던시 추가

```
dependencies {    
    ...
    implementation 'kr.co.dilo:dilo-sdk:0.0.1'
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
         닫기 버튼을 할당하기 위한 RelativeLayout 추가
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

### [iii. Class <code>RequestParam</code>](#목차)

> 광고 요청에 필요한 클래스 및 열거 명세입니다<br>
> Class <code>RequestParam</code>, <code>RequestParam.Builder</code> <br>
> Enum <code>RequestParam.ProductType</code>, <code>RequestParam.FillType</code>

> RequestParam.Builder를 통해 필요한 광고 요청을 세팅할 수 있습니다

```java
import androidx.annotation.Nullable;

class RequestParam {
   static class Builder {
      ///////////////////////
      // 필수 사항
      ///////////////////////

      /**
       * 에피소드 코드를 설정합니다
       *      ※ 광고요청 전 DILO 시스템에 등록되어야 합니다
       */
      public Builder epiCode(@NonNull String epiCode);

      /**
       * 번들 ID(패키지 이름)를 설정합니다
       *      ※ 광고요청 전 DILO 시스템에 등록되어야 합니다
       */
      public Builder bundleId(@NonNull String bundleId);

      /**
       * 광고 요청 길이를 설정합니다 (초)
       */
      public Builder drs(int duration);

      /**
       * 광고 상품 유형을 설정합니다
       */
      public Builder productType(@NonNull ProductType productType);

      /**
       * 광고 채우기 유형을 설정합니다
       */
      public Builder fillType(@NonNull FillType fillType);

      /**
       * Notification에 보여질 아이콘을 설정합니다
       */
      public Builder iconResourceId(@NonNull int iconResourceId);

      ///////////////////////
      // 선택 사항
      ///////////////////////

      /**
       * Companion이 할당된 사이즈를 설정합니다
       * 설정하지 않으면 자동으로 계산된 사이즈가 들어갑니다
       */
      public Builder companionSize(int width, int height);

      /**
       * Companion이 보여질 뷰를 설정합니다
       */
      public Builder companionAdView(@Nullable AdView companionAdView);

      /**
       * 광고 Close 뷰를 설정합니다
       */
      public Builder closeButton(@Nullable ViewGroup closeButton);

      /**
       * 광고 Skip 버튼을 설정합니다
       */
      public Builder skipButton(@Nullable Button skipButton);

      /**
       *
       * @param usePauseInNotification true: 사용 (기본), false: 미사용
       */
      public Builder usePauseInNotification(@Nullable boolean usePauseInNotification);

      /**
       * Notification 클릭 시 수행할 PendingIntent를 설정합니다
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
   }

   /**
    * 광고 상품 유형
    */
   enum ProductType {
      /**
       * Audio 광고
       */
      DILO("DILO"),
      /**
       * Audio 또는 Audio + Companion 광고
       */
      DILO_PLUS("DILO_PLUS"),
      /**
       * Audio + Companion 광고
       */
      DILO_PLUS_ONLY("DILO_PLUS_ONLY")
   }

   /**
    * 광고 채우기 유형
    */
   enum FillType {
      /**
       * 1개의 광고 요청 타입
       *      ※ Duration(drs) 은 6, 10, 15 중 하나이어야 합니다.
       *       (다른 값으로 요청하면 "광고 없음(NoFill)" 처리됩니다)
       */
      SINGLE("SINGLE"),
      /**
       * 1 개의 광고 요청 타입 (6, 10, 15 초 광고중 랜덤)
       *      ※ Duration 은 무시됩니다
       */
      SINGLE_ANY("SINGLE_ANY"),
      /**
       * Duration 만큼 채우는 n 개의 광고 요청 타입
       *      ※ padding 기능 설정 여부에 따라 5 초 이하의 오차발생 가능합니다.
       */
      MULTI("MULTI")
   }
}
```

## [3. 광고 요청](#목차)

### [i. Class <code>AdManager</code>](#목차)

* 광고 요청 및 제어에 대한 전반적인 사항은 <code>AdManager</code> 클래스를 통해 수행합니다

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
     */
    public void start();

    /**
     * 광고를 일시중지 또는 재개합니다
     */
    public void playOrPause();

    /**
     * 광고를 Skip합니다
     * ※ 광고가 Skip가능하지 않은 시점에 호출 시 무시됩니다
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

* App에서 원하는 광고 형태를 <code>RequestParam.Builder</code> 클래스를 통해 <code>RequestParam</code>에 설정한 후 <code>AdManager</code>
  의 <code>loadAd()</code>에 전달하여 광고를 요청합니다

```java
class MyActivity extends AppCompatActivity {

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        AdManager adManager = new AdManager(this);

        // 30초를 채우는 n개의 audio(Companion 없는)광고 요청
        requestParamBuilder =
            new RequestParam.Builder(this)
                .productType(RequestParam.ProductType.DILO)     // Audio 광고
                .fillType(RequestParam.FillType.MULTI)          // n개의 광고
                .drs(30)                                        // 30초
                .epiCode("test_live")                           // 앱코드 설정
                .bundleId("com.queen.sampleapp")                // 패키지 설정
                .iconResourceId(R.drawable.notification_icon);  // Notification 아이콘 설정

        adManager.loadAd(requestParamBuilder.build());

    }

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        AdManager adManager = new AdManager(this);

        // 랜덤 시간 1개의 광고를 요청
        requestParamBuilder =
            new RequestParam.Builder(this)
                .productType(RequestParam.ProductType.DILO_PLUS_ONLY) // Audio + Companion 광고
                .fillType(RequestParam.FillType.SINGLE_ANY)           // 랜덤 1개 광고
                .companionAdView(companionAdView)                     // Companion View 설정
                .closeButton(companionCloseButton)                    // 닫기 버튼 설정
                .skipButton(skipButton)                               // Skip 버튼 설정
                .drs(30)                                              // RequestParam.FillType.SINGLE_ANY 시 duration은 무시됩니다
                .epiCode("test_live")                                 // 앱코드 설정
                .bundleId("com.queen.sampleapp")                      // 패키지 설정
                .notificationContentIntent(notificationIntent)        // Notification Click PendingIntent 설정
                .usePauseInNotification(usePauseInNotification)       // Notification 사용자 일시정지/재개 기능 설정
                .iconResourceId(R.drawable.notification_icon);        // Notification 아이콘 설정

        adManager.loadAd(requestParamBuilder.build());
    }
}
```

## [4. 광고 액션 수신](#목차)

* Dilo SDK에서 보내는 광고에 대한 액션 수신은 <code>BroadcastReceiver</code>를 통해 가능합니다
* 아래의 모든 액션은 <code>DiloUtil.DILO_INTENT_FILTER</code>에 등록되어 있으니 registerReceiver시 IntentFilter로 등록하거나 필요한 액션만 등록해서 사용하시면
  됩니다
* 액션 목록은 아래와 같습니다

### [광고 액션](#목차)

액션<br>(prefix:DiloUtil.ACTION_)|설명|전달<br>데이터 클래스|비고
---|---|:---:|---
RELOAD_COMPANION|컴패니언 리로드| | Companion이 있는 광고에서 Companion이 노출됨(또는 노출해야 함)<br><br>**※ 비고 : Companion 광고를 노출/숨김 처리 하는 것은<br><code>AdManager</code>를 초기화 하고 광고를 요청한 뷰에서는<br>자동으로 처리되지만,<br>Task Kill 등으로 뷰가 완전히 사라졌을 경우에는<br><code> AdManager</code>를 다시 초기화 후에<br>BroadcastReceiver에서 이 액션을 받아 <code>AdManager</code>의<br><code>reloadCompanion(AdView, ViewGroup)</code>을 호출하여<br>리로드하여야합니다**
ON_SKIP_ENABLED|광고 스킵 가능| |광고 스킵 가능한 시점 도달
ON_AD_SKIPPED|광고 스킵| | 사용자가 Skip 버튼을 눌러 광고를 Skip 또는<br>매체사에서<code>AdManager</code>의 <code>skip()</code> 메소드 호출
ON_NO_FILL|광고 없음| |요청에 맞는 조건의 광고가 없음
ON_AD_READY|광고 재생<br>준비 완료| | 광고가 로드되어 재생 준비가 완료됨<br><br>**※ 비고 : 이 액션을 수신 시 <code>AdManager</code>의 <code>start()</code>메소드를 호출하여 광고를 시작하여야합니다**
ON_AD_START|광고 재생 시작| [AdInfo](#i-class-adinfo)|광고 재생이 시작됨
ON_TIME_UPDATE|광고 진행 사항<br>업데이트| [Progress](#ii-class-progress)| 광고 진행사항이 업데이트 됨<br><br>**※ 비고 : 이 액션은 광고가 재생중일 때 200ms마다 호출됩니다**
ON_AD_COMPLETED|광고 재생 완료| |하나의 광고가 재생 완료될 때마다 호출
ON_ALL_AD_COMPLETED|모든 광고<br>재생 완료| |모든 광고가 재생 완료되면 한 번 호출
ON_PAUSE|광고 일시 중지| |매체사에서 광고 재생 중 <code>AdManager</code>의 <code>playOrPause()</code> 호출<br>또는 사용자가 Notification에서 일시 중지 버튼 누름
ON_RESUME|광고 재개| |매체사에서 광고 일시 중지 중 <code>AdManager</code>의 <code>playOrPause()</code> 호출<br>또는 사용자가 Notification에서 재개 버튼 누름
ON_ERROR|에러 발생| [DiloError](#iii-class-diloerror)| 광고 요청/로드 또는 재생에 문제가 발생
ON_SVC_DESTROYED|서비스 종료| | 딜로 SDK 서비스 종료

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
                String epiCode = intent.getStringExtra(DiloUtil.INTENT_KEY_EPI_CODE);
                if (action != null) {
                    switch (action) {
                        // 컴패니언 리로드 액션
                        case DiloUtil.ACTION_RELOAD_COMPANION:
                            if (adManager != null) {
                                adWrapper.setVisibility(View.VISIBLE);
                                adManager.reloadCompanion(companionAdView, companionCloseButton);
                            }
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
                            AdInfo adInfo = (AdInfo) intent.getSerializableExtra(DiloUtil.INTENT_KEY_AD_INFO);
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
                            DiloError error = (DiloError) intent.getSerializableExtra(DiloUtil.INTENT_KEY_ERROR);
                            log(String.format("광고 요청 중 에러가 발생하였습니다\n\t타입: %s, 에러: %s, 상세: %s", error.type, error.error, error.detail));
                            playContent();
                            break;

                        // 광고 진행 사항 업데이트 액션
                        case DiloUtil.ACTION_ON_TIME_UPDATE:
                            Progress progress = (Progress) intent.getSerializableExtra(DiloUtil.INTENT_KEY_PROGRESS);

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

### [i. Class <code>AdInfo</code>](#목차)

> 광고 정보 클래스
>
> <code>BroadcastReceiver</code>의 <code>onReceive</code>의 <code>intent.getAction()==DiloUtil.ACTION_ON_AD_START</code>(광고 시작됨) 에서<br>
> <code>DiloUtil.INTENT_KEY_AD_INFO</code> Key로 가져온 후 Cast<br>
>
> ```java
> class MyActivity extends AppCompatActivity {
> 
>     @Override
>     public void onReceive(Context context, Intent intent) {
>         switch(intent.getAction()) {
>             case DiloUtil.ACTION_ON_AD_START:
>                 AdInfo adInfo = (AdInfo) intent.getSerializableExtra(DiloUtil.INTENT_KEY_AD_INFO);
>                 ...
>                 break;
>         }
>     }
> }
> ```

```java
/**
 * 매체사에 전달하는 광고 정보 클래스
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

### [ii. Class <code>Progress</code>](#목차)

> 광고 진행 정보 클래스
>
> <code>BroadcastReceiver</code>의 <code>onReceive</code>의 <code>intent.getAction()==DiloUtil.ACTION_ON_TIME_UPDATE</code>(광고 진행사항 업데이트) 에서<br>
> <code>DiloUtil.INTENT_KEY_PROGRESS</code> Key로 가져온 후 Cast
> ```java
> class MyActivity extends AppCompatActivity {
> 
>     @Override
>     public void onReceive(Context context, Intent intent) {
>         switch(intent.getAction()) {
>             case DiloUtil.ACTION_ON_TIME_UPDATE:
>                 Progress progress = (Progress) intent.getSerializableExtra(DiloUtil.INTENT_KEY_PROGRESS);
>                 ...
>                 break;
>         }
>     }
> }
> ```

```java
/**
 * 매체사에 전달할 광고 진행 정보 클래스
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

### [iii. Class <code>DiloError</code>](#목차)

> 오류 정보 클래스
>
> <code>BroadcastReceiver</code>의 <code>onReceive</code>의 <code>intent.getAction()==DiloUtil.ACTION_ON_ERROR</code>(오류 발생) 에서<br>
> <code>DiloUtil.INTENT_KEY_ERROR</code> Key로 가져온 후 Cast<br>
> ```java
> class MyActivity extends AppCompatActivity {
> 
>     @Override
>     public void onReceive(Context context, Intent intent) {
>         switch(intent.getAction()) {
>             case DiloUtil.ACTION_ON_ERROR:
>                 DiloError error = (DiloError) intent.getSerializableExtra(DiloUtil.INTENT_KEY_ERROR);
>                 ...
>                 break;
>         }
>     }
> }
> ```

```java
/**
 * 매체사에 전달할 오류 클래스
 */
public class DiloError extends Exception {

    @Retention(RetentionPolicy.SOURCE)
    @StringDef({MEDIA, NETWORK})
    public @interface ErrorType {
    }

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

### [iv. Class <code>DiloUtil</code>](#목차)

> 유틸성 변수/메소드 정의 클래스

```java
class DiloUtil {
    // 액션 정의
    /**
     * 컴패니언 리로드 액션
     */
    public static final String ACTION_RELOAD_COMPANION;
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
     * 딜로 서비스 종료 액션
     */
    public static final String ACTION_ON_SVC_DESTROYED    = "kr.co.dilo.sdk.ACTION_ON_SVC_DESTROYED";
   
    /**
     * 위의 액션들을 모두 등록해놓은 인텐트 필터
     */
    public static final IntentFilter DILO_INTENT_FILTER;

    // 데이터 가져오기위한 키 정의
    /**
     * 에피소드 코드
     * String epiCode = intent.getStringExtra(DiloUtil.INTENT_KEY_EPI_CODE);
     */
    public static final String INTENT_KEY_EPI_CODE;
    /**
     * 광고 진행 정보
     * Progress progress = (Progress) intent.getSerializableExtra(DiloUtil.INTENT_KEY_PROGRESS);
     */
    public static final String INTENT_KEY_PROGRESS;
    /**
     * 에러
     * DiloError error = (DiloError) intent.getSerializableExtra(DiloUtil.INTENT_KEY_ERROR);
     */
    public static final String INTENT_KEY_ERROR;
    /**
     * 광고 정보
     * AdInfo adInfo = (AdInfo) intent.getSerializableExtra(DiloUtil.INTENT_KEY_AD_INFO);
     */
    public static final String INTENT_KEY_AD_INFO;

    /**
     * 딜로 SDK 버전을 가져오는 메소드
     * @return 딜로 SDK 버전
     *  eg) "0.0.1"
     */
    public static String getSDKVersion();
}
```

## [6. Dilo SDK 동작](#목차)

### [i. Companion에 대한 동작](#목차)

1. Companion이 있는 광고 재생 시 자동으로 Companion View와 닫기 버튼을 **Visible** 처리합니다
2. Companion이 있는 광고가 끝나고 Companion이 없는(Audio만 재생되는) 광고 재생 시 자동으로 Companion View와 닫기 버튼을 **Gone** 처리합니다
3. Skip 가능한 광고의 경우 Skip 가능 시점에만 Skip 버튼을 **Visible** 처리합니다
4. 사용자가 Companion 클릭 시 Landing에 대한 처리가 **자동**으로 이루어집니다
5. 사용자가 Companion 내의 opt-out 클릭 시에 대한 처리가 **자동**으로 이루어집니다
> ※ View를 처리하는 1~3의 경우 Activity가 Destory되면 자동으로 처리하지 못하니 BroadcastReceiver의<br> ACTION_RELOAD_COMPANION, ACTION_ON_SKIP_ENABLED 액션을 통해 처리해야합니다


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

### [iii. Audio Focus에 대한 동작](#목차)

* 광고 요청 시 Dilo SDK에서는 <code>AudioManager.AUDIOFOCUS_GAIN</code>으로 Audio Focus를 요청합니다
* 광고 종료 또는 에러 발생 시 Audio Focus를 반환합니다
* Audio Focus에 관하여 아래 표와 같이 동작합니다

Audio Focus<br>(prefix:AudioManager.AUDIOFOCUS_)|상태|예시|Dilo SDK 동작
---|---|---|---
LOSS|완전히 잃었을 때|다른 앱에서 오디오/비디오 재생|광고가 중지됩니다
LOSS_TRANSIENT|잠시 잃었을 때|통화|광고가 일시중지된 후 통화가 끝나면 다시 재생합니다
LOSS_TRANSIENT_CAN_DUCK|볼륨 감소 요청이 있었을 때| |볼륨을 반으로 줄여 재생합니다
GAIN|최초 포커스를 얻거나 다시 얻었을 때| |이전 볼륨으로 재생합니다

### [iv. Notification에 대한 동작](#목차)

> <code>RequestParam</code>의 <code>usePauseInNotification</code> (사용자 일시 중지 허용)값에 따른 Notification 동작에 대한 설명입니다

<code>usePauseInNotification = true</code> 설정 시 (기본)

* 사용자가 Notification에서 버튼을 눌러 Dilo광고를 **일시중지/재개 할 수** 있습니다

<p align="center">
    <img src="https://user-images.githubusercontent.com/73524723/113123509-9ed89d80-924f-11eb-9023-db821a3684f8.jpg" width=30%>
</p>

<code>usePauseInNotification = false</code> 설정 시

* Notification에 일시중지/재개 버튼이 **사라**집니다

<p align="center">
   <img src="https://user-images.githubusercontent.com/73524723/113123510-9f713400-924f-11eb-91fb-4f04792167d7.jpg" width=30%>
</p>

### [v. Audio 재생에 대한 동작](#목차)

> Dilo SDK에서는 광고 오디오 음원을 Service로 재생하여 App이 종료되어도 광고가 재생될 수 있도록 구현하였습니다
>
> * Service는 광고가 모두 종료된 후 Destroy됩니다

> 하지만 아래와 같은 상황에서 Dilo SDK Service(광고 재생)가 Android 시스템에 의해 강제 종료될 수 있습니다
>
> 1. App의 "배터리 사용 관리" 설정의 백그라운드에서 실행이 꺼져있음
>
> <p align="center">
>     <img src="https://user-images.githubusercontent.com/73524723/113123501-9da77080-924f-11eb-8bcc-1e35f1ea15e6.jpg" width=30%>
> </p>
>
> 2. App의 "배터리 사용 관리" 설정의 배터리 사용량 최적화가 켜져있음
>
>
> <p align="center">
>     <img src="https://user-images.githubusercontent.com/73524723/113123504-9e400700-924f-11eb-9ec6-84e0a52dbacf.jpg" width="30%">
> </p>
>

## [문의](#목차)

> Dilo SDK 탑재 및 서비스 이용에 관한 문의는 [dilo@dilo.co.kr](mailto:dilo@dilo.co.kr)로 문의 주시기 바랍니다
