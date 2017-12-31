#!/usr/bin/env bash

# Run a command on a given GCP instance
#
# Usage:
#  $ ./gcp-run-command.sh command

set -exuo pipefail

INSTANCE=$1

gcloud compute ssh $INSTANCE --command "${@:2}"
