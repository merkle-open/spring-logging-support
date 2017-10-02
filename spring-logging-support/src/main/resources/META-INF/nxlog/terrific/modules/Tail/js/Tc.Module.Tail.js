(function ($) {
    Tc.Module.Tail = Tc.Module.extend({

        url: 'logs/tail?filename=',
        delay: 0,

        file: null,
        polling: false,


        on: function (callback) {
            var self = this;

            if (self.$ctx.data('url')) {
                self.url = self.$ctx.data('url');
            }
            if (self.$ctx.data('delay')) {
                self.delay = self.$ctx.data('delay');
            }
            if (self.$ctx.data('config-url')) {
                var configUrl = self.$ctx.data('config-url');
                $.getJSON(configUrl, function (data) {
                    self.url = data.url;
                    callback();
                });
            }
            else {
                callback();
            }
        },

        onTail: function (data) {
            var self = this;
            data = data || {};
            if (data.file != self.file) {
                self.polling = false;
                var $lines = self.$ctx.find('.lines');
                var id = data.file.replace(/\W/g, "");
                $lines.attr('id', id);
                $lines.empty();
            }
            if (!self.polling) {
                self.polling = true;
                self.file = data.file;
                self.poll(data.file, self, data.start);
            }

        },

        onStop: function (data) {
            var self = this;
            self.polling = false;
            self.file = '';
        },

        onClear: function (data) {
            var self = this;
            self.$ctx.find('.lines').empty();
        },

        poll: function (file, self, start) {
            if (self.polling && file == self.file) {
                var url = self.url + file + (start > 0 ? "&start=" + start : "");
                $.getJSON(url, function (data) {
                    if (data.length > 0) {
                        var filtered = data.filter(function (item) {
                            return item.indexOf(self.url) == -1;
                        });
                        var id = file.replace(/\W/g, "")
                        var $lines = self.$ctx.find('#' + id);
                        $.each(filtered,function(){
                            $lines.append(this).append('\n');
                        })

                    }
                    $('html, body').animate({scrollTop: $(document).height()}, 0);
                    setTimeout(self.poll(file, self, 0), self.delay); // start only relevant for first call, use delay if configured.
                });
            }
        }
    });
})(Tc.$);