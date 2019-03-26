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
    inserted: function (el) {
        el.focus()
    }
});

const store = require('./store/vuex-store').default;

const routes = [
    {path: '/login', component: Login, name: 'login'},
];

const router = new VueRouter({
    mode: 'history',
    routes,
});

new Vue({
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
