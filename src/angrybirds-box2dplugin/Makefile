
OUTFILE=libdlvhexplugin-hexagentplugin.so
PLUGIN_DIR=/usr/local/lib/dlvhex/plugins/

INCLUDES= \
  -isystem /usr/local/include \
  -isystem /usr/local/include/dlvhex2/bm3.7.0

LIBS=-lBox2D -lboost_system -ldlvhex2-base

.PHONY: all compile install clean

all: compile

compile:
	# IMPORTANT the hex plugin has to be built with g++-4.4, otherwise it cannot be loaded by dlvhex
	g++-4.4 -fPIC -shared ${INCLUDES} ${LIBS} -o $(OUTFILE) libdlvhexplugin-hexagentplugin.cpp -g -O0 -Wall -Wextra -pedantic

install: compile
	mkdir -p ${PLUGIN_DIR}
	cp ${OUTFILE} ${PLUGIN_DIR}

clean:
	rm -rf ${OUTFILE}*
