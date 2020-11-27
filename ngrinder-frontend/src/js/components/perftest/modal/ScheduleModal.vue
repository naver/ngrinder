<template>
    <div class="modal fade" id="schedule-modal">
        <div class="modal-dialog">
            <div class="modal-content">
                <div class="modal-header">
                    <h4 v-text="i18n('perfTest.running.scheduleTitle')"></h4>
                    <a href="#" class="close" data-dismiss="modal">x</a>
                </div>
                <div class="modal-body">
                    <div class="control-group d-flex justify-content-center align-items-center" :class="{'error': !validation}">
                        <label class="mr-5 mb-0" v-text="i18n('perfTest.running.schedule')"></label>
                        <div>
                            <datepicker :value="schedule.date"
                                        format="yyyy-MM-dd"
                                        @selected="datePickerSelected"
                                        name="scheduleDatePicker">
                            </datepicker>
                            <select class="select-item form-control" v-model="schedule.hour">
                                <option v-for="(val, hour) in 24" :value="hour" v-text="hour < 10 ? `0${hour}` : hour"></option>
                            </select> :
                            <select class="select-item form-control" v-model="schedule.minute">
                                <option v-for="(val, min) in 60" :value="min" v-text="min < 10 ? `0${min}` : min"></option>
                            </select>
                            <code>HH:MM</code>
                        </div>
                    </div>
                    <div v-show="!validation" class="mt-2 text-danger" v-text="i18n('perfTest.message.scheduleDate.error')"></div>
                </div>
                <div class="modal-footer">
                    <button class="btn btn-primary" @click="$emit('run')">
                        <i class="fa fa-play mr-1"></i>
                        <span v-text="i18n('perfTest.running.runNow')"></span>
                    </button>
                    <button class="btn btn-primary" @click="runSchedule">
                        <i class="fa fa-calendar mr-1"></i>
                        <span v-text="i18n('perfTest.running.schedule')"></span>
                    </button>
                </div>
            </div>
        </div>
    </div>
</template>

<script>
    import Component from 'vue-class-component';
    import Datepicker from 'vuejs-datepicker';
    import Dateformat from 'dateformat';
    import ModalBase from '../../common/modal/ModalBase.vue';

    @Component({
        name: 'scheduleModal',
        components: { Datepicker },
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

        validation = true;

        beforeShown() {
            const date = this.getBrowserTimeApplyingTimezone();
            const year = date.getFullYear();
            const month = date.getMonth() + 1;
            const day = date.getDate();

            this.schedule.hour = date.getHours();
            this.schedule.minute = date.getMinutes();
            this.schedule.date = `${year}-${(month < 10 ? `0${month}` : month)}-${(day < 10 ? `0${day}` : day)}`;
        }

        beforeHidden() {
            this.validation = true;
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
            let scheduledTime = new Date(`${this.schedule.date} ${this.schedule.hour}:${this.schedule.minute}:0`);
            scheduledTime = this.getBrowserTimeApplyingTimezone(scheduledTime.getTime());
            this.validation = new Date() <= scheduledTime;
            if (!this.validation) {
                return;
            }
            this.$emit('run', scheduledTime);
        }

        datePickerSelected(date) {
            this.schedule.date = Dateformat(date, 'yyyy-mm-dd');
        }
    }
</script>

<style lang="less">

    #schedule-modal {
        .vdp-datepicker {
            display: inline-block;

            input {
                height: 29px;
                padding-left: 8px;
                color: #495057;
                border: 1px solid #ced4da;
                border-radius: 0.25rem;
            }
        }
    }

</style>

<style lang="less" scoped>

    #schedule-modal {
        .modal-body {
            label {
                color: #666;
                font-weight: bold;
            }

            .text-danger {
                margin-left: 118px;
            }
        }

        .select-item {
            width: 60px;
        }

        code {
            margin-left: 3px;
        }

        input {
            width: 140px;
        }
    }

    .modal-open {
        #schedule-modal {
            top: 10%;
        }
    }
</style>
