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
    docker build -f nginx/Dockerfile -t cherrybooker-nginx .
```
### How to manage the cluseter?
- Install minikube, the lightweight cluster mangaement service
- Then, do the following.
``` console
    minikube image load cherrybooker-mariadb
    minikube image load cherrybooker-backend:dev
```

### How to apply Kubernetes manifest?
``` console
    kubectl apply -f k8s/mariadb.yaml
    kubectl apply -f k8s/redis.yaml
    kubectl apply -f k8s/backend.yaml
```
- caution: you should frst apply mariadb and redis first.

### How to check the pods are well managed?
``` console
    kubectl get pods -A
```
