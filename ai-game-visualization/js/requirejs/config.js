requirejs.config({
    paths: {
        'bootstrap': 'libs/bootstrap',
        'jquery': 'libs/jquery',
        'handlebars': 'libs/handlebars',
        'text': 'libs/text',
        'ChartCore': 'libs/ChartCore',
        'ChartFork': 'libs/ChartFork',
        'ChartScatter': 'libs/ChartScatter',
        'cytoscape': 'libs/cytoscape'
    },
    map: {
        'ChartScatter': {
            'Chart': 'ChartCore'
        }
    }
});