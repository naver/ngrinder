#!/bin/sh
java -server -cp "${ngrinder_core}:lib/*" org.ngrinder.NGrinderAgentStarter --mode=agent --command=run \$@
