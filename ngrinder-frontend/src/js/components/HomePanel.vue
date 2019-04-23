<template>
    <div class="span6 panel-container">
        <div class="page-header">
            <h4 :data-step="introJsDataSetp" :data-intro="introJsDataIntro" v-text="title"></h4>
        </div>

        <div class="well">
            <br>
            <table class="table table-striped ellipsis">
                <colgroup>
                    <col width="350">
                </colgroup>
                <tbody>
                <template v-for="(entry, index) in entries">
                    <tr v-if="index < panelSize">
                        <td class="ellipsis">
                            <span v-if="entry.new" class="label label-info" v-text="'new'"></span>
                            <a :href="entry.link" target="_blank" v-text="entry.title"></a>
                        </td>
                        <td>{{entry.lastUpdatedDate | dateFormat('YYYY-MM-DD') }}</td>
                    </tr>
                </template>
                <tr>
                    <td v-if="askQuestionUrl">
                        <img src="/img/asksupport.gif" />
                        <a :href="askQuestionUrl" target="_blank" v-text="i18n('home.button.ask')"></a>
                    </td>
                    <td v-else></td>
                    <td v-if="seeMoreQuestionUrl"><a :href="seeMoreQuestionUrl" target="_blank" v-text="i18n('home.button.more')"><i class="icon-share-alt"></i></a></td>
                </tr>
                </tbody>
            </table>
        </div>
    </div>
</template>

<script>
    import Component from 'vue-class-component';
    import Base from 'Base.vue';

    @Component({
        name: 'homePanel',
        props: {
            title: {
                type: String,
                required: true,
            },
            entries: {
                type: Array,
                required: true,
            },
            panelSize: {
                type: Number,
                default: 6,
            },
            seeMoreQuestionUrl: String,
            askQuestionUrl: String,
            introJsDataIntro: String,
            introJsDataSetp: Number,
        },
    })
    export default class HomePanel extends Base {}
</script>

<style lang="less" scoped>
    .panel-container {
        table {
            font-size: 12px;
        }
    }
</style>
