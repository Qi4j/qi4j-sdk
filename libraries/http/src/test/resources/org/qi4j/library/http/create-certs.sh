#!/usr/bin/env bash
# Script created by AI to set up the security stores, but I can't make it work.

set -euo pipefail

# Output directory
OUT_DIR="tls-artifacts"
mkdir -p "${OUT_DIR}"
rm -f "${OUT_DIR}"/*

# Validity (days)
DAYS_CA="3650"
DAYS_SRV="825"
DAYS_CLI="825"

# Passwords (use placeholders for tests)
CA_PASS="changeit"
SRV_PASS="changeit"
CLI_PASS="changeit"
TRUST_PASS="changeit"

# Aliases
CA_ALIAS="test-ca"
SRV_ALIAS="server"
CLI_ALIAS="client"

# DNames
CA_DN="CN=Test CA, OU=Test, O=Example, L=City, S=State, C=US"
SRV_DN="CN=localhost, OU=Test, O=Example, L=City, S=State, C=US"
CLI_DN="CN=test-client, OU=Test, O=Example, L=City, S=State, C=US"

# SANs
SRV_SAN="san=dns:localhost,ip:127.0.0.1"
CLI_SAN="san=dns:localhost,ip:127.0.0.1"

# Files
CA_KS="${OUT_DIR}/ca-keystore.p12"
CA_CRT_PEM="${OUT_DIR}/ca-cert.pem"

SRV_KS="${OUT_DIR}/server-keystore.p12"
SRV_CSR="${OUT_DIR}/server.csr"
SRV_CRT_PEM="${OUT_DIR}/server-cert.pem"
SRV_TRUST="${OUT_DIR}/server-truststore.p12"

CLI_KS="${OUT_DIR}/client-keystore.p12"
CLI_CSR="${OUT_DIR}/client.csr"
CLI_CRT_PEM="${OUT_DIR}/client-cert.pem"
CLI_TRUST="${OUT_DIR}/client-truststore.p12"

info() { echo "==> $*"; }

verify_store() {
  local file="$1" pass="$2"
  info "Verifying ${file}"
  keytool -list -v -storetype PKCS12 -keystore "${file}" -storepass "${pass}" > /dev/null
}

# 1) CA keystore (self-signed, CA=true)
info "Generating CA keystore"
keytool -genkeypair \
  -alias "${CA_ALIAS}" \
  -keyalg RSA -keysize 2048 \
  -storetype PKCS12 \
  -keystore "${CA_KS}" \
  -storepass "${CA_PASS}" \
  -dname "${CA_DN}" \
  -ext bc=ca:true \
  -validity "${DAYS_CA}"

info "Exporting CA certificate (PEM)"
keytool -exportcert \
  -alias "${CA_ALIAS}" \
  -storetype PKCS12 \
  -keystore "${CA_KS}" \
  -storepass "${CA_PASS}" \
  -rfc -file "${CA_CRT_PEM}"

# 2) Server keystore: keypair -> CSR -> sign with CA -> import chain
info "Generating Server keypair"
keytool -genkeypair \
  -alias "${SRV_ALIAS}" \
  -keyalg RSA -keysize 2048 \
  -storetype PKCS12 \
  -keystore "${SRV_KS}" \
  -storepass "${SRV_PASS}" \
  -dname "${SRV_DN}" \
  -ext "${SRV_SAN}" \
  -validity "${DAYS_SRV}"

info "Creating Server CSR"
keytool -certreq \
  -alias "${SRV_ALIAS}" \
  -storetype PKCS12 \
  -keystore "${SRV_KS}" \
  -storepass "${SRV_PASS}" \
  -file "${SRV_CSR}"

info "Signing Server CSR with CA (serverAuth)"
keytool -gencert \
  -alias "${CA_ALIAS}" \
  -storetype PKCS12 \
  -keystore "${CA_KS}" \
  -storepass "${CA_PASS}" \
  -infile "${SRV_CSR}" \
  -outfile "${SRV_CRT_PEM}" \
  -rfc \
  -ext ku=digitalSignature,keyEncipherment \
  -ext eku=serverAuth \
  -ext "${SRV_SAN}" \
  -validity "${DAYS_SRV}"

info "Importing CA cert into Server keystore"
keytool -importcert \
  -alias "${CA_ALIAS}" \
  -file "${CA_CRT_PEM}" \
  -storetype PKCS12 \
  -keystore "${SRV_KS}" \
  -storepass "${SRV_PASS}" \
  -noprompt

info "Importing signed Server cert (replaces self-signed)"
keytool -importcert \
  -alias "${SRV_ALIAS}" \
  -file "${SRV_CRT_PEM}" \
  -storetype PKCS12 \
  -keystore "${SRV_KS}" \
  -storepass "${SRV_PASS}"

info "Creating Server truststore (trusts CA)"
keytool -importcert \
  -alias "${CA_ALIAS}" \
  -file "${CA_CRT_PEM}" \
  -storetype PKCS12 \
  -keystore "${SRV_TRUST}" \
  -storepass "${TRUST_PASS}" \
  -noprompt

# 3) Client keystore: keypair -> CSR -> sign with CA -> import chain
info "Generating Client keypair"
keytool -genkeypair \
  -alias "${CLI_ALIAS}" \
  -keyalg RSA -keysize 2048 \
  -storetype PKCS12 \
  -keystore "${CLI_KS}" \
  -storepass "${CLI_PASS}" \
  -dname "${CLI_DN}" \
  -ext "${CLI_SAN}" \
  -validity "${DAYS_CLI}"

info "Creating Client CSR"
keytool -certreq \
  -alias "${CLI_ALIAS}" \
  -storetype PKCS12 \
  -keystore "${CLI_KS}" \
  -storepass "${CLI_PASS}" \
  -file "${CLI_CSR}"

info "Signing Client CSR with CA (clientAuth)"
keytool -gencert \
  -alias "${CA_ALIAS}" \
  -storetype PKCS12 \
  -keystore "${CA_KS}" \
  -storepass "${CA_PASS}" \
  -infile "${CLI_CSR}" \
  -outfile "${CLI_CRT_PEM}" \
  -rfc \
  -ext ku=digitalSignature,keyEncipherment \
  -ext eku=clientAuth \
  -ext "${CLI_SAN}" \
  -validity "${DAYS_CLI}"

info "Importing CA cert into Client keystore"
keytool -importcert \
  -alias "${CA_ALIAS}" \
  -file "${CA_CRT_PEM}" \
  -storetype PKCS12 \
  -keystore "${CLI_KS}" \
  -storepass "${CLI_PASS}" \
  -noprompt

info "Importing signed Client cert (replaces self-signed)"
keytool -importcert \
  -alias "${CLI_ALIAS}" \
  -file "${CLI_CRT_PEM}" \
  -storetype PKCS12 \
  -keystore "${CLI_KS}" \
  -storepass "${CLI_PASS}"

info "Creating Client truststore (trusts CA)"
keytool -importcert \
  -alias "${CA_ALIAS}" \
  -file "${CA_CRT_PEM}" \
  -storetype PKCS12 \
  -keystore "${CLI_TRUST}" \
  -storepass "${TRUST_PASS}" \
  -noprompt

# Verify all stores exist and are readable
verify_store "${CA_KS}" "${CA_PASS}"
verify_store "${SRV_KS}" "${SRV_PASS}"
verify_store "${SRV_TRUST}" "${TRUST_PASS}"
verify_store "${CLI_KS}" "${CLI_PASS}"
verify_store "${CLI_TRUST}" "${TRUST_PASS}"

cat <<EOF

All done. Artifacts in: ${OUT_DIR}

- CA
  * Keystore: ${CA_KS}
  * Certificate (PEM): ${CA_CRT_PEM}

- Server
  * Keystore: ${SRV_KS} (alias: ${SRV_ALIAS}, EKU: serverAuth, SAN: localhost/127.0.0.1)
  * Truststore: ${SRV_TRUST} (contains only CA)

- Client
  * Keystore: ${CLI_KS} (alias: ${CLI_ALIAS}, EKU: clientAuth)
  * Truststore: ${CLI_TRUST} (contains only CA)

Use the configured passwords in your test wiring.
EOF