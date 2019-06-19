#!/bin/sh

KEYSTORE=/etc/keystore/cacerts

if [ -f $KEYSTORE ]; then
	exec $@
fi

echo "Keystore not present yet at $KEYSTORE" 
exit 1
