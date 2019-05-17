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

        getShortenString(str, start, end) {
            if (typeof(start) === 'undefined') {
                start = 0;
            }
            if (typeof(end) === 'undefined') {
                end = 20;
            }
            if (str.length >= end) {
                str = str.substr(start, end - 4);
                str += '...';
            }
            return str;
        }

        formatNetwork(format, value) {
            value = value || 0;
            if (value < 1024) {
                return `${value.toFixed(1)}B `;
            } else if (value < 1048576) { //1024 * 1024
                return `${(value / 1024).toFixed(1)}K `;
            } else {
                return `${(value / 1048576).toFixed(2)}M `;
            }
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

    div{
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
