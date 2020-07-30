<template>
    <div class="agent-detail container">
        <vue-headful :title="i18n('agent.info.title')"></vue-headful>
        <fieldset>
            <legend class="header border-bottom d-flex">
                <span v-text="i18n('agent.info.head')"></span>
                <button class="btn btn-success ml-auto mt-auto mb-auto" @click="$router.go(-1)">
                    <i class="fa fa-undo"></i>
                    <span v-text="i18n('common.button.return')"></span>
                </button>
            </legend>
        </fieldset>
        <div class="row">
            <div>
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
                        <template v-if="ngrinder.config.clustered">
                            <tr>
                                <th v-text="i18n('agent.info.region')"></th>
                            </tr>
                            <tr>
                                <td v-text="agent.region"></td>
                            </tr>
                        </template>
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
                <label v-text="i18n('agent.info.refreshInterval')" class="d-block"></label>
                <input type="text" class="input-refresh-interval form-control"
                       placeholder="number" v-model="interval" @keyup="refreshInterval">
            </div>
            <div class="chart-container">
                <h5 v-text="i18n('agent.info.cpu')"></h5>
                <div class="chart border border-secondary mb-4" id="cpu-usage-chart"></div>
                <h5 v-text="i18n('agent.info.memory')"></h5>
                <div class="chart border border-secondary" id="memory-usage-chart"></div>
            </div>
        </div>
    </div>
</template>

<script>
    import { Mixins } from 'vue-mixin-decorator';
    import { Component, Prop } from 'vue-property-decorator';
    import VueHeadful from 'vue-headful';
    import Base from '../Base.vue';
    import Queue from '../../queue.js';
    import ChartMixin from '../common/mixin/ChartMixin.vue';
    import FormatMixin from '../common/mixin/FormatMixin.vue';

    Component.registerHooks(['beforeRouteEnter']);
    @Component({
        name: 'agentDetail',
        components: { VueHeadful },
    })
    export default class AgentDetail extends Mixins(Base, ChartMixin, FormatMixin) {
        @Prop({ type: Object, required: false })
        agent;

        @Prop({ type: String, required: true })
        ip;

        @Prop({ type: String, required: true })
        name;

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

        beforeRouteEnter(to, from, next) {
            AgentDetail.prepare(to).then(next);
        }

        mounted() {
            this.cpu.queue = new Queue(60);
            this.cpu.chart = this.drawChart('cpu-usage-chart', { 'cpu-usage': this.cpu.queue.getArray() }, this.interval, this.formatPercentage, {
                transition: { duration: null },
                legend: { show: false },
            });

            this.memory.queue = new Queue(60);
            this.memory.chart = this.drawChart('memory-usage-chart', { 'memory-usage': this.memory.queue.getArray() }, this.interval, this.formatMemory, {
                transition: { duration: null },
                legend: { show: false },
            });

            this.intervalTimer = setInterval(this.getState, this.interval * 1000);
        }

        beforeDestroy() {
            clearInterval(this.intervalTimer);
        }

        static prepare(route) {
            if (!route.params.agent) {
                return Base.prototype.$http.get(`/agent/api/${route.params.ip}/${route.params.name}`)
                    .then(res => route.params.agent = res.data)
                    .then(() => Promise.resolve());
            } else {
                return Promise.resolve();
            }
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
                this.cpu.chart.load({ json: { 'cpu-usage': this.cpu.queue.getArray() } });
                this.memory.chart.load({ json: { 'memory-usage': this.memory.queue.getArray() } });
            });
        }

        refreshInterval() {
            if (this.interval < 1 || isNaN(this.interval)) {
                this.interval = 1;
            }

            clearInterval(this.intervalTimer);
            this.intervalTimer = setInterval(this.getState, (Number(this.interval) || 1) * 1000);
        }
    }
</script>

<style lang="less" scoped>
    .agent-detail {
        table {
            font-size: 12px;
            border-top:#cccccc solid 1px;
            margin-top:14px;
            width: 220px;
        }

        .input-refresh-interval {
            width: 220px;
        }

        .chart-container {
            width: 940px;
            margin-left: auto;

            .chart {
                height: 300px;
            }
        }
    }
</style>
