### How to clone submodules recursively
```console
git clone --recurse-submodules <repository-url>
```

### How to update each submodules
``` console
git submodule update --remote --recursive
```

### How to start building Docker images
``` console
docker build -t cherrybooker-mariadb mariadb
docker build -t cherrybooker-backend:dev backend
docker build -t cherrybooker-ocr:dev ocr
docker build -f nginx/Dockerfile -t cherrybooker-nginx:dev .
```

### How to manage the cluseter?
- Install minikube, the lightweight cluster mangaement service
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
   kubectl create secret generic backend-secrets --from-env-file=.k8s/backend-secrets.env
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
- caution: you should frst apply mariadb and redis first.

### How to check the pods are well managed?
``` console
kubectl get pods -A
```
