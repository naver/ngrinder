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
                    name: '__slot:userName',
                    title: this.i18n('user.info.name'),
                    sortField: 'userName',
                    width: '140px',
                },
                {
                    name: '__slot:role',
                    title: this.i18n('user.info.role'),
                    width: '140px',
                },
                {
                    name: '__slot:email',
                    title: this.i18n('user.info.email'),
                    sortField: 'email',
                    width: '220px',
                },
                {
                    name: '__slot:description',
                    title: this.i18n('common.label.description'),
                },
                {
                    name: '__slot:createdAt',
                    title: this.i18n('user.list.table.date'),
                    sortField: 'createdAt',
                    width: '140px',
                },
                {
                    name: '__slot:edit',
                    title: this.i18n('user.list.table.edit'),
                    titleClass: 'center aligned',
                    dataClass: 'center aligned',
                    width: '45px',
                },
                {
                    name: '__slot:delete',
                    title: this.i18n('user.list.table.delete'),
                    titleClass: 'center aligned',
                    dataClass: 'center aligned',
                    width: '45px',
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
