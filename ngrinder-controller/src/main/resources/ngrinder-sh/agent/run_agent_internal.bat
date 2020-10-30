@ECHO OFF
setLocal EnableDelayedExpansion
for /R ./lib %%a in (*.jar) do (
  set CLASSPATH=!CLASSPATH!;%%a
)
set CLASSPATH=!CLASSPATH!
java -server -cp "${ngrinder_core};${ngrinder_groovy};%CLASSPATH%" org.ngrinder.NGrinderAgentStarter --mode agent --command run %*
