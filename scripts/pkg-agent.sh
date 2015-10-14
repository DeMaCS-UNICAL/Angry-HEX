#!/bin/bash

SRC=src       # source
SCR=scripts   # scripts
FWK=framework # framework
ENC=dlv       # encodings of hex programs
OUT=angryhex  # out directory

function pack {
	rsync -r --cvs-exclude $1 ${OUT}
}

function packsrc {
	rsync -r --cvs-exclude $1 ${OUT}/src
}

rm -rf ${OUT}
mkdir  ${OUT}
mkdir  ${OUT}/src

packsrc ${SRC}/angryhexclient
packsrc ${FWK}/src/
pack ${FWK}/external
pack ${ENC}

pack build.xml
pack ${SCR}/rebuild.sh
pack ${SCR}/client.sh
pack ${SCR}/server.sh

pack ${FWK}/plugin
pack ${FWK}/ABServer.jar

# TODO where is config.properties.tournament used???
pack config.properties
pack config.properties.tournament
