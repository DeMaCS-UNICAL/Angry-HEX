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

BOOST_URL=https://sourceforge.net/projects/boost/files/boost/1.58.0/boost_1_58_0.tar.bz2/download
DLVHEX=dlvhex-core
DLVHEX_URL=https://github.com/hexhex/core.git
DLVHEX_BRANCHORTAG=Release_2_5_0 # does not build always
DLVHEX_BRANCHORTAG=015095f0bd143

BOX2Dversion=2.3.1
BOX2D_file=Box2D-${BOX2Dversion}
BOX2D_URL=https://github.com/DeMaCS-UNICAL/Angry-HEX/releases/download/dependencies/${BOX2D_file}.zip
BOX2D_folder=Box2D-${BOX2Dversion}


# miniconda location (will be installed by this script, used to install boost)
CONDADIR=$(pwd)/conda/
# external packages (boost, dlvhex2, box2d) will be built into this directory
BUILDDIR=$(pwd)/build/
# external packages and the dlvhex2 plugin will be installed into this directory
INSTALLDIR=$(pwd)/inst/

# %q quotes string so that directories with spaces do not kill us (an easier syntax is available but only for very new bash)
BUILDDIR_QUOTED=$(printf "%q" "$(pwd)/build/")
if [[ "$BUILDDIR" != "$BUILDDIR_QUOTED" ]]; then
	echo "Refusing to build in directory with special characters (for example space): '$(pwd)'!";
	exit -1;
fi

# for parallel builds (requires multiples of memory if more than 1!)
CPUCORES=2

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
	echo "    ${B}install${N}  [${U}all${N},dlv,boost,dlvhex,box2d,agent,agent-java,agent-plugin]"
	echo
	echo "    ${B}run${N}      [${U}client${N},server [client/server arguments]]"
	echo
	echo "    ${B}set${N}      [release,develop]"
	echo
	echo "    ${B}clean${N}"
	echo
	echo "    ${B}archive${N}"
	echo
	echo "    ${B}dlvhex2${N} [command-line arguments]"
	echo
	echo "        Runs the dlvhex2 solver with the same environment variables"
	echo "        that are used when dlvhex2 is called from Java."
	echo "        (dlvhex2 will load the locally built Box2D HEX plugin.)"
	echo
	echo "BUILDDIR is ${INSTALLDIR}"
	echo "INSTALLDIR is ${INSTALLDIR}"
	echo
	
	exit $1
}

RUN() {
  if [[ "$1" == "cd" ]]; then
    # builtin commands cannot run with pipe
    $@ && return ;
  else
    # run with pipe
    $@
		STATUS=$?
    if [[ "0" == "$STATUS" ]]; then
      return ;
    else
      echo "Exit Status ${STATUS}" ;
    fi
  fi
	echo "FAIL"
	echo
	echo "Installation FAILED. Please send the file ${F_LOG} to ${EMAIL}."
	exit 1
}

REQUIRE() {
  PACKAGES="$@"
  echo -n "checking if packages $PACKAGES are installed..."
  if [[ ${OSTYPE} == ubuntu ]]; then
    NOTFOUND=""
    for f in $PACKAGES; do
      FOUND=$(dpkg-query --show --showformat='${Status}\n' $f |grep 'install ok installed')
      if [[ "" == "$FOUND" ]]; then
        NOTFOUND="$NOTFOUND $f";
      fi
    done
    if [[ "" != "$NOTFOUND" ]]; then
      CMD="sudo apt-get -y install $NOTFOUND"
      echo "did not find $NOTFOUND therefore installing it"
			echo "$CMD requires sudo."
      $CMD
    else
      echo "ok."
    fi
  else
    echo "skipped (unknown OS)."
  fi
}

pushdir() {
	pushd $(pwd) &> /dev/null
}

popdir() {
	popd &> /dev/null
}

