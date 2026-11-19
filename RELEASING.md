# Releasing the Dremio SingleStore ARP Connector

Pushing a version tag to GitHub triggers the [Release](.github/workflows/release.yml) workflow.

## Prerequisites

- Changes for the release are merged to `master`
- You have permission to push tags to the repository

`master` is protected by a ruleset: every change, including the version bump, must go through a pull request with one approval, code-owner review, and resolved conversations. Pushing a version tag is not affected.

## Version format

The version in `pom.xml` has the form `<Dremio version>-<plugin version>`, for example `23.1.0-1.0.0`. The release tag is that version prefixed with `v`:

| `pom.xml` version | Tag |
| --- | --- |
| `23.1.0-1.0.0` | `v23.1.0-1.0.0` |

The workflow fails before building if the tag does not match the `pom.xml` version.

## Create a release

1. Open a pull request that updates `version` (and `version.dremio`, if needed) in `pom.xml`, get it approved, and merge it.
2. Tag the merged commit and push the tag:

   ```bash
   git checkout master
   git pull
   git tag v23.1.0-1.0.0
   git push origin v23.1.0-1.0.0
   ```

## What the workflow does

1. Checks that the tag matches the `pom.xml` version.
2. Builds the plugin JAR with Maven (JDK 11).
3. Signs the JAR with Azure Artifact Signing and verifies the signature, if signing is configured (see below).
4. Creates a **draft** GitHub release with the JAR attached and generated release notes.

## Code signing

The JAR is signed with Azure Artifact Signing (Trusted Signing) through [Jsign](https://github.com/ebourg/jsign) and `jarsigner`, using Workload Identity Federation.

Signing is optional. If any of the secrets or variables below is missing, the workflow skips signing, logs a warning, and publishes an **unsigned** JAR.

### Required GitHub Actions secrets

| Secret | Description |
| --- | --- |
| `AZURE_CLIENT_ID` | Application (client) ID of `github-singlestore-signing` |
| `AZURE_TENANT_ID` | Directory (tenant) ID |
| `AZURE_SUBSCRIPTION_ID` | Azure subscription ID used for signing |

### Required GitHub Actions variables

| Variable | Example value |
| --- | --- |
| `AZURE_SIGNING_ENDPOINT` | `https://eus.codesigning.azure.net` |
| `AZURE_SIGNING_ACCOUNT` | `SingleStore` |
| `AZURE_SIGNING_PROFILE` | `ConnectorsReleaseProfile` |

Setup instructions: [HOWTO Sign Our Releases with Azure Trusted Artifacts](https://memsql.atlassian.net/wiki/spaces/SUP/pages/4371644456/HOWTO+Sign+Our+Releases+with+Azure+Trusted+Artifacts+Artifact+Signing).

## Finalize the release

1. Open the draft on the [Releases](https://github.com/memsql/dremio-singlestore/releases) page.
2. Edit the release notes as needed.
3. Publish the release.
