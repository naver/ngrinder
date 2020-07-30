<template>
    <div class="search-bar card card-header">
        <div class="d-flex search-bar-container">
            <div class="d-inline-block">
                <div class="input-group">
                    <input type="text" class="input-search-append form-control"
                           placeholder="Keywords" v-model="query" @keyup.enter="search" v-focus>
                    <div class="input-group-append">
                        <button class="btn btn-info search-btn" @click="search">
                            <i class="fa fa-search mr-1"></i>
                            <span v-text="i18n('common.button.search')"></span>
                        </button>
                    </div>
                </div>
            </div>
            <div class="processing-btn-group">
                <div v-show="$route.name !== 'scriptSearch'">
                    <div class="btn-group">
                        <button class="btn btn-primary dropdown-toggle"
                                data-toggle="dropdown"
                                aria-haspopup="true"
                                aria-expanded="false">
                            <i class="fa fa-plus mr-1"></i>
                            <span v-text="i18n('common.button.create')"></span>
                        </button>
                        <div class="dropdown-menu">
                            <button class="dropdown-item" @click.prevent="$refs.createScriptModal.show">
                                <i class="fa fa-file mr-1"></i><span v-text="i18n('script.action.createScript')"></span>
                            </button>
                            <button class="dropdown-item" @click.prevent="$refs.createFolderModal.show">
                                <i class="fa fa-folder-open mr-1"></i><span v-text="i18n('script.action.createFolder')"></span>
                            </button>
                        </div>
                    </div>
                    <button class="btn btn-primary" @click.prevent="$refs.uploadFileModal.show">
                        <i class="fa fa-upload mr-1"></i>
                        <span v-text="i18n('script.action.upload')"></span>
                    </button>
                </div>
                <button class="pointer-cursor btn btn-danger" @click="$emit('deleteFile')">
                    <i class="fa fa-remove mr-1"></i>
                    <span v-text="i18n('script.action.delete')"></span>
                </button>
            </div>
            <div v-show="$route.name !== 'scriptSearch'" class="svn-url ml-auto">
                <div class="input-prepend"
                     data-toggle="popover"
                     data-trigger="hover"
                     data-html="true"
                     data-placement="bottom"
                     title="Subversion"
                     :data-content="i18n('script.message.svn')">
                    <div class="input-group">
                        <div class="input-group-prepend"><span class="input-group-text default-cursor">SVN</span></div>
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
                <div class="input-group-append pointer-cursor">
                    <span class="input-group-text"
                          v-clipboard:error="onError"
                          v-clipboard:success="onCopy"
                          v-clipboard:copy="currentPath !== '' ? `${svnPath}/${currentPath}` : svnPath">
                        <i class="fa fa-copy mr-1"></i>
                    </span>
                </div>
            </div>
        </div>
        <create-script-modal ref="createScriptModal" :currentPath="currentPath" focus="fileName"></create-script-modal>
        <create-folder-modal ref="createFolderModal" :currentPath="currentPath" focus="folderName"></create-folder-modal>
        <upload-file-modal ref="uploadFileModal" :currentPath="currentPath" focus="file"></upload-file-modal>
    </div>
</template>
<script>
    import { Mixins } from 'vue-mixin-decorator';
    import Component from 'vue-class-component';
    import Base from '../Base.vue';

    import CreateScriptModal from './modal/CreateScriptModal.vue';
    import CreateFolderModal from './modal/CreateFolderModal.vue';
    import UploadFileModal from './modal/UploadFileModal.vue';
    import MessagesMixin from '../common/mixin/MessagesMixin.vue';

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
    export default class SearchBar extends Mixins(Base, MessagesMixin) {
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

        onError() {
            this.showErrorMsg(this.i18n('common.message.copy.fail'));
        }

        onCopy() {
            this.showSuccessMsg(this.i18n('common.message.copy.success'));
        }

        get breadcrumbPathUrl() {
            return ['/script/list', ...this.currentPath.split('/')];
        }
    }
</script>

<style lang="less" scoped>
    .search-bar {
        height: 50px;

        button {
            height: 30px;
        }

        .search-bar-container {
            margin-bottom: 8px;

            .input-group-text {
                height: 30px;
            }

            .input-search-append {
                height: 30px;
                width: 200px;
            }

            .search-btn {
                height: 30px;
                vertical-align: baseline;
                margin: 0 3px 0 -3px;
                border-radius: 0 3px 3px 0;
            }

            .uneditable-input {
                width: 450px;
                height: 30px;
                border-radius: 3px 0 0 3px;

                > * {
                    vertical-align: middle;
                }
            }

            .input-group-append {
                .input-group-text {
                    border-radius: 0 3px 3px 0;

                    &:hover {
                        background: #d9dcdf;
                    }
                }
            }

            .dropdown-item {
                &:hover {
                    background: #007bff;
                    color: white;
                }

                i {
                    width: 15px;
                }
            }

            .svn-url {
                display: flex;
            }

            .processing-btn-group > div {
                display: inline-block;
            }
        }
    }
</style>

<style lang="less">
    .modal-dialog {
        margin-top: 200px;
    }
</style>
