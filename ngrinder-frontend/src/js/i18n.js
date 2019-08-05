import i18next from 'i18next';
import jqueryI18next from 'jquery-i18next';
import Cache from 'i18next-localstorage-cache';
import LanguageDetector from 'i18next-browser-languagedetector';

function replaceArgs(msgs) {
    for (const each in msgs) {
        const msg = msgs[each];
        if (msg !== null) {
            msgs[each] = msg.replace(/\{([0-9])\}/g, "{{arg$1}}");
        }
    }
    return msgs;
}

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
            resources: resources
        };

        i18next.use(Cache).use(LanguageDetector).init(options, () => {
            jqueryI18next.init(i18next, $);
            $(document.body).localize();
        });
    };

    i18n(key, args) {
        return i18next.t(key, args);
    }
}

export default new I18n();
