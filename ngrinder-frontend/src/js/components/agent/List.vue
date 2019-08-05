 <template>
    <div class="container">
        <fieldSet>
            <legend class="header border-bottom d-flex">
                <span v-text="i18n('agent.list.title')"></span>
                <select v-if="ngrinder.config.clustered" class="form-control change-region ml-auto mt-auto mb-auto"
                        v-model="region" @change="changeRegion">
                    <option value="">All</option>
                    <option v-for="region in regions" :value="region" v-text="region"></option>
                </select>
            </legend>
        </fieldSet>
        <div class="card card-header search-bar border-bottom-0">
            <button class="btn btn-primary mr-1" @click="update">
                <i class="mr-1 fa fa-arrow-up"></i>
                <span v-text="i18n('agent.list.update')"></span>
            </button>
            <button class="btn btn-primary mr-1" @click="cleanup">
                <i class="mr-1 fa fa-trash"></i>
                <span v-text="i18n('common.button.cleanup')"></span>
            </button>
            <button class="btn btn-danger" @click="stopAgents">
                <i class="mr-1 fa fa-stop"></i>
                <span v-text="i18n('common.button.stop')"></span>
            </button>

            <div class="input-prepend ml-auto mt-auto mb-auto">
                <div class="input-group-text" v-text="i18n('agent.list.download')"></div>
                <div class="border rounded uneditable-input">
                    <template v-if="downloadLink">
                        <a :href="downloadLink" v-html="downloadLink"></a>
                    </template>
                    <template v-else>
                        Please select the region in advance to download agent.
                    </template>
                </div>
            </div>
        </div>
        <table class="table table-striped table-bordered">
            <colgroup>
                <col width="30">
                <col width="80">
                <col width="130">
                <col width="60">
                <col width="*">
                <col width="100">
                <col width="150">
                <col width="160">
            </colgroup>
            <thead>
            <tr>
                <th class="no-click nothing">
                    <input type="checkbox" class="checkbox" v-model="selectAll" @change="changeSelectAll">
                </th>
                <th v-text="i18n('agent.list.state')"></th>
                <th v-text="i18n('agent.list.IPAndDns')"></th>
                <th v-text="i18n('agent.list.port')"></th>
                <th class="ellipsis" v-text="i18n('agent.list.name')"></th>
                <th v-text="i18n('agent.list.version')"></th>
                <th v-text="i18n('agent.list.region')"></th>
                <th v-text="i18n('agent.list.approved')"></th>
            </tr>
            </thead>
            <tbody>
            <template v-show="agents.length > 0">
                <tr v-for="agent in agents">
                    <td class="center">
                        <input type="checkbox" class="agent-state checkbox" v-model="selectedAgents" :value="agent.id">
                    </td>
                    <td class="center">
                        <div class="ball" data-html="true" rel="popover">
                            <img class="status" :src="`/img/ball/${agent.state.iconName}`"/>
                        </div>
                    </td>
                    <td>
                        <div class="ellipsis" :title="agent.ip">
                            <router-link :to="{ name: 'agentDetail', params: { agentId: `${agent.id}` , agentProp: agent } }"
                                         :value="agent.ip" v-text="agent.ip">
                            </router-link>
                        </div>
                    </td>
                    <td v-text="agent.port"></td>
                    <td class="ellipsis" :title="agent.hostName" v-text="agent.hostName"></td>
                    <td class="ellipsis" v-text="agent.version || 'Prior to 3.3'"></td>
                    <td class="ellipsis" :title="agent.region" v-text="agent.region"></td>
                    <td>
                        <div class="btn-group">
                            <button class="btn btn-primary disapproved"
                                    :class="{ active: !agent.approved }"
                                    v-text="i18n('agent.list.disapproved')" @click="disapprove(agent)">
                            </button>
                            <button class="btn btn-primary approved"
                                    :class="{ active: agent.approved }"
                                    v-text="i18n('agent.list.approved')" @click="approve(agent)">
                            </button>
                        </div>
                    </td>
                </tr>
            </template>
            <tr v-show="agents.length === 0">
                <td colspan="8" class="center" v-text="i18n('common.message.noData')"></td>
            </tr>
            </tbody>
        </table>
        <!-- TODO: Paginate using datatables -->
    </div>
