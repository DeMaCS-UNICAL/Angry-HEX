#!/bin/bash

PYTHONLIBS=/usr/lib/pymodules/python2.7
OPENCVLIBS=/usr/local/share/OpenCV/java
OPENCVJAR=/usr/local/share/OpenCV/java
BOX2DLIBS=/usr/local/lib

ant -f build.xml clean

ant -lib framework/external/:$PYTHONLIBS/:$OPENCVJAR/opencv-246.jar:$OPENCVLIBS/:$BOX2DLIBS/ -f build.xml compile

cp /usr/local/share/OpenCV/java/opencv-24*.jar external/

ant -lib framework/external/:$PYTHONLIBS/:$OPENCVJAR/opencv-246.jar:$OPENCVLIBS/:$BOX2DLIBS/ -f build.xml jar

