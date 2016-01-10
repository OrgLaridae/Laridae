#!/bin/bash

#set the path to automated.sh
AUTO_PATH="/home/ruveni/IdeaProjects/DataAgent/src/main/java/org/mora/cep/AutomateWRF";

export NETCDF=/usr/local;
cd $AUTO_PATH;
chmod +x automated.sh;
./automated.sh [options] <<-END
32
0
2
2
END