<script>
    import { Mixin } from 'vue-mixin-decorator';
    import Base from '../../Base.vue';

    @Mixin
    export default class TableConfig extends Base {
        get tableFields() {
            const fields = [
                {
                    name: '__slot:state',
                    title: this.i18n('agent.list.state'),
                    dataClass: 'center aligned',
                    width: '55px',
                },
                {
                    name: '__slot:domain',
                    title: this.i18n('agent.list.IPAndDns'),
                    width: '160px',
                },
                {
                    name: 'port',
                    title: this.i18n('agent.list.port'),
                    width: '65px',
                },
                {
                    name: '__slot:name',
                    title: this.i18n('agent.list.name'),
                    sortField: 'name',
                    width: '305px',
                },
                {
                    name: '__slot:version',
                    title: this.i18n('agent.list.version'),
                    sortField: 'version',
                    width: '145px',
                },
                {
                    name: '__slot:region',
                    title: this.i18n('agent.list.region'),
                    sortField: 'region',
                    width: '160px',
                },
                {
                    name: '__slot:owner',
                    title: this.i18n('agent.list.owner'),
                    sortField: 'owner',
                    width: '120px',
                },
            ];

            if (this.isAdmin) {
                fields.unshift({
                    name: '__checkbox',
                    titleClass: 'center aligned',
                    dataClass: 'center aligned',
                    width: '35px',
                });

                fields.push({
                    name: '__slot:approved',
                    title: this.i18n('agent.list.approved'),
                    dataClass: 'center aligned',
                    width: '155px',
                });
            }

            return fields;
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
