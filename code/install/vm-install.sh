#!/bin/bash

#########
# SHRINE Installation for existing i2b2 VM users.
#
# This quick install uses defaults.
#
# !!! You MUST CHANGE the default PASSWORDS before using real patient data. !!!
# !!! You MUST CHANGE the default PASSWORDS before using real patient data. !!!
# !!! You MUST CHANGE the default PASSWORDS before using real patient data. !!!
#
# @author Andrew McMurry
# @author Bill Simons
# @author Clint Gilbert
#
# http://open.med.harvard.edu/display/SHRINE
#
# Configuration params
#
# @see i2b2/i2b2.rc
# @see shrine/shrine.rc
#
#########

echo "[vm.sh] Preparing I2B2 for SHRINE"
chmod +x i2b2/*.sh

cd i2b2
# ./clean.sh
./prepare.sh
cd ..

echo "[vm.sh] Installing SHRINE "

cd shrine
chmod +x *.sh
# ./clean.sh
./install.sh
cd ..

echo "source ~/i2b2.rc"   >> ~/.bashrc
echo "source ~/shrine.rc" >> ~/.bashrc

echo "[vm.sh] Done. "
echo "[vm.sh] ********* "
echo "[vm.sh] You can now start shrine! "
echo "[vm.sh] ********* "
source ~/shrine.rc
