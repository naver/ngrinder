<template>
    <div class="well form-inline search-bar" style="margin-top:0;margin-bottom:0">
        <table>
            <tr>
                <td>
                    <table>
                        <colgroup>
                            <col width="400px"/>
                            <col width="*"/>
                        </colgroup>
                        <tr>
                            <td>
                                <input type="text" class="search-query span3" placeholder="Keywords" v-model="query" @keyup.enter="search">
                                <button class="btn" @click="search">
                                    <i class="icon-search"></i><span v-text="i18n('common.button.search')"></span>
                                </button>
                            </td>
                            <td>
                                <div v-show="$route.name !== 'scriptSearch'" id="svn-url" class="input-prepend"
                                     data-toggle="popover" :data-content="i18n('script.message.svn')" data-html="true"
                                     title="Subversion" data-placement="bottom">
                                    <span class="add-on" style="cursor:default">SVN</span>
                                    <span class="input-xlarge uneditable-input span7" v-html="svnUrl"></span>
                                </div>
                            </td>
                        </tr>
                    </table>
                </td>
            </tr>
            <tr>
                <td>
                    <table style="width:100%; margin-top:5px">
                        <colgroup>
                            <col width="600px"/>
                            <col width="340px"/>
                        </colgroup>
                        <tr>
                            <td>
                                <template v-if="$route.name !== 'scriptSearch'">
                                    <a class="btn btn-primary" data-toggle="modal" data-target="#create-script-modal">
                                        <i class="icon-file icon-white"></i>
                                        <span v-text="i18n('script.action.createScript')"></span>
                                    </a>
                                    <a class="btn" data-toggle="modal" data-target="#create-folder-modal">
                                        <i class="icon-folder-open"></i>
                                        <span v-text="i18n('script.action.createFolder')"></span>
                                    </a>
                                    <a class="btn" data-toggle="modal" data-target="#upload-file-modal">
                                        <i class="icon-upload"></i>
                                        <span v-text="i18n('script.action.uploadResources')"></span>
                                    </a>
                                </template>
                            </td>
                            <td>
                                <a class="pointer-cursor btn btn-danger pull-right" @click="deleteFile">
                                    <i class="icon-remove icon-white"></i>
                                    <span v-text="i18n('script.action.delete')"></span>
                                </a>
                            </td>
                        </tr>
                    </table>
                </td>
            </tr>
        </table>
        <create-script-modal :currentPath="currentPath"></create-script-modal>
        <create-folder-modal :currentPath="currentPath"></create-folder-modal>
        <upload-file-modal :currentPath="currentPath"></upload-file-modal>
    </div>
</template>
<script>
    import { Component, Watch } from 'vue-property-decorator';
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
            scripts: {
                type: Array,
                required: true,
            },
        },
        components: { CreateScriptModal, CreateFolderModal, UploadFileModal},
    })
    export default class SearchBar extends Base {
        query = '';
        svnUrl = '';

        mounted() {
            this.query = this.$route.query.query;

            this.initSvnUrl();

            this.$nextTick(() => {
                $('[data-toggle="popover"]').popover('destroy');

                $('#svn-url').popover({trigger: 'hover'});
                $('#script-sample').popover({trigger: 'hover'});

                $('#fileName').popover({trigger: 'focus'});
                $('#testUrl').popover({trigger: 'focus'});

                $('#folderName').popover({trigger: 'focus'});
            });
        }

        initSvnUrl() {
            if (!this.$route.query.query) {
                this.$http.get("/script/api/svnUrl", {
                    params: {
                        path: this.currentPath,
                    }
                }).then(res => {
                    this.svnUrl = res.data;
                });
            }
        }

        deleteFile() {
            const checkedScripts = this.scripts.filter(script => script.checked);
            if (checkedScripts.length === 0) {
                bootbox.alert(this.i18n("script.message.delete.alert"), this.i18n("common.button.ok"));
            } else {
                bootbox.confirm(
                    this.i18n("script.message.delete.confirm"),
                    this.i18n("common.button.cancel"),
                    this.i18n("common.button.ok"),
                    result => {
                        if (!result) {
                            return;
                        }

                        this.$http.post(`/script/api/delete`, checkedScripts.map(file => file.path))
                        .then(() => this.$EventBus.$emit(this.$Event.REFRESH_SCRIPT_LIST));
                    });
            }
        }

        search() {
            this.$router.push({ path: '/script/search', query: { query: this.query } });
        }

        @Watch('scripts')
        onRefresh() {
            this.initSvnUrl();
        }

        @Watch('query')
        onQueryChanged() {
            if (this.$route.name === 'scriptSearch') {
                this.$router.replace({
                    query: {
                        ...this.$route.query,
                        query: this.query
                    }
                })
            }
        }
    }
</script>

<style scoped>
    .search-query {
        width: 234px;
        height: inherit;
    }

    .uneditable-input {
        cursor: text;
    }
</style>
