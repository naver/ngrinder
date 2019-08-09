import Vue from 'vue';
import Vuex, {mapState} from 'vuex';
import VueRouter from 'vue-router';
import VeeValidate from 'vee-validate';
import moment from 'moment';
import axios from 'axios';
import VueSession from 'vue-session';
import bFormSlider from 'vue-bootstrap-slider';
import numFormat from 'vue-filter-number-format';

import Event from 'bus-event.js';
import Login from 'Login.vue';
import Home from 'Home.vue';
import PerfTestList from 'perftest/list/List.vue';
import PerfTestDetail from 'perftest/detail/Detail.vue';
import PerfTestDetailReport from 'perftest/report/DetailReport.vue';
import ScriptList from 'script/List.vue';
import ScriptEditor from 'script/Editor.vue';
import ScriptConsole from 'operation/ScriptConsole.vue';
import SystemConfig from 'operation/SystemConfig.vue';
import Announcement from 'operation/Announcement.vue';
import UserList from 'user/List.vue';
import UserDetail from 'user/UserDetail.vue';
import AgentList from 'agent/List.vue';
import AgentDetail from 'agent/Detail.vue';

import Copyright from 'common/Copyright.vue';
import Navigator from 'common/navigator/Navigator.vue';
import Messages from 'common/Messages.vue';

import VeeValidateInitializer from 'vee-validate-initializer.js';
import Utils from 'utils.js';
import bootbox from 'bootbox';

import 'moment-duration-format';
import 'moment-timezone';
import 'expose-loader?$!expose-loader?jQuery!jquery';
import 'bootstrap/dist/css/bootstrap.css';
import 'bootstrap/dist/js/bootstrap.js';
import 'font-awesome/css/font-awesome.min.css';
import 'bootstrap-slider/dist/css/bootstrap-slider.css';

axios.interceptors.request.use(config => {
    if (typeof config.params === 'undefined') {
        config.params = {};
    }
    if (typeof config.params === 'object') {
        if (typeof URLSearchParams === 'function' && config.params instanceof URLSearchParams) {
            config.params.append('_', Date.now());
        } else {
            config.params._ = Date.now();
        }
    }
    return config;
});

Vue.use(Vuex);
Vue.use(VueSession);
Vue.use(VueRouter);
Vue.use(VeeValidate, {inject: false});
Vue.use(bFormSlider);

Vue.prototype.$bootbox = bootbox;
Vue.prototype.$moment = moment;
Vue.prototype.$http = axios;
Vue.prototype.$utils = Utils;
Vue.prototype.$EventBus = new Vue();
Vue.prototype.$Event = Event;
Vue.prototype.initEventBus = () => Vue.prototype.$EventBus = new Vue();

Vue.prototype.$watchAll = function(props, callback) {
    props.forEach(prop => this.$watch(prop, callback));
};

Vue.directive('focus', {
    inserted: el => el.focus(),
});

Vue.filter('numFormat', numFormat);
Vue.filter('dateFormat', (value, format) => {
    if (value) {
        return moment(new Date(value)).tz(ngrinder.currentUser.timeZone).format(format);
    }
    return '';
});

const store = require('./store/vuex-store').default;

const routes = [
    {path: '/', component: Home, name: 'home'},
    {path: '/home', component: Home, alias: '/'},
    {path: '/login', component: Login, name: 'login'},
    {path: '/perftest', component: PerfTestList, name: 'perfTestList'},
    {path: '/perftest/list', redirect: '/perftest'},
    {path: '/perftest/new', component: PerfTestDetail, name: 'createNewPerftest'},
    {path: '/perftest/quickstart', component: PerfTestDetail, name: 'quickStart', props: true},
    {path: '/perftest/:id', component: PerfTestDetail, name: 'perfTestDetail', props: true},
    {path: '/perftest/:id/detail_report', component: PerfTestDetailReport, name: 'perfTestDetailReport', props: true},
    {path: '/perftest/:id/report', redirect: '/perftest/:id/detail_report'}, // backward compatibility
    {path: '/script', component: ScriptList, name: 'scriptList', alias: ['/script/list/(.*)?']},
    {path: '/script/search', component: ScriptList, name: 'scriptSearch'},
    {path: '/script/new', component: ScriptEditor, name: 'scriptEditor', props: true},
    {path: '/script/detail(.*)?', component: ScriptEditor, name: 'scriptEditorDetail'},
    {path: '/operation/script_console', component: ScriptConsole, name: 'scriptConsole'},
    {path: '/operation/system_config', component: SystemConfig, name: 'systemConfig'},
    {path: '/operation/announcement', component: Announcement, name: 'announcement'},
    {path: '/user', component: UserList, name: 'userList'},
    {path: '/user/new', component: UserDetail, name: 'createNewUser'},
    {path: '/user/:userId', component: UserDetail, name: 'userDetail', props: true},
    {path: '/agent', component: AgentList, name: 'agentList'},
    {path: '/agent/:agentId', component: AgentDetail, name: 'agentDetail', props: true},
];

const router = new VueRouter({
    mode: 'history',
    routes,
});

new Vue({
    components: {
        Copyright,
        Navigator,
        Messages,
    },
    beforeMount: function() {
        this.$store.commit('ngrinder', window.ngrinder);
        VeeValidateInitializer.initValidationMessages();
    },
    computed: {
        ...mapState([
            'ngrinder',
        ]),
    },
    store,
    router,
}).$mount('#ngrinder');
