# Spark

**Apache Spark** is a unified, multi-language analytics engine designed for large-scale data processing. In a cloud-native ecosystem, it has evolved from a static cluster resource (YARN/Mesos) to a dynamic, containerized workload that leverages the elasticity of Kubernetes.

## Core Terminology

- **Spark Client**: The "entry point" or submitter (such as a CLI, a Pod, or an Airflow Worker) that triggers the `spark-submit` command. It initiates the application.

- **Driver**: The central coordinator of the Spark application. In K8s, it runs as the Driver Pod. It hosts the SparkContext, analyzes code, and orchestrates the lifecycle of the job.

- **Executor**: Distributed worker nodes (running as Executor Pods) responsible for executing individual tasks and storing data.

- **Spark Application**: The highest-level unit. It represents the entire lifecycle of your user program, from start to finish. One Application can trigger multiple Jobs (DAGs).

- **DAG (Directed Acyclic Graph)**: The logical execution plan created by the Driver. It represents the sequence of Operators applied to the data.

- **Operator**: Functions like `map`, `filter`, or `join` that perform operations on data.

- **RDD (Resilient Distributed Dataset)**: The fundamental data abstraction, a fault-tolerant collection of elements partitioned across nodes.

- **DataFrame/DataSet**: Higher-level, schema-aware abstractions that sit on top of RDDs, offering better performance via the Catalyst Optimizer.


###  Spark Workflow in K8S

1. **Submission**: **Spark Client** initiates the **Application**.

2. **Orchestration**: K8s starts the **Driver** Pod.

3. **Execution**: The Driver asks K8s for **Executor** Pods, splits the **DAG** into Stages/Tasks, and dispatches them.

4. **Data Processing**: **Executors** run the **Operators** on data partitions in memory.

5. **Cleanup**: Once the **Application** ends, K8s destroys all Pods.

## Deploy Spark in Kubernetes

To run Spark on Kubernetes, a **ServiceAccount** with pod management permissions is required. Apply the following YAML to create the identity and tools pod.

```yaml
apiVersion: v1
kind: ServiceAccount
metadata:
  name: spark
  namespace: test
---
apiVersion: rbac.authorization.k8s.io/v1
kind: ClusterRoleBinding
metadata:
  name: spark-role-binding
subjects:
  - kind: ServiceAccount
    name: spark
    namespace: test
roleRef:
  kind: ClusterRole
  name: edit
  apiGroup: rbac.authorization.k8s.io
---
apiVersion: v1
kind: Pod
metadata:
  name: spark-client
  namespace: test
spec:
  serviceAccountName: spark
  containers:
  - name: spark-tools
    image: apache/spark:latest
    command: ["/bin/sh", "-c", "sleep infinity"]
```

The **Spark Driver** runs as a Kubernetes Pod, and dynamically launches **Executor** pods to run distributed tasks.

```shell
$ kubectl exec -it spark-client -n test -- bash

$ /opt/spark/bin/spark-submit \
  --master k8s://https://kubernetes.default.svc \
  --deploy-mode cluster \
  --conf spark.kubernetes.namespace=test \
  --conf spark.kubernetes.container.image=apache/spark:latest \
  --conf spark.executor.instances=2 \
  --conf spark.kubernetes.authenticate.driver.serviceAccountName=spark \
  local:///opt/spark/examples/src/main/python/pi.py
  
$ kubectl get po -n test
NAME                                                        READY   STATUS      RESTARTS   AGE
pi-py-46b73e9db32bf3a5-driver                               1/1     Running     0          28s
pythonpi-1e313e9db32c2490-exec-1                            1/1     Running     0          15s
pythonpi-1e313e9db32c2490-exec-2                            1/1     Running     0          15s
spark-client                                                1/1     Running     0          22h
```

Understand the lifecycle via logs:

```shell
$ kubectl logs -n test pi-py-46b73e9db32bf3a5-driver
# Spark application has been submitted and started
26/04/22 03:11:46 INFO SparkContext: Submitted application: PythonPi

# Driver requests 2 executor pods from Kubernetes
26/04/22 03:11:50 INFO ExecutorPodsAllocator: Going to request 2 executors from Kubernetes for ResourceProfile Id: 0, target: 2, known: 0, sharedSlotFromPendingPods: 2147483647.

# Executor pod connected to driver successfully
26/04/22 03:11:57 INFO KubernetesClusterSchedulerBackend$KubernetesDriverEndpoint: Registered executor NettyRpcEndpointRef(spark-client://Executor) (10.244.3.102:41238) with ID 2, ResourceProfileId 0
26/04/22 03:11:57 INFO KubernetesClusterSchedulerBackend$KubernetesDriverEndpoint: Registered executor NettyRpcEndpointRef(spark-client://Executor) (10.244.4.41:33864) with ID 1, ResourceProfileId 0

# Start running the actual computation job
26/04/22 03:12:04 INFO SparkContext: Starting job: reduce at /opt/spark/examples/src/main/python/pi.py:42

# Task finished on executor
26/04/22 03:12:07 INFO TaskSetManager: Finished task 0.0 in stage 0.0 (TID 0) in 2657 ms on 10.244.3.102 (executor 2) (1/2)
26/04/22 03:12:07 INFO TaskSetManager: Finished task 1.0 in stage 0.0 (TID 1) in 2899 ms on 10.244.4.41 (executor 1) (2/2)
26/04/22 03:12:07 INFO DAGScheduler: Job 0 finished: reduce at /opt/spark/examples/src/main/python/pi.py:42, took 3296.390034 ms

# Calculation result printed by Python code
Pi is roughly 3.141880

# Spark application stopped
26/04/22 03:12:07 INFO SparkContext: Successfully stopped SparkContext (Uptime: 21702 ms)
```

Learn this PySpark code `/opt/spark/examples/src/main/python/pi.py`.

```python
import sys
from random import random
from operator import add
from pyspark.sql import SparkSession

if __name__ == "__main__":
    """
        Usage: pi [partitions]
    """
    # Create a Spark session named "PythonPi"
    spark = SparkSession.builder.appName("PythonPi").getOrCreate()

     # Get partition count from command line, default to 2 if not provided
    partitions = int(sys.argv[1]) if len(sys.argv) > 1 else 2
    n = 100000 * partitions

    # Define the mapping function: 
    # For each input, pick a random point (x, y) in a 2x2 square and 
    # check if it falls inside the unit circle (x^2 + y^2 <= 1)
    def f(_: int) -> float:
        x = random() * 2 - 1
        y = random() * 2 - 1
        return 1 if x ** 2 + y ** 2 <= 1 else 0

    # --- CORE SPARK LOGIC ---
    # 1. parallelize: Create an RDD by distributing the local range to the cluster
    # 2. map(f): [Transformation] Apply function 'f' to every element in parallel
    # 3. reduce(add): [Action] Aggregate the results (sum up all 1s) to the Driver
    count = spark.sparkContext.parallelize(range(1, n + 1), partitions).map(f).reduce(add)
    
    # Calculate Pi using the ratio: Area_circle / Area_square = Pi / 4
    print("Pi is roughly %f" % (4.0 * count / n))

    # Stop Spark session
    spark.stop()
```