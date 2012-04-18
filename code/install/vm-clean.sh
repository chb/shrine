#!/bin/bash

#########
# Clean previous i2b2/shrine installation.
# This script can be run even without a previous installation attempt.
#
# http://open.med.harvard.edu/display/SHRINE
#
# Configuration params
#
# @see i2b2/i2b2.rc
# @see shrine/shrine.rc
#
#########

echo "[vm-clean.sh] Cleaning i2b2, restoring to pre-shrine-install state."
cd i2b2
chmod +x *.sh
./clean.sh
cd ..

echo "[vm-clean.sh] Cleaning shrine, restoring to pre-shrine-install state."

cd shrine
chmod +x *.sh
./clean.sh
cd ..

echo "[vm-clean.sh] Done."