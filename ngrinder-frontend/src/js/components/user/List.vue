<template>
    <div class="container">
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
        <table class="table table-striped table-bordered ellipsis dataTable">
            <colgroup>
                <col width="30">
                <col width="120">
                <col width="120">
                <col width="160">
                <col>
                <col width="180">
                <col width="45">
                <col width="45">
            </colgroup>
            <thead>
            <tr>
                <th class="no-click nothing">
                    <input type="checkbox" class="checkbox" v-model="selectAll">
                </th>
                <th v-text="i18n('user.info.name')"></th>
                <th class="no-click nothing" v-text="i18n('user.info.role')"></th>
                <th v-text="i18n('user.info.email')"></th>
                <th class="no-click nothing" v-text="i18n('common.label.description')"></th>
                <th v-text="i18n('user.list.table.date')"></th>
                <th class="no-click nothing" v-text="i18n('user.list.table.edit')"></th>
                <th class="no-click nothing" v-text="i18n('user.list.table.delete')"></th>
            </tr>
            </thead>
            <tbody>
            <tr v-for="user in users">
                <td class="center">
                    <input type="checkbox" class="checkbox"
                           :disabled="isAdminUser(user)"
                           v-model="user.checked"
                           v-text="user.userName"/>
                </td>
                <td class="ellipsis">
                    <router-link :to="`/user/${user.userId}`" v-text="user.userName"></router-link>
                </td>
                <td title="user.fullName" v-text="user.role ? user.role.fullName : ''"></td>
                <td class="ellipsis" v-text="user.email"></td>
                <td class="ellipsis" v-text="user.description"></td>
                <td>{{ user.createdDate | dateFormat('YYYY-MM-DD HH:mm') }}</td>
                <td class="center">
                    <router-link :to="`/user/${user.userId}`">
                        <i class="fa fa-edit"></i>
                    </router-link>
                </td>
                <td class="center">
                    <a class="pointer-cursor" v-if="!isAdminUser(user)"
                       @click="deleteUsers(user.userId, user.userName)">
                        <i class="fa fa-remove"></i>
                    </a>
                </td>
            </tr>
            <tr v-if="users.length === 0">
                <td colspan="8" class="center" v-text="i18n('common.message.noData')"></td>
            </tr>
            </tbody>
        </table>
        <div v-show="page.totalPages > 1" class="pagination">
            <paginate
                pageClass="page-item"
                prevClass="page-item"
                nextClass="page-item"
                pageLinkClass="page-link"
                prevLinkClass="page-link"
                nextLinkClass="page-link"
                containerClass="pagination pagination-sm"
                v-model="page.number"
                :pageCount="page.totalPages"
                :page-range="page.size"
                :click-handler="changePage"
                :prev-text="`← ${i18n('common.paging.previous')}`"
                :next-text="`${i18n('common.paging.next')} →`">
            </paginate>
        </div>
    </div>
</template>

<script>
    import { Mixins } from 'vue-mixin-decorator';
    import { Component, Watch } from 'vue-property-decorator';
    import vueHeadful from 'vue-headful';
    import Paginate from 'vuejs-paginate';
    import Base from '../Base.vue';
    import MessagesMixin from '../common/mixin/MessagesMixin.vue';

    @Component({
        name: 'userList',
        components: { vueHeadful, Paginate },
    })
    export default class UserList extends Mixins(Base, MessagesMixin) {
        roles = [{
            name: null,
            fullName: this.i18n('user.left.all'),
        }];
        role = this.roles[0];

        users = [];
        page = {
            number: 1,
            totalPages: 1,
            size: 10,
        };

        keywords = '';
        sort = 'userName,ASC';

        selectAll = false;

        mounted() {
            this.initByQueryParams();
            this.loadRoleSet().then(this.loadUsers);
        }

        initByQueryParams() {
            this.keywords = this.$route.query.keywords || this.keywords;
            this.page.number = this.$route.query.page || this.page.number;
            this.page.size = this.$route.query.page || this.page.size;
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

        loadUsers() {
            this.$http.get('/user/api/list', {
                params: {
                    'role': this.role.name,
                    'page.page': this.page.number - 1,
                    'page.size': this.page.size,
                    'keywords': this.keywords,
                    'sort': this.sort,
                },
            })
            .then(res => {
                this.users = res.data.content.map(user => {
                    user.checked = false;
                    return user;
                });

                this.page.totalPages = res.data.totalPages;

                this.selectAll = false;
            })
            .catch(() => this.showErrorMsg(this.i18n('common.message.loading.error', { content: this.i18n('common.user') })));
        }

        search() {
            this.role = this.roles[0];

            this.$router.replace({
                query: {
                    'page.page': this.page.number - 1,
                    'page.size': this.page.size,
                    'keywords': this.keywords,
                    'sort': this.sort,
                },
            });

            this.loadUsers();
        }

        changePage(page) {
            this.page.number = page;
            this.loadUsers();
        }

        deleteUsers(userIds, names) {
            this.$bootbox.confirm({
                message: `${this.i18n('user.list.confirm.delete')} ${names}?`,
                buttons: {
                    confirm: { label: this.i18n('common.button.ok') },
                    cancel: { label: this.i18n('common.button.cancel') },
                },
                callback: result => {
                    if (result) {
                        this.$http.delete('/user/api/', { params: { userIds } })
                            .then(this.loadUsers)
                            .catch(() => this.showErrorMsg(this.i18n('user.message.delete.error')));
                    }
                },
            });
        }

        deleteCheckedUsers() {
            const userIds = this.users.filter(u => u.checked).map(u => u.userId).join(',');
            const userNames = this.users.filter(u => u.checked).map(u => u.userName).join(', ');
            this.deleteUsers(userIds, userNames);
        }

        changeRole() {
            this.keywords = '';
            this.page.number = 1;
            const queryParam = this.role.name ? `?role=${this.role.name}` : '';
            history.replaceState('', '', `${this.$route.path}${queryParam}`);
            this.loadUsers();
        }

        @Watch('selectAll')
        changeSelectAll(newValue) {
            this.users.filter(user => !this.isAdminUser(user)).forEach(user => user.checked = newValue);
        }

        isAdminUser(user) {
            return user.userId === 'admin';
        }
    }
</script>

<style lang="less" scoped>
    .container {
        table {
            i {
                color: black;
            }
        }

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

        .pagination {
            margin-top: -3px !important;
        }

        .role-select {
            width: 220px;
        }
    }
</style>
