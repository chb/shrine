#!/bin/bash

echo "[shrine/monitor.sh] Begin."

#########
# SHRINE Monitor
#
# Components
# 1. scanner   = benchmark and qc every queryable concept
# 2. heartbeat = sends a message periodically to ensure the network is active (extends scanner code)
#
#########
source shrine.rc

mkdir -p $SHRINE_HOME
mkdir -p work; cd work
#########

echo "[shrine/monitor.sh] Downloading and unpacking..."

# wget --no-clobber  http://repo.open.med.harvard.edu/nexus/content/repositories/releases/net/shrine/shrine-monitor/1.10/shrine-monitor-1.10-dist.zip
# wget --no-clobber   http://repo.open.med.harvard.edu/nexus/content/repositories/snapshots/net/shrine/shrine-monitor/1.11-SNAPSHOT/shrine-monitor-1.11-SNAPSHOT-dist.zip

unzip -qo shrine-monitor-1.10*.zip

rm -rf monitor
rm -rf $SHRINE_HOME/monitor
mv shrine-monitor-1.10-dist monitor

mv monitor/scanner.properties monitor/scanner.properties.default
chmod +x monitor/*.sh

echo "[shrine/monitor.sh] creating scanner profile for I2B2 standalone instance."

sed s:I2B2_HIVE_IP:$I2B2_HIVE_IP:g  ../skel/scanner.properties.i2b2   > monitor/scanner.properties.i2b2
sed s:SPIN_CONF:$HOME/.spin/conf:g  monitor/scanner.properties.i2b2   > monitor/scanner.properties.i2b2.tmp

cp monitor/scanner.properties.i2b2.tmp    monitor/scanner.properties.i2b2
rm monitor/scanner.properties.i2b2.tmp

echo "[shrine/monitor.sh] creating scanner profile for SHRINE instance."

sed s:SHRINE_IP:$SHRINE_IP:g              ../skel/scanner.properties.shrine   > monitor/scanner.properties.shrine
sed s:SPIN_CONF:$HOME/.spin/conf:g        monitor/scanner.properties.shrine   > monitor/scanner.properties.shrine.tmp

cp monitor/scanner.properties.shrine.tmp    monitor/scanner.properties.shrine
rm monitor/scanner.properties.shrine.tmp

rm -rf $SHRINE_HOME/monitor
cp -a monitor $SHRINE_HOME/.

echo "[shrine/monitor.sh] Done."

cd ..