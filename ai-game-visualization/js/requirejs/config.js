requirejs.config({
    paths: {
        'bootstrap': 'libs/bootstrap',
        'jquery': 'libs/jquery',
        'handlebars': 'libs/handlebars',
        'text': 'libs/text',
        'ChartCore': 'libs/ChartCore',
        'ChartFork': 'libs/ChartFork',
        'ChartScatter': 'libs/ChartScatter'
    },
    map: {
        'ChartScatter': {
            'Chart': 'ChartCore'
        }
    }
});