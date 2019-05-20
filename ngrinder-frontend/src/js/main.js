import Vue from 'vue';
import Vuex, { mapState } from 'vuex';
import VueRouter from 'vue-router';
import VeeValidate from 'vee-validate';
import moment from 'moment';
import axios from 'axios';
import VueSession from 'vue-session';

import Event from 'bus-event.js';
import Login from 'Login.vue';
import Home from 'Home.vue';
import PerfTestList from 'perftest/list/List.vue';
import PerfTestDetail from 'perftest/detail/Detail.vue';
import ScriptList from 'script/List.vue';
import ScriptEditor from 'script/Editor.vue';
import ScriptConsole from 'operation/ScriptConsole.vue';
import SystemConfig from 'operation/SystemConfig.vue';

import Copyright from 'common/Copyright.vue';
import Navigator from 'common/navigator/Navigator.vue';

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
Vue.use(VeeValidate);

Vue.prototype.$moment = moment;
Vue.prototype.$http = axios;
Vue.prototype.$EventBus = new Vue();
Vue.prototype.$Event = Event;
Vue.prototype.initEventBus = () => Vue.prototype.$EventBus = new Vue();

Vue.prototype.$watchAll = function(props, callback) {
    props.forEach(prop => this.$watch(prop, callback));
};

Vue.directive('focus', {
    inserted: el => el.focus(),
});

Vue.filter('dateFormat', function (value, format) {
    if (value) {
        return moment(new Date(value)).format(format);
    }
});

const store = require('./store/vuex-store').default;

const routes = [
    {path: '/', component: Home, name: 'home'},
    {path: '/home', component: Home, alias: '/'},
    {path: '/login', component: Login, name: 'login'},
    {path: '/perftest', component: PerfTestList, name: 'perfTestList'},
    {path: '/perftest/:id', component: PerfTestDetail, name: 'perfTestDetail'},
    {path: '/script', component: ScriptList, name: 'scriptList', alias: ['/script/list/(.*)?']},
    {path: '/script/search', component: ScriptList, name: 'scriptSearch'},
    {path: '/script/detail(.*)?', component: ScriptEditor, name: 'scriptEditor'},
    {path: '/operation/script_console', component: ScriptConsole, name: 'scriptConsole'},
    {path: '/operation/system_config', component: SystemConfig, name: 'systemConfig'},
];

const router = new VueRouter({
    mode: 'history',
    routes,
});

new Vue({
    components: {
        Copyright,
        Navigator,
    },
    beforeMount: function() {
        this.$store.commit('version', window.ngrinder.version);
        this.$store.commit('config', window.ngrinder.config);
        this.$store.commit('currentUser', window.ngrinder.currentUser);
    },
    computed: {
        ...mapState([
            'version',
            'currentUser',
        ]),
    },
    store,
    router,
}).$mount('#ngrinder');
