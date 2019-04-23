<template>
    <div v-if="dataLoadFinished" class="perftest-detail-container">
        <div class="container">
            <form id="test_config_form" name="test_config_form" action="/perftest/new" ref="configForm">
                <div class="well">
                    <input type="hidden" name="id" :value="test.id">
                    <div class="form-horizontal info">
                        <fieldset>
                            <div class="control-group">
                                <div class="row">
                                    <div class="span4-5" data-step="1" :data-intro="i18n('intro.detail.testName')">
                                        <control-group name="testName" labelMessageKey="perfTest.config.testName">
                                            <input class="required span3 left-float" maxlength="80" size="30" type="text" id="test_name" name="testName" :value="test.testName"/>
                                        </control-group>
                                    </div>
                                    <div class="span3-4 tag-container" data-step="2" :data-intro="i18n('intro.detail.tags')">
                                        <control-group name="tagString" labelMessageKey="perfTest.config.tags">
                                            <select2 v-model="selectedTag" :value="selectedTag" customStyle="width: 175px" type="input" name="tagString"
                                                     :option="{tokenSeparators: [',', ' '], tags:[''], placeholder: i18n('perfTest.config.tagInput'),
                                                      maximumSelectionSize: 5, initSelection: initSelection, query: select2Query}"></select2>
                                        </control-group>
                                    </div>
                                    <div class="span1 status-image-container">
                                        <img id="test_status_img" class="ball"
                                             :src="`/img/ball/${test.iconName}`"
                                             data-toggle="popover"
                                             data-html="true"
                                             :data-content="`${test.progressMessage}<br><b>${test.lastProgressMessage}</b>`.replace(/\n/g, '<br>')"
                                             :title="i18n(test.springMessageKey)"
                                             data-placement="bottom"/>
                                    </div>
                                    <div class="span2-3 start-button-container" data-step="3" :data-intro="i18n('intro.detail.startbutton')">
                                        <div class="control-group">
                                            <input type="hidden" name="isClone" :value="isClone"/>
                                            <button class="btn btn-success" :disabled="disabled" @click.prevent="clonePerftest"
                                                    v-text="isClone ? i18n('perfTest.action.clone') : i18n('common.button.save')"></button>
                                            <button class="btn btn-primary" :disabled="disabled" @click.prevent="saveAndStart" v-text="saveScheduleBtnTitle"></button>
                                        </div>
                                    </div>
                                </div>
                            </div>
                            <div class="control-group description-container">
                                <label for="description" class="control-label" v-text="i18n('common.label.description')"></label>
                                <div class="controls">
                                    <textarea id="description" rows="2" name="description" v-text="test.description"></textarea>
                                </div>
                            </div>
                        </fieldset>
                    </div>
                </div>

                <div class="pull-right">
                    <a :href="`/user/switch?to=${test.createdUserId}`" v-text="switchUserTitle"></a>
                </div>

                <div class="tabbable tab-container">
                    <ul class="nav nav-tabs" id="homeTab">
                        <li>
                            <a v-show="tab.display.config" href="#test-config-section" data-toggle="tab" ref="configTab" v-text="i18n('perfTest.config.testConfiguration')"></a>
                        </li>
                        <li>
                            <a v-show="tab.display.running" href="#running-section" data-toggle="tab" v-text="i18n('perfTest.running.title')"></a>
                        </li>
                        <li>
                            <a v-show="tab.display.report" href="#report-section" data-toggle="tab" ref="reportTab" v-text="i18n('perfTest.report.tab')"></a>
                        </li>
                    </ul>
                    <div class="tab-content">
                        <div class="tab-pane" id="test-config-section">
                            <config ref="config"></config>
                        </div>
                        <div class="tab-pane" id="running-section">
                            <running></running>
                        </div>
                        <div class="tab-pane" id="report-section">
                            <report></report>
                        </div>
                    </div>
                    <input v-if="scheduledTime" type="hidden" name="scheduledTime" :value="scheduledTime">
                    <input type="hidden" name="status" value="SAVED">
                </div>
            </form>
            <intro-button></intro-button>
        </div>
    </div>
