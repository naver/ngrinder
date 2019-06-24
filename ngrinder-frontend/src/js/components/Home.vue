<template>
    <div class="container">
        <vue-headful title="Home"/>
        <div class="hero-unit main-banner" data-step="1" :data-intro="i18n('intro.index.quick.start')">
            <div class="quick-start" :data-original-title="i18n('home.tip.url.title')" :data-content="i18n('home.tip.url.content')" data-placement="bottom" rel="popover">
                <input v-focus type="text" name="url" class="span6" v-validate="{url: {require_protocol: true}, required: true}" ref="inputQuickStartUrl"
                       :class="{error: errors.any()}" :placeholder="i18n('home.placeholder.url')" data-step="2" :data-intro="i18n('intro.index.test.url')" v-model="quickStartUrl"/>
                <select class="select-item span2" v-model="scriptType" name="scriptType" data-step="3" :data-intro="i18n('intro.index.select.language')">
                    <option v-for="handler in handlers" :value="handler.key" v-text="handler.title"></option>
                </select>
                <button id="start_test_btn" class="btn btn-primary" data-step="4"
                        :data-intro="i18n('intro.index.create')" @click.prevent="quickStart" v-text="i18n('home.button.startTest')">
                </button>
            </div>
        </div>
        <div class="row">
            <home-panel :title="i18n('home.qa.title')" :entries="leftPanelEntries" :introJsDataSetp="5" :introJsDataIntro="i18n('intro.index.qna')"
                        :seeMoreQuestionUrl="seeMoreQuestionUrl" :askQuestionUrl="askQuestionUrl"></home-panel>
            <home-panel :title="i18n('home.resources.title')" :entries="rightPanelEntries" :introJsDataSetp="6" :introJsDataIntro="i18n('intro.index.resource')"
                        :seeMoreQuestionUrl="seeMoreQuestionUrl"></home-panel>
        </div>
        <intro-button></intro-button>
    </div>
</template>

<script>
    import { Mixins } from 'vue-mixin-decorator';
    import Component from 'vue-class-component';
    import Base from 'Base.vue';
    import vueHeadful from 'vue-headful';
    import HomePanel from 'HomePanel.vue';
    import IntroButton from 'common/IntroButton.vue';
    import MessagesMixin from 'common/mixin/MessagesMixin.vue';

    @Component({
        name: 'home',
        components: { IntroButton, vueHeadful, HomePanel },
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
            if (this.$route.query.type === '404') {
                this.showErrorMsg('Requested URL does not exist');
            }
            this.getPanel();
            this.getHandlers();
            this.getConfig();
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
                this.$router.push({ name: 'quickStart', params });
            });
        }
    }
</script>

<style lang="less" scoped>
    .container {
        .main-banner {
            background-image: url('/img/bg_main_banner_en.png');
            margin-bottom: 10px;
            height: 160px;
            padding: 0;
            margin-top: 0;
        }

        .quick-start {
            padding-left: 160px;
            padding-top: 35px;

            input {
                height: 30px;

                &.error {
                    border: 2px solid #b94a48;
                }
            }

            & > * {
                margin: 0;
            }
        }

        .intro-button-container {
            margin-top: -40px;
        }
    }
</style>
