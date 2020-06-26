import Vue from 'vue';
import { mapState } from 'vuex';
import VueRouter from 'vue-router';
import VeeValidate from 'vee-validate';
import moment from 'moment';
import axios from 'axios';
import VueLocalStorage from 'vue-localstorage';
import VueShortkey from 'vue-shortkey';
import bFormSlider from 'vue-bootstrap-slider/es';
import numFormat from 'vue-filter-number-format';
import vuescroll from 'vuescroll';
import numeral from 'numeral';
import store from 'store/vuex-store.js';
import { AllHtmlEntities } from 'html-entities';

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
import AnnouncementEditor from 'operation/Announcement.vue';
import UserList from 'user/List.vue';
import AgentList from 'agent/List.vue';
import AgentDetail from 'agent/Detail.vue';

import Copyright from 'common/Copyright.vue';
import Navigator from 'common/navigator/Navigator.vue';
import Messages from 'common/Messages.vue';
import Announcement from 'common/Announcement.vue';
import Tip from 'common/Tip.vue';

import VeeValidateInitializer from 'vee-validate-initializer.js';
import Utils from 'utils.js';
import BootBox from 'boot-box.js';

import 'moment-duration-format';
import 'moment-timezone';
import 'expose-loader?$!expose-loader?jQuery!jquery';
import 'bootstrap/dist/css/bootstrap.css';
import 'bootstrap/dist/js/bootstrap.js';
import 'font-awesome/css/font-awesome.min.css';
import 'bootstrap-slider/dist/css/bootstrap-slider.css';

let axiosInstance = axios.create({
    baseURL: window.ngrinder.contextPath,
});

axiosInstance.interceptors.request.use(config => {
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

Vue.use(VueShortkey);
Vue.use(vuescroll);
Vue.use(VueLocalStorage, {
    name: 'localStorage',
    bind: true,
});
Vue.use(VueRouter);
Vue.use(VeeValidate, {
    inject: false,
    useConstraintAttrs: false,
});
Vue.use(bFormSlider);

Vue.prototype.$htmlEntities = AllHtmlEntities;
Vue.prototype.$bootbox = BootBox;
Vue.prototype.$moment = moment;
Vue.prototype.$http = axiosInstance;
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

Vue.directive('htmlBindScript', (el, binding) => {
    $(el).html(binding.value);
});

Vue.directive('visible', (el, binding) => {
    el.style.visibility = !!binding.value ? 'visible' : 'hidden';
});

Vue.filter('numFormat', numFormat(numeral));
Vue.filter('dateFormat', (value, format) => {
    if (value) {
        return moment(new Date(value)).tz(ngrinder.currentUser.timeZone).format(format);
    }
    return '';
});
Vue.filter('durationFormat', (value, format) => {
    if (value) {
        return moment.duration(value).format(format, { trim: false });
    }
    return '';
});

const routes = [
    {path: '/', component: Home, name: 'home'},
    {path: '/home', component: Home, alias: '/'},
    {path: '/login', component: Login, name: 'login'},
    {path: '/perftest', component: PerfTestList, name: 'perfTestList'},
    {path: '/perftest/list', redirect: '/perftest'},
    {path: '/perftest/new', component: PerfTestDetail, name: 'createNewPerfTest', props: true},
    {path: '/perftest/quickstart', component: PerfTestDetail, name: 'quickStart', props: true},
    {
        path: '/perftest/:id', component: PerfTestDetail, name: 'perfTestDetail', props: true,
        beforeEnter: (to, from, next) => {
            to.params.isAdmin = window.ngrinder.currentUser.role === 'A';
            next();
        }
    },
    {path: '/perftest/:id/detail_report', component: PerfTestDetailReport, name: 'perfTestDetailReport', props: true},
    {path: '/perftest/:id/report', redirect: '/perftest/:id/detail_report'}, // backward compatibility
    {path: '/script/list/:remainedPath(.*)?', component: ScriptList, name: 'scriptList', alias: ['/script'], props: true},
    {path: '/script/search', component: ScriptList, name: 'scriptSearch', props: true},
    {path: '/script/new', component: ScriptEditor, name: 'scriptEditor', props: true},
    {path: '/script/detail/:remainedPath(.*)?', component: ScriptEditor, name: 'scriptEditorDetail', props: true},
    {path: '/operation/script_console', component: ScriptConsole, name: 'scriptConsole'},
    {path: '/operation/system_config', component: SystemConfig, name: 'systemConfig'},
    {path: '/operation/announcement', component: AnnouncementEditor, name: 'announcementEditor'},
    {path: '/user', component: UserList, name: 'userList'},
    {path: '/agent', component: AgentList, name: 'agentList'},
    {path: '/agent/:ip/:name', component: AgentDetail, name: 'agentDetail', props: true},
];

const router = new VueRouter({
    mode: 'history',
    base: window.ngrinder.contextPath,
    routes,
});

router.beforeEach((to, from, next) => {
    $('[data-toggle="popover"]').popover('hide');
    next();
});

new Vue({
    components: {
        Copyright,
        Navigator,
        Messages,
        Announcement,
        Tip,
    },
    beforeMount: function() {
        this.$store.commit('ngrinder', window.ngrinder);
        this.$store.commit('activeTip', '');
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
