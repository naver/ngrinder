import Vuex from 'vuex';
import Vue from 'vue';

Vue.use(Vuex);
const store = new Vuex.Store({
    state: {
        ngrinder: {},
    },
    mutations: {
        ngrinder(state, ngrinder) {
            state.ngrinder = ngrinder;
        },
    },
});

export default store;
