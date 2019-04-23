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

        .validation-message {
            font-size: 12px;
            color: #b94a48;
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
            width: auto;
            min-width: 300px;
            max-width: 600px;
            max-height: 500px;
        }

        .no-padding {
            padding: 0;
        }

        .fade-enter-active {
            transition-duration: 1s;
            transition-timing-function: ease-in;
        }

        .fade-leave-active {
            transition-duration: .4s;
            transition-timing-function: ease-out;
        }

        .fade-leave-to, .fade-enter {
            max-height: 0;
            overflow: hidden;
        }

        .fade-enter-to, .fade-leave {
            max-height: 100%;
            overflow: hidden;
        }

        .error {
            .select2-choice {
                border-color: #b94a48;
                span {
                    color: #b94a48;
                }
            }
        }

        .span4-5 {
            width: 340px;
        }

        .span3-4 {
            width: 260px;
        }

        .span2-3 {
            width: 180px;
        }
    }
</style>
