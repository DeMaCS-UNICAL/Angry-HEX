#!/bin/bash

#-------------------------------------------------------------------------------

UNAME=$(uname -v)
OSTYPE=unknown
if [[ ${UNAME} == *Darwin* ]]; then
	OSTYPE=osx
elif [[ ${UNAME} == *Ubuntu* ]]; then
	OSTYPE=ubuntu
else
	echo "OS type not supported"
fi

#-------------------------------------------------------------------------------

if [[ ${OSTYPE} == osx ]]; then
	DLV=dlv.i386-apple-darwin.bin
else
	DLV=dlv.x86-64-linux-elf-static.bin
fi
DLV_URL=http://www.dlvsystem.com/files/${DLV}

DLVHEX=core
DLVHEX_URL=https://github.com/hexhex/${DLVHEX}.git
DLVHEX_BRANCHORTAG=Release_2_5_0 # does not build always
DLVHEX_BRANCHORTAG=015095f0bd143

BOX2Dversion=2.3.1
BOX2D_file=Box2D-${BOX2Dversion}
BOX2D_URL=https://github.com/DeMaCS-UNICAL/Angry-HEX/releases/download/dependencies/${BOX2D_file}.zip
BOX2D_folder=Box2D-${BOX2Dversion}

ANGRYHEX=angryhex

#-------------------------------------------------------------------------------

EMAIL=angryhex@mat.unical.it

#-------------------------------------------------------------------------------

B=`tput bold`
N=`tput sgr0 tput rmul`
U=`tput smul`

F_LOG=$(pwd)/install.log

usage() {
	echo
	echo "${B}SYNOPSIS${N}"
	echo "    ${B}./$(basename $0)${N} command [argument]"
	echo
	echo "${B}COMMANDS${N}"
	echo
	echo "    ${B}install${N}  [${U}all${N},dlv,dlvhex,box2d,agent,agent-java,agent-plugin]"
	echo
	echo "    ${B}run${N}      [${U}client${N},server]"
	echo
	echo "    ${B}set${N}      [release,develop]"
	echo
	echo "    ${B}archive${N}"
	echo
	
	exit $1
}

RUN() {
  if [[ "$1" == "cd" ]]; then
    # builtin commands cannot run with pipe
    $@ && return ;
  else
    # run with pipe
    $@ 2>&1 |tee -a ${F_LOG}
    if [[ "${PIPESTATUS[0]}" == "0" ]]; then
      return ;
    else
      echo "Exit Status ${PIPESTATUS[0]}" ;
    fi
  fi
	echo "FAIL"
	echo
	echo "Installation FAILED. Please send the file ${F_LOG} to ${EMAIL}."
	exit 1
}

REQUIRE() {
	if [[ ${OSTYPE} == ubuntu ]]; then
		apt-get -y install $@ >> ${F_LOG} 2>&1
	fi
}

pushdir() {
	pushd $(pwd) &> /dev/null
}

popdir() {
	popd &> /dev/null
}

#-------------------------------------------------------------------------------

F_DEVELOP=.angryhex_dev

setdevelop() {
	touch ${F_DEVELOP}
}

setrelease() {
	rm -f ${F_DEVELOP}
}

#-------------------------------------------------------------------------------

runclient() {
	cd ${D_AGENT}
	java -jar Client.jar $@
}

runserver() {
	cd ${D_AGENT}
	java -jar ABServer.jar $@
}

#-------------------------------------------------------------------------------

installdlv() {
	pushdir
	echo -n "Installing DLV....."
	
	# check if reinstall is forced
	if [[ $1 != "-f" ]]; then
		command -v dlv >> ${F_LOG} 2>&1 && echo "ALREADY INSTALLED" && return
	fi

	REQUIRE  wget

	RUN  cd /tmp
	RUN  wget ${DLV_URL} -O ${DLV}
	RUN  chmod +x ${DLV}
	RUN  cp ${DLV} /usr/local/bin/dlv

	echo "SUCCESS"
	popdir
}

