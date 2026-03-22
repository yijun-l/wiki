# Workload Controllers

Kubernetes operates around two core concepts: **resource controllers** and **desired state**. Each controller continuously watches the cluster state via the API server, reconciling any discrepancies between current and desired states to ensure the cluster behaves as intended.

We focus on **Workload Resources** first. A workload is an application running on Kubernetes via Pods, which encapsulates containers, storage, and network resources. Workload Resources are higher-level abstractions built on Pods, handling replica management, updates, and stateful application needs, eliminating manual intervention.

## Pod and ReplicaSet

A **Pod (po)** is Kubernetes’ smallest schedulable unit, typically running one container. Containers in a Pod share network, storage, and lifecycle.

A Pod does not have its own dedicated controller. Instead, its scheduling and lifecycle coordination are managed by other core components, primarily the `kube-scheduler`.

When a Pod creation request is initiated:
- A request is submitted to the `kube-apiserver` and persisted.
- The `kube-scheduler` detects the unscheduled Pod and assigns it to an appropriate node.
- The `kubelet` on the target node observes the newly assigned Pod and instructs the **CRI** (e.g., `containerd`) to create and start the container.
- The **CRI** reports container and Pod status back to the `kube-apiserver`.

```yaml
apiVersion: v1
kind: Pod
metadata:
  name: nginx
spec:
  containers:
    - name: nginx
      image: nginx:1.14.2
      ports:
        - containerPort: 80
```

Pods are ephemeral and not self-healing. If a Pod fails, it will not be recreated automatically.

A **ReplicaSet (rs)** fixes this by ensuring a stable number of identical Pods using `replicas`, a `selector`, and a Pod `template`. It automatically replaces failed Pods and removes extra Pods when the desired count changes.

ReplicaSets are generally not used directly. They act as a foundational controller for higher-level resources like **Deployments**.

```yaml
apiVersion: apps/v1
kind: ReplicaSet
metadata:
  name: nginx-rs
spec:
  replicas: 3
  selector:
    matchLabels:
      app: nginx
  template:
    metadata:
      labels:
        app: nginx
    spec:
      containers:
        - name: nginx
          image: nginx:1.14.2
          ports:
            - containerPort: 80
```

## Deployment

The **Deployment (deploy)** is Kubernetes’ most widely used **stateless workload resource** (e.g. web servers), built on top of **ReplicaSets**. It enables declarative application updates and rollbacks.

Deployments come with several key features to simplify application management. Its default strategy is **rolling updates**, which creates new **ReplicaSets** gradually while terminating old ones to ensure zero downtime. They also support **rollbacks**, allowing you to revert to previous stable revisions by leveraging Kubernetes’ revision history. 

Additionally, deployments enable both **manual scaling** by updating the `spec.replicas` field and **automatic scaling** through the **Horizontal Pod Autoscaler (HPA)**.


```yaml
apiVersion: apps/v1
kind: Deployment
metadata:
  name: nginx-deploy
spec:
  replicas: 3
  selector:
    matchLabels:
      app: nginx
  template:
    metadata:
      labels:
        app: nginx
    spec:
      containers:
      - name: nginx
        image: nginx:1.14.2
        ports:
        - containerPort: 80
```

## StatefulSet

A **StatefulSet (sts)** manages stateful applications (e.g., databases, Kafka) requiring stable identities, network, and storage. It provides **predictable Pod names**, **fixed DNS hostnames**, **unique PVCs** that persist after Pod deletion, and ordered deployment/termination to maintain data integrity.

```yaml
apiVersion: apps/v1
kind: StatefulSet
metadata:
  name: mysql-sts
spec:
  serviceName: mysql-service
  replicas: 2
  selector:
    matchLabels:
      app: mysql
  template:
    metadata:
      labels:
        app: mysql
    spec:
      containers:
      - name: mysql
        image: mysql:8.0
        ports:
        - containerPort: 3306
        volumeMounts:
        - name: mysql-data
          mountPath: /var/lib/mysql
  volumeClaimTemplates:
  - metadata:
      name: mysql-data
    spec:
      accessModes: [ "ReadWriteOnce" ]
      resources:
        requests:
          storage: 10Gi
```

## DaemonSet

A **DaemonSet (ds)** ensures one Pod runs on every (or selected) cluster node, used for node-level tasks like network plugins, log collectors, and monitoring agents.

It auto-adds/removes Pods as nodes join/leave.

```yaml
apiVersion: apps/v1
kind: DaemonSet
metadata:
  name: node-exporter
spec:
  selector:
    matchLabels:
      app: node-exporter
  template:
    metadata:
      labels:
        app: node-exporter
    spec:
      containers:
      - name: node-exporter
        image: prom/node-exporter:v1.5.0
        ports:
        - containerPort: 9100
```

## CronJob

A **Job** runs one-time tasks to completion with retries. 

A **CronJob (cj)** schedules recurring Jobs via Cron syntax, using a `jobTemplate`.

It uses a Cron schedule (e.g., `0 3 * * *`), specifies Pod config via `jobTemplate`, controls concurrency, and retains a fixed number of job histories.

```yaml
apiVersion: batch/v1
kind: CronJob
metadata:
  name: backup-cronjob
spec:
  schedule: "0 3 * * *" # Daily at 3 AM
  jobTemplate:
    spec:
      template:
        spec:
          containers:
          - name: backup
            image: busybox:1.35
            command: ["/bin/sh", "-c", "echo 'Backup completed'"]
          restartPolicy: OnFailure
```