class Utils {
    exists(value) {
        return value !== undefined && value !== null && !isNaN(value);
    }

    isLocale(locale) {
        return document.cookie.split(';')
            .find(c => c.indexOf('naveruserlocale=') > -1)
            .split('=')[1] === locale;
    }
}

export default new Utils();
