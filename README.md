# ytudl-web

유튜브 영상 음원을 MP3로 다운받는 HTML5 웹앱입니다.

## 요구사항

로컬 Mac에서 실행해야 합니다.

```bash
# yt-dlp 설치
brew install yt-dlp

# ffmpeg 설치
brew install ffmpeg

# Node.js 설치 (https://nodejs.org 또는 brew)
brew install node
```

## 실행

```bash
npm install
npm start
```

브라우저에서 `http://localhost:3000` 접속

## 기능

- 상단 검색창에 유튜브 제목/아티스트 입력 후 검색
- 검색 결과: 썸네일, 제목, 채널명, 재생시간, 조회수 표시
- 각 결과 우하단 **MP3 다운로드** 버튼 클릭 시 mp3로 저장
