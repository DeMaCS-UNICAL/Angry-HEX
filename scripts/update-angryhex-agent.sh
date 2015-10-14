#!/bin/bash

cd ..

cp ./abV1.2/extractBirdsTypesAndScreenshotsNamesFromLog.pl ./angryhex-agent/
cp ./abV1.2/extractOnGoingScoresFromLog.pl ./angryhex-agent/
cp ./abV1.2/syntaxcheck ./angryhex-agent/
cp ./abV1.2/install.sh ./angryhex-agent/
cp ./abV1.2/algorithm* ./angryhex-agent/
cp ./abV1.2/build.xml  ./angryhex-agent/
cp ./abV1.2/calibrate.dlv  ./angryhex-agent/
cp ./abV1.2/asparse.dlv  ./angryhex-agent/
cp ./abV1.2/debugger ./angryhex-agent/
cp ./abV1.2/config.properties.tournament  ./angryhex-agent/config.properties
cp ./TestServer/Server.jar ./angryhex-agent/
cp ./abV1.1/server.sh ./angryhex-agent/

cp ./abV1.2/external/*.jar  ./angryhex-agent/external/
rm ./angryhex-agent/external/opencv-24*.jar 
cp ./abV1.2/external/*.zip  ./angryhex-agent/external/

cp ./abV1.1/plugin/*.js  ./angryhex-agent/plugin/
cp ./abV1.1/plugin/*.css  ./angryhex-agent/plugin/
cp ./abV1.1/plugin/*.json  ./angryhex-agent/plugin/

cp ./abV1.2/doc/*.pdf  ./angryhex-agent/doc/

cp -R ./abV1.2/src/ab/*  ./angryhex-agent/src/ab/
rm -Rf ./angryhex-agent/src/ab/CVS
rm -Rf ./angryhex-agent/src/ab/demo/CVS
rm -Rf ./angryhex-agent/src/ab/demo/other/CVS
rm -Rf ./angryhex-agent/src/ab/demo/util/CVS
rm -Rf ./angryhex-agent/src/ab/planner/CVS
rm -Rf ./angryhex-agent/src/ab/server/CVS
rm -Rf ./angryhex-agent/src/ab/server/proxy/CVS
rm -Rf ./angryhex-agent/src/ab/server/proxy/message/CVS
rm -Rf ./angryhex-agent/src/ab/utils/CVS
rm -Rf ./angryhex-agent/src/ab/vision/CVS
rm -Rf ./angryhex-agent/src/ab/vision/resources/CVS

cp -R ./abV1.2/src/external/*  ./angryhex-agent/src/external/
rm -Rf ./angryhex-agent/src/external/CVS


