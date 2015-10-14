#!/bin/bash

cd ..

aptitude install libglu-dev libxi-dev build-essential unzip
BOX2D=Box2D_v2.2.1
rm -rf /usr/local/include/Box2D/
cd /tmp
wget http://box2d.googlecode.com/files/$BOX2D.zip -O $BOX2D.zip
unzip $BOX2D
cd $BOX2D/Build
cmake -DBOX2D_INSTALL=ON -DBOX2D_BUILD_SHARED=ON -DBOX2D_BUILD_STATIC=OFF -DBOX2D_BUILD_EXAMPLES=OFF ..
make -j4
make install
cd
