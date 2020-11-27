<template>
    <div class="container">
        <vue-headful :title="i18n('script.list.title')"></vue-headful>
        <div class="script-img-unit" :style="`background-image: url('${contextPath}/img/bg_script_banner.png')`"></div>

        <search-bar :currentPath="currentPath" @deleteFile="deleteFile"></search-bar>

        <vuetable
            ref="vuetable"
            data-path="data"
            pagination-path="pagination"
            track-by="path"
            :api-mode="false"
            :css="table.css.table"
            :fields="tableFields"
            :per-page="table.renderingData.pagination.perPage"
            :data-manager="dataManager"
            @vuetable:pagination-data="beforePagination">

            <template slot="fileIcon" slot-scope="props">
                <i v-if="props.rowData.editable" class="fa fa-file"></i>
                <i v-else-if="props.rowData.fileType === 'DIR'" class="fa fa-folder-open"></i>
                <i v-else class="fa fa-briefcase"></i>
            </template>

            <template slot="path" slot-scope="props">
                <div class="ellipsis path">
                    <router-link v-if="props.rowData.editable"
                                 :to="`/script/detail/${props.rowData.path}`"
                                 :title="props.rowData.path" target="_self"
                                 v-text="props.rowData.fileName">
                    </router-link>
                    <router-link v-else-if="props.rowData.fileType === 'DIR'"
                                 :to="`/script/list/${props.rowData.path}`"
                                 :title="props.rowData.path" target="_self"
                                 v-text="props.rowData.fileName">
                    </router-link>
                    <a v-else :href="`${contextPath}/script/api/download/${props.rowData.path}`"
                       :title="props.rowData.path" target="_blank"
                       v-text="props.rowData.fileName">
                    </a>
                </div>
            </template>

            <template slot="description" slot-scope="props">
                <div class="ellipsis description">
                    <span v-text="props.rowData.description" :title="props.rowData.description"></span>
                </div>
            </template>

            <template slot="lastModifiedAt" slot-scope="props">
                <span v-if="!!props.rowData.lastModifiedAt">{{ props.rowData.lastModifiedAt | dateFormat('YYYY-MM-DD HH:mm') }}</span>
            </template>

            <template slot="fileSize" slot-scope="props">
                <span v-text="props.rowData.fileType !== 'DIR' ? getFileSize(props.rowData.fileSize) : ''"></span>
            </template>

            <template slot="download" slot-scope="props">
                <i v-if="props.rowData.fileType !== 'DIR'" class="pointer-cursor fa fa-download"
                   @click="downloadScript(props.rowData.path)"></i>
            </template>

        </vuetable>
        <vuetable-pagination
            ref="pagination"
            :css="table.css.pagination"
            :on-each-side=5
            @vuetable-pagination:change-page="changePage">
        </vuetable-pagination>
    </div>
</template>

