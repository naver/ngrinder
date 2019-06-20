import Vuex from 'vuex';

export default new Vuex.Store({
    state: {
        ngrinder: {},
        messages: {},
    },
    mutations: {
        ngrinder(state, ngrinder) {
            state.ngrinder = ngrinder;
        },
        messages(state, messages) {
            state.messages = messages;
        }
    },
});
