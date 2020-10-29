<script>
    import { Mixin } from 'vue-mixin-decorator';
    import Base from '../../Base.vue';

    @Mixin
    export default class TableConfig extends Base {
        get tableFields() {
            return [
                {
                    name: '__checkbox',
                    titleClass: 'center aligned',
                    dataClass: 'center aligned',
                    width: '30px',
                },
                {
                    name: '__slot:fileIcon',
                    title: `<a id="file-icon-back" class="pointer-cursor"><img src="${this.contextPath}/img/up_folder.png"/></a>`,
                    dataClass: 'center aligned',
                    width: '32px',
                },
                {
                    name: '__slot:path',
                    title: this.i18n('script.list.name'),
                    sortField: 'path',
                    width: '330px',
                },
                {
                    name: '__slot:description',
                    title: this.i18n('script.list.commit'),
                    width: '390px',
                },
                {
                    name: '__slot:lastModifiedAt',
                    title: this.i18n('script.list.lastDate'),
                    sortField: 'lastModifiedAt',
                    width: '180px',
                },
                {
                    name: 'revision',
                    title: this.i18n('script.list.revision'),
                    sortField: 'revision',
                    width: '80px',
                },
                {
                    name: '__slot:fileSize',
                    title: this.i18n('script.list.size'),
                    sortField: 'fileSize',
                    width: '80px',
                },
                {
                    name: '__slot:download',
                    title: this.i18n('script.list.download'),
                    dataClass: 'center aligned',
                    width: '80px',
                },
            ];
        }

        get tableCss() {
            return {
                table: {
                    ascendingIcon: 'fa fa-arrow-up',
                        descendingIcon: 'fa fa-arrow-down',
                        tableClass: 'table table-striped table-bordered',
                },
                pagination: {
                    infoClass: 'float-left',
                        wrapperClass: 'pagination float-right',
                        activeClass: 'active',
                        disabledClass: 'disabled',
                        pageClass: 'page-link',
                        linkClass: 'page-link',
                        icons: {
                            first: 'fa fa-angle-double-left',
                            prev: 'fa fa-angle-left',
                            next: 'fa fa-angle-right',
                            last: 'fa fa-angle-double-right',
                    },
                },
            };
        }
    }

</script>