installdlvhex() {
	pushdir
	echo -n "Installing DLVHEX....."

	# check if reinstall is forced
	if [[ $1 != "-f" ]]; then
		command -v dlvhex2 >> ${F_LOG} 2>&1 && echo "ALREADY INSTALLED" && return
	fi

	REQUIRE  git-core g++ autoconf automake pkg-config libtool libltdl-dev libboost-all-dev libcurl4-openssl-dev

	RUN  cd /tmp
	RUN  rm -rf ${DLVHEX}
	RUN  git clone --recursive ${DLVHEX_URL}
	RUN  cd ${DLVHEX}
	RUN  git checkout ${DLVHEX_BRANCHORTAG}
	RUN  ./bootstrap.sh
	RUN  ./configure --without-buildclaspgringo
	RUN  make -j4
	RUN  make install
	# otherwise libraries might not be found
	if [[ ${OSTYPE} == osx ]]; then
		echo "running update_dyld_shared_cache: required sudo. Please, insert Password:"
		RUN sudo update_dyld_shared_cache
	else
		echo "running ldconfig: required sudo. Please, insert Password:"
		RUN sudo ldconfig
	fi

	echo "SUCCESS"
	popdir
}

installbox2d() {
	pushdir
	echo -n "Installing Box2D....."
	
	# check if reinstall is forced
	if [[ $1 != "-f" ]]; then
		ld -lBox2D >> ${F_LOG} 2>&1 && echo "ALREADY INSTALLED" && return
	fi

	REQUIRE  wget unzip cmake libglu-dev libxi-dev build-essential

	RUN  rm -rf /usr/local/include/Box2D/
	RUN  cd /tmp
	RUN  rm -rf ${BOX2D_folder}
	RUN  wget ${BOX2D_URL}
	RUN  unzip ${BOX2D_file}
	RUN  cd ${BOX2D_folder}/Box2D/Build
	RUN  cmake -DBOX2D_INSTALL=ON -DBOX2D_BUILD_SHARED=ON -DBOX2D_BUILD_STATIC=OFF -DBOX2D_BUILD_EXAMPLES=OFF ..
	RUN  make -j4
	RUN  make install
	# otherwise libraries might not be found
	if [[ ${OSTYPE} == osx ]]; then
		echo "running update_dyld_shared_cache: required sudo. Please, insert Password:"
		RUN sudo update_dyld_shared_cache
	else
		echo "running ldconfig: required sudo. Please, insert Password:"
		RUN sudo ldconfig
	fi

	echo "SUCCESS"
	popdir
}

installagent() {
	pushdir
	echo -n "Installing AngryHEX agent....."

	REQUIRE  ant default-jdk g++

	if [ -e ${F_DEVELOP} ]; then
		rm -rf ${D_AGENT}
		packjava
		packplugin
		
		RUN  cd ${D_AGENT}
		RUN  make all
	else
		#packjava
		#packplugin
        
		RUN  cd ${D_AGENT}
		RUN  make all
	fi
	echo "SUCCESS"
	popdir
}

installagentjava() {
	pushdir
	if [ -e ${F_DEVELOP} ]; then
		packjava
	else
		echo "Individual building of components not possible in REMOTE mode"
		return
	fi
	echo -n "Installing Java client....."

	RUN  cd ${D_AGENT}
	RUN  make java

	echo "SUCCESS"
	popdir
}

installagentplugin() {
	pushdir
	if [ -e ${F_DEVELOP} ]; then
		packplugin
	else
		echo "Individual building of components not possible in REMOTE mode"
		return
	fi
	echo -n "Installing DLVHEX plugin....."

	RUN  cd ${D_AGENT}
	RUN  make plugin
	# otherwise libraries might not be found
	if [[ ${OSTYPE} == osx ]]; then
		echo "running update_dyld_shared_cache: required sudo. Please, insert Password:"
		RUN sudo update_dyld_shared_cache
	else
		echo "running ldconfig: required sudo. Please, insert Password:"
		RUN sudo ldconfig
	fi

	echo "SUCCESS"
	popdir
}

