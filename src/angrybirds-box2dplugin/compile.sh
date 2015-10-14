HEXPREFIX=/usr/local
HEXINSTALL=/usr/local/lib
BOX2DINSTALL=/usr/local
OUTFILE=libdlvhexplugin-hexagentplugin.so
INSTALLPATH=$HEXINSTALL/dlvhex/plugins/

mkdir $HEXINSTALL/dlvhex
mkdir $INSTALLPATH

g++-4.4 -fPIC -shared -isystem $HEXPREFIX/include -isystem $BOX2DINSTALL/include -L $BOX2DINSTALL/lib -lBox2D -o $OUTFILE libdlvhexplugin-hexagentplugin.cpp -g -O0 -Wall -Wextra -pedantic 2>&1

cp $OUTFILE $INSTALLPATH
