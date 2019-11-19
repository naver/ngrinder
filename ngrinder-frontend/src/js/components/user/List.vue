<template>
    <div class="container user-list-container">
        <vue-headful :title="i18n('user.list.title')"/>
        <fieldSet>
            <legend class="header border-bottom d-flex">
                <span v-text="i18n('navigator.dropDown.userManagement')"></span>
                <select class="form-control role-select ml-auto mt-auto mb-auto" v-model="role" @change="changeRole">
                    <option v-for="role in roles" :value="role" v-text="role.fullName"></option>
                </select>
            </legend>
        </fieldSet>
        <div class="card card-header search-bar border-bottom-0">
            <input type="text" class="search-query-without-radios form-control"
                   placeholder="Keywords" @keyup.enter="search" v-model="keywords">
            <button class="btn btn-info btn-search" @click="search">
                <i class="fa fa-search mr-1"></i>
                <span v-text="i18n('common.button.search')"></span>
            </button>
            <div class="ml-auto">
                <button class="btn btn-info" @click="$router.push({ path : '/user/new' })">
                    <i class="fa fa-user mr-1"></i>
                    <span v-text="i18n('user.list.button.create')"></span>
                </button>
                <button class="btn btn-danger" @click="deleteCheckedUsers">
                    <i class="fa fa-remove mr-1"></i>
                    <span v-text="i18n('user.list.button.delete')"></span>
                </button>
            </div>
        </div>
        <vuetable
            v-show="showTable"
            ref="vuetable"
            data-path="content"
            pagination-path=""
            track-by="userId"
            :api-url="`${contextPath}/user/api/list`"
            :append-params="table.appendParams"
            :query-params="{ sort: 'sort', page: 'page.page', perPage: 'page.size' }"
            :per-page="table.pagination.perPage"
            :css="table.css.table"
            :multi-sort="true"
            :fields="tableFields"
            :load-on-start="false"
            @vuetable:pagination-data="beforePagination"
            @vuetable:loading="beforeTableLoading">

            <template slot="userName" slot-scope="props">
                <router-link :to="`/user/${props.rowData.userId}`" v-text="props.rowData.userName"></router-link>
            </template>

            <template slot="role" slot-scope="props">
                <div :title="props.rowData.role.fullName" v-text="props.rowData.role ? props.rowData.role.fullName : ''"></div>
            </template>

            <template slot="email" slot-scope="props">
                <div class="ellipsis email" :title="props.rowData.email" v-text="props.rowData.email"></div>
            </template>

            <template slot="description" slot-scope="props">
                <div class="ellipsis description" :title="props.rowData.description" v-text="props.rowData.description"></div>
            </template>

            <template slot="createdDate" slot-scope="props">
                {{ props.rowData.createdDate | dateFormat('YYYY-MM-DD HH:mm') }}
            </template>

            <template slot="edit" slot-scope="props">
                <router-link :to="`/user/${props.rowData.userId}`">
                    <i class="fa fa-edit text-dark fa-angle-"></i>
                </router-link>
            </template>

            <template slot="delete" slot-scope="props">
                <a class="pointer-cursor" v-if="!isAdminUser(props.rowData)"
                   @click="deleteUsers(props.rowData.userId)">
                    <i class="fa fa-remove"></i>
                </a>
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
    import { Component } from 'vue-property-decorator';
    import vueHeadful from 'vue-headful';
    import Paginate from 'vuejs-paginate';
    import Vuetable from 'vuetable-2';
    import VuetablePagination from 'vuetable-2/src/components/VuetablePagination.vue';
    import Base from '../Base.vue';
    import TableConfig from './mixin/TableConfig.vue';
    import MessagesMixin from '../common/mixin/MessagesMixin.vue';

    @Component({
        name: 'userList',
        components: { vueHeadful, Paginate, Vuetable, VuetablePagination },
    })
    export default class UserList extends Mixins(Base, MessagesMixin, TableConfig) {
        roles = [{
            name: null,
            fullName: this.i18n('user.left.all'),
        }];

        role = this.roles[0];

        table = {
            css: {},
            appendParams: {},
            pagination: {
                perPage: 15,
            },
        };

        keywords = '';
        sort = 'userName,ASC';
        showTable = false;

        created() {
            this.table.css = this.tableCss;
        }

        mounted() {
            this.init();
            this.loadRoleSet().then(() => this.$refs.vuetable.reload().then(() => this.showTable = true));
        }

        changePage(nextPage) {
            this.$refs.vuetable.changePage(nextPage);
            history.replaceState('', '', this.makeQueryString(this.$refs.vuetable.currentPage,
                this.table.pagination.perPage, this.keywords, this.sort, this.role ? this.role.name : ''));
        }

        beforePagination(paginationData) {
            paginationData.total = paginationData.totalElements;
            paginationData.last_page = paginationData.totalPages;
            paginationData.per_page = this.table.pagination.perPage;
            paginationData.current_page = this.$refs.vuetable.currentPage;
            this.$refs.pagination.setPaginationData(paginationData);
        }

        beforeTableLoading() {
            this.table.appendParams['page.page'] = this.$refs.vuetable.currentPage - 1;
            this.table.appendParams['page.size'] = this.table.pagination.perPage;
            this.table.appendParams.keywords = this.keywords;
            this.table.appendParams.role = this.role ? this.role.name : '';
            this.table.appendParams.sort = this.$refs.vuetable.getSortParam().split('|').join(',');
        }

        init() {
            this.keywords = this.$route.query.keywords || this.keywords;
            this.$refs.vuetable.currentPage = parseInt(this.$route.query['page.page']) || 1;
            this.table.pagination.perPage = parseInt(this.$route.query['page.size']) || this.table.pagination.perPage;
            this.sort = this.$route.query.sort || this.sort;
        }

        loadRoleSet() {
            return this.$http.get('/user/api/role')
            .then(res => {
                this.roles.push(...res.data);
                if (this.$route.query.role) {
                    this.role = this.roles.find(role => role.name === this.$route.query.role);
                }
            })
            .catch(() => this.showErrorMsg(this.i18n('common.message.loading.error')));
        }

        search() {
            this.role = this.roles[0];
            history.replaceState('', '', this.makeQueryString(1, this.table.pagination.perPage,
                this.keywords, this.sort));
            this.$refs.vuetable.refresh();
        }

        makeQueryString(page, pageSize, keyword, sort, role) {
            let queryString = `${this.contextPath}/user?page.page=${page}&page.size=${pageSize}&keywords=${keyword}&sort=${sort}`;
            if (role) {
                queryString += `&role=${role}`;
            }
            return queryString;
        }

        deleteUsers(userIds) {
            this.$bootbox.confirm({
                message: `${this.i18n('user.list.confirm.delete')}?`,
                buttons: {
                    confirm: { label: this.i18n('common.button.ok') },
                    cancel: { label: this.i18n('common.button.cancel') },
                },
                onConfirm: () => this.$http.delete('/user/api/', { params: { userIds } })
                    .then(this.$refs.vuetable.refresh)
                    .then(() => this.$refs.vuetable.selectedTo = [])
                    .catch(() => this.showErrorMsg(this.i18n('user.message.delete.error'))),
            });
        }

        deleteCheckedUsers() {
            this.deleteUsers(this.$refs.vuetable.selectedTo.join(','));
        }

        changeRole() {
            this.keywords = '';
            this.$refs.vuetable.currentPage = 1;
            const queryParam = this.role.name ? `?role=${this.role.name}` : '';
            history.replaceState('', '', `${this.contextPath}${this.$route.path}${queryParam}`);
            this.$refs.vuetable.refresh();
        }

        isAdminUser(user) {
            return user.userId === 'admin';
        }
    }
</script>

<style lang="less" scoped>
    .user-list-container {
        .search-bar {
            flex-direction: row;

            .search-query-without-radios {
                width: 234px;
                height: 30px;
            }

            .btn-search {
                margin-left: 5px;
            }
        }

        .table {
            margin-bottom: 7px;
        }

        .pagination {
            margin-top: -3px !important;
        }

        .role-select {
            width: 220px;
        }
    }
</style>