MKDIR() {
	for d in "$@"; do
		mkdir -p $d &> /dev/null
	done
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
	diff -q config.properties ${D_AGENT}/${OUT_JAVA}/config.properties || echo "WARNING: config.properties in ./ and in ${D_AGENT}/${OUT_JAVA} differs!"
	cd ${D_AGENT}/${OUT_JAVA}
	java -jar Client.jar $@
}

runserver() {
	cd ${D_AGENT}/${OUT_JAVA}
	java -jar ABServer.jar $@
}

#-------------------------------------------------------------------------------

installdlv() {
	pushdir
	echo -n "Installing DLV....."
	
	# check if reinstall is forced
	if [[ $1 != "-f" ]]; then
		command -v dlv && echo "ALREADY INSTALLED" && return
	fi

	REQUIRE  wget
	MKDIR $INSTALLDIR/bin/

	TARGET=$INSTALLDIR/bin/dlv
	RUN  wget ${DLV_URL} -O $TARGET
	RUN  chmod +x $TARGET

	echo "SUCCESS"
	popdir
}

installboost() {
	pushdir
	echo -n "Installing boost....."

	# download and install boost if it does not exist yet
	# (do this even if there is an OS installed version,
	# because we can never be sure the OS version works with dlvhex)

	# check if reinstall is forced
	if [[ $1 != "-f" ]]; then
		test -e $INSTALLDIR/include/boost/version.hpp && echo "ALREADY INSTALLED" && return
	fi

	REQUIRE  wget build-essential g++ libbz2-dev

	RUN  rm -rf "$BUILDDIR/boost/"
	MKDIR "$BUILDDIR/boost/"
	RUN  cd "$BUILDDIR/"
	RUN  wget ${BOOST_URL} -O boost.tar.bz2
	RUN  cd boost/
	RUN  tar xjf ../boost.tar.bz2 --strip-components=1
	RUN  ./bootstrap.sh --prefix=$INSTALLDIR/
	RUN  ./b2 -j$CPUCORES --layout=tagged threading=single,multi variant=release --with-iostreams --with-graph --with-program_options --with-test --with-system --with-filesystem --with-thread --with-date_time install

	popdir
}

# this is currently unused (dlvhex builds only on some systems with conda boost)
installcondaboost() {
	pushdir

	echo -n "Installing (mini)conda to $CONDADIR ..."
	if [[ $1 != "-f" ]]; then
		test -e $CONDADIR/bin/conda && echo "ALREADY INSTALLED" && return
	fi

	RUN  rm -rf "$CONDADIR"
	# TODO change for MacOS
	RUN  wget https://repo.continuum.io/miniconda/Miniconda3-latest-Linux-x86_64.sh -O /tmp/miniconda.sh
	RUN  bash /tmp/miniconda.sh -b -p $CONDADIR
	RUN  hash -r
	RUN  conda config --set always_yes yes --set changeps1 no
	RUN  conda update -q conda
	RUN  conda info -a
	RUN  conda install python=2.7 boost=1.57.0 libcurl=7.60.0 gcc=4.8.5 icu=54.1 
	# the following lines are some requirements for also building clasp/gringo
	#RUN  conda install scons bison
	#RUN  conda install -c potassco re2c tbb

	echo "SUCCESS"
	popdir
}

installdlvhex() {
	pushdir
	echo -n "Installing DLVHEX....."

	# check if reinstall is forced
	if [[ $1 != "-f" ]]; then
		command -v dlvhex2 && echo "ALREADY INSTALLED" && return
	fi

	REQUIRE  build-essential git-core g++ autoconf automake pkg-config libtool libltdl-dev libcurl4-openssl-dev re2c scons bison

	RUN  rm -rf "${BUILDDIR}/${DLVHEX}/"
	MKDIR "$INSTALLDIR/"
	MKDIR "$BUILDDIR/"
	RUN  cd "$BUILDDIR/"
	RUN  git clone --recursive ${DLVHEX_URL} ${DLVHEX}
	RUN  cd ${DLVHEX}
	RUN  git checkout ${DLVHEX_BRANCHORTAG}
	RUN  ./bootstrap.sh
	#export CXXFLAGS="-D_GLIBCXX_USE_CXX11_ABI=0 -std=c++98"
	RUN  ./configure --disable-python --with-boost=$INSTALLDIR --prefix=$INSTALLDIR/
	RUN  make -j${CPUCORES}
	RUN  make install

	echo "SUCCESS"
	popdir
}

