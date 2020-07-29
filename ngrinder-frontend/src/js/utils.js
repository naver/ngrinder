class Utils {
    exists(value) {
        return value !== undefined && value !== null && !isNaN(value);
    }

    isLocale(locale) {
        const localStorageLocale = localStorage.getItem('naveruserlocale');
        const cookieLocale = document.cookie.split(';').find(c => c.indexOf('naveruserlocale=') > -1);
        return locale === (localStorageLocale || cookieLocale || "en");
    }

    isNumeric(num){
        return !isNaN(num)
    }

    equalsIgnoreNewLineChar(str1, str2) {
        if (typeof str1 === 'string' && typeof str2 === 'string') {
            return str1.replace(/\r\n/gm, '\n') === str2.replace(/\r\n/gm, '\n');
        }
        throw new TypeError('Arguments are not string');
    }
}

export default new Utils();
