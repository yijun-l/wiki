# Cert Manager

**Cert Manager** is a critical add-on for Kubernetes that **automates the issuance, renewal, and management of TLS/SSL certificates**. It eliminates manual certificate handling, ensuring secure communication between Kubernetes components, services, and external clients.

## Core Components

Cert Manager relies on three key components to function, all deployed in the cert-manager namespace:

- **cert-manager Controller**: The core component that orchestrates certificate issuance and renewal. It watches for `Certificate` resources and interacts with issuers to fulfill certificate requests.

- **cainjector**: Injects **CA (Certificate Authority)** certificates into Kubernetes resources (e.g.,`ValidatingWebhookConfiguration`), ensuring components trust the issued certificates.

- **webhook**: Validates `Certificate` and `Issuer` resources to ensure they follow the correct schema, preventing misconfigurations.

Cert-manager leverages three key Custom Resources (CRs) to automate the certificate lifecycle:

- `Issuer`: A namespace-scoped resource that defines a CA (e.g., self-signed, Let’s Encrypt) to issue certificates for resources in its namespace.

- `ClusterIssuer`: A cluster-scoped Issuer that can issue certificates for resources across all namespaces—ideal for shared CAs.

- `Certificate`: Defines the desired TLS certificate (e.g., common name, DNS names) and references an Issuer/ClusterIssuer to request issuance.

## Deploy Cert Manager in Kubernetes

Deploying Cert Manager is straightforward using its official YAML manifest.

```shell
$ kubectl apply -f https://github.com/cert-manager/cert-manager/releases/download/v1.20.0/cert-manager.yaml

$ kubectl get po -n cert-manager
NAME                                       READY   STATUS    RESTARTS   AGE
cert-manager-66487b786-xrpd8               1/1     Running   0          97s
cert-manager-cainjector-65fdc9b9d7-wb7mg   1/1     Running   0          97s
cert-manager-webhook-7d96469bf8-2s77x      1/1     Running   0          97s
```

For internal cluster communication, we’ll create a self-signed CA (Root CA) using an Issuer, then use it to create a `ClusterIssuer` for cross-namespace certificate issuance.

```yaml
apiVersion: cert-manager.io/v1
kind: Issuer
metadata:
  name: internal-issuer
  namespace: cert-manager
spec:
  selfSigned: {}
---
apiVersion: cert-manager.io/v1
kind: Certificate
metadata:
  name: internal-ca
  namespace: cert-manager
spec:
  isCA: true
  commonName: "Internal Root CA"
  secretName: internal-ca-secret
  issuerRef:
    name: internal-issuer
    kind: Issuer
    group: cert-manager.io
---
apiVersion: cert-manager.io/v1
kind: ClusterIssuer
metadata:
  name: internal-ca-issuer
spec:
  ca:
    secretName: internal-ca-secret
```

Apply the manifest and verify the `ClusterIssuer` is ready:

```shell
$ kubectl apply -f ca-file.yaml
issuer.cert-manager.io/internal-issuer created
certificate.cert-manager.io/internal-ca created
clusterissuer.cert-manager.io/internal-ca-issuer created

$ kubectl get clusterissuer
NAME                 READY   AGE
internal-ca-issuer   True    20s
```

### Sign an Identity Certificate for an Internal HTTPS Service

Next, we’ll issue a TLS certificate for an internal Nginx service using the `internal-ca-issuer`. This ensures secure HTTPS communication within the cluster.

```yaml
apiVersion: cert-manager.io/v1
kind: Certificate
metadata:
  name: nginx-test-cert
  namespace: test
spec:
  secretName: nginx-test-tls
  issuerRef:
    name: internal-ca-issuer
    kind: ClusterIssuer
  commonName: nginx-test.test.svc
  dnsNames:
    - nginx-test.test.svc
---
apiVersion: v1
kind: Pod
metadata:
  name: nginx-test
  namespace: test
  labels:
    app: nginx-test
spec:
  containers:
    - name: nginx
      image: nginx:alpine
      command: ["/bin/sh", "-c"]
      args:
        - |
          echo "server { 
              listen 443 ssl; 
              ssl_certificate /etc/nginx/ssl/tls.crt; 
              ssl_certificate_key /etc/nginx/ssl/tls.key; 
              location / { root /usr/share/nginx/html; }
          }" > /etc/nginx/conf.d/default.conf && nginx -g 'daemon off;'
      ports:
        - containerPort: 443
      volumeMounts:
        - name: tls-vol
          mountPath: /etc/nginx/ssl
          readOnly: true
  volumes:
    - name: tls-vol
      secret:
        secretName: nginx-test-tls
---
apiVersion: v1
kind: Service
metadata:
  name: nginx-test
  namespace: test
spec:
  selector:
    app: nginx-test
  ports:
    - port: 443
      targetPort: 443
```

