#!/bin/bash

#-------------------------------------------------------------------------------

SCR_ANGRYHEX=dev.angryhex.sh
ZIP_ANGRYHEX=angryhex.zip
DIR_ANGRYHEX=angryhex
URL_ANGRYHEX=https://github.com/DeMaCS-UNICAL/Angry-HEX/releases/download/angryhex2017/angryhex.zip

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
	echo "    ${B}install${N}  [${U}all${N},dlv,dlvhex,box2d,agent]"
	echo
	echo "    ${B}run${N}"
	echo
	echo "    ${B}update${N}"
	echo
	
	exit $1
}

#-------------------------------------------------------------------------------

downloadAgent() {
	rm -rf ${SCR_ANGRYHEX}
	rm -rf ${ZIP_ANGRYHEX}
	rm -rf ${DIR_ANGRYHEX}
	apt-get -y install wget unzip
	wget ${URL_ANGRYHEX} -O ${ZIP_ANGRYHEX}
	unzip ${ZIP_ANGRYHEX}
	chmod +x ${SCR_ANGRYHEX}
}

ensureAgent() {
	if [ ! -e ${SCR_ANGRYHEX} ]; then
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
			"all" | "dlv" | "dlvhex" | "box2d" | "agent")
				ensureAgent
				./${SCR_ANGRYHEX} $CMD $ARG
        ldconfig
				;;
			*) echo "Unknown argument $ARG" && usage 1
		esac
		;;
	"update") downloadAgent && ./${SCR_ANGRYHEX} install all ;;
	"run")    ./${SCR_ANGRYHEX} run client ;;
   *) echo "Invalid command: $CMD" && usage 1
esac
