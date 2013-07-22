echo "[install-mysql-server.sh] NOTE: Shrine saves only query history, not patient data"
echo "[install-mysql-server.sh] installing mysql which is the default database for SHRINE. Any SQL database can be used."

yum -y install mysql-server
yum clean all
service mysqld start