Apply the manifest and test the HTTPS service:

```shell
$ kubectl apply -f nginx-test.yaml
certificate.cert-manager.io/nginx-test-cert created
pod/nginx-test created
service/nginx-test created

$ kubectl run tester -n test -it --rm --image=curlimages/curl -- /bin/sh
$ curl -kiv https://nginx-test.test.svc
* Server certificate:
*   subject: CN=nginx-test.test.svc
*   issuer: CN=Internal Root CA
```

## Integrate Istio with Cert Manager

Istio uses **mutual TLS (mTLS)** for service-to-service communication. We can integrate Cert Manager with Istio using **istio-csr**, a component that acts as a bridge between Istio and Cert Manager, allowing Istio to use Cert Manager-issued certificates.

### Install Helm

Helm is required to install istio-csr. 

```shell
$ sudo apt-get install curl gpg apt-transport-https --yes
$ curl -fsSL https://packages.buildkite.com/helm-linux/helm-debian/gpgkey | gpg --dearmor | sudo tee /usr/share/keyrings/helm.gpg > /dev/null
$ echo "deb [signed-by=/usr/share/keyrings/helm.gpg] https://packages.buildkite.com/helm-linux/helm-debian/any/ any main" | sudo tee /etc/apt/sources.list.d/helm-stable-debian.list
$ sudo apt-get update
$ sudo apt-get install helm
```

### Install Istio-csr

Export the internal CA certificate and create a secret for Istio. Then install **istio-csr** using Helm, configuring it to use our **internal-ca-issuer**.

```shell
# Export the internal CA certificate
$ kubectl get -n cert-manager secret internal-ca-secret -ogo-template='{{index .data "tls.crt"}}' | base64 -d > ca.pem

# Create a secret for Istio to trust the CA
$ kubectl create secret generic -n cert-manager istio-root-ca --from-file=ca.pem=ca.pem
secret/istio-root-ca created

$ helm upgrade cert-manager-istio-csr oci://quay.io/jetstack/charts/cert-manager-istio-csr \
  --install \
  --namespace cert-manager \
  --wait \
  --set app.certmanager.issuer.name=internal-ca-issuer \
  --set app.certmanager.issuer.kind=ClusterIssuer \
  --set app.certmanager.issuer.group=cert-manager.io \
  --set "app.tls.rootCAFile=/var/run/secrets/istio-csr/ca.pem" \
  --set "volumeMounts[0].name=root-ca" \
  --set "volumeMounts[0].mountPath=/var/run/secrets/istio-csr" \
  --set "volumes[0].name=root-ca" \
  --set "volumes[0].secret.secretName=istio-root-ca" \
  --set app.server.caTrustedNodeAccounts=istio-system/ztunnel
  
$ kubectl set env deployment/istiod -n istio-system ENABLE_CA_SERVER=false CA_ADDRESS=cert-manager-istio-csr.cert-manager.svc:443
$ kubectl set env daemonset/ztunnel -n istio-system CA_ADDRESS=cert-manager-istio-csr.cert-manager.svc:443
```

### How It Works

The integration between **Istio** and **cert-manager** via **istio-csr** follows this flow:
- An **Istio** component such as **ztunnel** generates a private key and CSR, then sends it to **istiod** over SDS (Secret Discovery Service).
- **istiod** forwards the CSR to **istio-csr** via gRPC.
- **istio-csr** creates a `CertificateRequest` to request signing from **cert-manager**.
- **cert-manager** validates the request and signs the certificate using the configured internal CA issuer.
- **istio-csr** watches the `CertificateRequest`, retrieves the signed certificate, and returns it to **istiod**.
- **istiod** distributes the certificate to the target Istio workload (e.g., ztunnel) for mTLS use.