#-------------------------------------------------------------------------------

D_SRC=src         # source
D_FWK=framework   # framework
D_ENC=dlv         # encodings of hex programs
D_AGENT=angryhex  # out directory

F_ARCHIVE=angryhex.zip

function PKG {
    mkdir -p ${D_AGENT} &> /dev/null
	rsync -r --cvs-exclude $1 ${D_AGENT}/$2
}

function PKGSRC {
	mkdir -p ${D_AGENT}/src &> /dev/null
	rsync -r --cvs-exclude $1 ${D_AGENT}/src/$2
}

packjava() {
#	rm -rf ${D_AGENT}/${D_SRC}/angryhexclient
	PKGSRC ${D_SRC}/angryhexclient
	PKGSRC ${D_FWK}/src/
	PKG ${D_FWK}/ABServer.jar
	PKG ${D_FWK}/external
	PKG ${D_FWK}/plugin
	PKG ${D_ENC}
	PKG build.xml
	PKG Makefile

	if [[ $1 == archive ]]; then
		PKG config.properties.tournament config.properties
	else
		PKG config.properties
	fi
}

# ${DIR_PLUGIN} is the path the hex plugin
# - this path can be changed in order to use another plugin
# - the plugin folder must contain a Makefile with a 'install' target
DIR_PLUGIN=src/angrybirds-box2dplugin

# DO NOT change ${OUT_PLUGIN}
OUT_PLUGIN=src/angrybirds-box2dplugin

packplugin() {
#	rm -rf ${D_AGENT}/${OUT_PLUGIN}
	rsync -a --cvs-exclude ${DIR_PLUGIN}/* ${D_AGENT}/${OUT_PLUGIN}
	PKG Makefile
}

archive() {
	echo -n "Archiving AngryHEX....."
	
	REQUIRE zip
	
	RUN  rm -rf ${D_AGENT}
	RUN  rm -rf ${F_ARCHIVE}
	RUN  packjava archive
	RUN  packplugin archive
	RUN  zip -r ${F_ARCHIVE} ${D_AGENT}/*
	RUN  zip ${F_ARCHIVE} $(basename $0)
	echo "SUCCESS"
}

#-------------------------------------------------------------------------------

renewlog() {
	rm -rf ${F_LOG}
	# print system info to ${F_LOG}
	uname -a >> ${F_LOG} 2>&1
	if [[ ${OSTYPE} == osx ]]; then
		sw_vers >> ${F_LOG} 2>&1
	elif [[ ${OSTYPE} == ubuntu ]]; then
		lsb_release -a >> ${F_LOG} 2>&1
	fi
}

#-------------------------------------------------------------------------------

CMD=$1
ARG=$2

if [ -z $CMD ]; then
	usage 1
elif [ -z $ARG ]; then
	case $CMD in
		"install") ARG=all ;;
		"run")     ARG=client ;;
	esac
fi

case $CMD in
	"install")
		renewlog
 		case $ARG in
			"all")           installdlv && installdlvhex && installbox2d && installagent ;; 
			"dlv")          installdlv -f ;;
			"dlvhex")       installdlvhex -f ;;
			"box2d")        installbox2d -f ;;
			"agent")        installagent ;;
			"agent-java")   installagentjava ;;
			"agent-plugin") installagentplugin ;;
			*) echo "Unknown argument $ARG" && usage 1
		esac
		;;
	"run")
		case $ARG in
			"client") runclient ${@:3} ;;
			"server") runserver ${@:3} ;;
			*) echo "Unknown argument $ARG" && usage 1
		esac
		;;
	"set")
		case $ARG in
			"release") setrelease ;;
			"develop") setdevelop ;;
			*) echo "Unknown argument $ARG" && usage 1
		esac
		;;
	"archive") archive ;;
   *) echo "Invalid command: $CMD" && usage 1
esac

# vim:noet:
