# 배경화면 위젯 (Wallpaper Widget)

안드로이드 홈 화면 위젯으로 배경화면을 빠르게 변경하는 앱입니다.

## 기능

| 기능 | 설명 |
|------|------|
| 홈 화면 위젯 | 2×2 크기 위젯, 홈 화면에 추가 가능 |
| 배경화면 변경 버튼 | 위젯에서 바로 갤러리 열기 |
| 적용 대상 선택 | 홈 화면 / 잠금 화면 / 둘 다 선택 |
| 이미지 미리보기 | 적용 전 화면에서 확인 |
| 권한 안내 | 저장소 접근 권한 자동 요청 |

## 요구사항

- Android 8.0 (API 26) 이상
- targetSdk 34

## 빌드 방법

```bash
# 디버그 APK
./gradlew assembleDebug

# 릴리즈 APK (서명 필요)
./gradlew assembleRelease
```

빌드된 APK 위치: `app/build/outputs/apk/debug/app-debug.apk`

## 프로젝트 구조

```
app/src/main/
├── kotlin/com/wallpaper/widget/
│   ├── MainActivity.kt            # 메인 화면 (이미지 선택 + 미리보기)
│   ├── WallpaperPickerActivity.kt # 위젯에서 열리는 경량 선택 화면
│   ├── WallpaperWidgetProvider.kt # 홈 화면 위젯 AppWidgetProvider
│   ├── WallpaperWidgetService.kt  # 위젯 RemoteViews 서비스
│   └── WallpaperHelper.kt         # WallpaperManager 래퍼
├── res/
│   ├── layout/
│   │   ├── activity_main.xml
│   │   ├── activity_wallpaper_picker.xml
│   │   └── widget_wallpaper.xml   # 위젯 레이아웃
│   └── xml/
│       └── widget_info.xml        # 위젯 메타데이터
└── AndroidManifest.xml
```

## 사용 방법

1. 앱 설치 후 홈 화면 길게 누르기 → 위젯 → **배경화면** 위젯 추가
2. 위젯의 **배경화면 변경** 버튼 탭 → 갤러리에서 이미지 선택
3. 적용 대상 선택 (홈 / 잠금 / 둘 다)
4. 완료!
