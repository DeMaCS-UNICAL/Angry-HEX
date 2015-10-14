#!/bin/bash

cd ..

aptitude install default-jdk ant

cd /tmp
rm -Rf opencv
rm -Rf opencv-2.4.6.*
#git clone https://github.com/Itseez/opencv.git
wget http://sourceforge.net/projects/opencvlibrary/files/opencv-unix/2.4.6.1/opencv-2.4.6.1.tar.gz/download -O OpenCV.2.4.6.tar.gz
tar zxvf OpenCV.2.4.6.tar.gz
#cd opencv
cd opencv-2.4.6.1
#git checkout 2.4
mkdir build
cd build
#export JAVA_HOME=/usr/lib/jvm/default-java
#export JAVA_ROOT=/usr/lib/jvm/default-java
#export JDK_HOME=/usr/lib/jvm/default-java
#export JAVA_BINDIR=/usr/lib64/jvm/default-java/bin
#which ant
#which java
#which javac
cmake -DBUILD_SHARED_LIBS=OFF ..
make -j4
make install
cd

# install cv2.so
echo "Checking if cv2.so was built"
if [ $(find /tmp/opencv-2.4.6.1/build -name cv2.so | wc -l) -ge 1 ]; then
	echo "Yes: installing cv2.so"
	cp $(find /tmp/opencv-2.4.6.1/build -name cv2.so | head) /usr/local/share/OpenCV/java/
else
	echo "No: no installation required"
fi

