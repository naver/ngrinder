class Utils {
    exists(value) {
        return value !== undefined && value !== null && !isNaN(value);
    }
}

export default new Utils();
