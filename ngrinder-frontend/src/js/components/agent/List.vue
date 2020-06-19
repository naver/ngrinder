<template>
    <div class="container">
        <vue-headful :title="i18n('agent.title')"></vue-headful>
        <fieldSet>
            <legend class="header border-bottom d-flex">
                <span v-text="i18n('agent.list.title')"></span>
                <select v-if="ngrinder.config.clustered" class="form-control change-region ml-auto mt-auto mb-auto"
                        v-model="region" @change="changeRegion">
                    <option value="">All</option>
                    <option v-for="region in regions" :value="region" v-text="i18n(region)"></option>
                </select>
            </legend>
        </fieldSet>
        <div class="card card-header search-bar border-bottom-0">
            <div v-if="isAdmin">
                <button class="btn btn-primary mr-1" @click="update">
                    <i class="mr-1 fa fa-arrow-up"></i>
                    <span v-text="i18n('agent.list.update')"></span>
                </button>
                <button class="btn btn-danger mr-1" @click="stopAgents">
                    <i class="mr-1 fa fa-stop"></i>
                    <span v-text="i18n('common.button.stop')"></span>
                </button>
                <button class="btn btn-info mr-3" @click="$refs.addConnectionAgentModal.show()">
                    <i class="mr-1 fa fa-plus"></i>
                    <span v-text="i18n('common.button.add')"></span>
                </button>
            </div>

            <input class="mr-1 form-control search-input" type="text" ref="searchInput"
                   placeholder="Keywords" @keydown.enter.prevent="search" v-focus/>
            <button class="btn btn-info" @click="search">
                <i class="fa fa-search mr-1"></i>
                <span v-text="i18n('common.button.search')"></span>
            </button>

            <div class="input-prepend ml-auto mt-auto mb-auto">
                <div class="input-group-text" v-text="i18n('agent.list.download')"></div>
                <div class="border rounded uneditable-input">
                    <template v-if="downloadLink">
                        <a :href="downloadLink" v-html="downloadLink"></a>
                    </template>
                    <template v-else>
                        <span class="ml-2 text-muted">Please select the region in advance to download agent.</span>
                    </template>
                </div>
            </div>
        </div>
        <vuetable
            ref="vuetable"
            data-path="data"
            pagination-path="pagination"
            track-by="key"
            :api-mode="false"
            :css="table.css.table"
            :fields="tableFields"
            :per-page="table.renderingData.pagination.perPage"
            :data-manager="dataManager"
            @vuetable:pagination-data="beforePagination">

            <template slot="state" slot-scope="props">
                <div class="ball"
                     data-toggle="popover"
                     data-html="true"
                     data-trigger="hover"
                     :id="`ball_${props.rowData.key}`"
                     :data-content="getAgentStatePopoverContent(props.rowData)">
                    <img class="status" :src="`${contextPath}/img/ball/${props.rowData.state.iconName}`"/>
                </div>
            </template>

            <template slot="domain" slot-scope="props">
                <router-link :to="{ name: 'agentDetail', params: { ip: props.rowData.ip, name: props.rowData.name, agent: props.rowData } }"
                             :value="props.rowData.ip" v-text="props.rowData.ip">
                </router-link>
            </template>

            <template slot="name" slot-scope="props">
                <div class="ellipsis name" :title="props.rowData.name" v-text="props.rowData.name"></div>
            </template>

            <template slot="version" slot-scope="props">
                <div class="ellipsis version" :title="props.rowData.version" v-text="props.rowData.version"></div>
            </template>

            <template slot="region" slot-scope="props">
                <div class="ellipsis region" :title="props.rowData.region" v-text="props.rowData.region"></div>
            </template>

            <template slot="approved" slot-scope="props">
                <div class="btn-group">
                    <button class="btn btn-primary disapproved"
                            :class="{ active: !props.rowData.approved }"
                            v-text="i18n('agent.list.disapproved')" @click="disapprove(props.rowData)">
                    </button>
                    <button class="btn btn-primary approved"
                            :class="{ active: props.rowData.approved }"
                            v-text="i18n('agent.list.approved')" @click="approve(props.rowData)">
                    </button>
                </div>
            </template>
        </vuetable>
        <vuetable-pagination
            ref="pagination"
            :css="table.css.pagination"
            :on-each-side=5
            @vuetable-pagination:change-page="changePage">
        </vuetable-pagination>
        <add-connection-agent-modal ref="addConnectionAgentModal" :regions="regions" v-if="isAdmin"/>
    </div>
</template>

