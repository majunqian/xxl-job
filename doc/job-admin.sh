#/bin/sh
# majq 2020-06-12 15:59:13

PROGNAME="xxl-job-admin-2.2.1-SNAPSHOT.jar"
SHORTNAME="job-admin"
JOB_ADMIN_HOME=${HOME}/xxljob/
export LOG_PATH=${JOB_ADMIN_HOME}
export SPRING_CONFIG_LOCATION=${JOB_ADMIN_HOME}

start (){
  status
  if [ "$?" -ne "0" ]
  then
    exit 0
  fi

  if [ -d "${JOB_ADMIN_HOME}/jdk1.8.0_251" ]
  then
    export JAVA_HOME=${JOB_ADMIN_HOME}/jdk1.8.0_251
    export JRE_HOME=$JAVA_HOME/jre
    export PATH=$JAVA_HOME/bin:$PATH
  fi
  nohup java -Dfile.encoding=UTF-8 -jar ${JOB_ADMIN_HOME}${PROGNAME} >/dev/null 2>&1  &

  status
  if [ "$?" -ne "0" ]
  then
    exit 0
  else
    exit 2
  fi
}

stop() {
  status
  if [ "$?" -ne "0" ]
  then
    ps auxwww | grep $LOGNAME | grep ${PROGNAME} | grep -v grep | awk '{ print $2}'| xargs kill -9
    status
  else
    exit 0
  fi
}

status(){
  cnt=`ps auxwww  | grep $LOGNAME | grep ${PROGNAME} | grep -v grep |wc -l`
  if [ "$cnt" -eq "0" ]
  then
    echo "${SHORTNAME} is not running"
  else
    echo "${SHORTNAME} is running"
  fi
  return "$cnt"
}

restart() {
  stop
  start
}

usage() {
  echo "Usage: `basename $0` [OPTION]"
  element_count=${#args[@]}
  for (( a=0; a<element_count; a++  ))
  do
    echo "  "${args[a]}": "${desc[a]}
  done
}

args[0]="start"
desc[0]="${SHORTNAME} start"
args[1]="stop"
desc[1]="${SHORTNAME} stop"
args[2]="restart"
desc[2]="${SHORTNAME} restart"
args[3]="status"
desc[3]="${SHORTNAME} running status"
FAILURE=64
parm=$1
element_count=${#args[@]}
for (( a=0; a<element_count; a++  ))
do
  if [ "${args[a]}" = "${parm}" ]
  then
    MATCHED="yes"
      ${parm}
    break
  fi
done

if [ "${MATCHED}" != "yes" ]
then
  usage
  exit ${FAILURE}
fi