installbox2d() {
	pushdir
	echo -n "Installing Box2D....."
	
	# check if reinstall is forced
	if [[ $1 != "-f" ]]; then
		ld -lBox2D "-L$INSTALLDIR/lib" && echo "ALREADY INSTALLED" && return
	fi

	REQUIRE  wget unzip cmake libglu-dev libxi-dev build-essential

	MKDIR "$BUILDDIR/"
	MKDIR "$INSTALLDIR/"
	RUN  cd "${BUILDDIR}/"
	RUN  wget -N ${BOX2D_URL}
	RUN  rm -rf "${BUILDDIR}/box2d/"
	RUN  unzip ${BOX2D_file}
	RUN  mv ${BOX2D_folder} box2d
	RUN  cd box2d/Box2D/Build
	RUN  cmake -DBOX2D_INSTALL=ON -DBOX2D_BUILD_SHARED=ON -DBOX2D_BUILD_STATIC=OFF -DBOX2D_BUILD_EXAMPLES=OFF .. -DCMAKE_INSTALL_PREFIX=${INSTALLDIR}/
	RUN  make -j${CPUCORES}
	RUN  make install

	echo "SUCCESS"
	popdir
}

installagent() {
	installagentjava
	installagentplugin
}

installagentjava() {
	pushdir
	echo -n "Installing Java agent (client)...."

	REQUIRE  ant default-jdk

	if [ -e ${F_DEVELOP} ]; then
		echo -n "re-creating java code parts in ${D_AGENT} [develop]"
		RUN rm -rf ${D_AGENT}/${OUT_JAVA}
		packjava
	fi

	RUN  cd ${D_AGENT}/${OUT_JAVA}
	RUN  ant jar

	echo "SUCCESS"
	popdir
}

# ${DIR_PLUGIN} is the path the hex plugin
# - this path can be changed in order to use another plugin
# - the plugin folder must contain a Makefile with a 'install' target
DIR_PLUGIN=src/angrybirds-box2dplugin

installagentplugin() {
	pushdir
	echo -n "Installing DLVHEX plugin....."

	if [ -e ${F_DEVELOP} ]; then
		echo -n "re-creating C++-plugin code parts in ${D_AGENT} [develop]"
		RUN rm -rf ${D_AGENT}/${OUT_PLUGIN}
		packplugin
	fi

	RUN  cd ${D_AGENT}/${OUT_PLUGIN}
	export PKG_CONFIG_PATH=${CONDADIR}/lib/pkgconfig:${INSTALLDIR}/lib64/pkgconfig:${INSTALLDIR}/lib/pkgconfig
	export LDFLAGS="-L${CONDADIR}/lib -L${INSTALLDIR}/lib -L${INSTALLDIR}/lib64"
	RUN ./configure --with-boost=${INSTALLDIR} 
	RUN make install 

	echo "SUCCESS"
	popdir
}

#-------------------------------------------------------------------------------

D_SRC=src         # source
D_FWK=framework   # framework
D_ENC=dlv         # encodings of hex programs
D_AGENT=angryhex  # out directory

OUT_JAVA=java-agent
OUT_PLUGIN=dlvhex-plugin

F_ARCHIVE=angryhex.zip

function PKG {
	# first argument: source file/dir
	# second argument: target within ${D_AGENT}
	# IMPORTANT: rsync rules apply!
	# replace directory/put into directory depending on target ending with or without "/"
	rsync -r --cvs-exclude $1 ${D_AGENT}/$2
}

