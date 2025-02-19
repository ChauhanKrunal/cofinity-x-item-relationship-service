# Changelog
All notable changes to this project will be documented in this file.

The format is based on [Keep a Changelog](https://keepachangelog.com/en/1.0.0/),
and this project adheres to [Semantic Versioning](https://semver.org/spec/v2.0.0.html).

## [Unreleased]

## [5.0.9] - 2023-03-30
### Fixed
- Moved license headers out of if clauses and add dashes (`---`) after each license header

## [5.0.8] - 2023-03-30
### Fixed
- Moved license headers into if clauses to avoid empty resource files which lead to installation errors

## [5.0.7] - 2023-03-30
### Added
- Add minio resource limits 
- Extended configmap and values.yaml with catalog cache configuration  
  You can configure the EDC catalog caching configuration like this:
  ```
  edc:
    catalog:
      cache:
        enabled: true
        ttl: P1D
        maxCachedItems: 64000
  ```
### Changed
- Updated IRS Version to 2.4.0

## [5.0.6] - 2023-03-28
### Added
- Added config parameter for SemanticsHub request page size when retrieving all models. Can be used to fine tune requests. Default: 100 items per page

### Changed
- Updated default path in template for `edc.controlplane.endpoint.data` to match EDC 0.3.0 management endpoint `/api/v1/management`

## [5.0.5] - 2023-03-20
### Changed
- Update IRS to version 2.3.2

## [5.0.4] - 2023-03-07
### Changed
- Update IRS to version 2.3.1


## [5.0.3] - 2023-03-01
### Fixed
- Fixed helm template for bpnEndpoint


## [5.0.2] - 2023-02-27
### Changed
- Updated default values so that IRS can start out of the box without technical errors. Please note that custom configuration is still necessary for IRS to work properly.


## [5.0.1] - 2023-02-21
### Changed
- Fixed semantic hub placeholder in default values


## [5.0.0] - 2023-02-21
### Changed
- Changed config parameter ``semanticsHub`` to ``semanticshub``
- Moved path ``/models/`` from ``semanticshub.modelJsonSchemaEndpoint`` to ``semanticshub.url``


### Migration note
Please make sure that you update your URL config for the semantics hub (see "Changed" section). Otherwise, IRS can not pick up the config correctly. Your new URL needs to contain the /model path.


## [4.3.2] - 2023-03-15
### Changed
- Update IRS version to 2.2.1

## [4.3.1] - 2023-03-01
### Changed
- Updated default values so that IRS can start out of the box without technical errors. Please note that custom configuration is still necessary for IRS to work properly.
- Fixed semantic hub placeholder in default values

## [4.3.0] - 2023-02-07
### Added
- Add support for custom environment variables in Helm chart.

## [4.2.1] - 2023-01-26
### Added
- Added parameter ``edc.controlplane.catalog.pagesize`` for configuration of EDC catalog page size for pagination. Default value is 50.

### Changed
- Minio now uses 1Gi of storage by default.
  > When upgrading from a previous version make sure that the minio PVC and pod is created and accessible by the IRS pod. The previous storage default was 500Gi and Kubernetes can not reduce the PVC size automatically.

## [4.2.0] - 2023-01-20
### Added
- It is now possible to provide semantic schema files as Base64 strings which will be mounted to the IRS container and then loaded via the configuration. 

### Changed
- Update IRS version to 2.2.0

## [4.1.0] - 2023-01-11
### Changed
- IRS configuration is now provided via ConfigMap instead of ENVs. This can be overwritten completely in the values.yaml. This is backward compatible with the previous configuration layout.

## [4.0.0] - 2022-12-09
### Changed
- Update IRS version to 2.0.0

## [3.0.1] - 2022-11-27
### Changed
- Updated default config for Prometheus / Grafana by disabling automatic RBAC creation

## [3.0.0] - 2022-11-25

### Changed
- Replaced the custom charts for Grafana, Prometheus and Minio with dependencies on stock charts. Please see the updated documentation for the new configuration layout.

### Removed
- Removed EDC from deployment. Instead, a new Helm chart is available which contains the EDC consumer: "irs-edc-consumer"
- Removed API wrapper from deployment

## [2.3.0]
### Changed
- Update IRS version to 1.5.0

## [2.2.0]
### Changed
- Update IRS version to 1.4.0

## [2.1.0]
### Added
- BPDM URL is now configurable
- SemanticsHub URL and default URNs are now configurable


## [2.0.0] - 2022-10-10
### Changed
- Refactored chart structure to no longer include environment values
- Simplified configuration in values.yaml

