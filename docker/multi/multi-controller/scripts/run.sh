#!/bin/bash
cd ${BASE_DIR}/h2/bin
java -cp h2*.jar org.h2.tools.Server &
sleep 2

idx=1
p_base=79
clp_base=10009
cp_base=9009
tail_cmd="tail "
while [ $idx -le ${COUNT} ]; do
        var_region="REGION_${idx}"
        eval region_name='$'$var_region
        if [ ${#region_name} -eq 0 ]; then
                region_name="region${idx}"
        fi
        java -jar -XX:MaxPermSize=200m -jar ${BASE_DIR}/multi-controller/ngrinder-controller-3.3.war -p $((p_base + idx)) -cm easy -clp $((clp_base + idx)) -r ${region_name} -cp $((cp_base + idx)) -Dcontroller.max_concurrent_test=3 -Dcontroller.verbose=false &
        i=1
        while [ $i -le 10 ]; do
                curl http://127.0.0.1:$((p_base + idx)) && break
                sleep 10 && i=$((i + 1))
        done
        tail_cmd="${tail_cmd} -f /root/.ngrinder_ex/logs/ngrinder_${region_name}.log "
        idx=$((idx + 1))
done
eval ${tail_cmd}
