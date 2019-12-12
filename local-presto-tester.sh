#!/usr/bin/env bash

set -euxo

pc=192.168.35.234
pw=192.168.47.70
#pw0=192.168.40.119

mvn clean package

scp target/QueryAuditEventListener-1.2-jar-with-dependencies.jar ec2-user@$pc:~
scp target/QueryAuditEventListener-1.2-jar-with-dependencies.jar ec2-user@$pw:~
#scp target/QueryAuditEventListener-1.2-jar-with-dependencies.jar ec2-user@$pw0:~


#ssh -i ~/Projects/atlan-infra-atlan-dev.pem ec2-user@$pw << EOF
#  sudo rm -rf /usr/lib/presto/lib/plugin/atlan-audit-logger-presto-experimental/*.jar
#  sudo cp -v ~/QueryAuditEventListener-1.2-jar-with-dependencies.jar /usr/lib/presto/lib/plugin/atlan-audit-logger-presto-experimental/
#  sudo service presto restart
#EOF

ssh -i ~/Projects/atlan-infra-atlan-dev.pem ec2-user@$pw << EOF
  sudo rm -rf /usr/lib/presto/lib/plugin/atlan-audit-logger-presto-experimental/*.jar
  sudo cp -v ~/QueryAuditEventListener-1.2-jar-with-dependencies.jar /usr/lib/presto/lib/plugin/atlan-audit-logger-presto-experimental/
  sudo service presto restart
EOF

ssh -i ~/Projects/atlan-infra-atlan-dev.pem ec2-user@$pc << EOF
  sudo rm -rf /usr/lib/presto/lib/plugin/atlan-audit-logger-presto-experimental/*.jar
  sudo cp -v ~/QueryAuditEventListener-1.2-jar-with-dependencies.jar /usr/lib/presto/lib/plugin/atlan-audit-logger-presto-experimental/
  sudo service presto restart
EOF

exit 0