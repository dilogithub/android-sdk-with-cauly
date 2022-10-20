# Dilo Android SDK with Cauly SDK

* 최신 버전의 SDK 사용을 권장합니다


* 최신 버전의 **Android Studio** 사용을 권장합니다. **Eclipse**에 대한 기술 지원은 하지 않습니다


* Dilo SDK는 Android 4.1 (Jelly Bean, API Level 16)이상 기기에서 동작합니다
  - Android 5.0 (Lollipop, API 수준 21)이상 사용을 권장합니다


* 최신 릴리즈 버전은 아래 버전에서 개발되었습니다
  - ext.kotlin_version `1.5.10`
  - com.android.tools.build:gradle `4.2.0`

---

## 안내 사항

Google Play의 `데이터 보안정책 업데이트`에 따라, 앱 개발사(자)는 구글측에, 해당 앱이 수집하는 데이터의 종류와 범위를 설문양식에 작성하여 제출해야합니다

아래의 일정 참고하여, 기한안에 Play Console에서 데이터 보안 양식 작성이 필요함을 알려드립니다
* 22년 4월 말 : Google Play 스토어에서 보안섹션이 사용자에게 표기
* 22년 7월 20일 : 양식 제출 및 개인정보처리방침 승인 기한(양식과 관련해 해결되지 않은 문제가 있는 경우 `신규 앱 제출 및 앱 업데이트가 거부`될 수 있습니다)

업데이트된 정책을 준수하실수 있도록 딜로 SDK에서 수집하는 데이터 항목에 대해 안내드립니다
* 딜로 SDK에서 광고 및 분석 목적으로 다음 데이터를 공유합니다

| 카테고리 | 데이터 유형 |
|---|---|
| 기기 또는 기타 식별자 | Android 광고 ID를 공유 |

* 딜로 SDK에서 전송하는 모든 사용자 데이터는 전송 중에 암호화되며, 사용자가 데이터 삭제를 요청할 수 있는 방편을 제공하지 않습니다

구글플레이 보안섹션 양식 작성과 관련 상세 내용은 [가이드문서](https://github.com/dilogithub/android-sdk/files/8559212/default.pdf) 를 확인부탁드립니다

---

## Release Note

* [0.6.10 (2022/04/13)](https://dilogithub.github.io/android/0.6.10-with-cauly.html)

## 문의

Dilo Android SDK 적용 관련 문의 사항은 [dilo@dilo.co.kr](dilo@dilo.co.kr)로 문의주시기 바랍니다
