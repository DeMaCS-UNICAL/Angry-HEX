#!/bin/bash

cd ..

apt-get install aptitude
aptitude install git-core g++ autoconf automake wget cmake pkg-config libtool libcurl4-openssl-dev libzip-dev libbz2-dev libboost-all-dev bmagic 

cd /tmp
wget http://www.dlvsystem.com/files/dlv.x86-64-linux-elf-static.bin
chmod +x dlv.x86-64-linux-elf-static.bin
cp dlv.x86-64-linux-elf-static.bin /usr/local/bin/dlv

git clone --recursive https://github.com/hexhex/core.git
cd core
./bootstrap.sh
./configure --without-buildclaspgringo
make -j4
make install
cd 

