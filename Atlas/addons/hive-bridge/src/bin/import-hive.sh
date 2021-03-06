#!/bin/bash
#
# Licensed under the Apache License, Version 2.0 (the "License");
# you may not use this file except in compliance with the License.
# You may obtain a copy of the License at
#
#   http://www.apache.org/licenses/LICENSE-2.0
#
#  Unless required by applicable law or agreed to in writing, software
#  distributed under the License is distributed on an "AS IS" BASIS,
#  WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
#  See the License for the specific language governing permissions and
#  limitations under the License. See accompanying LICENSE file.
#

# resolve links - $0 may be a softlink
PRG="${0}"

while [ -h "${PRG}" ]; do
  ls=`ls -ld "${PRG}"`
  link=`expr "$ls" : '.*-> \(.*\)$'`
  if expr "$link" : '/.*' > /dev/null; then
    PRG="$link"
  else
    PRG=`dirname "${PRG}"`/"$link"
  fi
done

BASEDIR=`dirname ${PRG}`
BASEDIR=`cd ${BASEDIR}/..;pwd`

if [ -z "$METADATA_CONF" ]; then
  METADATA_CONF=${BASEDIR}/conf
fi
export METADATA_CONF

if [ -f "${METADATA_CONF}/metadata-env.sh" ]; then
  . "${METADATA_CONF}/metadata-env.sh"
fi

if test -z ${JAVA_HOME}
then
    JAVA_BIN=`which java`
    JAR_BIN=`which jar`
else
    JAVA_BIN=${JAVA_HOME}/bin/java
    JAR_BIN=${JAVA_HOME}/bin/jar
fi
export JAVA_BIN

if [ ! -e $JAVA_BIN ] || [ ! -e $JAR_BIN ]; then
  echo "$JAVA_BIN and/or $JAR_BIN not found on the system. Please make sure java and jar commands are available."
  exit 1
fi

METADATACPPATH="$METADATA_CONF"

for i in "${BASEDIR}/lib-bridge/"*.jar; do
  METADATACPPATH="${METADATACPPATH}:$i"
done

echo $METADATACPPATH

JAVA_PROPERTIES="$METADATA_OPTS"
shift

while [[ ${1} =~ ^\-D ]]; do
  JAVA_PROPERTIES="${JAVA_PROPERTIES} ${1}"
  shift
done
TIME=`date +%Y%m%d%H%M%s`

${JAVA_BIN} ${JAVA_PROPERTIES} -cp ${METADATACPPATH} org.apache.hadoop.metadata.hive.bridge.HiveMetaStoreBridge

RETVAL=$?
[ $RETVAL -eq 0 ] && echo Hive Data Model Imported!!!
[ $RETVAL -ne 0 ] && echo Failure in Hive Data Model import!!!
