#!/usr/bin/env bash

set -euxo

pc=192.168.32.95
pw=192.168.46.149

mvn clean install

scp target/QueryAuditEventListener-1.0.jar ec2-user@$pc:~ && scp target/QueryAuditEventListener-1.0.jar ec2-user@$pw:~

ssh -i ~/Projects/atlan-infra.pem ec2-user@$pw << EOF
  sudo cp -v ~/QueryAuditEventListener-1.0.jar /usr/lib/presto/lib/plugin/atlan-audit-logger-presto-experimental/
  sudo service presto restart
EOF

ssh -i ~/Projects/atlan-infra.pem ec2-user@$pc << EOF
  sudo cp -v ~/QueryAuditEventListener-1.0.jar /usr/lib/presto/lib/plugin/atlan-audit-logger-presto-experimental/
  sudo service presto restart
EOF

exit 0