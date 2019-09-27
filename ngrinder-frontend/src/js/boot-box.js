import bootbox from 'bootbox';

class BootBox {
    confirm(options) {
        return bootbox.confirm(getOptions(options));
    }

    alert(options) {
        return bootbox.alert(options);
    }
}

const getOptions = options => {
    if (options.onConfirm || options.onCancel) {
        const onConfirm = options.onConfirm || noop;
        const onCancel = options.onCancel || noop;

        return {
            ...options, callback: result => result ? onConfirm() : onCancel()
        };
    }
    else {
        return options;
    }
};

const noop = () => {
};

export default new BootBox();
