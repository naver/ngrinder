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

        get contextPath() {
            return this.ngrinder.contextPath;
        }

        get isAdmin() {
            return this.ngrinder.currentUser.role === 'A';
        }

        get isSuperUser() {
            return this.ngrinder.currentUser.role === 'S';
        }

        get isAdminOrSuperUser() {
            return this.ngrinder.currentUser.role === 'A' || this.ngrinder.currentUser.role === 'S';
        }
    }
</script>

<style lang="less">
    @error-color: #d9534f;

    html {
        font-size: 12px !important;

        body {
            font-family: "Helvetica Neue", Helvetica, Arial, sans-serif !important;
            font-size: 12px !important;
        }

        #ngrinder {

            &.hasHBar {
                .__panel {
                    // For Firefox.
                    scrollbar-width: none;

                    // For Internet Explorer and Edge.
                    -ms-overflow-style: none;

                    // For Chrome, Safari, and Opera.
                    &::-webkit-scrollbar {
                        width: 0;
                        height: 0;
                    }
                }
            }

            // vue-scroll style
            .__bar-is-vertical,
            .__bar-is-horizontal {
                background: #c0c0c0 !important;

                &:hover {
                    background: #707070 !important;
                }
            }

            .__rail-is-vertical {
                right: 0 !important;
            }
            // end vue-scroll style

            .container {
                width: 1200px;
                min-width: 1200px;
            }

            .pointer-cursor {
                cursor: pointer;
            }

            .wait-cursor {
                cursor: wait;
            }

            .default-cursor {
                cursor: default;
            }

            .container {
                padding: 0;
            }

            .validation-message {
                color: @error-color;
                line-height: 12px;
                margin-top: 2px;
            }

            .pagination {
                float: right;
                margin: 0;
            }

            button, select, a, i {
                outline: none;
            }

            code {
                border-radius: 2px;
                font-size: 12px;
                padding: 2px 4px;
                color: #d14;
                background-color: #f7f7f9;
                border: 1px solid #e1e1e8;
                white-space: nowrap;
            }

            .display-inline {
                display: inline;
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
                    border-color: @error-color;
                    span {
                        color: @error-color;
                    }
                }

                [data-toggle="dropdown"] {
                    border-color: @error-color;
                    color: @error-color;
                }
            }
        }

        .introjs-helperNumberLayer {
            padding: 0;
            width: 23px;
            height: 23px;
        }

        .vuetable {
            .sort-icon {
                padding-top: 4px;
            }

            &.table-striped tbody > tr:nth-child(odd) > td {
                background-color: #f9f9f9;
            }

            .vuetable-checkboxes, [class*='vuetable-th-checkbox'] {
                input {
                    margin-top: 5px;
                }
            }
        }

        .pagination {
            .page-link {
                cursor: pointer;

                &.active {
                    border-color: #007bff;
                    background: #007bff;
                    color: #fff;

                    &:hover {
                        color: #fff;
                    }
                }
            }

            a {
                &.disabled {
                    opacity: 0.5;
                    pointer-events: none;
                    cursor: default;
                }
            }
        }

        .select2-container {
            .select2-default {
                color: #777 !important;
            }
        }

        .modal-dialog {
            .modal-body {
                word-break: break-all;
            }
        }

        .uneditable-input {
            border-color: #ced4da !important;
            background-color: #fff;
            padding: 6px;
            cursor: text;
        }

        .control-group.error {
            input, select, .input-group-text {
                color: @error-color !important;
                border-color: @error-color !important;
                box-shadow: inset 0 1px 1px rgba(0, 0, 0, 0.075);

                &.input-group-text {
                    background-color: #f2dede;
                }
            }

            input[type="file"] {
                outline-color: @error-color;
            }

            label {
                color: @error-color !important;
            }
        }

        .row {
            margin: 0;
        }

        .form-control {
            display: inline-block;
            padding: 4px 6px;
        }

        .popover {
            width: auto;
            max-width: 600px;
            font-size: 12px;

            .popover-body {
                max-height: 450px;
                line-height: 20px;
                overflow-y: auto;
            }
        }

        .control-label {
            float: left;
            text-align: right;
        }

        .required-mark {
            vertical-align: middle;
            margin-right: 2px;
            color: red;
        }

        div {
            &.datepicker {
                z-index: 1200;
            }
        }
    }
</style>
