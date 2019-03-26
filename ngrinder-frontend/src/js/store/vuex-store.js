import Vuex from 'vuex';

export default new Vuex.Store({
    state: {
        version: '',
        currentUser: {},
        config: {},
    },
    mutations: {
        version (state, version) {
            state.version = version;
        },
        config (state, config) {
            state.config = config;
        },
        currentUser (state, user) {
            state.currentUser = user;
        },
    },
});
