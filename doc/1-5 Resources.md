# Resources

**Kubernetes manages a rich set of core resources** (e.g., Pod, Deployment, StatefulSet, Service) **paired with dedicated controllers** (orchestrated by `kube-controller-manager`). It operates on a **declarative model** at its core: administrators define the desired state of resources, and Kubernetes' control plane continuously works to reconcile the actual state of the cluster with that expectation.

Under the hood, all controllers establish persistent watch connections to the `kube-apiserver` to monitor resource events. When a user applies a YAML manifest to declare a resource: the `kube-apiserver` validates the configuration, persists the resource object to the distributed key-value store `etcd`, and broadcasts a change event to all registered watchers. The target controller then triggers its predefined workflow to create, update, or delete underlying resources and enforce the desired state.

## Monitor Resource Storage in `etcd`

All Kubernetes resources are stored as key-value pairs in `etcd`, the single source of truth for the cluster. We can directly inspect resource entries to verify their persistence.

```shell
# 1. Log into a Kubernetes master node as the root user

# 2. Configure crictl for container runtime communication
$ cat <<EOF | sudo tee /etc/crictl.yaml
runtime-endpoint: unix:///run/containerd/containerd.sock
image-endpoint: unix:///run/containerd/containerd.sock
timeout: 10
debug: false
pull-image-on-create: false
EOF

# 3. Create an alias for etcdctl to interact with the cluster's etcd instance
$ alias k-etcd='crictl exec -it $(crictl ps --name etcd -q) etcdctl \
--endpoints=https://127.0.0.1:2379 \
--cacert=/etc/kubernetes/pki/etcd/ca.crt \
--cert=/etc/kubernetes/pki/etcd/server.crt \
--key=/etc/kubernetes/pki/etcd/server.key'

# 4. List all resource keys stored in etcd under the /registry prefix, and grep specific resources (namespace)
$ k-etcd get /registry --prefix --keys-only | grep namespaces
/registry/namespaces/default
/registry/namespaces/kube-flannel
/registry/namespaces/kube-node-lease
/registry/namespaces/kube-public
/registry/namespaces/kube-system
```

## Validate Resource Lifecycle with Namespace

**Namespace** is a native Kubernetes resource used for logical cluster isolation. We can track its full lifecycle (creation to deletion) in both the `kube-apiserver` and `etcd` to confirm resource persistence.

```shell
## Create a new Namespace

$ kubectl create ns test
namespace/test created

$ kubectl get ns
NAME              STATUS   AGE
default           Active   13d
kube-flannel      Active   13d
kube-node-lease   Active   13d
kube-public       Active   13d
kube-system       Active   13d
test              Active   5s


$ k-etcd get /registry/namespaces --prefix --keys-only
/registry/namespaces/default
/registry/namespaces/kube-flannel
/registry/namespaces/kube-node-lease
/registry/namespaces/kube-public
/registry/namespaces/kube-system
/registry/namespaces/test

## Delete the Namespace

$ kubectl delete ns test
namespace "test" deleted

$ kubectl get ns
NAME              STATUS   AGE
default           Active   13d
kube-flannel      Active   13d
kube-node-lease   Active   13d
kube-public       Active   13d
kube-system       Active   13d

$ k-etcd get /registry/namespaces --prefix --keys-only
/registry/namespaces/default
/registry/namespaces/kube-flannel
/registry/namespaces/kube-node-lease
/registry/namespaces/kube-public
/registry/namespaces/kube-system
```

## Core Workflow of Kubernetes Resources

The Kubernetes resource control loop follows a standardized, automated workflow across all resource types:
1. The `kube-apiserver` hosts and enforces Resource Schemas (API groups, versions, and object types).
2. The `kube-controller` runs dedicated controllers that continuously watch the `kube-apiserver` for resource create/update/delete events.
3. When the `kube-apiserver` receives a manifest (resource request), it validates the object, saves it to `etcd`, and emits a state change event.
4. The listening controller detects the event, compares the desired state (from the manifest) with the actual state (live cluster status), and runs the reconciliation loop to align the cluster with the desired state.

### Extend Resources with CRD

Kubernetes allows users to define custom resources via **Custom Resource Definitions (CRDs)**, extending the API with user-defined object types without modifying Kubernetes core code. 

Combined with a custom controller, CRDs enable full lifecycle management of custom resources.

#### 1. Define a CRD: `fruit-crd.yaml`

```yaml
apiVersion: apiextensions.k8s.io/v1
kind: CustomResourceDefinition
metadata:
  name: fruits.stable.example.com
spec:
  group: stable.example.com
  versions:
    - name: v1
      served: true
      storage: true
      schema:
        openAPIV3Schema:
          type: object
          properties:
            spec:
              type: object
              properties:
                color:
                  type: string
                sweetness:
                  type: integer
  scope: Namespaced
  names:
    plural: fruits
    singular: fruit
    kind: Fruit
    shortNames:
    - fr
```
#### 2. Create a Custom Resource Instance: `apple.yaml`

```yaml
apiVersion: stable.example.com/v1
kind: Fruit
metadata:
  name: apple
spec:
  color: "red"
  sweetness: 8
```

#### 3. Build a Simple Custom Controller: `fruit-controller.sh `

This lightweight shell controller watches the Fruit custom resource and validates the sweetness field.

```shell
#!/bin/bash

echo "Fruits controller is running..."

# Start the Watch stream
kubectl get fr --watch -o jsonpath='{.spec.sweetness}' | while read -r SWEET
do
    if [ "$SWEET" -gt 10 ]; then
        echo "STATUS: ALERT - ($SWEET) is too sweet"
    else
        echo "STATUS: OK - ($SWEET) is normal"
    fi
done
```

#### 4. Deploy & Test the CRD Workflow

```shell
$ kubectl apply -f fruit-crd.yaml
customresourcedefinition.apiextensions.k8s.io/fruits.stable.example.com created

$ kubectl api-resources | grep fruits
fruits                              fr           stable.example.com/v1             true         Fruit

$ kubectl apply -f apple.yaml
fruit.stable.example.com/apple created

$ kubectl get fr
NAME    AGE
apple   15s

$ k-etcd get /registry --prefix --keys-only | grep fruits
/registry/apiextensions.k8s.io/customresourcedefinitions/fruits.stable.example.com
/registry/stable.example.com/fruits/default/apple

$ bash fruit-controller.sh &
Frusts controller is running...

$ kubectl patch fruit apple --type='merge' -p '{"spec":{"sweetness":20}}'
fruit.stable.example.com/apple patched
STATUS: ALERT - (20) is too sweet
```