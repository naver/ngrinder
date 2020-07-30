import i18next from 'i18next';
import Backend from 'i18next-chained-backend';
import LocalStorageBackend from 'i18next-localstorage-backend';
import XHR from 'i18next-xhr-backend';
import LanguageDetector from 'i18next-browser-languagedetector';

const resources = {
    kr: {
        translation: require('messages_kr.properties'),
    },
    en: {
        translation: require('messages_en.properties'),
    },
    cn: {
        translation: require('messages_cn.properties'),
    },
};

class I18n {
    constructor() {
        const options = {
            keySeparator: "#",
            detection: {
                order: ["cookie", "localStorage"],
                caches: ["localStorage"],
                lookupCookie: "naveruserlocale",
                lookupLocalStorage: "naveruserlocale"
            },
            fallbackLng: "en",
            resources: resources,
            backend: {
                backends: [ LocalStorageBackend, XHR ],
                backendOptions: [
                    { prefix: 'i18next-cache-', expirationTime: 24 * 60 * 60 * 1000 },
                    { loadPath: '/home/api/messagesources/{{lng}}' },
                ],
            }
        };

        i18next
            .use(LanguageDetector)
            .use(Backend)
            .init(options, () => i18next.reloadResources());
    };

    i18n(key, args) {
        return i18next.t(key, args);
    }
}

export default new I18n();
