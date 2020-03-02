<template>
    <article class="panel-container">
        <header class="pb-2 mt-4 mb-3 border-bottom">
            <h4 :data-step="introJsDataStep" :data-intro="introJsDataIntro" v-text="title"></h4>
        </header>
        <div class="card bg-light">
            <div class="table-container">
                <table class="table ellipsis">
                    <colgroup>
                        <col width="480">
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
                    </tbody>
                </table>
            </div>
            <div class="d-flex">
                <div v-if="askQuestionUrl">
                    <img :src="`${contextPath}/img/asksupport.gif`" />
                    <a :href="askQuestionUrl" target="_blank" v-text="i18n('home.button.ask')"></a>
                </div>
                <div v-if="seeMoreUrl" :colspan="(askQuestionUrl || entries.length === 0) ? 1 : 2" class="ml-auto more">
                    <a :href="seeMoreUrl" target="_blank">
                        <i class="fa fa-share"></i>
                        <span v-text="i18n('home.button.more')"></span>
                    </a>
                </div>
            </div>
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
                default: 8,
            },
            seeMoreQuestionUrl: String,
            seeMoreResourcesUrl: String,
            askQuestionUrl: String,
            introJsDataIntro: String,
            introJsDataStep: Number,
        },
    })
    export default class HomePanel extends Base {
        get seeMoreUrl() {
            return this.seeMoreQuestionUrl || this.seeMoreResourcesUrl;
        }
    }
</script>

<style lang="less" scoped>
    .panel-container {
        width: 590px;

        .card {
            height: 350px;
            padding: 10px;
        }

        .table-container {
            height: 285px;
            margin-top: 14px;

            table {
                background-color: #f8f9fa;
                font-size: 12px;

                td, th {
                    padding: 8px;
                    border-bottom: 1px solid #dee2e6;
                }
            }
        }

        .d-flex {
            padding: 6px;
        }

        .more {
            padding-right: 12px;
        }
    }
</style>
