<template>
    <div class="container">
        <vue-headful :title="i18n('user.list.title')"/>
        <fieldSet>
            <legend class="header">
                <span v-text="i18n('navigator.dropDown.userManagement')"></span>
                <select class="pull-right form-control role-select" v-model="role">
                    <option v-for="role in roles" :value="role" v-text="role.fullName"></option>
                </select>
            </legend>
        </fieldSet>
        <div class="well form-inline search-bar">
            <input type="text" class="search-query search-query-without-radios form-control"
                   placeholder="Keywords" @keyup.enter="search" v-model="keywords">
            <a class="btn" @click="search">
                <i class="icon-search"></i><span v-text="i18n('common.button.search')"></span>
            </a>
            <span class="pull-right">
                <a class="btn" data-toggle="modal" data-target="#sign_up_modal"
                   @click="$EventBus.$emit($Event.RESET_SIGN_UP_MODAL)">
                    <i class="icon-user"></i><span v-text="i18n('user.list.button.create')"></span>
                </a>
                <a class="btn btn-danger" @click="deleteCheckedUsers">
                    <i class="icon-remove icon-white"></i><span v-text="i18n('user.list.button.delete')"></span>
                </a>
            </span>
        </div>
        <table class="table table-striped table-bordered ellipsis dataTable" id="user_table">
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
                        <i class="icon-edit"></i>
                    </router-link>
                </td>
                <td class="center">
                    <a class="pointer-cursor" v-if="!isAdminUser(user)" @click="deleteUsers(user.userId)">
                        <i class="icon-remove"></i>
                    </a>
                </td>
            </tr>
            <tr v-if="users.length === 0">
                <td colspan="8" class="center" v-text="i18n('common.message.noData')"></td>
            </tr>
            </tbody>
        </table>
        <div v-show="page.totalPages > 1" class="pagination dataTables_paginate">
            <paginate
                v-model="page.number"
                :pageCount="page.totalPages"
                :page-range="page.size"
                :click-handler="changePage"
                :prev-text="`← ${i18n('common.paging.previous')}`"
                :next-text="`${i18n('common.paging.next')} →`">
            </paginate>
        </div>
        <sign-up-modal></sign-up-modal>
    </div>
</template>

<script>
    import { Component, Watch } from 'vue-property-decorator';
    import Base from '../Base.vue';
    import vueHeadful from 'vue-headful';
    import Paginate from 'vuejs-paginate';
    import SignUpModal from './modal/SignUpModal.vue';


    @Component({
        name: "userList",
        components: { vueHeadful, SignUpModal, Paginate, },
    })
    export default class UserList extends Base {

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
            this.loadRoleSet();
            this.loadUsers();
        }

        initByQueryParams() {
            this.keywords = getOrDefault(this.$route.query.keywords, this.keywords);
            this.page.number = getOrDefault(this.$route.query.page, this.page.number);
            this.page.size = getOrDefault(this.$route.query.page, this.page.size);
            this.sort = getOrDefault(this.$route.query.sort, this.sort);
        }

        loadRoleSet() {
            this.$http.get('/user/api/role')
            .then(res => {
                this.roles.push(...res.data);
                if (this.$route.query.role) {
                    this.role = this.roles.find(role => role.name === this.$route.query.role);
                }
            })
        }

        loadUsers() {
            this.$http.get('/user/api/list', {
                params: {
                    'role': this.role.name,
                    'page.page': this.page.number - 1,
                    'page.size': this.page.size,
                    'keywords': this.keywords,
                    'sort': this.sort,
                }
            })
            .then(res => {
                this.users = res.data.content.map(user => {
                    user.checked = false;
                    return user;
                });

                this.page.totalPages = res.data.totalPages;

                this.selectAll = false;
            })
        }

        search() {
            this.role = this.roles[0];

            this.$router.replace({
                query: {
                    'page.page': this.page.number - 1,
                    'page.size': this.page.size,
                    'keywords': this.keywords,
                    'sort': this.sort,
                }
            });

            this.loadUsers();
        }

        changePage(page) {
            this.page.number = page;
            this.loadUsers();
        }

        deleteUsers(userIds) {
            this.$http.delete('/user/api/', { params: { userIds: userIds } })
            .then(() => this.loadUsers());
        }

        deleteCheckedUsers() {
            const userIds = this.users.filter(u => u.checked).map(u => u.userId).join(',');
            this.deleteUsers(userIds);
        }

        @Watch('role')
        changeRole() {
            this.keywords = '';
            this.page.number = 1;
            this.$router.replace({
                query: {
                    role: this.role.name ? this.role.name : undefined
                }
            });
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

    const getOrDefault = (value, defaultValue) => {
        try {
            return value ? value : defaultValue;
        } catch (e) {
            // Do nothing
        }
        return defaultValue;
    };
</script>

<style lang="less" scoped>
    .search-query {
        width: 234px;
        height: 30px;
    }

    table {
        font-size: 12px;
    }
</style>
