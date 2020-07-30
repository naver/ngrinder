<template>
    <table class="table table-bordered ellipsis">
        <colgroup>
            <col width="45px">
            <col width="85px">
            <col width="70px">
            <col width="55px">
            <col width="60px">
            <col width="65px">
            <col width="65px">
            <col width="60px">
        </colgroup>
        <thead>
            <tr>
                <th v-text="i18n('perfTest.running.testID')"></th>
                <th v-text="i18n('perfTest.running.testName')"></th>
                <th v-text="i18n('perfTest.running.success')"></th>
                <th v-text="i18n('perfTest.running.errors')"></th>
                <th :title="i18n('perfTest.running.meantime')">MTT</th>
                <th v-text="i18n('perfTest.running.tps')"></th>
                <th v-if="type === 'last'" :title="i18n('perfTest.running.meanTimeToFirstByte')">MTTFB</th>
                <th v-else v-text="i18n('perfTest.running.peakTPS')" :title="i18n('perfTest.running.peakTPS.full')"></th>
                <th v-text="i18n('perfTest.running.responseBytePerSecond')" :title="i18n('perfTest.running.responseBytePerSecond.full')"></th>
            </tr>
        </thead>
        <tbody>
            <tr v-for="statistic in statistics">
                <td class="ellipsis" :title="statistic.testNumber">{{ statistic.testNumber | numFormat }}</td>
                <td class="ellipsis" :title="` ${statistic.testDescription} `" v-text="statistic.testDescription"></td>
                <td>{{ statistic.Tests | numFormat }}</td>
                <td>{{ statistic.Errors | numFormat }}</td>
                <td>{{ statistic['Mean_Test_Time_(ms)'] | numFormat }}</td>
                <td>{{ statistic.TPS | numFormat }}</td>
                <td v-if="type === 'last'">{{ statistic['Mean_time_to_first_byte'] | numFormat }}</td>
                <td v-else>{{ statistic.Peak_TPS | numFormat }}</td>
                <td v-text="formatNetwork(statistic['Response_bytes_per_second'])"></td>
            </tr>
        </tbody>
    </table>
</template>

<script>
    import { Mixins } from 'vue-mixin-decorator';
    import Component from 'vue-class-component';
    import Base from '../../Base.vue';
    import FormatMixin from '../../common/mixin/FormatMixin.vue';

    @Component({
        name: 'samplingTable',
        props: {
            statistics: {
                type: [Array, Object],
                required: true,
            },
            type: {
                type: String,
                default: 'last',
            },
        },
    })
    export default class SamplingTable extends Mixins(Base, FormatMixin) {}
</script>

<style lang="less" scoped>

</style>
