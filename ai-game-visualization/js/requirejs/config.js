requirejs.config({
    paths: {
        'bootstrap': 'libs/bootstrap',
        'jquery': 'libs/jquery',
        'jqueryQtip': 'libs/jquery-qtip',
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