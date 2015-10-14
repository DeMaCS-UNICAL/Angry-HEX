#!/bin/bash

cd ..

OUTNAME=angrybirds-box2dplugin.tar.gz

aptitude install g++-4.4
cd /tmp
rm $OUTNAME
wget http://www.kr.tuwien.ac.at/staff/michael/$OUTNAME -O $OUTNAME
tar zxvf $OUTNAME
cd angrybirds-box2dplugin
./compile.sh
cd
