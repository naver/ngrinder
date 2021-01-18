#!/bin/sh
java -server -cp "${ngrinder_core}:${ngrinder_runtime}:lib/*" org.ngrinder.NGrinderAgentStarter --mode=agent --command=run \$@
