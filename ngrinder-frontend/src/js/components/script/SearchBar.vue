<template>
    <div class="search-bar card card-header">
        <div class="d-flex justify-content-between search-container">
            <div>
                <input type="text" class="search-query form-control"
                       placeholder="Keywords" v-model="query" @keyup.enter="search" v-focus>
                <button class="btn btn-info search-btn" @click="search">
                    <i class="fa fa-search mr-1"></i>
                    <span v-text="i18n('common.button.search')"></span>
                </button>
            </div>
            <div id="svn-url" class="input-prepend d-flex" v-show="$route.name !== 'scriptSearch'"
                 data-toggle="popover"
                 data-trigger="hover"
                 data-html="true"
                 data-placement="bottom"
                 title="Subversion"
                 :data-content="i18n('script.message.svn')">
                <div class="input-group">
                    <div class="input-group-prepend"><span class="input-group-text">SVN</span></div>
                    <div class="border form-control uneditable-input ellipsis">
                        <router-link v-text="svnPath" to="/script/list"></router-link><!--
                     --><span v-if="currentPath !== ''">
                            <template v-for="(each, index) in currentPath.split('/')">/<!--
                             --><router-link :key="each" :to="breadcrumbPathUrl.slice(0, index + 2).join('/')" v-text="each"></router-link>
                            </template>
                        </span>
                    </div>
                </div>
            </div>
        </div>
        <div class="d-flex justify-content-between">
            <div v-show="$route.name !== 'scriptSearch'">
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
            </div>
            <button class="pointer-cursor btn btn-danger ml-auto" @click="$emit('deleteFile')">
                <i class="fa fa-remove mr-1"></i>
                <span v-text="i18n('script.action.delete')"></span>
            </button>
        </div>
        <create-script-modal ref="createScriptModal" :currentPath="currentPath" focus="fileName"></create-script-modal>
        <create-folder-modal ref="createFolderModal" :currentPath="currentPath" focus="folderName"></create-folder-modal>
        <upload-file-modal ref="uploadFileModal" :currentPath="currentPath" focus="file"></upload-file-modal>
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
        svnPath = '';

        created() {
            const userId = this.ngrinder.config.userSwitchMode ? this.ngrinder.currentUser.factualUser.id : this.ngrinder.currentUser.id;
            this.svnPath = `${window.location.protocol}//${window.location.host}/svn/${userId}`;
        }

        mounted() {
            this.query = this.$route.query.query;
            this.$nextTick(() => $('[data-toggle="popover"]').popover());
        }

        search() {
            if (!this.query) {
                if (this.$route.path !== '/script') {
                    this.$router.push('/script');
                }
                return;
            }

            this.$router.push({ path: '/script/search', query: { query: this.query } });
        }

        get breadcrumbPathUrl() {
            return ['/script/list', ...this.currentPath.split('/')];
        }
    }
</script>

<style lang="less" scoped>
    .search-bar {
        height: 90px;

        .search-container {
            line-height: 0;
            margin-bottom: 8px;

            .input-group-text {
                height: 30px;
            }

            .search-query {
                height: 30px;
                width: 339px;
            }

            .search-btn {
                height: 30px;
                vertical-align: baseline;
            }

            .uneditable-input {
                width: 500px;
                height: 30px;

                > * {
                    vertical-align: middle;
                }
            }
        }
    }
</style>

<style lang="less">
    .modal-dialog {
        margin-top: 200px;
    }
</style>