<script>
    import { Mixins } from 'vue-mixin-decorator';
    import { Component, Prop } from 'vue-property-decorator';
    import VueHeadful from 'vue-headful';
    import _ from 'lodash';
    import Vuetable from 'vuetable-2';
    import VuetablePagination from 'vuetable-2/src/components/VuetablePagination.vue';

    import TableConfig from './mixin/TableConfig.vue';
    import MessagesMixin from '../common/mixin/MessagesMixin.vue';
    import Base from '../Base.vue';
    import SearchBar from './SearchBar.vue';

    Component.registerHooks(['beforeRouteEnter', 'beforeRouteUpdate']);
    @Component({
        name: 'scriptList',
        components: { VueHeadful, SearchBar, Vuetable, VuetablePagination },
    })
    export default class ScriptList extends Mixins(Base, MessagesMixin, TableConfig) {
        @Prop({ type: Array, required: true })
        scripts;

        @Prop({ type: String, required: false, default: '' })
        remainedPath;

        table = {
            css: {},
            renderingData: {
                data: [],
                pagination: {
                    perPage: 15,
                },
            },
        };

        beforeRouteEnter(to, from, next) {
            ScriptList.prepare(to).then(next);
        }

        beforeRouteUpdate(to, from, next) {
            ScriptList.prepare(to).then(next);
        }

        created() {
            this.table.css = this.tableCss;
        }

        mounted() {
            this.init();
            this.$EventBus.$on(this.$Event.REFRESH_SCRIPT_LIST, this.refresh);

            this.$nextTick(() => {
                document.getElementById('file-icon-back').onclick = () => this.$router.push(`${this.baseDirectory}`);
            });
        }

        static prepare(route) {
            let promise;
            if (route.name === 'scriptSearch') {
                const query = route.query.query || -1;
                promise = Base.prototype.$http.get(`/script/api/search?query=${query}`);
            } else {
                promise = Base.prototype.$http.get(`/script/api/${route.params.remainedPath || ''}`);
            }

            return promise.then(res => route.params.scripts = res.data);
        }

        dataManager(sortOrder) {
            let data = this.scripts;
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
        }

        beforePagination(paginationData) {
            this.$refs.pagination.setPaginationData(paginationData);
        }

        refresh() {
            if (this.$route.name === 'scriptSearch') {
                this.$http.get(`/script/api/search?query=${this.$route.query.query}`)
                    .then(res => this.scripts.splice(0, this.scripts.length, ...res.data))
                    .then(this.init);
            } else {
                this.$http.get(`/script/api/${this.currentPath}`)
                    .then(res => this.scripts.splice(0, this.scripts.length, ...res.data))
                    .then(this.init);
            }
        }

        deleteFile() {
            if (this.$refs.vuetable.selectedTo.length === 0) {
                this.$bootbox.alert({
                    message: this.i18n('script.message.delete.alert'),
                    buttons: {
                        ok: { label: this.i18n('common.button.ok') },
                    },
                });
                return;
            }

            this.$bootbox.confirm({
                message: this.i18n('script.message.delete.confirm'),
                buttons: {
                    confirm: { label: this.i18n('common.button.ok') },
                    cancel: { label: this.i18n('common.button.cancel') },
                },
                onConfirm: () => this.$http.delete('/script/api/delete', { data: this.$refs.vuetable.selectedTo })
                    .then(() => this.refresh()),
            });
        }

        init(data) {
            this.table.renderingData.data = _.slice(this.scripts, 0, this.table.renderingData.pagination.perPage);
            this.table.renderingData.pagination.total = this.scripts.length;
            this.table.renderingData.pagination.last_page =
                Math.ceil(this.table.renderingData.pagination.total / this.table.renderingData.pagination.perPage);
            this.$refs.vuetable.setData(this.table.renderingData);
            this.$refs.vuetable.selectedTo = [];
            this.$refs.vuetable.reload();
        }

        get currentPath() {
            if (this.$route.name === 'scriptSearch') {
                return '';
            }
            return this.remainedPath;
        }

        get baseDirectory() {
            if (this.currentPath === '' || this.currentPath.lastIndexOf('/') < 0) {
                return '/script/list';
            }
            return `/script/list/${this.currentPath.slice(0, this.currentPath.lastIndexOf('/'))}`;
        }

        downloadScript(path) {
            const link = document.createElement('a');
            link.style.display = 'none';
            link.href = `${this.contextPath}/script/api/download/${path}`;
            link.setAttribute('download', path);

            document.body.appendChild(link);
            link.click();
            document.body.removeChild(link);
        }

        getFileSize(size) {
            return this.formatNumber((size / 1024), 2);
        }

        formatNumber(number, decimal) {
            if (number === 0) {
                return 0;
            }
            const n = (`${number}`);
            const parts = n.toString().split('.');
            return parts[0].replace(/\B(?=(\d{3})+(?!\d))/g, ',') + (parts[1] ? `.${(parts[1].length > decimal ? parts[1].substring(0, decimal) : parts[1])}` : '');
        }
    }
</script>

<style lang="less" scoped>
    .script-img-unit {
        height: 110px;
        padding: 0;
        margin-top: 0;
    }

    .table {
        margin-bottom: 5px;
        font-size: 12px;

        th, td {
            padding: 8px;

            .path {
                width: 230px;
            }

            .description {
                width: 390px;
            }
        }
    }
</style>
