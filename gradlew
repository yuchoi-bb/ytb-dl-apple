#!/bin/sh
#
# Gradle start up script for POSIX compatible shells
#

APP_NAME="Gradle"
APP_BASE_NAME=`basename "$0"`
APP_HOME="`pwd -P`"
while [ -h "$APP_HOME/$APP_BASE_NAME" ]; do
    ls=`ls -ld "$APP_HOME/$APP_BASE_NAME"`
    link=`expr "$ls" : '.*-> \(.*\)$'`
    if expr "$link" : '/.*' > /dev/null; then
        APP_HOME="$link"
    else
        APP_HOME="$APP_HOME/$link"
    fi
    cd "$APP_HOME"
    APP_HOME="`pwd -P`"
done
cd "$APP_HOME"
CLASSPATH=$APP_HOME/gradle/wrapper/gradle-wrapper.jar
if [ -n "$JAVA_HOME" ] ; then
    if [ -x "$JAVA_HOME/jre/sh/java" ] ; then
        JAVACMD="$JAVA_HOME/jre/sh/java"
    else
        JAVACMD="$JAVA_HOME/bin/java"
    fi
else
    JAVACMD="java"
fi
exec "$JAVACMD" \
  -classpath "$CLASSPATH" \
  org.gradle.wrapper.GradleWrapperMain \
  "$@"
