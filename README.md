📢 프로젝트 소개

🐾 Catchy FE 테스트 결과 보고서

🔡 요구사항 명세서

📟 REST API 명세서
https://www.notion.so/2be341a375a5808da53cec8caa0995df?v=2be341a375a5805f8234000c08252a4a

🗃️ DB 모델링
ERD 작성
https://www.erdcloud.com/d/DzjLGrZmHWiuiGr8j

📈 플로우 차트

# 🛠 Tech Stack

## 💻 Backend
<p>
  <img src="https://img.shields.io/badge/Java-007396?logo=openjdk&logoColor=white&style=flat-square"/>
  <img src="https://img.shields.io/badge/Spring%20Boot-6DB33F?logo=springboot&logoColor=white&style=flat-square"/>
  <img src="https://img.shields.io/badge/Spring%20Cloud-6DB33F?logo=spring&logoColor=white&style=flat-square"/>
  <img src="https://img.shields.io/badge/Spring%20Security-6DB33F?logo=springsecurity&logoColor=white&style=flat-square"/>
  <img src="https://img.shields.io/badge/JPA-59666C?style=flat-square"/>
  <img src="https://img.shields.io/badge/JWT-black?logo=jsonwebtokens&style=flat-square"/>
  <img src="https://img.shields.io/badge/MyBatis-b31b1b?style=flat-square"/>
</p>

---

## 🎨 Frontend
<p>
  <img src="https://img.shields.io/badge/HTML5-E34F26?logo=html5&logoColor=white&style=flat-square"/>
  <img src="https://img.shields.io/badge/CSS3-1572B6?logo=css3&logoColor=white&style=flat-square"/>
  <img src="https://img.shields.io/badge/JavaScript-ES6-F7DF1E?logo=javascript&logoColor=black&style=flat-square"/>
  <img src="https://img.shields.io/badge/Vue.js-42b883?logo=vuedotjs&logoColor=white&style=flat-square"/>
  <img src="https://img.shields.io/badge/Fetch%20API-black?style=flat-square"/>
  <img src="https://img.shields.io/badge/Axios-5A29E4?style=flat-square"/>
  <img src="https://img.shields.io/badge/dayjs-black?style=flat-square"/>
</p>

---

## 🪄 Design
<p>
  <img src="https://img.shields.io/badge/Figma-F24E1E?logo=figma&logoColor=white&style=flat-square"/>
</p>

---

## 🗄️ Database
<p>
  <img src="https://img.shields.io/badge/MariaDB-003545?logo=mariadb&logoColor=white&style=flat-square"/>
  <img src="https://img.shields.io/badge/MyBatis-b31b1b?style=flat-square"/>
</p>

---

## ⚙️ Tools
<p>
  <img src="https://img.shields.io/badge/Notion-000000?logo=notion&logoColor=white&style=flat-square"/>
  <img src="https://img.shields.io/badge/Google%20Sheets-34A853?logo=google%20sheets&logoColor=white&style=flat-square"/>
  <img src="https://img.shields.io/badge/ERD%20Cloud-4285F4?style=flat-square"/>
  <img src="https://img.shields.io/badge/IntelliJ%20IDEA-000000?logo=intellijidea&logoColor=white&style=flat-square"/>
  <img src="https://img.shields.io/badge/VS%20Code-007ACC?logo=visualstudiocode&logoColor=white&style=flat-square"/>
  <img src="https://img.shields.io/badge/Git-F05032?logo=git&logoColor=white&style=flat-square"/>
  <img src="https://img.shields.io/badge/GitHub-181717?logo=github&logoColor=white&style=flat-square"/>
  <img src="https://img.shields.io/badge/Postman-FF6C37?logo=postman&logoColor=white&style=flat-square"/>
</p>


###🚩 젠킨스 파이프라인 파일 스크립트 코드 📱 CI/CD 테스트

### Basic SW Arhitecture for this project
<img width="754" height="533" alt="image" src="https://github.com/user-attachments/assets/d2708d20-34e1-442a-8135-f691ddd4208b" />

### Experiments (1) 
- Kubernetes pods are running without issues

![쿠버네티스_시연_영상](https://github.com/user-attachments/assets/12d55c6c-d072-4ce7-a2d9-e8d4673bfeca)


### Experiemnts (2)
- Kubernetes pods are being selected in round-robin manner

![kuberneteds pods being round-robin](https://github.com/user-attachments/assets/c35ea72a-0189-42bb-a16e-7af2d36d5527)



### How to clone submodules recursively
```console
git clone --recurse-submodules <repository-url>
```

### How to update each submodules
``` console
git submodule update --remote --recursive
```

### How to start building Docker images
- Docker build for Vue frontend is included in nginx/Dockerfile
``` console
docker build -t cherrybooker-mariadb mariadb
docker build -t cherrybooker-backend:dev backend
docker build -t cherrybooker-ocr:dev ocr
docker build -f nginx/Dockerfile -t cherrybooker-nginx:dev .
```

### How to manage the cluseter?
- Install minikube, the lightweight cluster mangaement service
- enter minikube start.
``` console
minikube start
```
- Then, do the following.
``` console
minikube image load cherrybooker-mariadb
minikube image load cherrybooker-backend:dev
minikube image load cherrybooker-ocr:dev
minikube image load cherrybooker-nginx:dev
```

### Prepare backend secret
1. Create `.k8s/backend-secrets.env` locally with the following keys:
   ```
   SPRING_DATASOURCE_USERNAME=...
   SPRING_DATASOURCE_PASSWORD=...
   JWT_SECRET=...
   SPRING_SECURITY_OAUTH2_CLIENT_REGISTRATION_KAKAO_CLIENT_ID=...
   SPRING_SECURITY_OAUTH2_CLIENT_REGISTRATION_KAKAO_CLIENT_SECRET=...
   SPRING_SECURITY_OAUTH2_CLIENT_REGISTRATION_GOOGLE_CLIENT_ID=...
   SPRING_SECURITY_OAUTH2_CLIENT_REGISTRATION_GOOGLE_CLIENT_SECRET=...
   ```
2. Apply it as a Kubernetes secret (rerun whenever values change):
   ``` console
   kubectl delete secret backend-secrets --ignore-not-found
   kubectl create secret generic backend-secrets --from-env-file=./k8s/backend-secrets.env
   ```

### Prepare shared uploads volume (Minikube)
``` console
minikube ssh -- sudo mkdir -p /data/cherrybooker/uploads
```

### How to apply Kubernetes manifest?
``` console
kubectl apply -f k8s/mariadb.yaml
kubectl apply -f k8s/redis.yaml
kubectl apply -f k8s/backend.yaml
kubectl apply -f k8s/ocr.yaml
kubectl apply -f k8s/nginx.yaml
```
- (optional) Enable HPA metrics support on Minikube:
``` console
minikube addons enable metrics-server
```

### How to check the pods are well managed?
``` console
kubectl get pods -A
```
if any crashloop occurs, check
``` console
kubectl describe pod {docker container name}
```

### How to delete currently running pods from namespace
- to delete cherrybooker-backend, for instance, will be like...
``` console
kubectl delete deployment cherrybooker-backend
```
- Then, stop minikube by
``` console
minikube stop
```

###🍪 개인 회고록

