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
                                    <span class="add-on">SVN</span>
                                    <span class="input-xlarge uneditable-input span7 svn-url">
                                        <router-link v-text="basePath" to="/script/list"></router-link><!--
                                        --><template v-if="currentPath !== ''"
                                                     v-for="(each, index) in currentPath.split('/')"><!--
                                            -->/<!--
                                            --><router-link :to="breadcrumbPathUrl.slice(0, index + 2).join('/')"
                                                            v-text="each"></router-link>
                                        </template>
                                    </span>
                                </div>
                            </td>
                        </tr>
                    </table>
                </td>
            </tr>
            <tr>
                <td>
                    <table class="search-bar-buttons">
                        <colgroup>
                            <col width="600px"/>
                            <col width="340px"/>
                        </colgroup>
                        <tr>
                            <td>
                                <template v-if="$route.name !== 'scriptSearch'">
                                    <a class="btn btn-primary" @click.prevent="$refs.createScriptModal.show">
                                        <i class="icon-file icon-white"></i>
                                        <span v-text="i18n('script.action.createScript')"></span>
                                    </a>
                                    <a class="btn" @click.prevent="$refs.createFolderModal.show">
                                        <i class="icon-folder-open"></i>
                                        <span v-text="i18n('script.action.createFolder')"></span>
                                    </a>
                                    <a class="btn" @click.prevent="$refs.uploadFileModal.show">
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
            scripts: {
                type: Array,
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
                $('[data-toggle="popover"]').popover('destroy');

                $('#svn-url').popover({ trigger: 'hover' });
                $('#script-sample').popover({ trigger: 'hover' });

                $('#fileName').popover({ trigger: 'focus' });
                $('#testUrl').popover({ trigger: 'focus' });

                $('#folderName').popover({ trigger: 'focus' });
            });
        }

        deleteFile() {
            const checkedScripts = this.scripts.filter(script => script.checked);
            if (checkedScripts.length === 0) {
                bootbox.alert(this.i18n('script.message.delete.alert'), this.i18n('common.button.ok'));
            } else {
                bootbox.confirm(
                    this.i18n('script.message.delete.confirm'),
                    this.i18n('common.button.cancel'),
                    this.i18n('common.button.ok'),
                    result => {
                        if (!result) {
                            return;
                        }

                        this.$http.post('/script/api/delete', checkedScripts.map(file => file.path))
                        .then(() => this.$EventBus.$emit(this.$Event.REFRESH_SCRIPT_LIST));
                    });
            }
        }

        search() {
            this.$router.push({ path: '/script/search', query: { query: this.query } });
        }

        get breadcrumbPathUrl() {
            return ['/script/list', ...this.currentPath.split('/')];
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

    .add-on {
        cursor: default;
    }

    .search-bar-buttons{
        width:100%;
        margin-top:5px;
    }
</style>
