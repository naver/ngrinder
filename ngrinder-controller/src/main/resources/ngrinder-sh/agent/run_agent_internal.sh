#!/bin/sh
java -server -cp "lib/*" org.ngrinder.NGrinderAgentStarter --mode=agent --command=run $@