<script>
    import Component from 'vue-class-component';
    import { Mixins } from 'vue-mixin-decorator';
    import _ from 'lodash';
    import Vuetable from 'vuetable-2';
    import VuetablePagination from 'vuetable-2/src/components/VuetablePagination.vue';
    import VueHeadful from 'vue-headful';

    import Base from '../Base.vue';
    import TableConfig from './mixin/TableConfig.vue';
    import MessagesMixin from '../common/mixin/MessagesMixin.vue';
    import AddConnectionAgentModal from './modal/AddConnectionAgentModal.vue';

    const AGENT_KEY_TOKEN = '_';

    @Component({
        name: 'agentList',
        components: { Vuetable, VuetablePagination, VueHeadful, AddConnectionAgentModal },
    })
    export default class AgentList extends Mixins(Base, MessagesMixin, TableConfig) {
        agents = [];
        regions = [];
        region = '';

        downloadLink = '';
        updateStatesTimer = null;

        query = '';
        queryFilter = () => true;

        table = {
            css: {},
            renderingData: {
                data: [],
                pagination: {
                    perPage: 15,
                },
            },
        };

        created() {
            this.table.css = this.tableCss;
        }

        mounted() {
            this.$http.get('/agent/api/regions').then(res => this.regions = res.data);

            this.region = this.$route.query.region || this.region;
            this.updateDownloadLink();

            this.query = this.$route.query.query || '';
            this.queryFilter = this.createQueryFilter(this.query);
            this.$refs.searchInput.value = this.query; // Prevent to update query by periodic status update

            this.initAgents();
        }

        beforeDestroy() {
            clearTimeout(this.updateStatesTimer);
        }

        dataManager(sortOrder) {
            let data = this.agents;
            const pagination = this.$refs.vuetable.makePagination(data.length);

            if (sortOrder.length > 0) {
                data = _.orderBy(data, [sortOrder[0].sortField], [sortOrder[0].direction]);
            }

            return {
                pagination,
                data: _.slice(data, pagination.from - 1, pagination.to),
            };
        }

        changePage(nextPage) {
            this.$refs.vuetable.changePage(nextPage);
            this.$nextTick(() => {
                $('[data-toggle="popover"]').popover('dispose');
                $('[data-toggle="popover"]').popover();
            });
        }

        beforePagination(paginationData) {
            this.$refs.pagination.setPaginationData(paginationData);
        }

        updateDownloadLink() {
            if (this.ngrinder.config.clustered && !this.region) {
                this.downloadLink = '';
                return;
            }
            this.$http.get('/agent/api/download_link', { params: { region: this.region } }).then(res => {
                this.downloadLink = `${this.contextPath}${res.data}`;
            });
        }

        initAgents() {
            this.getAgents()
                .then(() => {
                    this.table.renderingData.data = _.slice(this.agents, 0, this.table.renderingData.pagination.perPage);
                    this.table.renderingData.pagination.total = this.agents.length;
                    this.table.renderingData.pagination.last_page =
                        Math.ceil(this.table.renderingData.pagination.total / this.table.renderingData.pagination.perPage);
                    this.$refs.vuetable.setData(this.table.renderingData);
                })
                .then(() => this.$refs.vuetable.reload())
                .then(() => history.replaceState('', '', this.makeQueryString()))
                .finally(() => {
                    this.updateStatesTimer = setTimeout(this.updateStates, 2000);
                    this.$nextTick(() => $('[data-toggle="popover"]').popover());
                });
        }

        updateStates() {
            this.getAgents()
                .then(() => this.$refs.vuetable.reload())
                .then(() => history.replaceState('', '', this.makeQueryString()))
                .finally(() => {
                    this.updateAgentStatePopover();
                    this.updateStatesTimer = setTimeout(this.updateStates, 2000);
                });
        }

        updateAgentStatePopover() {
            this.$nextTick(() => this.agents.forEach(agent => {
                const ball = document.getElementById(`ball_${agent.key}`);
                if (ball != null) {
                    ball.setAttribute('data-content', this.getAgentStatePopoverContent(agent));
                }
            }));
        }

        getAgentStatePopoverContent(agent) {
            return `<b>${agent.state.name}</b>`;
        }

        getAgents() {
            return this.$http.get('/agent/api/list', { params: { region: this.region } })
                .then(res => this.agents = this.appendAgentKey(res.data).filter(this.queryFilter));
        }

        appendAgentKey(agents) {
            return agents.map(agent => {
                agent.key = `${agent.ip}${AGENT_KEY_TOKEN}${agent.name}`;
                return agent;
            });
        }

        changeRegion() {
            this.$refs.vuetable.currentPage = 1;
            clearTimeout(this.updateStatesTimer);
            this.updateStates();
            this.updateDownloadLink();
        }

        search() {
            this.query = this.$refs.searchInput.value;
            this.queryFilter = this.createQueryFilter(this.query);
            clearTimeout(this.updateStatesTimer);
            this.updateStates();
        }

        createQueryFilter(query) {
            const contains = (string, keyword) => string.indexOf(keyword) > -1;
            return agent => !query || contains(agent.name, query) || contains(agent.ip, query);
        }

        makeQueryString() {
            const url = `${this.contextPath}/agent`;
            const params = [];

            if (this.region) {
                params.push(`region=${this.region}`);
            }

            if (this.query) {
                params.push(`query=${this.query}`);
            }

            return params ? `${url}?${params.join('&')}` : url;
        }

        update() {
            if (this.$refs.vuetable.selectedTo.length === 0) {
                this.$bootbox.alert({
                    message: this.i18n('agent.message.common.noagent'),
                    buttons: {
                        ok: { label: this.i18n('common.button.ok') },
                    },
                });
                return;
            }

            const $confirm = this.$bootbox.confirm({
                message: this.i18n('agent.message.update.confirm'),
                buttons: {
                    confirm: { label: this.i18n('common.button.ok') },
                    cancel: { label: this.i18n('common.button.cancel') },
                },
                onConfirm: () => this.$http.put('/agent/api?action=update', this.getData())
                    .then(() => this.showSuccessMsg(this.i18n('agent.message.update.success')))
                    .then(() => this.$refs.vuetable.selectedTo = [])
                    .catch(() => this.showErrorMsg(this.i18n('agent.message.update.error'))),
            });
            $confirm.children('.modal-body').addClass('error-color');
        }

        stopAgents() {
            if (this.$refs.vuetable.selectedTo.length === 0) {
                this.$bootbox.alert({
                    message: this.i18n('agent.message.common.noagent'),
                    buttons: {
                        ok: { label: this.i18n('common.button.ok') },
                    },
                });
                return;
            }

            const $confirm = this.$bootbox.confirm({
                message: this.i18n('agent.message.stop.confirm'),
                buttons: {
                    confirm: { label: this.i18n('common.button.ok') },
                    cancel: { label: this.i18n('common.button.cancel') },
                },
                onConfirm: () => this.$http.put('/agent/api?action=stop', this.getData())
                    .then(() => this.showSuccessMsg(this.i18n('agent.message.stop.success')))
                    .then(() => this.$refs.vuetable.selectedTo = [])
                    .catch(() => this.showErrorMsg(this.i18n('agent.message.stop.error'))),
            });
            $confirm.children('.modal-body').addClass('error-color');
        }

        approve(agent) {
            if (agent.approved) {
                return;
            }

            this.$http.put(`/agent/api/${agent.ip}/${agent.name}?action=approve`)
                .then(() => {
                    this.showSuccessMsg(this.i18n('agent.message.approve'));
                    agent.approved = true;
                    this.$refs.vuetable.reload();
                });
        }

        disapprove(agent) {
            if (!agent.approved) {
                return;
            }

            this.$http.put(`/agent/api/${agent.ip}/${agent.name}?action=disapprove`)
                .then(() => {
                    this.showSuccessMsg(this.i18n('agent.message.disapprove'));
                    agent.approved = false;
                    this.$refs.vuetable.reload();
                });
        }

        getData() {
            return this.$refs.vuetable.selectedTo.map(keyToken => {
                const ipAndName = keyToken.split(AGENT_KEY_TOKEN);
                return { ip: ipAndName[0], name: ipAndName[1] };
            });
        }
    }
</script>

<style lang="less" scoped>
    table {
        font-size: 12px;

        td {
            height: 40px;
            padding: 8px;
        }

        input[type='checkbox'] {
            vertical-align: bottom;
        }
    }

    .change-region {
        width: 150px;
    }

    img.status {
        width: 23px;
        height: 23px;
    }

    .btn-group {
        button {
            outline: none;
            font-size: 10px;
            padding: 2px 4px;
            height: 22px;
        }
    }

    .input-group-text {
        float: left;
        cursor: default;
        padding: 6px 10px;
    }

    .uneditable-input {
        width: 580px;

        a {
            margin-left: 7px;
        }
    }

    .search-bar {
        flex-direction: row;

        .search-input {
            width: 280px;
            height: inherit;
        }
    }
</style>
