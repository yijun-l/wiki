# Storage 

By default, Kubernetes containers are ephemeral; all in-container data is lost when a container terminates, restarts, or is rescheduled. To support **stateful workloads**, **data sharing across containers**, and **direct access to node resources**, Kubernetes provides a flexible storage architecture for diverse scenarios.

## EmptyDir

An **emptyDir** is Kubernetes' basic ephemeral volume, designed **exclusively for data sharing between multiple containers within the same pod**.

It is created automatically when a pod is scheduled to a node and is strictly tied to the pod lifecycle: all data is permanently deleted once the pod is removed or evicted. It is suitable for **temporary caching**, **intermediate data transfer**, and **shared workspaces between sidecar and main containers**.

Below is a quick example to verify cross-container data sharing.

```yaml
apiVersion: v1
kind: Pod
metadata:
  name: emptydir-test
spec:
  containers:
  - name: writer
    image: busybox
    command: ["sh", "-c", "echo 'hello from writer' > /shared/data.txt"]
    volumeMounts:
    - name: shared-volume
      mountPath: /shared

  - name: reader
    image: busybox
    command: ["sh", "-c", "cat /shared/data.txt"]
    volumeMounts:
    - name: shared-volume
      mountPath: /shared

  volumes:
  - name: shared-volume
    emptyDir: {}
```

Run and verify:

```shell
$ kubectl apply -f emptydir-test.yaml
pod/emptydir-test created

$ kubectl get pod
NAME            READY   STATUS              RESTARTS   AGE
emptydir-test   0/2     ContainerCreating   0          3s

$ kubectl get pod
NAME            READY   STATUS      RESTARTS   AGE
emptydir-test   0/2     Completed   0          8s

$ kubectl logs emptydir-test -c reader
hello from writer
```
## HostPath

Unlike the pod-scoped **emptyDir**, **hostPath** mounts a file or directory directly from the host node’s filesystem into a pod. It bypasses standard Kubernetes storage abstractions and tightly couples pods to a specific node, which breaks cross-node portability.

**hostPath** is primarily **designed for DaemonSets and node-level components**, which run exactly one pod per node and require direct access to local node resources. It is not recommended for general application workloads, but ideal for **system agents**, **monitoring tools**, and **node-level services** that depend on node-local files, devices, or directories.

The following **DaemonSet** example uses **hostPath** to collect node system logs cluster-wide on every node.

```yaml
apiVersion: apps/v1
kind: DaemonSet
metadata:
  name: node-log-monitor
spec:
  selector:
    matchLabels:
      app: log-monitor
  template:
    metadata:
      labels:
        app: log-monitor
    spec:
      containers:
      - name: monitor
        image: busybox
        command: ["sh", "-c", "tail -f /host/log/syslog 2>/dev/null"]
        volumeMounts:
        - name: host-log
          mountPath: /host/log
      volumes:
      - name: host-log
        hostPath:
          path: /var/log
```

Run and verify:

```shell
$ kubectl apply -f hostpath-test.yaml
daemonset.apps/node-log-monitor created

$ kubectl get po -o wide
NAME                     READY   STATUS    RESTARTS   AGE   IP           NODE              NOMINATED NODE   READINESS GATES
node-log-monitor-79lhr   1/1     Running   0          10m   10.244.4.4   k8sprodworker02   <none>           <none>
node-log-monitor-f6nw6   1/1     Running   0          10m   10.244.3.6   k8sprodworker01   <none>           <none>

$ kubectl logs node-log-monitor-79lhr
2026-03-25T23:10:20.190164+00:00 k8sprodworker02 containerd[19343]: time="2026-03-25T23:10:20.189973292Z" level=info msg="ImageCreate event name:\"sha256:925ff61909aebae4bcc9bc04bb96a8bd15cd2271f13159fe95ce4338824531dd\" labels:{key:\"io.cri-containerd.image\" value:\"managed\"}"
2026-03-25T23:10:20.205347+00:00 k8sprodworker02 containerd[19343]: time="2026-03-25T23:10:20.205109968Z" level=info msg="ImageCreate event name:\"docker.io/library/busybox@sha256:1487d0af5f52b4ba31c7e465126ee2123fe3f2305d638e7827681e7cf6c83d5e\" labels:{key:\"io.cri-containerd.image\" value:\"managed\"}"
```

## Local Persistent Volume (PV)

**Local PV** is a Kubernetes persistent volume type that uses node-local storage devices to provide high-performance local storage for stateful applications. It supports the standard PV/PVC lifecycle and binds pods to specific nodes via node affinity to ensure data persistence and scheduling stability.

As a production-grade alternative to hostPath, Local PV is particularly suitable for high-I/O workloads such as databases and message queues. It employs static provisioning and requires preconfigured storage directories on the target node.

Below is a complete StatefulSet example for using persistent local storage.

Local PV uses **static provisioning** and requires pre-created directories on the target node; automatic directory creation is not supported.

```shell
$ ssh worker01 "sudo mkdir -p /mnt/ssd/data"
$ ssh worker01 "echo 'Hello from local volume' | sudo tee /mnt/ssd/data/index.html"
Hello from local volume
```

First, define a static Local PV with `nodeAffinity` to restrict the volume to a specific node and avoid cross-node access issues.

```yaml
apiVersion: v1
kind: PersistentVolume
metadata:
  name: example-local-pv
spec:
  capacity:
    storage: 5Gi
  volumeMode: Filesystem
  accessModes:
  - ReadWriteOnce
  persistentVolumeReclaimPolicy: Retain
  storageClassName: local-storage
  local:
    path: /mnt/ssd/data
  nodeAffinity:
    required:
      nodeSelectorTerms:
      - matchExpressions:
        - key: kubernetes.io/hostname
          operator: In
          values:
          - k8sprodworker01
```

