import Vuex from 'vuex';

export default new Vuex.Store({
    state: {
        ngrinder: {},
    },
    mutations: {
        ngrinder (state, ngrinder) {
            state.ngrinder = ngrinder;
        },
    },
});
