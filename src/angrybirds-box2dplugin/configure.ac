AC_PREREQ([2.61])
AC_INIT([dlvhex2-hexagentplugin],[0.0.1],[dlvhex-devel@lists.sourceforge.net])
AC_CONFIG_SRCDIR([libdlvhexplugin-hexagentplugin.cpp])

# for documentation of the versioning scheme see
# https://sourceforge.net/apps/trac/dlvhex/wiki/LibraryVersions
DLVHEX_DEFINE_VERSION([HEXAGENTPLUGIN],[$PACKAGE_VERSION])
DLVHEX_DEFINE_VERSION([DLVHEX_ABI],[7.0.0])

AM_INIT_AUTOMAKE

AC_CANONICAL_HOST

# by default we look into /opt/local and /sw for include files (macports vs. fink)
case $host_os in
     darwin*|macosx*)
	CPPFLAGS="$CPPFLAGS -I/opt/local/include -I/sw/include"
     ;;
esac 


# Checks for programs.
AC_PROG_CXX
AC_LANG([C++])

# Checks for header files.
AC_HEADER_DIRENT
AC_CHECK_HEADERS([sys/param.h])

# Checks for typedefs, structures, and compiler characteristics.
AC_HEADER_STDBOOL
AC_C_CONST

# Checks for library functions.
AC_LIBTOOL_DLOPEN # we build a module library
AC_DISABLE_STATIC # we don't need a static library
AC_PROG_LIBTOOL

#
# check for other libraries here
#

# check for dlvhex, sets various variables
DLVHEX_REQUIRE([2.0.0])

# checking for boost libs
BOOST_REQUIRE([1.55.0])
BOOST_SMART_PTR
BOOST_STRING_ALGO
BOOST_TOKENIZER

# where the plugin will be installed by default.
# running configure will take sysplugindir from dlvhex.pc,
# running configure --enable-userinstall will take homeplugindir.
AC_ARG_ENABLE(userinstall, 
             [  --enable-userinstall    Install plugin into home directory],
             [plugindir=$HOME/$DLVHEX_USERPLUGINDIR],
             [plugindir=$DLVHEX_SYSPLUGINDIR]
             )
AC_SUBST([plugindir])
if test "x$plugindir" == "x"; then
	AC_MSG_ERROR([Cannot determine nonempty value for plugindir!])
fi

#
# all needed makefiles
#
AC_CONFIG_FILES([
           Makefile 
])

AC_OUTPUT

dnl output configuration
cat <<EOF
 
$PACKAGE_TARNAME $PACKAGE_VERSION configuration:

 - Compiler settings
 CC: $CC
 CXX: $CXX
 CFLAGS: $CFLAGS
 CPPFLAGS: $CPPFLAGS
 CXXFLAGS: $CXXFLAGS
 LDFLAGS: $LDFLAGS

 - from pkg-config
 DLVHEX_CFLAGS: $DLVHEX_CFLAGS
 DLVHEX_LIBS: $DLVHEX_LIBS
 DLVHEX_BINDIR: $DLVHEX_BINDIR
 
 - Path settings
 prefix: $prefix
 plugindir: $plugindir
EOF