packjava() {
	# PKG = rsync into ${D_AGENT} !
	MKDIR ${D_AGENT}/${OUT_JAVA}/src
	PKG ${D_SRC}/angryhexclient ${OUT_JAVA}/src/
	PKG ${D_FWK}/src/           ${OUT_JAVA}/src/
	PKG ${D_FWK}/ABServer.jar   ${OUT_JAVA}/
	PKG ${D_FWK}/external       ${OUT_JAVA}/
	PKG ${D_FWK}/plugin         ${OUT_JAVA}/
	PKG ${D_ENC}                ${OUT_JAVA}/
	PKG build.xml               ${OUT_JAVA}/

	if [[ $1 == archive ]]; then
		PKG config.properties.tournament ${OUT_JAVA}/config.properties
	else
		PKG config.properties            ${OUT_JAVA}/config.properties
	fi
}

packplugin() {
	# PKG = rsync into ${D_AGENT} !
	MKDIR ${D_AGENT}/${OUT_PLUGIN}
	PKG ${DIR_PLUGIN}/ ${OUT_PLUGIN}/
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

clean() {
	echo -n "Removing all AngryHEX code for clean rebuild..."
	RUN rm -rf ${D_AGENT}
	echo "SUCCESS"
	echo "If you want to clean all locally built EXTERNAL dependencies, please remove ${BUILDDIR} and ${INSTALLDIR}."
}

#-------------------------------------------------------------------------------

renewlog() {
	rm -rf "${F_LOG}"
	# print system info to ${F_LOG}
	uname -a >> "${F_LOG}" 2>&1
	if [[ ${OSTYPE} == osx ]]; then
		sw_vers >> "${F_LOG}" 2>&1
	elif [[ ${OSTYPE} == ubuntu ]]; then
		lsb_release -a >> "${F_LOG}" 2>&1
	fi
}

#-------------------------------------------------------------------------------

CMD=$1
ARG=$2

# setup paths in install directory to have priority (we install to there and run from there)
export PATH="$CONDADIR/bin:$INSTALLDIR/bin:$PATH"
export LD_LIBRARY_PATH="$CONDADIR/lib:$INSTALLDIR/lib64:$INSTALLDIR/lib:$LD_LIBRARY_PATH"

if [ -z $CMD ]; then
	usage 1
elif [ -z $ARG ]; then
	case $CMD in
		"install") ARG=all ;;
		"run")     ARG=client ;;
	esac
fi

if [ ! "$CMD $ARG" == "set develop" ]; then
	# do this check if we are not currently setting develop mode
	# check if we are in a git repo but not in develop mode
	if [ -e ".git" ] && [ ! -e ${F_DEVELOP} ]; then
		# warn
		echo "WARNING: git clones should be used in development mode; please call ${B}./dev.angryhex.sh set develop${N}"
		# wait, to show the warning
		sleep 3
		# check if we are missing the important directories that cause installation issues so often
		if [ ! -d ${D_AGENT}/${OUT_JAVA} ] || [ ! -d ${D_AGENT}/${OUT_PLUGIN} ]; then
			echo "${B}EXTRA WARNING${N}: not in development mode and at least one of the folders ${D_AGENT}/${OUT_JAVA} and ${D_AGENT}/${OUT_PLUGIN} is missing"
			# wait to show the warning
			sleep 3
		fi
	fi
fi

set -x

case $CMD in
	"install")
		renewlog
		{
 		case $ARG in
			"all")          installdlv && installboost && installdlvhex && installbox2d && installagent ;; 
			"dlv")          installdlv -f ;;
			"boost")        installboost -f ;;
			"dlvhex")       installdlvhex -f ;;
			"box2d")        installbox2d -f ;;
			"agent")        installagent ;;
			"agent-java")   installagentjava ;;
			"agent-plugin") installagentplugin ;;
			*) echo "Unknown argument $ARG" && usage 1
		esac
		} 2>&1 |tee -a "${F_LOG}"
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
	"clean") clean ;;
	"archive") archive ;;
	"dlvhex2") ./inst/bin/dlvhex2 ${@:2} ;;
   *) echo "Invalid command: $CMD" && usage 1
esac

# vim:noet:
