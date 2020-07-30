export default function (user) {
    if (!user) {
        return '';
    } else if (!user.email) {
        return `${user.userName} (${user.userId})`;
    } else {
        return `${user.userName} (${user.email} / ${user.userId})`;
    }
};

