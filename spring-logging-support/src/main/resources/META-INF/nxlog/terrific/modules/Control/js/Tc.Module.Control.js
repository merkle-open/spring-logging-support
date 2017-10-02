(function ($) {
    Tc.Module.Control = Tc.Module.extend({

        url: '',
        tpl: null,

        on: function (callback) {
            var self = this;
            if (self.$ctx.data('opts-url')) {
                self.url = self.$ctx.data('opts-url');
                self.tpl = doT.template(self.$ctx.find('.tpl-opts').text());
                $.getJSON(self.url, function (data) {
                    self.$ctx.find('.opts').append(self.tpl(data));
                    callback();
                });
            }
            else {
                callback();
            }
        },

        after: function () {
            var self = this;

            self.$ctx.find('.ctrl').click(function () {
                self.performAction(this, self);
            });

            $('[data-href]', self.$ctx).click(function () {
                var prefix = $(this).attr('data-href');
                var filename = $('[data-field="file"]', self.$ctx).val();
                window.location.href = prefix + filename;
            });

            self.$ctx.find('form').submit(function () {
                self.performAction(this, self);
                return false;
            });
            self.$ctx.find('.input[type=text][data-action]').keyup(function () {
                self.performAction(this, self);
            });
            self.$ctx.find('select.input[data-action]').change(function () {
                self.performAction(this, self);
            });
        },

        performAction: function (element, self) {
            var prefix = $(element).attr('data-prefix');
            prefix = prefix || '';
            var message = {};
            self.$ctx.find('.input').each(function () {
                var field = $(this).attr('data-field');
                var value = $(this).val();
                message[field] = prefix + value;
            });
            self.fire($(element).attr('data-action'), message, '1');
        }
    });
})(Tc.$);