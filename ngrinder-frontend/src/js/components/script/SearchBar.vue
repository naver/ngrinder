<template>
    <div class="search-bar card card-header">
        <table>
            <tr>
                <td>
                    <table class="w-100">
                        <colgroup>
                            <col width="350px"/>
                        </colgroup>
                        <tr>
                            <td>
                                <input type="text" class="search-query form-control" placeholder="Keywords" v-model="query" @keyup.enter="search">
                                <button class="btn btn-info search-btn" @click="search">
                                    <i class="fa fa-search mr-1"></i>
                                    <span v-text="i18n('common.button.search')"></span>
                                </button>
                            </td>
                            <td>
                                <div v-show="$route.name !== 'scriptSearch'" id="svn-url" class="input-prepend"
                                     data-toggle="popover"
                                     data-trigger="hover"
                                     data-html="true"
                                     data-placement="bottom"
                                     title="Subversion"
                                     :data-content="i18n('script.message.svn')">
                                    <div class="input-group-text">SVN</div>
                                    <div class="border uneditable-input">
                                        <router-link v-text="basePath" to="/script/list"></router-link><!--
                                        --><template v-if="currentPath !== ''"
                                                     v-for="(each, index) in currentPath.split('/')"><!--
                                            -->/<!--
                                            --><router-link :to="breadcrumbPathUrl.slice(0, index + 2).join('/')"
                                                            v-text="each"></router-link>
                                        </template>
                                    </div>
                                </div>
                            </td>
                        </tr>
                    </table>
                </td>
            </tr>
            <tr>
                <td>
                    <table class="w-100 mt-1">
                        <colgroup>
                            <col width="600px"/>
                        </colgroup>
                        <tr>
                            <td>
                                <template v-if="$route.name !== 'scriptSearch'">
                                    <button class="btn btn-primary" @click.prevent="$refs.createScriptModal.show">
                                        <i class="fa fa-file mr-1"></i>
                                        <span v-text="i18n('script.action.createScript')"></span>
                                    </button>
                                    <button class="btn btn-primary" @click.prevent="$refs.createFolderModal.show">
                                        <i class="fa fa-folder-open mr-1"></i>
                                        <span v-text="i18n('script.action.createFolder')"></span>
                                    </button>
                                    <button class="btn btn-primary" @click.prevent="$refs.uploadFileModal.show">
                                        <i class="fa fa-upload mr-1"></i>
                                        <span v-text="i18n('script.action.uploadResources')"></span>
                                    </button>
                                </template>
                            </td>
                            <td>
                                <button class="pointer-cursor btn btn-danger float-right" @click="$emit('deleteFile')">
                                    <i class="fa fa-remove mr-1"></i>
                                    <span v-text="i18n('script.action.delete')"></span>
                                </button>
                            </td>
                        </tr>
                    </table>
                </td>
            </tr>
        </table>
        <create-script-modal ref="createScriptModal" :currentPath="currentPath"></create-script-modal>
        <create-folder-modal ref="createFolderModal" :currentPath="currentPath"></create-folder-modal>
        <upload-file-modal ref="uploadFileModal" :currentPath="currentPath"></upload-file-modal>
    </div>
</template>
<script>
    import Component from 'vue-class-component';
    import Base from '../Base.vue';

    import CreateScriptModal from './modal/CreateScriptModal.vue';
    import CreateFolderModal from './modal/CreateFolderModal.vue';
    import UploadFileModal from './modal/UploadFileModal.vue';

    @Component({
        name: 'searchBar',
        props: {
            currentPath: {
                type: String,
                required: true,
            },
        },
        components: { CreateScriptModal, CreateFolderModal, UploadFileModal },
    })
    export default class SearchBar extends Base {
        query = '';
        basePath = '';

        created() {
            this.basePath = `${window.location.hostname}:${window.location.port}/svn/${this.ngrinder.currentUser.id}`;
        }

        mounted() {
            this.query = this.$route.query.query;
            this.$nextTick(() => {
                $('#svn-url').popover();
                $('#script-sample').popover();

                $('#fileName').popover();
                $('#testUrl').popover();

                $('#folderName').popover({ trigger: 'focus' });
            });
        }

        search() {
            this.$router.push({ path: '/script/search', query: { query: this.query } });
        }

        get breadcrumbPathUrl() {
            return ['/script/list', ...this.currentPath.split('/')];
        }
    }
</script>

<style lang="less" scoped>
    .search-bar {
        .search-btn {
            height: 32px;
            vertical-align: baseline;
        }

        .input-group-text {
            float: left;
            cursor: default;
            padding: 6px 10px;
        }

        .uneditable-input {
            a {
                margin-left: 7px;
            }
        }

        .search-query {
            height: 32px;
            width: 220px;
        }
    }
</style>
