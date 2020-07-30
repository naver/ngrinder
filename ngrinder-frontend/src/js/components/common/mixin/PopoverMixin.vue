<script>
    import { Mixin } from 'vue-mixin-decorator';

    @Mixin
    export default class PopoverMixin {
        initPopover($popover) {
            $popover
                .popover()
                .unbind()
                .on('mouseenter', function () {
                    $(this).popover('show');
                    $('.popover').on('mouseleave', () => $(this).popover('hide'));
                })
                .on('mouseleave', function () {
                    if (!$(this).hasClass('ball')) {
                        $(this).popover('hide');
                        return;
                    }

                    setTimeout(() => {
                        if (!$('.popover:hover').length) {
                            $(this).popover('hide');
                        }
                    }, 50);
                });
        }
    }
</script>
