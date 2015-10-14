#!/bin/bash

# This file serves as single interface to the other
# setup and run scripts in the scripts directory.
#
# Keep the arguments to the script as simple as possible!
# FIXME 'setupandrun' not working yet

if [[ $# -ne 1 ]]; then
	echo "This script expects exactly one argument from the following list:"
	echo "   setupandrun   To install AngryHex (from scratch) and run it"
	echo "   setup         To install AngryHex (from scratch)"
	echo "   rebuild       Rebuilds the AngryHEX client (excluding the dlvhex-plugin)"
	echo "   run           To run the AngryHEX client"
	echo "   runserver     To run the AngryBirds AI Competition server"
	echo "   clean         Remove temporary files"
	exit 1
fi

SCR=scripts
AGT=agent
FWK=framework

function agtpack {
	./${SCR}/pkg-agent.sh
}

function agtbuild {
	cd ${AGT} && ./rebuild.sh && cd ..
}

function agtrun {
	cd ${AGT} && ./client.sh ; cd ..
}

function agtclean {
	rm -rf ${AGT}
}

function srvrun {
	java -jar ${FWK}/ABServer.jar
}

case $1 in
  "setupandrun") cd ./${SCR} && ./install-angryhex.sh && agtpack && agtbuild && agtrun ;;
  "setup")       cd ./${SCR} && ./install-angryhex.sh ;;
  "rebuild")     agtpack && agtbuild ;;
  "run")         agtrun ;;
  "runserver")   srvrun ;;
  "clean")       agtclean ;;
   *) echo "Invalid option: $1" && exit 1 ;;
esac

