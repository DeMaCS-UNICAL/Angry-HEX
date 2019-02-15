#!/bin/bash

#-------------------------------------------------------------------------------

SCR_ANGRYHEX=dev.angryhex.sh
ZIP_ANGRYHEX=angryhex.zip
DIR_ANGRYHEX=angryhex
URL_ANGRYHEX=https://github.com/DeMaCS-UNICAL/Angry-HEX/releases/download/angryhex2017/angryhex.zip
F_DEVELOP=.angryhex_dev

#-------------------------------------------------------------------------------

B=`tput bold`
N=`tput sgr0 tput rmul`
U=`tput smul`

usage() {
	echo
	echo "${B}SYNOPSIS${N}"
	echo "    ${B}./$(basename $0)${N} command [argument]"
	echo
	echo "${B}COMMANDS${N}"
	echo
	echo "    ${B}install${N}  [${U}all${N},dlv,boost,dlvhex,box2d,agent]"
	echo
	echo "    ${B}run${N}"
	echo
	echo "    ${B}update${N}"
	echo
	
	exit $1
}

#-------------------------------------------------------------------------------

REQUIRE() {
  PACKAGES=$@
  echo -n "checking if packages $PACKAGES are installed..."
	if [[ ${OSTYPE} == ubuntu ]]; then
    NOTFOUND=""
    for f in $PACKAGES; do
      FOUND=$(dpkg-query --show --showformat='${Status}\n' $f |grep 'install ok installed')
      if [ "" == "$FOUND" ]; then
        NOTFOUND="$NOTFOUND $f"
      fi
    done
    if [ "" -ne "$NOTFOUND" ]; then
      CMD="sudo apt-get -y install $NOTFOUND"
      echo "did not find $NOTFOUND therefore installing with $CMD"
      $CMD
    else
      echo "ok."
    fi
  else
    echo "skipped (unknown OS)."
  fi
}

downloadAgent() {
	rm -rf ${SCR_ANGRYHEX}
	rm -rf ${ZIP_ANGRYHEX}
	rm -rf ${DIR_ANGRYHEX}
	ensurePackagesAreInstalled wget unzip
	wget ${URL_ANGRYHEX} -O ${ZIP_ANGRYHEX}
	unzip ${ZIP_ANGRYHEX}
	chmod +x ${SCR_ANGRYHEX}
}

ensureAgent() {
	if [ -e ${SCR_ANGRYHEX} ]; then
		# dev.angryhex.sh exists --> git clone or already unzipped
		# git clone?
		if [ -e ".git" ] && [ ! -e ${F_DEVELOP} ]; then
			echo "angryhex.sh should be used in development mode when called from a git clone; please call ${B}./dev.angryhex.sh set develop${N}"
			exit 1
		fi
	else
		# dev.angryhex.sh exists --> no git clone and not already unzipped
		downloadAgent
	fi
}

#-------------------------------------------------------------------------------

CMD=$1
ARG=$2

if [ -z $CMD ]; then
	usage 1
elif [ -z $ARG ]; then
	case $CMD in
		"install") ARG=all ;;
	esac
fi

case $CMD in
	"install") 
 		case $ARG in
			"all" | "dlv" | "boost" | "dlvhex" | "box2d" | "agent")
				ensureAgent
				./${SCR_ANGRYHEX} $CMD $ARG
        #ldconfig
				;;
			*) echo "Unknown argument $ARG" && usage 1
		esac
		;;
	"update") downloadAgent && ./${SCR_ANGRYHEX} install all ;;
	"run")    ./${SCR_ANGRYHEX} run client ${@:2} ;;
   *) echo "Invalid command: $CMD" && usage 1
esac