Next, create a **Headless Service** and **StatefulSet** with `volumeClaimTemplates` to automatically create PVCs and mount the Local PV persistently.

```yaml
apiVersion: v1
kind: Service
metadata:
  name: nginx-service
  labels:
    app: local-nginx
spec:
  ports:
    - port: 80
      name: web
  clusterIP: None
  selector:
    app: local-nginx
    
---

apiVersion: apps/v1
kind: StatefulSet
metadata:
  name: local-web
spec:
  serviceName: "nginx-service"
  replicas: 1
  selector:
    matchLabels:
      app: local-nginx
  template:
    metadata:
      labels:
        app: local-nginx
    spec:
      containers:
        - name: web-server
          image: nginx
          ports:
            - containerPort: 80
              name: web
          volumeMounts:
            - mountPath: /usr/share/nginx/html
              name: data-volume
  volumeClaimTemplates:
    - metadata:
        name: data-volume
      spec:
        accessModes: [ "ReadWriteOnce" ]
        storageClassName: "local-storage"
        resources:
          requests:
            storage: 5Gi
```

Run and verify:

```shell
$ kubectl apply -f local-pv-test.yaml
persistentvolume/example-local-pv created

$ kubectl apply -f local-sts-test.yaml
service/nginx-service created
statefulset.apps/local-web created

$ kubectl get po -o wide
NAME                     READY   STATUS    RESTARTS   AGE     IP           NODE              NOMINATED NODE   READINESS GATES
local-web-0              1/1     Running   0          50s     10.244.3.8   k8sprodworker01   <none>           <none>

$ kubectl get svc -o wide
NAME            TYPE        CLUSTER-IP       EXTERNAL-IP   PORT(S)   AGE    SELECTOR
nginx-service   ClusterIP   None             <none>        80/TCP    38s    app=local-nginx

$ kubectl get pv
NAME               CAPACITY   ACCESS MODES   RECLAIM POLICY   STATUS   CLAIM                             STORAGECLASS    VOLUMEATTRIBUTESCLASS   REASON   AGE
example-local-pv   5Gi        RWO            Retain           Bound    default/data-volume-local-web-0   local-storage   <unset>                          14m

$ kubectl get pvc
NAME                      STATUS   VOLUME             CAPACITY   ACCESS MODES   STORAGECLASS    VOLUMEATTRIBUTESCLASS   AGE
data-volume-local-web-0   Bound    example-local-pv   5Gi        RWO            local-storage   <unset>                 14m


$ kubectl run -it --rm --image=busybox:1.28 -- sh

/ # wget -q -O - nginx-service.default.svc
Hello from local volume
/ # wget -q -O - local-web-0.nginx-service.default.svc
Hello from local volume
```

### Workflow of Local PV, PVC and StatefulSet Orchestration

1. **PV Provisioning**: the `kube-apiserver` validates the Local PV definition and persists it into `etcd` with an Available status. The `nodeAffinity` rule restricts the PV to node `k8sprodworker01`.

2. **Headless Service Creation**: the `kube-apiserver` creates the headless service, which provides stable DNS naming for StatefulSet pods without load-balancing rules from `kube-proxy`.

3. **VC Creation**: the **StatefulSet Controller** (running in `kube-controller-manager`) uses `volumeClaimTemplates` to automatically create a Pending PVC.

4. **PV-PVC Binding**: the PV Controller binds the PVC to the matching Local PV based on storage class, capacity, and access modes, marking both as **Bound**.

5. **Pod Creation**: only after the PVC is bound, the **StatefulSet Controller** creates the pod `local-web-0` with a stable, predictable identity.

6. **Pod Scheduling**: the `kube-scheduler` enforces the node affinity defined in the PV, scheduling the pod exclusively to `k8sprodworker01`.

7. **Volume Mount & Pod Startup**: the `kubelet` on the target node pulls the container image, sets up networking, mounts the Local PV, and transitions the pod to Running status.

8. **Endpoint & DNS Registration**: the **EndpointSlice Controller** updates service endpoints; **CoreDNS** exposes stable DNS records for cluster-internal access.

## Dynamic Volume Provisioning

Static Local PV requires administrators to pre-create volumes manually and binds them to specific nodes, which reduces automation and scalability in production clusters. To support **automatic**, **on-demand volume creation**, Kubernetes provides **dynamic volume provisioning**, which is implemented through the **Container Storage Interface (CSI)**.

CSI is a standard interface that separates storage driver logic from the Kubernetes core. It allows external storage vendors (such as cloud storage, distributed storage, and SAN/NAS systems) to develop their own drivers, enabling rich storage features without changing Kubernetes source code.

### Dynamic Volume Provisioning Workflow via CSI

1. **PVC Initiation**: A PVC that references a CSI-enabled **StorageClass** is created and enters the **Pending** state.

2. **CSI Driver Monitoring**: The **CSI external-provisioner sidecar** (deployed with the CSI driver) watches for unbound PVCs targeted at its associated StorageClass.

3. **Backend Volume Creation**: The **CSI driver** communicates with the external storage system to create a new volume automatically.

4. **PV Auto-Creation & Binding**: The **CSI driver** creates a PV automatically, and the Kubernetes **PV Controller** binds the PV to the PVC.

5. **Volume Mount & Pod Startup**: During pod startup, kubelet uses the **CSI driver** to attach and mount the volume, making it available to the container.

Compared with **static Local PV**, **dynamic volume provisioning based on CSI** provides elastic, fully automated storage lifecycle management. It is the standard production solution for stateful applications running on cloud, hybrid, and on-premises Kubernetes clusters.