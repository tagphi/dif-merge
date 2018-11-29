#!/bin/sh

darwin=false

case "`uname`" in
Darwin*) darwin=true;;
esac

if [ -z "$JAVA_HOME" ] && [ -z "$JRE_HOME" ]; then
  if $darwin; then
    if [ -x '/usr/libexec/java_home' ] ; then
      export JAVA_HOME=`/usr/libexec/java_home`
    elif [ -d "/System/Library/Frameworks/JavaVM.framework/Versions/CurrentJDK/Home" ]; then
      export JAVA_HOME="/System/Library/Frameworks/JavaVM.framework/Versions/CurrentJDK/Home"
    fi
  else
    JAVA_PATH=`which java 2>/dev/null`
    if [ "x$JAVA_PATH" != "x" ]; then
      JAVA_PATH=`dirname "$JAVA_PATH" 2>/dev/null`
      JRE_HOME=`dirname "$JAVA_PATH" 2>/dev/null`
    fi
    if [ "x$JRE_HOME" = "x" ]; then
      # XXX: Should we try other locations?
      if [ -x /usr/bin/java ]; then
        JRE_HOME=/usr
      fi
    fi
  fi
  if [ -z "$JAVA_HOME" ] && [ -z "$JRE_HOME" ]; then
    echo "Neither the JAVA_HOME nor the JRE_HOME environment variable is defined"
    echo "At least one of these environment variable is needed to run this program"
    exit 1
  fi
fi

if [ -z "$JRE_HOME" ]; then
  JRE_HOME="$JAVA_HOME"
fi

# Set standard commands for invoking Java, if not already set.
if [ -z "$_RUNJAVA" ]; then
  _RUNJAVA="$JRE_HOME"/bin/java
fi

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

# Get standard environment variables
PRGDIR=`dirname "$PRG"`

[ -z "$DIF_MERGE_HOME" ] && DIF_MERGE_HOME=`cd "$PRGDIR/.." >/dev/null; pwd`

JAVA_OPTS="$JAVA_OPTS -server -Xms2g -Xmx4g"
JAVA_OPTS="$JAVA_OPTS -XX:+UseConcMarkSweepGC -XX:+CMSClassUnloadingEnabled -XX:CMSInitiatingOccupancyFraction=80 -XX:+UseCMSInitiatingOccupancyOnly"
JAVA_OPTS="$JAVA_OPTS -XX:+UseParNewGC -XX:MaxTenuringThreshold=2 -XX:SurvivorRatio=2 -XX:+AlwaysPreTouch -XX:+UseCompressedOops"
JAVA_OPTS="$JAVA_OPTS -verbose:gc -XX:-OmitStackTraceInFastThrow -XX:+HeapDumpOnOutOfMemoryError -Xloggc:\"${DIF_MERGE_HOME}/gc.log\" -XX:NumberOfGCLogFiles=5 -XX:+UseGCLogFileRotation -XX:GCLogFileSize=20M"

JAR_DIR="${DIF_MERGE_HOME}/jar/dif-merge-0.0.1-SNAPSHOT.jar"

CONF_FILE="${DIF_MERGE_HOME}/conf/application.yml"

CURRENT_DIR=`pwd`

cd "$DIF_MERGE_HOME"

eval nohup "\"$_RUNJAVA\"" $JAVA_OPTS \
  -jar "\"$JAR_DIR\"" \
  --spring.config.location="\"$CONF_FILE\"" &

cd "$CURRENT_DIR"