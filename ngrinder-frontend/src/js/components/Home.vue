<template>
    <main class="container">
        <vue-headful :title="i18n('home.title')"/>
        <div class="main-banner" :style="`background-image: url('${contextPath}/img/bg_main_banner.png')`" data-step="1" :data-intro="i18n('intro.index.quick.start')">
            <div class="quick-start" :data-original-title="i18n('home.tip.url.title')" :data-content="i18n('home.tip.url.content')" data-placement="bottom" rel="popover">
                <input v-focus type="text" name="url" class="form-control" v-validate="{url: {require_protocol: true}, required: true}" ref="inputQuickStartUrl"
                       :class="{error: errors.any()}" :placeholder="i18n('home.placeholder.url')" data-step="2" :data-intro="i18n('intro.index.test.url')" v-model="quickStartUrl"/>
                <select class="select-item form-control" v-model="scriptType" name="scriptType" data-step="3" :data-intro="i18n('intro.index.select.language')">
                    <option v-for="handler in handlers" :value="handler.key" v-text="handler.title"></option>
                </select>
                <button class="btn btn-primary" data-step="4" :data-intro="i18n('intro.index.create')"
                        @click.prevent="quickStart" v-text="i18n('home.button.startTest')">
                </button>
            </div>
        </div>
        <section class="row">
            <home-panel :title="i18n('home.qa.title')" :entries="leftPanelEntries" :introJsDataStep="5" :introJsDataIntro="i18n('intro.index.qna')"
                        :seeMoreQuestionUrl="seeMoreQuestionUrl" :askQuestionUrl="askQuestionUrl">
            </home-panel>
            <home-panel :title="i18n('home.resources.title')" :entries="rightPanelEntries" :introJsDataStep="6" :introJsDataIntro="i18n('intro.index.resource')"
                        :seeMoreResourcesUrl="seeMoreResourcesUrl">
            </home-panel>
        </section>
    </main>
</template>

<script>
    import { Mixins } from 'vue-mixin-decorator';
    import Component from 'vue-class-component';
    import Base from 'Base.vue';
    import vueHeadful from 'vue-headful';
    import HomePanel from 'HomePanel.vue';
    import MessagesMixin from 'common/mixin/MessagesMixin.vue';
    import { TipType } from '../constants';

    @Component({
        name: 'home',
        components: { vueHeadful, HomePanel },
        $_veeValidate: {
            validator: 'new',
        },
    })
    export default class Index extends Mixins(Base, MessagesMixin) {
        leftPanelEntries = [];
        rightPanelEntries = [];
        handlers = [];
        askQuestionUrl = '';
        seeMoreQuestionUrl = '';
        seeMoreResourcesUrl = '';

        quickStartUrl = '';
        scriptType = '';

        mounted() {
            this.$store.commit('activeTip', TipType.INTROJS);
            if (this.$route.query.type === '404') {
                this.showErrorMsg('Requested URL does not exist');
            }
            this.getPanel();
            this.getHandlers();
            this.getConfig();
        }

        beforeDestroy() {
            this.$store.commit('activeTip', '');
        }

        getHandlers() {
            this.$http.get('home/api/handlers').then(res => {
                this.handlers = res.data;
                this.scriptType = this.handlers[0].key;
            }).catch(() => this.showErrorMsg(this.i18n('common.message.loading.error')));
        }

        getPanel() {
            this.showProgressBar();
            this.$http.get('home/api/panel').then(res => {
                this.leftPanelEntries = res.data.leftPanelEntries;
                this.rightPanelEntries = res.data.rightPanelEntries;
            })
            .catch(() => this.showErrorMsg(this.i18n('common.message.loading.error')))
            .finally(this.hideProgressBar);
        }

        getConfig() {
            this.$http.get('home/api/config').then(res => {
                this.askQuestionUrl = res.data.askQuestionUrl;
                this.seeMoreQuestionUrl = res.data.seeMoreQuestionUrl;
                this.seeMoreResourcesUrl = res.data.seeMoreResourcesUrl;
            }).catch(() => this.showErrorMsg(this.i18n('common.message.loading.error')));
        }

        quickStart() {
            this.$validator.validate('url').then(result => {
                if (!result) {
                    this.$nextTick(() => this.$refs.inputQuickStartUrl.focus());
                    return;
                }
                const params = {
                    scriptType: this.scriptType,
                    url: this.quickStartUrl,
                };
                this.$router.push({ name: 'quickStart', query: params });
            });
        }
    }
</script>

<style lang="less" scoped>
    .container {
        padding: 0;

        .main-banner {
            margin-bottom: 10px;
            height: 160px;
            padding: 0;
            margin-top: 0;
            border-radius: 6px;

            button {
                vertical-align: baseline;
            }
        }

        .quick-start {
            padding-left: 290px;
            padding-top: 30px;

            * {
                margin: 0;
            }

            .form-control {
                display: inline-block;
            }

            input {
                width: 460px;
                height: 30px;

                &.error {
                    border: 2px solid #B94A48;
                }
            }

            select {
                width: 140px;
            }
        }

        .row {
            justify-content: space-between;
        }
    }
</style>
