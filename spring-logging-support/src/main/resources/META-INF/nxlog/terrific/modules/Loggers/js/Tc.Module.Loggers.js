(function($) {
    Tc.Module.Loggers = Tc.Module.extend({

        tplLoggers  : null,
        urlLoggers  : 'config/loggers',
        data	    : null,
        filter		: null,

        on : function(callback){
            var self = this;

            if (this.$ctx.data('listUrl')){
                self.urlLoggers = this.$ctx.data('listUrl');
            }

            self.tplLoggers = doT.template(this.$ctx.find('.tpl-loggers').text());
            callback();
        },

        after : function(){
            var self = this;
            $.getJSON(self.urlLoggers, function(data){
                self.data = data;
                self.renderData(self);
            });
        },

        onFilter: function(data){
            var self = this;
            self.filter = data;
            self.renderData(self);
        },

        renderData : function (self){
            self.$ctx.find('.data').empty();

            var filtered = [];
            if (self.filter && self.filter.value && self.filter.value != ''){
                if ( self.filter.value && self.filter.value != ''){
                    $.each(self.data,function(){
                        var criteria = this[self.filter.field];
                        criteria = criteria || '';
                        if ( self.filter.type == 'contains'){
                            if (criteria.indexOf(self.filter.value) != -1 ){
                                filtered.push(this);
                            }
                        }
                        else if (self.filter.type == 'begins'){
                            if (criteria.indexOf(self.filter.value) == 0){
                                filtered.push(this);
                            }
                        }
                    });
                }
            }
            else{
                filtered = self.data;
            }


            self.$ctx.find('.data').append(self.tplLoggers(filtered));
            self.$ctx.find('.level').click(function(){
                var url = $(this).attr('href');
                self.changeLevel(url,self);
                return false;
            });
        },

        changeLevel : function(url,self){
            $.getJSON(url, function(data){
                self.data = data;
                self.renderData(self);
            });
        }
    });
})(Tc.$);