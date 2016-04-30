//  Load RequireJS configuration before any other actions
require(["./requirejs/config"], function() {
    //  App entry point
    require([
        'jquery',
        './controllers/mainController'
        
    ], function($, mainController) {
        $.get('/main', function () {
            mainController.render();
        });
    });
});
