#!/bin/sh
java -server -cp "${ngrinder_core}:${ngrinder_groovy}:lib/*" org.ngrinder.NGrinderAgentStarter --mode=agent --command=run \$@
