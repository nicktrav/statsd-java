#!/usr/bin/env bash

# Tears down one or more GCE instances
#
# Usage:
#  $ ./gcp-teardown.sh instance1 [... instanceN]

set -exuo pipefail

gcloud compute instances delete "$@"
