import Vuex from 'vuex';
import Vue from 'vue';

Vue.use(Vuex);
const store = new Vuex.Store({
    state: {
        ngrinder: {},
        activeTip: '',
    },
    mutations: {
        ngrinder(state, ngrinder) {
            state.ngrinder = ngrinder;
        },
        activeTip(state, activeTip) {
            state.activeTip = activeTip;
        },
    },
});

export default store;
