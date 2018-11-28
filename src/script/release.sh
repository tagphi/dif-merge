#!/bin/sh

# resolve links - $0 may be a softlink
PRG="$0"

while [ -h "$PRG" ]; do
  ls=`ls -ld "$PRG"`
  link=`expr "$ls" : '.*-> \(.*\)$'`
  if expr "$link" : '/.*' > /dev/null; then
    PRG="$link"
  else
    PRG=`dirname "$PRG"`/"$link"
  fi
done

PRGDIR=`dirname "$PRG"`

PROJ_HOME=`cd "$PRGDIR/../.." >/dev/null; pwd`

RELEASE_DIR="${PROJ_HOME}/release"
TEMP_DIR="${RELEASE_DIR}/dif-merge"
JAR_DIR="${TEMP_DIR}/jar"
CONF_DIR="${TEMP_DIR}/conf"
BIN_DIR="${TEMP_DIR}/bin"
LOG_DIR="${TEMP_DIR}/log"

eval rm -fr "${TEMP_DIR}" && mkdir -pv "$TEMP_DIR" \
  && mkdir "$JAR_DIR" \
  && mkdir "$CONF_DIR" \
  && mkdir "$BIN_DIR" \
  && mkdir "$LOG_DIR"

cd "$PROJ_HOME" && mvn clean package -Dmaven.test.skip=true

cp "${PROJ_HOME}/target/dif-merge-0.0.1-SNAPSHOT.jar" "$JAR_DIR"
cp "${PROJ_HOME}/src/script/application.yml" "$CONF_DIR"
cp "${PROJ_HOME}/src/script/start.sh" "$BIN_DIR"
cp "${PROJ_HOME}/src/script/stop.sh" "$BIN_DIR"

cd "$RELEASE_DIR" && tar czvf dif-merge-0.0.1.tar.gz dif-merge && rm -fr "$TEMP_DIR"