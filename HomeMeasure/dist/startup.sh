#!/bin/bash
cp=$(find libs -name "*.jar" -exec printf :{} ';') 
	if [[ -n "$CLASSPATH" ]]; 
		then cp="$cp;CLASSPATH"
	fi
echo $cp
java -Djava.library.path=/usr/lib/jni/ -cp "$cp" ginious.home.measure.server.HMServer

