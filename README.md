# ytudl-web

서버 없이 브라우저에서 바로 실행되는 YouTube MP3 다운로더입니다.

## 사용법 (서버 불필요)

1. `index.html` 파일을 다운로드
2. Chrome에서 파일 열기
3. 검색 → MP3 다운로드

## 안드로이드

1. `index.html`을 폰에 저장
2. Chrome에서 열기 (`파일에서 열기`)

## 구조

- 검색: [Invidious](https://invidious.io) 오픈소스 YouTube API (CORS 허용)
- 다운로드: [cobalt.tools](https://cobalt.tools) 변환 API (무료)
- 단일 HTML 파일, 외부 의존성 없음
