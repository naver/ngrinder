<template>
    <div class="modal hide fade" id="user-switch-modal">
        <div class="modal-dialog">
            <div class="modal-content border-0">
                <div class="modal-header border-0 pb-2">
                    <button type="button" class="close" data-dismiss="modal" aria-hidden="true">x</button>
                </div>
                <div class="modal-body pt-0">
                    <fieldset>
                        <control-group labelMessageKey="user.switch.title">
                            <select2 v-if="isAdmin" :option="option" type="input" v-model="switchTargetUserId" @change="switchUser"></select2>
                            <select2 v-else v-model="switchTargetUserId" @change="switchUser">
                                <option value=""></option>
                                <option v-for="user in switchableUsers" :value="user.userId">{{ user | userDescription }}</option>
                            </select2>
                        </control-group>
                    </fieldset>
                </div>
            </div>
        </div>
    </div>
</template>

<script>
    import Component from 'vue-class-component';
    import ModalBase from '../../../common/modal/ModalBase.vue';
    import ControlGroup from '../../../common/ControlGroup.vue';
    import Select2 from '../../Select2.vue';
    import userDescription from '../../filter/UserDescriptionFilter';


    @Component({
        name: 'userSwitchModal',
        components: { Select2, ControlGroup },
        filters: { userDescription },
    })
    export default class UserSwitchModal extends ModalBase {
        switchTargetUserId = '';
        switchableUsers = [];
        option = {};

        created() {
            if (this.isAdmin) {
                this.option = {
                    minimumInputLength: 2,
                    ajax: {
                        url: `${this.contextPath}/user/api/switch_options`,
                        dataType: 'json',
                        quietMillis: 1000,
                        data: term => ({ keywords: term }),
                        results: users => {
                            const select2Data = users.map(user => ({
                                id: user.userId,
                                text: userDescription(user),
                            }));
                            return { results: select2Data };
                        },
                    },
                    formatSelection: data => data.text,
                    formatResult: data => data.text,
                };
            } else {
                this.$http.get('/user/api/switch_options')
                    .then(res => this.switchableUsers = res.data);
            }
        }

        switchUser() {
            document.location.href = `${this.contextPath}/user/switch?to=${this.switchTargetUserId}`;
        }
    }
</script>

<style lang="less" scoped>
    #user-switch-modal {
        .modal-dialog {
            margin-top: 80px;

            .modal-content {
                overflow-y: hidden;
            }
        }
    }
</style>

<style lang="less">
    #user-switch-modal {
        .modal-content {
            .control-group {
                display: flex;
                align-items: center;
                margin-bottom: 20px;

                .control-label {
                    margin-right: 20px;
                }
            }

            .control-label {
                width: 100px;
                color: #666;
                font-weight: bold;
            }

            .select2-container {
                width: 310px;
            }
        }
    }
</style>
