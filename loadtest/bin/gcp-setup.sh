#!/usr/bin/env bash

# Set up one or more GCE n1-standard instances with a configurable number of
# cores. Also copies the loadtest.sh script to the new instance.
#
# Usage:
#  $ ./gcp-setup.sh instance_name instance_cores

set -exuo pipefail

DIR="$( cd "$( dirname "${BASH_SOURCE[0]}" )" && pwd )"

INSTANCE_NAME="$1"
CORES="$2"

gcloud compute instances create \
  "$INSTANCE_NAME" \
  --machine-type "n1-standard-$CORES" \
  --image-project "centos-cloud" \
  --image "centos-7-v20171213"

gcloud compute scp "$DIR/gcp-loadtest.sh" "$INSTANCE_NAME:"
