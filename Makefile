PYTHONLIBS=/usr/lib/pymodules/python2.7
OPENCVLIBS=/usr/local/share/OpenCV/java
OPENCVJAR=/usr/local/share/OpenCV/java
BOX2DLIBS=/usr/local/lib

.PHONY: all java plugin clean

all: java plugin

java:
	ant -lib framework/external/:${PYTHONLIBS}/:${OPENCVJAR}/opencv-246.jar:${OPENCVLIBS}/:${BOX2DLIBS}/ -f build.xml jar

plugin:
	make -C src/angrybirds-box2dplugin install

clean:
	ant -f build.xml clean
