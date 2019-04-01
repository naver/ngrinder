<script>
    import Component from 'vue-class-component';
    import Vue from 'vue';
    import I18n from '../i18n.js';
    import { mapState } from 'vuex';

    @Component({
        computed: {
            ...mapState([
                'version',
                'config',
                'currentUser',
            ]),
        },
    })
    export default class Base extends Vue {
        i18n(key, args) {
            return I18n.i18n(key, args);
        }

        exist(value) {
            return typeof value !== 'undefined';
        }

        get isAdmin() {
            return this.currentUser.role === 'A';
        }

        get isAdminOrSuperUser() {
            return this.currentUser.role === 'A' || this.currentUser.role === 'S';
        }
    }
</script>

<style lang="less">
    #ngrinder-main-content-wrapper {
        .clickable {
            cursor: pointer;
        }

        .pagination {
            float: right;
            margin: 0;
        }

        button, select, a, i {
            outline: none;
        }

        .popover {
            width: auto;
            min-width: 200px;
            max-width: 600px;
            max-height: 500px;
        }

        .display-inline {
            display: inline;
        }

        .hide {
            display: none;
        }

        .popover {
            width: 300px;
        }

        .no-padding {
            padding: 0;
        }
    }
</style>
