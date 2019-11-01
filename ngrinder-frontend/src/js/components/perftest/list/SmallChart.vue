<template>
    <table class="small-chart-table">
        <tr>
            <td><div style="width: 280px; height: 150px;" class="small-chart" :id="`tps_${rowData.id}`"></div></td>
            <td><div style="width: 280px; height: 150px;" class="small-chart" :id="`mtt_${rowData.id}`"></div></td>
            <td><div style="width: 280px; height: 150px;" class="small-chart" :id="`err_${rowData.id}`"></div></td>
        </tr>
    </table>
</template>

<script>
    import { Mixins } from 'vue-mixin-decorator';
    import Component from 'vue-class-component';

    import ChartMixin from '../../common/mixin/ChartMixin.vue';

    @Component({
        name: 'smallChart',
        props: {
            rowData: {
                type: Object,
                required: true,
            },
            rowIndex: {
                type: Number,
            },
        },
    })
    export default class SmallChart extends Mixins(ChartMixin) {
        created() {
            this.showChart();
        }

        showChart() {
            this.$http.get(`/perftest/api/${this.rowData.id}/graph`, {
                params: {
                    dataType: 'TPS,Errors,Mean_Test_Time_(ms),Mean_time_to_first_byte,User_defined',
                    imgWidth: 100,
                    onlyTotal: true,
                },
            }).then(res => this.initCharts(res.data));
        }

        initCharts(data) {
            const interval = data.chartInterval;

            this.drawChart(`tps_${this.rowData.id}`, 'TPS', data.TPS, interval);
            this.drawChart(`mtt_${this.rowData.id}`, 'MTT', data.Mean_Test_Time_ms, interval);
            this.drawChart(`err_${this.rowData.id}`, 'ERR', data.Errors, interval);
        }
    }
</script>

<style lang="less" scoped>
    .fade-enter-active {
        transition: opacity .6s;
    }
    .fade-enter, .fade-leave-to {
        opacity: 0;
    }
</style>

<style lang="less">
    @import '../../../../plugins/jqplot/css/jquery.jqplot.min.css';

    .small-chart-table {
        width: 100%;

        &.jqplot-table-legend {
            left: 32px !important;
            top: 17px !important;
            width: 20px;
            height: 16px;
            padding-bottom: 0;
        }

        div.small-chart {
            border: 1px solid #878988;
            height: 150px;
            width: 289px;
        }

        th.small-border {
            padding-left: 3px;
            padding-right: 3px;
        }
    }

</style>
