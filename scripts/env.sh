#!/usr/bin/env bash
#
#  vim:ts=2:sw=2:et
#

DRONE_DEPLOY_TO=${DRONE_DEPLOY_TO:-ci}
export JAVA_OPTS="-server -Xms64m -Xmx2g -XX:MetaspaceSize=96M -XX:MaxMetaspaceSize=256m -Djava.net.preferIPv4Stack=true -Djboss.modules.system.pkgs=org.jboss.byteman -Djava.awt.headless=true"
export KUBE_CPU_LIMIT="400m"
export KUBE_MEMORY_LIMIT="1024Mi"
export KUBE_SERVICE="service"
export USE_CFSSL=false
export USE_INGRESS=false
export USE_NETWORK_POLICY=false
export USE_PROXY_PROTOCOL=false
export USE_SELF_SIGNED_CERTS=false

case "${DRONE_DEPLOY_TO}" in
  dev)
    export WORKFLOW_ENGINE_RELICAS="2"
    export KUBE_CPU_LIMIT="800m"
    export KUBE_MEMORY_LIMIT="3072Mi"
    export KUBE_NAMESPACE="bfarch-dev"
    export KUBE_SERVER="https://kube-api-notprod.notprod.acp.homeoffice.gov.uk"
    export KUBE_TOKEN="${KUBE_TOKEN_DEV}"
    ;;
  *)
    echo "The envionment: ${DRONE_DEPLOY_TO} does is not configured"
    exit 1
    ;;
esac


export KUBECTL_OPTIONS="--insecure-skip-tls-verify=true --server=${KUBE_SERVER} --token=${KUBE_TOKEN} --namespace=${KUBE_NAMESPACE}"
