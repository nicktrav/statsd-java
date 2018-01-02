#!/usr/bin/env bash

# Runs a specific load test command
#
# Usage:
#  $ ./gcp-loadtest.sh bootstrap
#  $ ./gcp-loadtest.sh run_server arg1 [... argN]
#
# bootstrap: sets up the instance with requisite libraries and code
# run_server: runs a server instance

set -exuo pipefail

function bootstrap() {
  # Dependencies
  sudo yum -y update
  sudo yum -y install \
    net-tools java-1.8.0-openjdk-devel git unzip maven htop

  # YourKit
  curl -O https://www.yourkit.com/download/YourKit-JavaProfiler-2017.02-b68.zip
  unzip -j YourKit-JavaProfiler-2017.02-b68.zip '*/bin/linux-x86-64/libyjpagent.so'

  # Get code and build
  git clone https://github.com/nicktrav/statsd-java.git
  pushd statsd-java
    mvn clean package -DskipTests
  popd

  # Set up networking defaults
  sudo sysctl -w net.core.rmem_max=8388608
  sudo sysctl -w net.core.wmem_max=8388608
  sudo sysctl -w net.core.rmem_default=65536
  sudo sysctl -w net.core.wmem_default=65536
}

function run_server() {
  java -cp statsd-java/loadtest/target/loadtest.jar \
    -agentpath:./libyjpagent.so \
    rs.nicktrave.statsd.loadtest.TestServer "$@"
}

function run_client() {
  java -cp statsd-java/loadtest/target/loadtest.jar \
    rs.nicktrave.statsd.loadtest.LoadGenerator "$@"
}

$@
