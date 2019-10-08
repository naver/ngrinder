<template>
    <article class="panel-container">
        <header class="pb-2 mt-4 mb-3 border-bottom">
            <h4 :data-step="introJsDataSetp" :data-intro="introJsDataIntro" v-text="title"></h4>
        </header>
        <div class="card bg-light">
            <br>
            <table class="table ellipsis">
                <colgroup>
                    <col width="350">
                </colgroup>
                <tbody>
                    <template v-for="(entry, index) in entries">
                        <tr v-if="index < panelSize">
                            <td class="ellipsis">
                                <span v-if="entry.new" class="badge badge-danger" v-text="'new'"></span>
                                <a :href="entry.link" target="_blank" v-text="entry.title"></a>
                            </td>
                            <td>{{ entry.lastUpdatedDate | dateFormat('YYYY-MM-DD') }}</td>
                        </tr>
                    </template>
                    <tr>
                        <td v-if="askQuestionUrl">
                            <img :src="`${contextPath}/img/asksupport.gif`" />
                            <a :href="askQuestionUrl" target="_blank" v-text="i18n('home.button.ask')"></a>
                        </td>
                        <td v-if="seeMoreQuestionUrl" :colspan="(askQuestionUrl || entries.length === 0) ? 1 : 2" class="text-right">
                            <a :href="seeMoreQuestionUrl" target="_blank">
                                <i class="fa fa-share"></i>
                                <span v-text="i18n('home.button.more')"></span>
                            </a>
                        </td>
                    </tr>
                </tbody>
            </table>
        </div>
    </article>
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
        width: 460px;

        .card {
            height: 300px;
            padding: 10px;
        }

        table {
            background-color: #f8f9fa;
            font-size: 12px;

            td, th {
                padding: 8px;
            }
        }
    }
</style>
