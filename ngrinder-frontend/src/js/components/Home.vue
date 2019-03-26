<template>
    <div>
        <div class="container">
            <vue-headful title="Home"/>
            <div class="hero-unit main-banner" data-step="1" :data-intro="i18n('intro.index.quick.start')">
                <form name="quick_start" id="quick_start" action="/perftest/quickstart" method="post">
                    <div class="quick-start" :data-original-title="i18n('home.tip.url.title')" :data-content="i18n('home.tip.url.content')" data-placement="bottom" rel="popover">
                        <input v-focus type="text" name="url" id="url" class="span6" v-validate="{url: {require_protocol: true}, required: true}" ref="inputQuickStartUrl"
                               :class="{error: errors.any()}" :placeholder="i18n('home.placeholder.url')" data-step="2" :data-intro="i18n('intro.index.test.url')" v-model="quickStartUrl"/>
                        <select class="select-item span2" id="script_type" v-model="scriptType" name="scriptType" data-step="3" :data-intro="i18n('intro.index.select.language')">
                            <option v-for="handler in handlers" :value="handler.key" v-text="handler.title"></option>
                        </select>
                        <button id="start_test_btn" class="btn btn-primary" data-step="4"
                                :data-intro="i18n('intro.index.create')" @click.prevent="quickStart" v-on:change="" v-text="i18n('home.button.startTest')">
                        </button>
                    </div>
                </form>
            </div>
            <div class="row">
                <home-panel :title="i18n('home.qa.title')" :entries="leftPanelEntries" :introJsDataSetp="5" :introJsDataIntro="i18n('intro.index.qna')"
                            :seeMoreQuestionUrl="seeMoreQuestionUrl" :askQuestionUrl="askQuestionUrl"></home-panel>
                <home-panel :title="i18n('home.resources.title')" :entries="rightPanelEntries" :introJsDataSetp="6" :introJsDataIntro="i18n('intro.index.resource')"
                            :seeMoreQuestionUrl="seeMoreQuestionUrl"></home-panel>
            </div>
            <intro-button></intro-button>
        </div>
    </div>
</template>

<script>
    import Component from 'vue-class-component';
    import Base from 'Base.vue';
    import vueHeadful from 'vue-headful';
    import HomePanel from 'HomePanel.vue';
    import IntroButton from 'common/IntroButton.vue';

    @Component({
        name: 'home',
        components: { IntroButton, vueHeadful, HomePanel },
    })
    export default class Index extends Base {
        leftPanelEntries = [];
        rightPanelEntries = [];
        handlers = [];
        askQuestionUrl = '';
        seeMoreQuestionUrl = '';
        seeMoreResourcesUrl = '';

        quickStartUrl = '';
        scriptType = '';

        created() {
            this.getHandlers();
            this.getPanel();
            this.getConfig();
        }

        getHandlers() {
            this.$http.get('home/api/handlers').then(res => {
                this.handlers = res.data;
                this.scriptType = this.handlers[0].key;
            }).catch(error => console.error(error));
        }

        getPanel() {
            this.$http.get('home/api/panel').then(res => {
                this.leftPanelEntries = res.data.leftPanelEntries;
                this.rightPanelEntries = res.data.rightPanelEntries;
            }).catch(error => console.error(error));
        }

        getConfig() {
            this.$http.get('home/api/config').then(res => {
                this.askQuestionUrl = res.data.askQuestionUrl;
                this.seeMoreQuestionUrl = res.data.seeMoreQuestionUrl;
                this.seeMoreResourcesUrl = res.data.seeMoreResourcesUrl;
            }).catch(error => console.error(error));
        }

        quickStart() {
            this.$validator.validateAll().then(result => {
                if (!result) {
                    this.$nextTick(() => this.$refs.inputQuickStartUrl.focus());
                    return;
                }

                this.$http.post('/perftest/quickstart', {
                    url: this.quickStartUrl,
                    scriptType: this.scriptType,
                }).then(res => {
                    // DOTO go to perftest detail.
                }).catch(error => console.error(error));
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
