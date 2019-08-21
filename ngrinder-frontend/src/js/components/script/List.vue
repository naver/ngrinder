<template>
    <div class="container">
        <vue-headful title="Script"></vue-headful>
        <div class="script-img-unit"></div>

        <search-bar :scripts="scripts" :currentPath="currentPath"></search-bar>

        <vuetable
            ref="vuetable"
            data-path="data"
            pagination-path="pagination"
            :api-mode="false"
            :css="table.css.table"
            :fields="tableFields"
            :per-page="table.renderingData.pagination.per_page"
            :data-manager="dataManager"
            @vuetable:pagination-data="beforePagination">

            <template slot="fileIcon" slot-scope="props">
                <i v-if="isEditable(props.rowData.fileType , props.rowData.path)" class="fa fa-file"></i>
                <i v-else-if="props.rowData.fileType === 'DIR'" class="fa fa-folder-open"></i>
                <i v-else class="fa fa-briefcase"></i>
            </template>

            <template slot="path" slot-scope="props">
                <router-link v-if="isEditable(props.rowData.fileType, props.rowData.path)"
                             :to="`/script/detail/${props.rowData.path}`"
                             :title="props.rowData.path" target="_self"
                             v-text="getFileName(props.rowData.path)">
                </router-link>
                <router-link v-else-if="props.rowData.fileType === 'DIR'"
                             :to="`/script/list/${props.rowData.path}`"
                             :title="props.rowData.path" target="_self"
                             v-text="getFileName(props.rowData.path)">
                </router-link>
                <a v-else :href="`/script/download/${props.rowData.path}`"
                   :title="props.rowData.path" target="_blank"
                   v-text="getFileName(props.rowData.path)">
                </a>
            </template>

            <template slot="lastModifiedDate" slot-scope="props">
                <span v-if="!!props.rowData.lastModifiedDate">{{ props.rowData.lastModifiedDate | dateFormat('YYYY-MM-DD HH:mm') }}</span>
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
            @vuetable-pagination:change-page="changePage">
        </vuetable-pagination>
    </div>
</template>

<script>
    import { Mixins } from 'vue-mixin-decorator';
    import { Component, Watch } from 'vue-property-decorator';
    import VueHeadful from 'vue-headful';
    import _ from 'lodash';
    import Vuetable from 'vuetable-2';
    import VuetablePagination from 'vuetable-2/src/components/VuetablePagination.vue';
    import TableConfig from './mixin/TableConfig.vue';
    import FileType from '../../common/file-type';
    import MessagesMixin from '../common/mixin/MessagesMixin.vue';
    import Base from '../Base.vue';
    import SearchBar from './SearchBar.vue';

    const removePrependedSlash = path => (path.endsWith('/') ? path.slice(0, path.length - 1) : path);

    @Component({
        name: 'scriptList',
        components: { VueHeadful, SearchBar, Vuetable, VuetablePagination },
    })
    export default class ScriptList extends Mixins(Base, MessagesMixin, TableConfig) {
        scripts = [];
        table = {
            css: {},
            renderingData: {
                data: [],
                pagination: {
                    per_page: 10,
                },
            },
        };

        created() {
            this.table.css = this.tableCss;
        }

        mounted() {
            this.showProgressBar();
            this.refreshScriptList(this.hideProgressBar);
            this.$EventBus.$on(this.$Event.REFRESH_SCRIPT_LIST, this.refreshScriptList);
        }

        dataManager(sortOrder, pagination) {
            let data = this.scripts;

            pagination = this.$refs.vuetable.makePagination(data.length);

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

        refreshScriptList(callback) {
            if (this.$route.name === 'scriptSearch') {
                this.$http.get(`/script/api/search?query=${this.$route.query.query}`)
                    .then(res => this.initTableData(res.data))
                    .finally(() => {
                        if (typeof callback === 'function') {
                            callback();
                        }
                    });
            } else {
                this.$http.get(`/script/api/${this.currentPath}`)
                    .then(res => this.initTableData(res.data))
                    .finally(() => {
                        if (typeof callback === 'function') {
                            callback();
                        }
                    });
            }
        }

        initTableData(data) {
            this.scripts = data;
            this.table.renderingData.data = _.slice(this.scripts, 0, this.table.renderingData.pagination.per_page);
            this.table.renderingData.pagination.total = this.scripts.length;
            this.table.renderingData.pagination.last_page =
                Math.ceil(this.table.renderingData.pagination.total / this.table.renderingData.pagination.per_page);
            this.$refs.vuetable.setData(this.table.renderingData);
        }

        get currentPath() {
            if (this.$route.name === 'scriptSearch') {
                return '';
            }
            return removePrependedSlash(this.$route.path).replace('/script', '').replace('/list', '').replace('/', '');
        }

        get baseDirectory() {
            if (this.currentPath === '' || this.currentPath.lastIndexOf('/') < 0) {
                return '/script/list';
            }
            return `/script/list/${this.currentPath.slice(0, this.currentPath.lastIndexOf('/'))}`;
        }

        @Watch('$route')
        watchRoute(newValue, oldValue) {
            if ((newValue.name === 'scriptList' && newValue.path !== oldValue.path) ||
                (newValue.name === 'scriptSearch' && newValue.query.query !== oldValue.query.query)) {
                this.refreshScriptList();
            }
        }

        downloadScript(path) {
            window.location.href = `/script/download/${path}`;
        }

        getFileSize(size) {
            return this.formatNumber((size / 1024), 2);
        }

        isEditable(type, path) {
            if (type) {
                return FileType.FileTypeEnum[type].isEditable();
            } else {
                const ext = this.getFileExtension(path);
                return FileType.getFileTypeByExtension(ext).isEditable();
            }
        }

        getFileName(filepath) {
            if (filepath) {
                const lastIndex = this.indexOfLastSeparator(filepath);
                return filepath.substring(lastIndex + 1);
            } else {
                return '';
            }
        }

        indexOfLastSeparator(filepath) {
            const lastUnixPos = filepath.lastIndexOf('/');
            const lastWindowsPos = filepath.lastIndexOf('\\');
            return Math.max(lastUnixPos, lastWindowsPos);
        }

        getFileExtension(filepath) {
            if (!filepath) {
                return '';
            } else {
                let extensionPos = filepath.lastIndexOf('.');
                const lastSeparator = this.indexOfLastSeparator(filepath);
                extensionPos = lastSeparator > extensionPos ? -1 : extensionPos;
                return extensionPos === -1 ? '' : filepath.substring(extensionPos + 1);
            }
        }

        formatNumber(number, decimal) {
            if (number === 0) return 0;
            const n = (`${number}`);
            const parts = n.toString().split('.');
            return parts[0].replace(/\B(?=(\d{3})+(?!\d))/g, ',') + (parts[1] ? `.${(parts[1].length > decimal ? parts[1].substring(0, decimal) : parts[1])}` : '');
        }
    }
</script>

<style lang="less" scoped>
    .script-img-unit {
        background-image: url('/img/bg_script_banner_en.png');
        height: 110px;
        padding: 0;
        margin-top: 0;
    }

    .table {
        font-size: 12px;

        th, td {
            padding: 8px;
        }
    }
</style>
