#!/bin/bash

cd ..

ROOT=$(pwd)
FWK=${ROOT}/framework
AGENT=${ROOT}/agent

# Run server/client
#   start server of framework to make 'buildandrun' possible even if server is running
alias runserver='java -jar ${FWK}/ABServer.jar'
alias runclient='DIR=$(pwd) ; cd ${AGENT} ; ./client.sh ; cd ${DIR}'

# Build client
alias buildclient='DIR=$(pwd) && cd ${AGENT} && ./rebuild.sh && cd ${DIR}'

# Package agent
alias pkgagent='DIR=$(pwd) && cd ${ROOT} && ./pkg-agent.sh && cd ${DIR}'

# Package > Build > Run client
alias buildandrun='pkgagent && buildclient && runclient'

# TODO Archive agent
