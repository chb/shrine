echo "[shrine/spin.sh]"

#########
# SSL and Networking setup
#
# SHRINE uses the spin p2p networking subsystem.
# SSL and routing tables are required for operation.
#
# Generates SSL certificate
# @see ssl_keytool.sh
#
# Communication between Peers occurs over SSL.
# @see skel/keystore.xml
#
# By default we only allow communication with ourselves.
# @see skel/routingtable.xml
#
#
#########
source ./shrine.rc

#  TODO: use work directory
#########

#####
echo "[shrine/spin.sh] Ensuring SPIN_HOME exists : $SPIN_CONF_DIR"

rm -rf    ${SPIN_CONF_DIR}
mkdir -p  ${SPIN_CONF_DIR}

#####
echo "[shrine/spin.sh] Generating a keystore pair (public key, private key) "

./ssl_keytool.sh -generate
mv $KEYSTORE_ALIAS.cer ${SPIN_CONF_DIR}/. 

#####
echo "[shrine/spin.sh] Configuring keystore.xml"

sed s:KEYSTORE_FILE:$KEYSTORE_FILE:g                    skel/keystore.xml    > keystore.xml.1
sed s:KEYSTORE_PASSWORD:$KEYSTORE_PASSWORD:g                 keystore.xml.1  > keystore.xml.2
sed s:KEYSTORE_ALIAS:$KEYSTORE_ALIAS:g                       keystore.xml.2  > keystore.xml.3 

cp keystore.xml.3 ${SPIN_CONF_DIR}/keystore.xml 
rm keystore.xml.1 keystore.xml.2 keystore.xml.3

#####
echo "[shrine/spin.sh] Configuring routingtable.xml"

sed s:SHRINE_IP:$SHRINE_IP:g  skel/routingtable.xml > ${SPIN_CONF_DIR}/routingtable.xml

#####
echo "[shrine/spin.sh] Done."
