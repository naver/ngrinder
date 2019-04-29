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
                                <input type="text" class="search-query span3" placeholder="Keywords" v-model="query">
                                <button type="submit" class="btn">
                                    <i class="icon-search"></i><span v-text="i18n('common.button.search')"></span>
                                </button>
                            </td>
                            <td>
                                <div v-show="!query" id="svn-url" class="input-prepend"
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
                                <template v-if="!query">
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
    </div>
</template>
<script>
    import Component from 'vue-class-component';
    import Base from '../Base.vue';

    import CreateScriptModal from './modal/CreateScriptModal.vue';

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
        components: { CreateScriptModal, },
    })
    export default class SearchBar extends Base {
        handlers = [];
        query = '';
        svnUrl = '';

        mounted() {
            this.query = this.$route.query.query;

            if (!this.query) {
                this.$http.get("/script/api/svnUrl", {
                    params: {
                        user: this.currentUser,
                        path: this.currentPath,
                    }
                }).then(res => {
                    this.svnUrl = res.data;
                });
            }

            this.$nextTick(() => {
                $('[data-toggle="popover"]').popover('destroy');

                $('#svn-url').popover({trigger: 'hover'});
                $('#script-sample').popover({trigger: 'hover'});

                $('#fileName').popover({trigger: 'focus'});
                $('#testUrl').popover({trigger: 'focus'});

                $('#folderName').popover({trigger: 'focus'});
            });
        }

        deleteFile() {
            const checkedScripts = this.scripts.filter(script => script.checked);
            if (checkedScripts.length === 0) {
                bootbox.alert(this.i18n("script.message.delete.alert"), this.i18n("common.button.ok"));
            } else {
                bootbox.confirm(this.i18n("script.message.delete.confirm"), this.i18n("common.button.cancel"), this.i18n("common.button.ok"), result => {
                    if (!result) {
                        return;
                    }

                    const scriptsString = checkedScripts.map(file => file.fileName).join(",");
                    this.$http.post(`/script/api/delete/${this.currentPath}`, null, {
                        params: {
                            filesString: scriptsString
                        }
                    })
                    .then(() => this.$EventBus.$emit(this.$Event.REFRESH_SCRIPT_LIST));
                });
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
