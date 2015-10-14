#!/bin/bash

./install-hex.sh
./install-box2d.sh
./install-opencv.sh
./install-abirdsplugin.sh



exit 0

PLUGINFILE=angrybirds-box2dplugin.tar.gz
AGENTFILE=angryhex-agent.tar.gz

if test -e /tmp/$PLUGINFILE; then
 rm /tmp/$PLUGINFILE
fi

if test -e $AGENTFILE; then
 rm $AGENTFILE
fi
OLDDIR=$(pwd)
cd /tmp
wget http://www.kr.tuwien.ac.at/staff/michael/$PLUGINFILE -O $PLUGINFILE
tar zxvf $PLUGINFILE
cd angrybirds-box2dplugin
./compile.sh
cd $OLDDIR

./install-angryhex.sh
./rebuild.sh
