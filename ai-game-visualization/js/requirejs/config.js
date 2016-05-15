requirejs.config({
    paths: {
        'bootstrap': 'libs/bootstrap',
        'jquery': 'libs/jquery',
        'handlebars': 'libs/handlebars',
        'text': 'libs/text',
        'cytoscape': 'libs/cytoscape'
    },
    map: {
        'ChartScatter': {
            'Chart': 'ChartCore'
        }
    }
});