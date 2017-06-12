AUTOMAKE_OPTIONS = gnu 1.10
ACLOCAL_AMFLAGS = -I m4

AM_CPPFLAGS = $(BOOST_CPPFLAGS) $(DLVHEX_CFLAGS) -std=c++11

plugin_LTLIBRARIES = libdlvhexplugin-hexagentplugin.la
libdlvhexplugin_hexagentplugin_la_SOURCES = libdlvhexplugin-hexagentplugin.cpp
libdlvhexplugin_hexagentplugin_la_LDFLAGS = -avoid-version -module
libdlvhexplugin_hexagentplugin_la_LIBADD = -lBox2D
