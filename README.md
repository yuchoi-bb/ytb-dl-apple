# ytudl-web

유튜브 영상 음원을 MP3로 다운받는 HTML5 웹앱입니다.

## 안드로이드에서 실행 (Termux)

### 1. Termux 설치

[F-Droid](https://f-droid.org/packages/com.termux/)에서 Termux 설치  
(Play Store 버전은 구버전이라 권장하지 않음)

### 2. Termux 패키지 설치

```bash
pkg update && pkg upgrade -y
pkg install -y nodejs python ffmpeg git
pip install yt-dlp
```

### 3. 앱 다운로드 및 실행

```bash
git clone https://github.com/yuchoi-bb/ytb-dl-apple
cd ytb-dl-apple
git checkout claude/ytudl-web-app-mh3d1y
npm install
npm start
```

### 4. 브라우저에서 접속

Chrome에서 `http://localhost:3000` 접속

---

## 기능

- 상단 검색창에 유튜브 제목/아티스트 입력 후 검색
- 검색 결과: 썸네일, 제목, 채널명, 재생시간, 조회수 표시
- 각 결과 우하단 **MP3 다운로드** 버튼 클릭 시 mp3로 저장
