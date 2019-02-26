import Vuex from 'vuex';

export default new Vuex.Store({
    state: {
        nGrinderVersion: '',
    },
    mutations: {
        nGrinderVersion (state, version) {
            state.nGrinderVersion = version;
        },
    },
});
