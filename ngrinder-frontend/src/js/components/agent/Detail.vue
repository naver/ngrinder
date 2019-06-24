<template>
    <div class="container">
        <vue-headful :title="i18n('agent.info.title')"></vue-headful>
        <fieldset>
            <legend class="header">
                <span v-text="i18n('agent.info.head')"></span>
                <button class="btn pull-right" @click="$router.go(-1)"
                        v-text="i18n('common.button.return')">
                </button>
            </legend>
        </fieldset>
        <div class="row">
            <div class="span3">
                <table class="table table-bordered table-striped">
                    <tbody>
                    <tr>
                        <th v-text="i18n('agent.info.IP')"></th>
                    </tr>
                    <tr>
                        <td v-text="agent.ip"></td>
                    </tr>

                    <tr>
                        <th v-text="i18n('agent.info.port')"></th>
                    </tr>
                    <tr>
                        <td v-text="agent.port"></td>
                    </tr>

                    <tr>
                        <th v-text="i18n('agent.info.name')"></th>
                    </tr>
                    <tr>
                        <td v-text="agent.hostName"></td>
                    </tr>

                    <tr>
                        <th v-text="i18n('agent.info.region')"></th>
                    </tr>
                    <tr>
                        <td v-text="agent.region"></td>
                    </tr>

                    <tr>
                        <th v-text="i18n('agent.info.version')"></th>
                    </tr>
                    <tr>
                        <td v-text="agent.version ? agent.version : 'Prior to 3.3'"></td>
                    </tr>

                    <tr>
                        <th v-text="i18n('agent.info.state')"></th>
                    </tr>
                    <tr>
                        <td v-text="agent.state.name"></td>
                    </tr>
                    </tbody>
                </table>
                <label v-text="i18n('agent.info.refreshInterval')"></label>
                <input type="text" class="span3" placeholder="number" v-model="interval" @keyup="refreshInterval">
            </div>
            <div class="span9">
                <h5 v-text="i18n('agent.info.cpu')"></h5>
                <div class="chart" id="cpu_usage_chart"></div>
                <h5 v-text="i18n('agent.info.memory')"></h5>
                <div class="chart" id="memory_usage_chart"></div>
            </div>
        </div>
        <!--content-->
    </div>
</template>

<script>
    import { Mixins } from 'vue-mixin-decorator';
    import Component from 'vue-class-component';
    import VueHeadful from 'vue-headful';
    import Base from '../Base.vue';
    import Chart from '../../chart.js';
    import Queue from '../../queue.js';
    import FormatMixin from '../perftest/mixin/FormatMixin.vue';

    @Component({
        name: 'agentDetail',
        components: { VueHeadful },
        props: {
            agentId: {
                type: String,
                required: true,
            },
            agentProp: {
                type: Object,
                required: false,
            },
        },
    })
    export default class AgentDetail extends Mixins(Base, FormatMixin) {
        agent = {
            state: { name: '' },
        };

        interval = 1;

        cpu = {
            queue: {},
            chart: {},
        };

        memory = {
            queue: {},
            chart: {},
        };

        intervalTimer = null;

        created() {
            if (this.agentProp) {
                this.agent = this.agentProp;
            } else {
                this.$http.get(`/agent/api/${this.agentId}`).then(res => this.agent = res.data);
            }
        }

        mounted() {
            this.cpu.queue = new Queue(60);
            this.cpu.chart = new Chart('cpu_usage_chart', [this.cpu.queue.getArray()], this.interval,
                { yAxisFormatter: this.formatPercentage }).plot();

            this.memory.queue = new Queue(60);
            this.memory.chart = new Chart('memory_usage_chart', [this.memory.queue.getArray()], this.interval,
                { yAxisFormatter: this.formatMemory }).plot();

            this.intervalTimer = setInterval(this.getState, this.interval * 1000);
        }

        beforeDestroy() {
            clearInterval(this.intervalTimer);
        }

        getState() {
            this.$http.get('/agent/api/state', {
                params: {
                    ip: this.agent.ip,
                    name: this.agent.hostName,
                    region: this.agent.region,
                },
            }).then(res => {
                this.cpu.queue.enQueue(res.data.cpuUsedPercentage);
                this.memory.queue.enQueue(res.data.totalMemory - res.data.freeMemory);
                this.cpu.chart.plot();
                this.memory.chart.plot();
            }).catch(err => console.error(err));
        }

        refreshInterval() {
            clearInterval(this.intervalTimer);
            this.intervalTimer = setInterval(this.getState, (Number(this.interval) || 1) * 1000);
        }
    }
</script>

<style lang="less" scoped>
    .table {
        font-size: 12px;
        border-top:#cccccc solid 1px;
        margin-top:14px;
    }
</style>
