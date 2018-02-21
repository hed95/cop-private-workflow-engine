#!/usr/bin/env bash
#
#  vim:ts=2:sw=2:et

source scripts/env.sh

echo "--- kube api url: ${KUBE_SERVER}"
echo "--- namespace: ${KUBE_NAMESPACE}"

if [[ "${USE_NETWORK_POLICY}" == true ]]; then
  echo "[INFO] applying the network policy"
  if ! kd --insecure-skip-tls-verify \
    --check-interval=5s \
    --namespace=${KUBE_NAMESPACE} \
    --timeout=5m \
    -f kube/network-policy.yml; then
    echo "[error] failed to apply network policy"
    exit 1
  fi
fi

if [[ "${USE_INGRESS}" == "true" ]]; then
  echo "[INFO] deploying ingress resource"
  if ! kd --insecure-skip-tls-verify \
    --check-interval=5s \
    --namespace=${KUBE_NAMESPACE} \
    --timeout=5m \
    -f kube/ingress.yml; then
    return 1
  fi
fi

kd --insecure-skip-tls-verify \
  --namespace=${KUBE_NAMESPACE} \
  --timeout=5m \
  -f kube/service.yml \
  -f kube/deployment.yml

exit $?