</template>
<script>
    import Base from '../../Base.vue';
    import Component from 'vue-class-component';
    import Config from './Config.vue';
    import Report from './Report.vue';
    import Running from './Running.vue';
    import ControlGroup from '../../common/ControlGroup.vue';
    import IntroButton from '../../common/IntroButton.vue';
    import Select2 from '../../common/Select2.vue';

    @Component({
        name: 'perfTestDetail',
        components: { ControlGroup, Config, Report, Running, IntroButton, Select2 },
    })
    export default class PerfTestDetail extends Base {
        test = {};
        selectedTag = '';
        dataLoadFinished = false;
        scheduledTime = 0;

        tab = {
            display: {
                running: false,
                report: false,
                config: false,
            },
        };

        created() {
            this.$http.get(`/perftest/api/${this.$route.params.id}`).then(res => {
                this.test = res.data.test;
                this.selectedTag = this.test.tagString;
                this.updateTabDisplay();
                this.dataLoadFinished = true;
                this.$nextTick(() => {
                    $(this.$refs.configTab).on('shown.bs.tab', () => {
                        this.$EventBus.$emit(this.$Event.UPDATE_RAMPUP_CHART);
                    });

                    if (this.tab.display.report) {
                        this.$refs.reportTab.click();
                        return;
                    }
                    this.$refs.configTab.click();
                });
            }).catch((error) => console.log(error));
        }

        updateTabDisplay() {
            this.tab.display.config = true;
            if (this.test.category === "TESTING") {
                this.tab.display.running = true;
                this.tab.display.report = false;
                return;
            }

            if (this.test.category === "FINISHED" || this.test.category === "STOP" || this.test.category === "ERROR") {
                this.tab.display.report = true;
                this.tab.display.running = false;
            }
        }

        initSelection(element, callback) {
            let data = [];
            this.selectedTag.split(',').forEach((tag) => {
                if (tag) {
                    data.push({id: tag, text: tag});
                }
            });
            element.val("");
            callback(data);
        }

        select2Query(query) {
            let data = {
                results: [],
            };

            this.$http.get('/perftest/api/search_tag', {
                params: {
                    query: query.term,
                },
            }).then(res => {
                res.data.forEach((tag) => data.results.push({id: tag, text: tag}));
                query.callback(data);
            }).catch((error) => console.log(error));
        }

        clonePerftest() {
            this.$refs.configTab.click();
            if (this.$refs.config.hasValidationError()) {
                return;
            }

            this.$http.post('/perftest/api/new', $(this.$refs.configForm).serialize(),
            ).then(res => {
                if (res.data === 'list') {
                    this.$router.push('/perftest');
                } else {
                    this.$router.push(`/perftest/${res.data}`);
                }
            }).catch((error) => console.log(error));
        }

        saveAndStart() {
            // TODO
        }

        get saveScheduleBtnTitle() {
            return `${this.isClone ? this.i18n('perfTest.action.clone') : this.i18n('common.button.save')} ${this.i18n('perfTest.action.andStart')}`;
        }

        get switchUserTitle() {
            return `${this.i18n('perfTest.list.owner')} : ${this.test.createdUserName} (${this.test.createdUserId})`;
        }

        get isClone() {
            return this.test.status !== 'SAVED' || this.test.createdUserId !== this.currentUser.factualUser.id;
        }

        get disabled() {
            return this.test.createdUserId !== this.currentUser.factualUser.id;
        }
    }
</script>

<style lang="less">
    .perftest-detail-container {
        .info {
            .control-label {
                width: 95px;
            }
        }

        .controls {
            margin-left: 120px;
        }

        .tag-container {
            label.control-label {
                width: 60px;
            }

            .controls {
                margin-left: 85px;
            }
        }

        input[type="text"] {
            height: 30px;
        }

        .intro-button-container {
            margin-top: -50px;
        }
    }
</style>

<style lang="less" scoped>
    .perftest-detail-container {
        .well {
            margin-bottom: 5px;
            margin-top: 0;
        }

        .status-image-container {
            text-align: center;
        }

        .start-button-container {
            margin-left: 19px;
        }

        #description {
            resize: none;
            width: 776px;
        }

        #save_schedule_btn {
            width: 116px;
        }

        .tab-container {
            ul {
                &#homeTab {
                    margin-bottom: 5px;
                }
            }
        }

        .description-container {
            margin-bottom: 0;

            div.controls {
                margin-left: 120px;
            }
        }

        .control-label {
            input {
                vertical-align: top;
                margin-left: 2px
            }
        }

        li {
            &.monitor-state {
                height: 10px;
            }
        }

        .controls {
            code {
                vertical-align: middle;
            }

            .span3 {
                margin-left: 0;
            }
        }

        .datepicker {
            z-index:1151;
        }

        div {
            &.chart {
                border: 1px solid #878988;
                margin-bottom: 12px;
            }
        }

        div.modal-body div.chart {
            border:1px solid #878988;
            height:250px;
            min-width:500px;
            margin-bottom:12px;
            padding:5px
        }

        .table {
            thead {
                th {
                    vertical-align: middle;
                }
            }
        }

        .jqplot-yaxis {
            margin-right: 20px;
        }

        .jqplot-xaxis {
            margin-top: 5px;
        }

        .rampup-chart {
            width: 400px;
            height: 300px
        }

        i {
            &.collapse{
                background: url('/img/icon_collapse.png') no-repeat;
                display: inline-block;
                height: 16px;
                width: 16px;
                line-height: 16px;
                vertical-align: text-top;
            }
        }

        #test_name + span {
            float: left;
        }

        .form-horizontal {
            .control-group {
                margin-bottom:10px;
            }
        }

        .control-group.success td > label[for="test_name"] {
            color: #468847;
        }

        .control-group.error td > label[for="test_name"] {
            color: #B94A48;
        }

        #script_control.error {
            .select2-choice {
                border-color: #B94A48;
                color: #B94A48;
            }
        }

        #script_control.success {
            .select2-choice {
                border-color: #468847;
                color: #468847;
            }
        }

        legend {
            padding-top: 10px;
            &.region {
                margin-left:-40px;
            }
        }
    }

</style>
