<template>
    <div class="modal fade" :id="id">
        <div class="modal-dialog">
            <div class="modal-content">
                <div class="modal-header">
                    <a class="close" data-dismiss="modal">&times;</a>
                    <h4 v-text="i18n('perfTest.running.scheduleTitle')"></h4>
                </div>
                <div class="modal-body">
                    <div class="form-horizontal">
                        <fieldset>
                            <div class="control-group">
                                <label class="control-label" v-text="i18n('perfTest.running.schedule')"></label>
                                <div class="controls form-inline">
                                    <input type="text" class="input span2" ref="scheduledDate" :value="schedule.date">&nbsp;
                                    <select id="scheduled_hour" class="select-item" v-model="schedule.hour">
                                        <option v-for="(val, hour) in 24" :value="hour" v-text="hour < 10 ? `0${hour}` : hour"></option>
                                    </select> :
                                    <select id="scheduled_min" class="select-item" v-model="schedule.minute">
                                        <option v-for="(val, min) in 60" :value="min" v-text="min < 10 ? `0${min}` : min"></option>
                                    </select>
                                    <code>HH:MM</code>
                                    <div class="help-inline"></div>
                                </div>
                            </div>
                        </fieldset>
                    </div>
                </div>
                <div class="modal-footer">
                    <a class="btn btn-primary" v-text="i18n('perfTest.running.runNow')" @click="$emit('runNow')"></a>
                    <a class="btn btn-primary" v-text="i18n('perfTest.running.schedule')" @click="runSchedule"></a>
                </div>
            </div>
        </div>
    </div>
</template>

<script>
    import Component from 'vue-class-component';
    import ModalBase from '../../common/modal/ModalBase.vue';
    import '../../../../plugins/datepicker/js/bootstrap-datepicker.js';

    @Component({
        name: 'scheduleModal',
        props: {
            timezoneOffset: {
                type: [Number],
                required: true,
            },
        },
    })
    export default class ScheduleModal extends ModalBase {
        schedule = {
            date: '',
            hour: 0,
            minute: 0,
        };

        mounted() {
            this.initScheduleDate();
        }

        initScheduleDate() {
            let date = this.getBrowserTimeApplyingTimezone();
            let year = date.getFullYear();
            let month = date.getMonth() + 1;
            let day = date.getDate();

            this.schedule.hour = date.getHours();
            this.schedule.minute = date.getMinutes();
            this.schedule.date = `${year}-${(month < 10 ? '0' + month : month)}-${(day < 10 ? '0' + day : day)}`;

            $(this.$refs.scheduledDate).val(this.schedule.date);
            $(this.$refs.scheduledDate).datepicker({
                format : 'yyyy-mm-dd',
            });
        }

        getBrowserTimeApplyingTimezone(time) {
            let date = new Date();
            if (!time) {
                return new Date(date.getTime() + (date.getTimezoneOffset() * 60 * 1000) + this.timezoneOffset);
            } else {
                date = new Date(time - this.timezoneOffset);
                // Now it's browser time reflecting the timezone difference.
                return new Date(Date.UTC(date.getFullYear(), date.getMonth(), date.getDate(), date.getHours(), date.getMinutes()));
            }
        }

        runSchedule() {
            // TODO
        }
    }
</script>

<style lang="less" scoped>
    @import '../../../../plugins/datepicker/css/datepicker.css';

    #schedule-modal {
        .help-inline {
            margin-left: 30px;
        }

        .select-item {
            width: 60px;
        }

        label {
            &.control-label {
                width: 130px;
            }
        }

        div {
            &.controls {
                margin-left: 150px;
            }
        }

        code {
            margin-left: 3px;
        }
    }

    &.fade div {
        display: none;
    }

    &.in div {
        display: block;
    }
</style>
