## 🚀 EasyOCR FastAPI Service 실행 방법 (Docker 기반)

이 저장소는 **FastAPI + EasyOCR 기반의 한국어 OCR API 서버**입니다.  
로컬 환경에 **Docker만 설치되어 있으면 별도의 Python 환경 설정 없이 바로 실행**할 수 있습니다.

### 1. 프로젝트 클론
**cmd에

git clone https://github.com/be20-4th-CherryBooker-CherryBooker/EasyOCR-fastapi-service.git

cd EasyOCR-fastapi-service

### 2.docker 이미지 빌드(시간이 매우 오래 걸립니다.)
docker build -t ocr-api .

### 3. 빌드 완료 후 해당 명령어로 ocr 서버를 실행
docker run -p 8000:8000 ocr-api

### 4.브라우저에서 다음 주소로 swagger 문서로 확인할 수 있습니다.
http://127.0.0.1:8000/docs


### 5.백엔드 서버에서는 아래의 엔드포인트로 이미지를 post 방식으로 전송하면 json 형태로 받을 수 있습니다.
POST http://127.0.0.1:8000/ocr

