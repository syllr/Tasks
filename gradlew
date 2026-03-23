#!/bin/sh
#
# Gradle start up script for UN*X

##############################################################################
#
#  Gradle Start up Script for
#
##############################################################################

APP_NAME="`basename "$0"`"
APP_BASE_NAME=`dirname "$0"`

# Use the maximum available, or set MAX_FD != -1 to use that
MAX_FD="maximum"

warn () {
    echo "$*" >&2
}

die () {
    warn "$*"
    exit 1
}

# OS specific support (must be 'true' or 'false').
cygwin=false
msys=false
darwin=false
noncase=false
case "`uname`" in
  Darwin*)
    darwin=true
    noncase=true
    ;;
  CYGWIN*)
    cygwin=true
    noncase=true
    ;;
  MINGW*)
    msys=true
    ;;
esac

# Attempt to set APP_HOME
# Resolve links: $0 may be a link
PRG="$0"
# Need this for relative symlinks.
while [ -h "$PRG" ] ; do
    ls=`ls -ld "$PRG"`
    link=`expr "$ls" : '.*-> \(.*\)$'`
    if expr "$link" : '/.*' > /dev/null; then
        PRG="$link"
    else
        PRG=`dirname "$PRG"`"/$link"
    fi
done
SAVED="`pwd`"
cd "`dirname \"$PRG\"`"/.. >&/dev/null
APP_HOME="`pwd -P`"
cd "$SAVED" >&/dev/null

CLASSPATH=$APP_HOME/gradle/wrapper/gradle-wrapper.jar

# Determine the Java command to use to start the JVM.
if [ -n "$JAVA_HOME" ] ; then
    if [ -x "$JAVA_HOME/jre/sh/java" ] ; then
        # IBM's JDK on AIX uses strange locations for the executables
        JAVACMD="$JAVA_HOME/jre/sh/java"
    else
        JAVACMD="$JAVA_HOME/bin/java"
    fi
else
    JAVACMD="java"
fi

if [ ! -x "$JAVACMD" ] ; then
    die "
ERROR: JAVA_HOME is not set and no 'java' command could be found in your PATH.

Please set the JAVA_HOME variable in your environment to match the
location of your Java installation."
fi

# Increase the maximum file descriptors if we can.
if [ "$cygwin" = "false" -o "$darwin" = "true" ] ; then
    MAX_FD_LIMIT=`ulimit -H -n`
    if [ $? -eq 0 ] ; then
        if [ "$MAX_FD" = "maximum" -o "$MAX_FD" = "max" ] ; then
            MAX_FD="$MAX_FD_LIMIT"
        fi
        ulimit -n $MAX_FD
        if [ $? -ne 0 ] ; then
            warn "Could not set maximum file descriptor limit: $MAX_FD"
        fi
    else
        warn "Could not query maximum file descriptor limit: $MAX_FD_LIMIT"
    fi
fi

# For Darwin, add options to specify how the application appears in the dock
if $darwin; then
    GRADLE_OPTS="$GRADLE_OPTS \"-Dapple.awt.graphics.UseQuartz\" \"-Dapple.awt.UIElement\""
fi

# Start the Gradle Wrapper
exec "$JAVACMD" $GRADLE_OPTS "-Dorg.gradle.appname=$APP_NAME" \
-classpath "$CLASSPATH" \
org.gradle.wrapper.GradleWrapperMain "$@"