</template>

<script>
    import Component from 'vue-class-component';
    import { Mixins } from 'vue-mixin-decorator';
    import Base from '../Base.vue';
    import MessagesMixin from '../common/mixin/MessagesMixin.vue';

    @Component({
        name: 'agentList',
    })
    export default class AgentList extends Mixins(Base, MessagesMixin) {
        regions = [];
        region = '';

        agents = [];

        downloadLink = '';

        page = {
            number: 1,
            totalPages: 1,
            size: 10,
        };

        selectAll = false;
        selectedAgents = [];

        updateStatesTimer = null;

        created() {
            this.$http.get('/agent/api/regions').then(res => this.regions = res.data);
            this.region = this.$route.query.region || this.region;

            this.updateDownloadLink();
            this.updateStates();
        }

        beforeDestroy() {
            clearTimeout(this.updateStatesTimer);
        }

        updateDownloadLink() {
            this.$http.get('/agent/api/download_link', { params: { region: this.region } }).then(res => this.downloadLink = res.data);
        }

        updateStates() {
            this.$http.get('/agent/api/list', { params: { region: this.region } })
                .then(res => this.agents = res.data)
                .then(() => history.replaceState('', '', this.region ? `/agent?region=${this.region}` : '/agent'))
                .finally(() => this.updateStatesTimer = setTimeout(this.updateStates, 2000));
        }

        changeRegion(event) {
            this.region = event.target.value;

            clearTimeout(this.updateStatesTimer);
            this.updateStates();
            this.updateDownloadLink();
        }

        update() {
            if (this.selectedAgents.length === 0) {
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
                callback: result => {
                    if (result) {
                        this.$http.put('/agent/api?action=update', null, this.getParams())
                            .then(() => this.showSuccessMsg(this.i18n('agent.message.update.success')))
                            .catch(() => this.showErrorMsg(this.i18n('agent.message.update.error')))
                            .finally(() => this.selectedAgents = []);
                    }
                },
            });
            $confirm.children('.modal-body').addClass('error-color');
        }

        cleanup() {
            this.$bootbox.confirm({
                message: this.i18n('agent.message.cleanup.confirm'),
                buttons: {
                    confirm: { label: this.i18n('common.button.ok') },
                    cancel: { label: this.i18n('common.button.cancel') },
                },
                callback: result => {
                    if (result) {
                        this.$http.post('/agent/api?action=cleanup', null, this.getParams())
                            .then(() => this.showSuccessMsg(this.i18n('agent.message.cleanup.success')))
                            .catch(() => this.showErrorMsg(this.i18n('agent.message.cleanup.error')))
                            .finally(() => this.selectedAgents = []);
                    }
                },
            });
        }

        stopAgents() {
            if (this.selectedAgents.length === 0) {
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
                callback: result => {
                    if (result) {
                        this.$http.put('/agent/api?action=stop', null, this.getParams())
                            .then(() => this.showSuccessMsg(this.i18n('agent.message.stop.success')))
                            .catch(() => this.showErrorMsg(this.i18n('agent.message.stop.error')))
                            .finally(() => this.selectedAgents = []);
                    }
                },
            });
            $confirm.children('.modal-body').addClass('error-color');
        }

        approve(agent) {
            if (agent.approved) {
                return;
            }

            this.$http.put(`/agent/api/${agent.id}?action=approve`)
                .then(() => agent.approved = true);
        }

        disapprove(agent) {
            if (!agent.approved) {
                return;
            }

            this.$http.put(`/agent/api/${agent.id}?action=disapprove`)
                .then(() => agent.approved = false);
        }

        changeSelectAll(event) {
            if (event.target.checked) {
                this.selectedAgents = this.agents.map(agent => agent.id);
            } else {
                this.selectedAgents = [];
            }
        }

        getParams() {
            return { params: { ids: this.selectedAgents.join(',') } };
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
        width: 530px;

        a {
            margin-left: 7px;
        }
    }

    .search-bar {
        flex-direction: row;
    }
</style>
