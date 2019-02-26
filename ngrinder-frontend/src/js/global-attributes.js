
class GlobalAttributes {
    constructor() {
        this.config = window.nGrinderConfig || {};
    }

    get(key) {
        return this.config[key];
    }

    getOrDefault(key, defaultValue) {
        return this.config[key] || defaultValue;
    }
}

export default new GlobalAttributes();
