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
}

export default new Utils();
