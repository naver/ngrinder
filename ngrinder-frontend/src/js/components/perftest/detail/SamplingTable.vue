<template>
    <table class="table table-striped table-bordered ellipsis">
        <colgroup>
            <col width="30px">
            <col width="85px">
            <col width="85px">
            <col width="55px">
            <col width="60px">
            <col width="65px">
            <col width="65px">
            <col width="60px">
        </colgroup>
        <thead>
        <tr>
            <th class="no-click" v-text="i18n('perfTest.running.testID')"></th>
            <th class="no-click" v-text="i18n('perfTest.running.testName')"></th>
            <th class="no-click" v-text="i18n('perfTest.running.success')"></th>
            <th class="no-click" v-text="i18n('perfTest.running.errors')"></th>
            <th class="no-click" v-text="'MTT'" :title="i18n('perfTest.running.meantime')"></th>
            <th class="no-click" v-text="i18n('perfTest.running.tps')"></th>
            <th class="no-click" v-text="i18n('perfTest.running.peakTPS')" :title="i18n('perfTest.running.peakTPS.full')"></th>
            <th class="no-click" v-text="i18n('perfTest.running.responseBytePerSecond')" :title="i18n('perfTest.running.responseBytePerSecond.full')"></th>
        </tr>
        </thead>
        <tbody>
        <tr v-for="statistic in statistics">
            <td v-text="statistic.testNumber.toFixed(0)"></td>
            <td class="ellipsis" :title="` ${statistic.testDescription} `" v-text="statistic.testDescription"></td>
            <td v-text="statistic.Tests.toFixed(0)"></td>
            <td v-text="statistic.Errors.toFixed(0)"></td>
            <td v-text="statistic['Mean_Test_Time_(ms)'].toFixed(0)"></td>
            <td v-text="statistic.TPS.toFixed(0)"></td>
            <td v-text="statistic['Mean_time_to_first_byte'].toFixed(0)"></td>
            <td v-text="formatNetwork(null, statistic['Response_bytes_per_second'])"></td>
        </tr>
        </tbody>
    </table>
</template>

<script>
    import Base from '../../Base.vue';
    import Component from 'vue-class-component';

    @Component({
        name: 'samplingTable',
        props: {
            statistics: {
                type: [Array, Object],
                required: true,
            },
        },
    })
    export default class SamplingTable extends Base {}
</script>

<style lang="less" scoped>

</style>
