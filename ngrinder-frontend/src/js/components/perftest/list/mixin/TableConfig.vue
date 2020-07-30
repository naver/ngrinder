<script>
    import { Mixin } from 'vue-mixin-decorator';
    import Base from '../../../Base.vue';

    @Mixin
    export default class TableConfig extends Base {
        get tableFields() {
            const fields = [
                {
                    name: '__checkbox',
                    titleClass: 'center aligned',
                    dataClass: 'center aligned',
                    width: '30px',
                },
                {
                    name: '__slot:status',
                    title: this.i18n('common.label.status'),
                    titleClass: `${this.$utils.isLocale('en') ? 'pl-2' : ''}`,
                    dataClass: 'center aligned',
                    width: '50px',
                },
                {
                    name: '__slot:testName',
                    title: this.i18n('perfTest.list.testName'),
                    dataClass: 'position-relative',
                    sortField: 'testName',
                    width: this.ngrinder.config.clustered ? '185px' : '240px',
                },
                {
                    name: '__slot:scriptName',
                    title: this.i18n('perfTest.list.scriptName'),
                    sortField: 'scriptName',
                    width: this.ngrinder.config.clustered ? '210px' : '255px',
                },
                {
                    name: '__slot:createUser',
                    title: this.isAdmin ? this.i18n('perfTest.list.owner') :
                        this.i18n('perfTest.list.modifier.oneLine'),
                    width: '80px',
                },
                {
                    name: '__slot:startTime',
                    title: this.i18n('perfTest.list.startTime'),
                    sortField: 'startTime',
                    width: '130px',
                },
                {
                    name: '__slot:threshold',
                    title: this.i18n('perfTest.list.threshold'),
                    width: '80px',
                },
                {
                    name: '__slot:tps',
                    title: this.i18n('perfTest.list.tps'),
                    sortField: 'tps',
                    width: '70px',
                },
                {
                    name: '__slot:meanTestTime',
                    title: 'MTT',
                    sortField: 'meanTestTime',
                    width: '70px',
                },
                {
                    name: '__slot:errorRate',
                    title: this.i18n('perfTest.list.errorRate'),
                    sortField: 'errors',
                    width: '70px',
                },
                {
                    name: '__slot:vuser',
                    title: this.i18n('perfTest.list.vusers'),
                    width: '55px',
                },
                {
                    name: '__slot:actions',
                    title: this.i18n('common.label.actions'),
                    titleClass: `${this.$utils.isLocale('en') ? 'pl-2' : ''}`,
                    dataClass: 'center aligned',
                    width: '55px',
                },
            ];

            if (this.ngrinder.config.clustered) {
                fields.splice(5, 0, {
                    name: '__slot:region',
                    title: this.i18n('common.region'),
                    sortField: 'region',
                    width: '100px',
                });
            }

            return fields;
        }

        get tableCss() {
            return {
                table: {
                    ascendingIcon: 'fa fa-arrow-up',
                    descendingIcon: 'fa fa-arrow-down',
                    tableClass: 'table table-bordered table-striped',
                    detailRowClass: 'vuetable-detail-row',
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
