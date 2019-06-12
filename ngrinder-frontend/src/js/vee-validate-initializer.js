import I18n from 'i18n.js';
import VeeValidate from 'vee-validate';

class VeeValidateInitializer {
    initValidationMessages() {
        const dictionary = {
            required: () => I18n.i18n('common.message.validate.empty'),
            regex: name => {
                switch (name) {
                    case 'domain': return I18n.i18n('perfTest.config.addHost.inputTargetDomain');
                    case 'ip': return I18n.i18n('perfTest.config.addHost.inputTargetIp');
                    case 'folderName': return I18n.i18n('common.message.validate.format');
                    case 'userId': return I18n.i18n('user.info.userId.help');
                    case 'mobilePhone': return I18n.i18n('user.info.phone.help');
                    case 'params': return I18n.i18n('perfTest.message.param');
                }
            },
            email: () => I18n.i18n('user.info.email.help'),
            max: (name, val) => I18n.i18n('common.message.validate.maxLength', {maxLength: val[0]}),
        };

        const messages = {
            en: {
                messages: dictionary,
            },
            kr: {
                messages: dictionary,
            },
            cn: {
                messages: dictionary,
            },
        };

        VeeValidate.Validator.localize(messages);
    }
}

export default new VeeValidateInitializer();
