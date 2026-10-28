# 대구 버스 알림 (Android / Kotlin)
1. Android Studio 로 이 폴더를 연다 (Gradle wrapper 는 자동 생성됨).
2. local.properties.example → local.properties 로 복사 후 DATA_GO_KR_KEY 입력
   (공공데이터포털 '대구광역시_버스정보시스템' 활용신청 후 받은 일반 인증키, Decoding 값).
3. 폰 USB 디버깅 연결 후 Run. 알림 권한 허용.
4. 정류장 ID(bsId)를 입력하고 '감시 시작'.
※ BusApi.kt 의 URL/응답 태그는 활용가이드와 대조해 확인 필요.
※ 삼성 배터리 최적화: 설정 > 앱 > 대구 버스 알림 > 배터리 > '제한 없음'.

## Android Studio 없이 빌드 (GitHub Actions)
1. GitHub 가입 → New repository (이름 DaeguBusAlert, Public/Private 무관)
2. 압축을 푼 폴더 내용을 저장소에 업로드 (Add file > Upload files, 폴더째 드래그). .github 폴더 포함 필수.
3. Settings > Secrets and variables > Actions > New repository secret
   이름 DATA_GO_KR_KEY, 값 = 공공데이터포털 인증키(Decoding)
4. Actions 탭 > Build APK > Run workflow. 약 3~5분 후 완료.
5. 완료된 실행 화면 맨 아래 Artifacts 의 DaeguBusAlert-apk 다운로드 → 압축 해제 → app-debug.apk 를 폰으로 옮겨 설치 (출처 불명 앱 허용).
