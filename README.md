# 🍒 CherryBooker (체리북커)

> **책 속 문장을 체리픽(Cherrypick)하다**  
> OCR 기반 개인 책 서재 & 글귀 아카이브 서비스

---

## 📌 프로젝트 개요

**CherryBooker(체리북커)** 는  
사용자가 책에서 인상 깊은 문장을 *체리픽*하듯 선택해 저장하는  
**개인 독서 기록 및 글귀 아카이빙 서비스**입니다.

책의 구절을 촬영하거나 이미지를 업로드하면  
OCR 기술을 통해 텍스트가 자동으로 추출·저장되며,  
이를 기반으로 개인화된 독서 아카이브를 구축할 수 있습니다.

또한 글귀 스레드 기능을 통해  
다른 사용자들과 문장을 공유하며 독서 경험을 확장합니다.

---

## 👩‍👧‍👦 멤버 소개


<div align="center">

| 김명진 | 김동리 | 김현수 | 박연수 | 배창민 |
|:--:|:--:|:--:|:--:|:--:|
| <img src="hhttps://github.com/user-attachments/assets/73878a63-ca64-4772-88f0-838d4a6c93e2" width="150"> | <img src="hhttps://github.com/user-attachments/assets/73878a63-ca64-4772-88f0-838d4a6c93e2" width="150"> | <img src="https://github.com/user-attachments/assets/73878a63-ca64-4772-88f0-838d4a6c93e2" width="150"> | <img src="https://github.com/user-attachments/assets/73878a63-ca64-4772-88f0-838d4a6c93e2" width="150"> | <img src="https://github.com/user-attachments/assets/73878a63-ca64-4772-88f0-838d4a6c93e2" width="150"> |

</div>


## 🎯 서비스 목적

- 독서 중 인상 깊은 문장을 쉽고 빠르게 기록
- OCR 기반 자동 텍스트 추출로 문장 기록의 불편함 해소
- 글귀 중심의 개인화된 독서 아카이브 구축
- 글귀 스레드를 통한 독서 커뮤니티 활성화

---

## ✨ 핵심 기능

### 📚 개인 서재 관리
- 읽은 책 / 읽고 싶은 책 분류 관리
- 책 표지 이미지 및 메타 정보(제목, 저자, ISBN) 등록
- 책별 글귀 모아보기 기능

### 📸 OCR 기반 구절 추출
- 카메라 촬영 또는 이미지 업로드
- OCR을 통한 문장 자동 인식
- 원본 이미지 + 텍스트 함께 저장

### 📝 글귀 아카이브 & 메모
- 글귀 등록 / 수정 / 삭제
- 사용자 코멘트(메모) 추가
- 책 및 태그 기반 분류

### 💬 글귀 스레드
- 본인이 추출한 글귀를 스레드에 공유
- 타 사용자는 **본인이 추출한 글귀로만 답변 가능**
- 글귀 중심의 독서 커뮤니티 제공

---

## 🛠 기술 스택

### Frontend
![Vue](https://img.shields.io/badge/Vue-4FC08D?style=for-the-badge&logo=vue.js&logoColor=white)

### Backend
![Spring Boot](https://img.shields.io/badge/Spring_Boot-6DB33F?style=for-the-badge&logo=springboot&logoColor=white)
![Spring Security](https://img.shields.io/badge/Spring_Security-6DB33F?style=for-the-badge&logo=springsecurity&logoColor=white)
![JPA](https://img.shields.io/badge/JPA-59666C?style=for-the-badge)
![JWT](https://img.shields.io/badge/JWT-000000?style=for-the-badge&logo=jsonwebtokens&logoColor=white)

### Database
![MariaDB](https://img.shields.io/badge/MariaDB-003545?style=for-the-badge&logo=mariadb&logoColor=white)

### OCR
![EasyOCR](https://img.shields.io/badge/EasyOCR-4B8BBE?style=for-the-badge&logo=python&logoColor=white)

### Infra & DevOps
![Docker](https://img.shields.io/badge/Docker-2496ED?style=for-the-badge&logo=docker&logoColor=white)
![Kubernetes](https://img.shields.io/badge/Kubernetes-326CE5?style=for-the-badge&logo=kubernetes&logoColor=white)
![Jenkins](https://img.shields.io/badge/Jenkins-D24939?style=for-the-badge&logo=jenkins&logoColor=white)
![Nginx](https://img.shields.io/badge/Nginx_Ingress-009639?style=for-the-badge&logo=nginx&logoColor=white)
![Grafana](https://img.shields.io/badge/Grafana-F46800?style=for-the-badge&logo=grafana&logoColor=white)

---

## 🚀 프로젝트 목표

- 사용자 중심 UX의 독서 기록 서비스 구축  
- OCR 기반 자동 문장 추출 기능 구현  
- 문장 아카이브 확장 (태그, 감정, 추천)  
- CI/CD + Kubernetes 기반의 안정적인 서비스 운영  
- 실서비스 수준의 DevOps 파이프라인 구축 경험  

---

## 🌱 기대 효과

- 개인 독서 경험을 체계적으로 축적하는 맞춤형 독서 플랫폼
- 글귀 스레드를 통한 새로운 형태의 독서 커뮤니티
- OCR 기반 자동화로 편리한 문장 수집 경험 제공
- 배포·확장·모니터링까지 포함한 실무형 프로젝트 완성

---

## 🗂️ 프로젝트 산출물

- ### 📚 요구사항 명세서**
  요구사항 명세서를 자세히 보려면 [여기](https://docs.google.com/spreadsheets/d/1QR_PiTqoFvpmC2nNCuG4F1nPMkxDIkgN3bHzirLP0Fc/edit?gid=0#gid=0)를 클릭하세요.

- ### 🗺️ ERD

  ERD를 자세히 보려면 [여기](https://www.figma.com/design/nQwpBL8EiSqc1z86ha7lxl/CheeryBooker?node-id=0-1&p=f&t=GDbVhfKM7e2URF7i-0)를 클릭하세요

- ### 🎨 피그마

  피그마를 자세히 보려면 [여기](https://www.erdcloud.com/d/DzjLGrZmHWiuiGr8j)를 클릭하세요

---

## 💻 시스템 아키텍쳐


## 🐋 CI/CD 계획


## ✅ 기능 수행 테스트 결과
<details>
     <summary><b>1️⃣ 회원</b></summary>
   
</details>

<details>
     <summary><b>2️⃣ 서재관리</b></summary>
   
</details>

<details>
     <summary><b>3️⃣ 글귀</b></summary>
     - 글귀 추출 (OCR 텍스트 추출)<br>
     - 구글 로그인<br>
  ![Image](https://github.com/user-attachments/assets/e7f5e196-9fc2-4a84-aaba-e3ce22eca34e)
   
</details>

<details>
     <summary><b>4️⃣ 커뮤니티</b></summary>
   
</details>

<details>
     <summary><b>5️⃣ 신고</b></summary>
   
</details>

<details>
     <summary><b>6️⃣ 알림</b></summary>
   
</details>
