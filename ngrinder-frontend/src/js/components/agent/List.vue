 <template>
    <div class="container">
        <fieldSet>
            <legend class="header" v-text="i18n('agent.list.title')"></legend>
        </fieldSet>
        <select v-if="ngrinder.config.clustered" class="pull-right form-control change-region"
                v-model="region" @change="changeRegion">
            <option value="">All</option>
            <option v-for="region in regions" :value="region" v-text="region"></option>
        </select>
        <div class="well search-bar">
            <button class="btn btn-success" @click="update">
                <i class="icon-arrow-up"></i><span v-text="i18n('agent.list.update')"></span>
            </button>
            <button class="btn" @click="cleanup">
                <i class="icon-trash"></i><span v-text="i18n('common.button.cleanup')"></span>
            </button>
            <button class="btn" @click="stopAgents">
                <i class="icon-stop"></i><span v-text="i18n('common.button.stop')"></span>
            </button>

            <div class="input-prepend pull-right">
                <span class="add-on" style="cursor:default" v-text="i18n('agent.list.download')"></span>
                <span class="input-xlarge uneditable-input span6" style="cursor: text">
                <template v-if="downloadLink">
                    <a :href="downloadLink" v-html="downloadLink"></a>
                </template>
                <template v-else>
                    Please select the region in advance to download agent.
                </template>
			</span>
            </div>
        </div>
        <table class="table table-striped table-bordered ellipsis" id="agent_table">
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
                <th class="no-click" v-text="i18n('agent.list.port')"></th>
                <th class="ellipsis" v-text="i18n('agent.list.name')"></th>
                <th v-text="i18n('agent.list.version')"></th>
                <th v-text="i18n('agent.list.region')"></th>
                <th class="no-click" v-text="i18n('agent.list.approved')"></th>
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
                    <td class="ellipsis agent-name" :title="agent.hostName" v-text="agent.hostName"></td>
                    <td class="ellipsis" v-text="agent.version || 'Prior to 3.3'"></td>
                    <td class="ellipsis" :title="agent.region" v-text="agent.region"></td>
                    <td>
                        <div class="btn-group" data-toggle="buttons-radio">
                            <button type="button" class="btn btn-mini btn-primary disapproved"
                                    :class="{ active: !agent.approved }"
                                    v-text="i18n('agent.list.disapproved')" @click="disapprove(agent)">
                            </button>
                            <button type="button" class="btn btn-mini btn-primary approved"
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
            this.regions = this.ngrinder.config.visibleRegions;
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
                bootbox.alert(
                    this.i18n('agent.message.common.noagent'),
                    this.i18n('common.button.ok'));
                return;
            }

            const $confirm = bootbox.confirm(
                this.i18n('agent.message.update.confirm'),
                this.i18n('common.button.cancel'),
                this.i18n('common.button.ok'),
                result => {
                    if (result) {
                        this.$http.put('/agent/api?action=update', null, this.getParams())
                            .then(() => this.showSuccessMsg(this.i18n('agent.message.update.success')))
                            .catch(() => this.showErrorMsg(this.i18n('agent.message.update.error')))
                            .finally(() => this.selectedAgents = []);
                    }
                });
            $confirm.children('.modal-body').addClass('error-color');
        }

        cleanup() {
            bootbox.confirm(
                this.i18n('agent.message.cleanup.confirm'),
                this.i18n('common.button.cancel'),
                this.i18n('common.button.ok'),
                result => {
                    if (result) {
                        this.$http.post('/agent/api?action=cleanup', null, this.getParams())
                            .then(() => this.showSuccessMsg(this.i18n('agent.message.cleanup.success')))
                            .catch(() => this.showErrorMsg(this.i18n('agent.message.cleanup.error')))
                            .finally(() => this.selectedAgents = []);
                    }
                });
        }

        stopAgents() {
            if (this.selectedAgents.length === 0) {
                bootbox.alert(
                    this.i18n('agent.message.common.noagent'),
                    this.i18n('common.button.ok'));
                return;
            }

            const $confirm = bootbox.confirm(
                this.i18n('agent.message.stop.confirm'),
                this.i18n('common.button.cancel'),
                this.i18n('common.button.ok'),
                result => {
                    if (result) {
                        this.$http.put('/agent/api?action=stop', null, this.getParams())
                            .then(() => this.showSuccessMsg(this.i18n('agent.message.stop.success')))
                            .catch(() => this.showErrorMsg(this.i18n('agent.message.stop.error')))
                            .finally(() => this.selectedAgents = []);
                    }
                });
            $confirm.children('.modal-body').addClass('error-color');
        }

        approve(agent) {
            this.$http.put(`/agent/api/${agent.id}?action=approve`)
                .then(() => this.i18n('agent.message.approve'));
        }

        disapprove(agent) {
            this.$http.put(`/agent/api/${agent.id}?action=disapprove`)
                .then(() => this.i18n('agent.message.disapprove'));
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
    }

    .change-region {
        margin-top: -53px;
        width: 150px;
    }

    .search-bar .btn i {
        padding-right: 1px;
    }
</style>
