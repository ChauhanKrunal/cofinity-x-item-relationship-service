# Item Relationship Service

## Deployment of the IRS with all dependencies

### Step 1: Prerequisites

1. [Rancher Desktop](https://docs.rancherdesktop.io/getting-started/installation/) is installed and running
2. [helm](https://helm.sh/docs/intro/install/) is installed
3. [kubectl](https://kubernetes.io/docs/tasks/tools/) is installed
4. [Python3](https://www.python.org/downloads/) is installed
5. [Ruby](https://www.ruby-lang.org/de/documentation/installation/) is installed (optional)
6. [psql](https://www.compose.com/articles/postgresql-tips-installing-the-postgresql-client/) client is installed (optional)
7. **CURRENTLY STILL NECESSARY:** Add the digital twin secret in the file:

   ```bash
   ./template/digital-twin-registry-docker-secret.yaml
   ```

   Get the **digital twin secret**  via issue in the following [Digital Twin Repository](https://github.com/eclipse-tractusx/sldt-digital-twin-registry) request the image secret for the private digital twin registry image.

### Step 2: Check out the code

Check out the project [Item Relationship Service](https://github.com/eclipse-tractusx/item-relationship-service) or download a [released version](https://github.com/eclipse-tractusx/item-relationship-service/releases) of the Item Relationship Service

### Step 3: Installing the services

#### 1. Start the cluster

To deploy the services on kubernetes, run

```bash
./start.sh true true
```

The script takes 2 parameters as input:

* INSTALL_EDC: default is set to true. If this is passed as true, will delete all helm charts related to EDC (vault, DAPS, EDC consumer and EDC provider) and install them again.
* INSTALL_IRS: default is set to true. If this is passed as true, will delete all helm charts related to IRS (dependencies, IRS backend and IRS frontend) and install them again.

This can take up to **20 minutes**.

When the deployment is finished you can expect that 13 deployments can be seen in the minikube dashboard:

* irs-frontend
* irs
* irs-minio
* keycloak (mocked Service)
* digital-twin-registry
* semantic-hub (mocked Service)
* irs-provider-backend
* edc-provider-control-plane
* edc-provider-data-plane
* edc-consumer-control-plane
* edc-consumer-data-plane
* edc-vault-agent-injector

Also in total 17 Pods are up and running.

**INFO**: sometimes you will get the following message during deployment, which can be ignored. This is caused when a service takes longer than 90 seconds to be available.

```bash
-e Waiting for the deployments to be available
error: timed out waiting for the condition on deployments/irs-frontend
```

##### 1.1 Get the Status of the deployment

You can see the status of the deployment in Rancher Desktop Dashboard

![expected status](img/rancher-desktop-dashboard.png)

#### 2. Forwarding Ports

When the deployment has been finished please use the script to forward the ports:

```bash
./forwardingPorts.sh
```

After that you can access the:

* **Digital Twin Registry:** [http://localhost:10200](http://localhost:10200)
* **IRS Frontend:** [http://localhost:3000](http://localhost:3000)

#### 3. Prepare test data

> Only if Step 2 has been applied and the ports are forwarded.

To provision Testdate to the provider EDC and register the testdata to the digital twin registry use the following script:

```bash
./upload-testdata.sh
```

If you like, you can remove the test data with:

```bash
./deleteIRSTestData.sh
```

### Step4: access Debuggin View

open [http://localhost:3000/](http://localhost:3000/) and you should see the Item Relationship Service login screen:

![irs-login](img/irs-login.png)

## Testing the Item Relationship Service

You can use several approaches to interact with the IRS. One is through the **IRS API** and another way is through the **IRS API Frontend**.

### Valid Global Asset Ids for Testing

use these globalAssetId's for testing

| globalAssetId                                 | type                      |
|-----------------------------------------------|---------------------------|
| urn:uuid:d3c0bf85-d44f-47c5-990d-fec8a36065c6 | vehicle combustion engine |
| urn:uuid:61a22b1c-5725-41fb-8e1e-dccaaba83838 | vehicle combustion engine |
| urn:uuid:513d7be8-e7e4-49f4-a22b-8cd31317e454 | vehicle combustion engine |

### Valid test requests for testing

use these snippets for testing purposes.

```json
{
  "aspects": [
    "AssemblyPartRelationship",
    "SerialPartTypization"
  ],
  "bomLifecycle": "asBuilt",
  "collectAspects": true,
  "direction": "downward",
  "depth": 10,
  "globalAssetId": "urn:uuid:d387fa8e-603c-42bd-98c3-4d87fef8d2bb"
}
````

```json
{
  "aspects": [
    "SerialPartTypization"
  ],
  "depth": 1,
  "globalAssetId": "urn:uuid:d387fa8e-603c-42bd-98c3-4d87fef8d2bb"
}
```

<!-- #### Get global asset id

1. Forward ports of digital twin database: ``` kubectl port-forward svc/digital-twin-registry-database 5432:5432 ```
2. Connect to the database: ``` export PGPASSWORD=digital-twin-registry-pass; psql -h localhost -p 5432 -d digital-twin-registry -U digital-twin-registry-user ```
3. Execute query to get the global asset id: ``` select id_external from shell where id_short = 'VehicleCombustion' limit 1; ``` -->

### Testing the IRS API Endpoints

Precondition:

* Visual Studio extension: [REST Client by Huachao Mao](https://marketplace.visualstudio.com/items?itemName=humao.rest-client)
* All Installation-Steps have been successfully conducted
* A valid Global Asset Id

Test-steps:

1. to interact with the API Endpoints, you need a valid token. You can generate an access token with using the ``` ./test/keycloack-service.rest ```.

2. **copy & past** the to valid token into line 8 of ``` ./test/irs-backend-service.rest ```
3. **copy & past** a valid globalAssetId into the request body
4. **execute the request** ```./test/irs-backend-service.rest```

#### Testing with the IRS frontend

Precondition:

* All Installation-Steps have been successfully conducted

Test-steps:

1. **open** [http://localhost:3000](http://localhost:3000) and click 'Login'
2. **copy & past** a valid globalAssetId into the request body
3. **click** 'Build Data Chain' to start a new IRS job
4. **open** visualization to see result of the job

----

## Deployment to ArgoCD
**IMPORTANT: This is only for demonstration purposes and must not be used in a production environment.**

It is also possible to deploy the chart to ArgoCD. 
Take [values-dev.yaml](values-dev.yaml) as reference.  
The following hostnames have to be changed. They will be used for the ingresses:

- config-edc-consumer-controlplane-hostname
- config-edc-consumer-dataplane-hostname
- config-edc-provider-controlplane-hostname
- config-edc-provider-dataplane-hostname
- config-keycloak-hostname
- config-irs-hostname
- config-irs-url
- config-digitalTwin-host-name
- config-provider-backend-hostname
- config-irs-frontend-hostname

In case your ArgoCD instance already has a HashiCorp Vault, you don't need to deploy your own and can use the existing one instead.

## Additional Information

### Generate Key Cloak token

Precondition:

* Visual Studio extension: [REST Client by Huachao Mao](https://marketplace.visualstudio.com/items?itemName=humao.rest-client)
* All Installation-Steps have been successfully conducted

Using the ```./test/keycloack-service.rest```, you can execute the token request to get a new token.

### Generate DAPS token

Precondition:

* Visual Studio extension: [REST Client by Huachao Mao](https://marketplace.visualstudio.com/items?itemName=humao.rest-client)
* All Installation-Steps have been successfully conducted
* Token used as client assertion should be created with the script: ```./daps/create_test_token```.

``` bash
  ruby create_test_token.rb edc ./keys/edc.key
```

where edc is a client

Using the ``` ./test/omejdn-service.rest.rest ```, you can execute the token request to get a new token.

### HELM

Initiate the helm repositories

``` bash
helm repo list

helm repo add bitnami https://charts.bitnami.com/bitnami
helm repo add vault https://helm.releases.hashicorp.com
helm repo add grafana https://grafana.github.io/helm-charts
helm repo add prometheus https://prometheus-community.github.io/helm-charts
helm repo add minio https://charts.min.io/
helm repo add cx-backend-service https://denisneuling.github.io/cx-backend-service
helm repo add codecentric https://codecentric.github.io/helm-charts

```

Build and update helm charts

``` bash
helm dependency build
helm dependency update
```

Install the helm chart

``` bash
helm install irs --namespace irs --create-namespace .
```

Uninstall the helm chart

``` bash
helm uninstall irs --namespace irs
```

### Kubernetes

Change default namespace

``` bash
kubectl config set-context rancher-desktop --namespace=irs
```

### VAULT

``` bash
kubectl port-forward svc/edc-vault 8200:8200
```

### Daps

``` bash
kubectl port-forward svc/edc-daps 4567:4567
```

Register new client

``` bash
register_connector.sh edc
```

Get client token

``` bash
ruby create_test_token.rb edc ./keys/edc.key
```

### EDC Consumer

#### EDC Consumer Database

``` bash
kubectl port-forward svc/edc-consumer-database 5432:5432

export PGPASSWORD=edc-consumer-pass; psql -h localhost -p 5432 -d edc-consumer -U edc-consumer-user

psql \d

SELECT * FROM edc_asset;
SELECT * FROM edc_policydefinitions;

```

#### EDC Consumer Control Plane

``` bash
kubectl port-forward svc/edc-consumer-control-plane 7181:8181 7080:8080
```

#### EDC Consumer Data Plane

### EDC Provider

#### EDC Provider Database

``` bash
kubectl port-forward svc/edc-provider-database 5432:5432

export PGPASSWORD=edc-provider-pass; psql -h localhost -p 5432 -d edc-provider -U edc-provider-user

psql \d

SELECT * FROM edc_asset;
SELECT * FROM edc_policydefinitions;

```

#### EDC Provider Control Plane

``` bash
kubectl port-forward svc/edc-provider-control-plane 8181:8181 8080:8080
```

#### EDC Provider Data Plane

TODO: Is this section needed?

## IRS Dependencies

### Semantic Hub

``` bash
kubectl port-forward svc/semantic-hub 8088:8080
```

### Digital Twins Registry Database

``` bash

kubectl port-forward svc/digital-twin-registry-database 5432:5432

export PGPASSWORD=digital-twin-registry-pass; psql -h localhost -p 5432 -d digital-twin-registry -U digital-twin-registry-user

select * from shell where id_short = 'VehicleCombustion';
psql \l
```

### Digital Twins Registry

``` bash

kubectl port-forward svc/digital-twin-registry 8080:8080
```

### KeyCloak

``` bash

kubectl port-forward svc/keycloak 4011:8080
```

### IRS Provider Backend

``` bash
kubectl port-forward svc/irs-provider-backend 8080:8080
```


## IRS Service

### Grafana

``` bash
kubectl port-forward svc/irs-grafana 4000:80
```

### Prometheus

``` bash
kubectl port-forward svc/irs-prometheus-server 9090:80
```

### Minio

``` bash
kubectl port-forward svc/irs-minio 9000:9000
kubectl port-forward svc/irs-minio-console 9001:9001
```

### IRS Backend Service

``` bash
kubectl port-forward svc/irs 8080:8080 8181:8181 4004:4004
```

### IRS Frontend Service

``` bash

kubectl port-forward svc/irs-frontend 3000:8080
```

### Next steps

1. Helm Security issues
2. Use the official digital twin chart
3. Deploy on a cloud provider
4. Vault: store certificates in secrets
5. Add liveness and readiness probe for irs provider backend service
6. Use the DAPS chart form: https://catenax-ng.github.io/product-DAPS


### Additional

1. Grafana Dashboards for EDC control plane and data plane
2. Grafana Dashboards for database
3. Helm documentation
4. Version of kubernetes and updating to a new version ?

### Decisions

1. No BPN mockup
2. Use a custom helm chart for semantic hub.
   1. Reason: test data provided
   2. Next steps:  
      1. Use the official helm chart from: https://eclipse-tractusx.github.io/sldt-semantic-hub
      2. Include scripts to provide test data into the script ``` upload-testdata.sh ```
3. Use a custom helm chart for daps.
   1. Reason: already configured with a default client
   2. Next steps:  
      1. Use the official helm chart from: https://catenax-ng.github.io/product-DAPS
      2. Provide configuration with default client from start.
4. Use a custom helm chart for digital twin
   1. Reason: A secret for pulling docker images is missing to use the default chart from: https://eclipse-tractusx.github.io/sldt-digital-twin-registry

