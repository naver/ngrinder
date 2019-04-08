import Vue from 'vue';
import Vuex from 'vuex';
import VueRouter from 'vue-router';
import VueMoment from 'vue-moment';
import VeeValidate from 'vee-validate';
import moment from 'moment';
import axios from 'axios';
import VueSession from 'vue-session';

import Event from 'bus-event.js';
import Login from 'Login.vue';
import Home from 'Home.vue';
import PerfTestList from 'perftest/list/List.vue';
import PerfTestDetail from 'perftest/detail/Detail.vue';

import Copyright from 'common/Copyright.vue';
import Navigator from 'common/navigator/Navigator.vue';

import { mapState } from 'vuex';

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
Vue.use(VueMoment, {
    moment,
});

Vue.prototype.$http = axios;
Vue.prototype.$EventBus = new Vue();
Vue.prototype.$Event = Event;
Vue.prototype.initEventBus = () => Vue.prototype.$EventBus = new Vue();

Vue.directive('focus', {
    inserted: el => el.focus(),
});

Vue.filter('dateFormat', function(value, format) {
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
}).$mount('#app');
