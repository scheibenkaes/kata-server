var plot;
$(function () {
    var dist = {};
    
    $.getJSON("/data/dist", function (data) {
        dist.ticks = data.ticks;
        dist.data = data.data;

        plot = $.jqplot('chart', dist.data, {
            legend: {show: true, placement: 'outsideGrid'},
            seriesDefaults: {
                                renderer: $.jqplot.BarRenderer,
             rendererOptions: {fillToZero: false},
             pointLabels: {show:true, location: 'n'}
                
                            },


             axes: {
                       xaxis: {
                                  renderer: $.jqplot.CategoryAxisRenderer,
             ticks: dist.ticks
                              },

             yaxis: {
                        min: 0,
                        pad: 1.05,
             tickOptions: {formatString : '%d'}
                    }

                   }
        });
    });
});

