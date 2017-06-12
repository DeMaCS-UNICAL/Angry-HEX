#!/bin/sh
#
# Copyright 2005-2011 Thomas Krennwallner <tkren@kr.tuwien.ac.at>
#
# This is bootstrap.sh, a script to
#  1. check for libtool, autoconf, automake, and pkg-config; and to
#  2. run autoreconf to create libltdl, configure, and all Makefile.in files.
#
# See also http://sourceware.org/autobook/autobook/autobook_43.html
#
# This program is free software: you can redistribute it and/or modify
# it under the terms of the GNU General Public License as published by
# the Free Software Foundation, either version 3 of the License, or
# (at your option) any later version.
#
# This program is distributed in the hope that it will be useful,
# but WITHOUT ANY WARRANTY; without even the implied warranty of
# MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
# GNU General Public License for more details.
#
# You should have received a copy of the GNU General Public License
# along with this program.  If not, see <http://www.gnu.org/licenses/>.
#

# first check that everything is properly installed

ARC=`which autoreconf`
if [ ! -x "$ARC" ]; then
    echo "autoreconf: command not found. Please install GNU autoconf."
    exit 1
fi

AM=`which automake`
if [ ! -x "$AM" ]; then
    echo "automake: command not found. Please install GNU automake."
    exit 1
fi

LT=`which libtoolize`
GLT=`which glibtoolize`
if [ ! -x "$LT" ] && [ ! -x "$GLT" ]; then
    echo "libtoolize: command not found. Please install GNU libtool."
    exit 1
fi

PC=`which pkg-config`
if [ ! -x "$PC" ]; then
    echo "pkg-config: command not found. Please install pkg-config."
    exit 1
fi

# copy libltdl and rebuild autotool files
autoreconf -f -i -W all
