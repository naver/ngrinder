<script>
    import Component from 'vue-class-component';
    import Vue from 'vue';
    import { mapState } from 'vuex';
    import I18n from '../i18n.js';

    @Component({
        computed: {
            ...mapState(
                ['ngrinder'],
            ),
        },
    })
    export default class Base extends Vue {
        i18n(key, args) {
            return I18n.i18n(key, args);
        }

        get isAdmin() {
            return this.ngrinder.currentUser.role === 'A';
        }

        get isAdminOrSuperUser() {
            return this.ngrinder.currentUser.role === 'A' || this.ngrinder.currentUser.role === 'S';
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

            .popover-content {
                line-height: 18px;
            }
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

    div {
        &.datepicker {
            z-index: 1200;
        }
    }

    @media only screen and (max-height: 800px) {
        body {
            margin-bottom: 45px;
        }
    }
</style>
