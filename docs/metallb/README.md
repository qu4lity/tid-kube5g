# MetalLB testing

An example-metallb.yaml file is provided for convenience

```
kubectl apply -f example-metallb.yaml
kubectl logs -f $(kubectl get pods | grep udplistener | awk '{print $1}')
```

And from another shell:

```
nc -u 10.0.1.205 5005
```

## MetalLB manual deployment

1. Include the line
 - --service-node-port-range=2000-65000
in the file /etc/kubernetes/manifests/kube-apiserver.yaml

2. Create metallb-config.yaml
```
apiVersion: v1
kind: ConfigMap
metadata:
  namespace: metallb-system
  name: config
data:
  config: |
    address-pools:
    - name: default
      protocol: layer2
      addresses:
      - 10.0.1.205-10.0.1.210
```

3. Create example deployment example-deploy.yaml
```
apiVersion: apps/v1
kind: Deployment
metadata:
  name: udplistener
  namespace: default
spec:
  selector:
    matchLabels:
      app: udplistener
  template:
    metadata:
      labels:
        app: udplistener
    spec:
      containers:
      - image: mendhak/udp-listener
        imagePullPolicy: Always
        name: udp-listener
        ports:
        - containerPort: 5005
          protocol: UDP
          hostPort: 5005
      restartPolicy: Always
```

4. Create example-svc.yaml
```
apiVersion: v1
kind: Service
metadata:
  name: udplistener
spec:
  ports:
    - protocol: UDP
      port: 5005
      targetPort: 5005
      nodePort: 5005
  selector:
    app: udplistener
  type: LoadBalancer
```

5. Kubectl your way
```
$ kubectl create ns metallb-system
$ kubectl apply -f https://raw.githubusercontent.com/google/metallb/v0.9.2/manifests/metallb.yaml
$ kubectl create secret generic -n metallb-system memberlist --from-literal=secretkey="$(openssl rand -base64 128)"
$ kubectl apply -f metallb-config.yaml
$ kubectl apply -f example-deploy.yaml
$ kubectl apply -f example-svc.yaml
```
