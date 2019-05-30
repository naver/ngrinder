<template>
    <div class="modal hide fade" id="user-switch-modal" role="dialog">
        <div class="modal-header">
            <button type="button" class="close" data-dismiss="modal" aria-hidden="true">x</button>
        </div>
        <div class="modal-body">
            <div class="form-horizontal modal-content">
                <fieldset>
                    <control-group labelMessageKey="user.switch.title">
                        <select2 v-if="isAdmin" :option="option" type="input" v-model="switchTargetUser" @change="switchUser"></select2>
                        <select2 v-else v-model="switchTargetUser" @change="switchUser">
                            <option value=""></option>
                            <option v-for="user in switchableUsers" :value="user.id" v-text="user.text"></option>
                        </select2>
                    </control-group>
                </fieldset>
            </div>
        </div>
    </div>
</template>

<script>
    import ModalBase from '../../../common/modal/ModalBase.vue';
    import ControlGroup from '../../../common/ControlGroup.vue';
    import Select2 from '../../Select2.vue';
    import Component from 'vue-class-component';

    @Component({
        name: 'userSwitchModal',
        components: { Select2, ControlGroup },
    })
    export default class UserSwitchModal extends ModalBase {
        switchTargetUser = '';
        switchableUsers = [];
        option = {};

        created() {
            if (this.isAdmin) {
                this.option = {
                    minimumInputLength: 2,
                    ajax: {
                        url: '/user/api/switch_options',
                        dataType: 'json',
                        quietMillis: 1000,
                        data: term => {
                            return { keywords: term };
                        },
                        results: data => {
                            return { results: data };
                        }
                    },
                    formatSelection: data => data.text,
                    formatResult: data => data.text,
                };
            } else {
                this.$http.get('/user/api/switch_options')
                    .then(res => this.switchableUsers = res.data)
                    .catch((error) => console.log(error));
            }
        }

        switchUser() {
            document.location.href = `/user/switch?to=${this.switchTargetUser}`;
        }
    }
</script>

<style lang="less">
    #user-switch-modal {
        .modal-content {
            .controls {
                margin-left: 140px;
            }

            .control-label {
                width: 100px;
            }

            .select2-container {
                width: 310px;
            }
        }
    }
</style>

<style lang="less" scoped>
    #user-switch-modal {
        .modal-header {
            border: none;
        }

        .modal-content {
            margin-left: 20px;
            overflow-y: hidden;
        }
    }
</style>
