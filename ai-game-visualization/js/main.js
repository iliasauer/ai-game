//  Load RequireJS configuration before any other actions
require(["./requirejs/config"], function() {
    //  App entry point
    require([
        './controllers/mainController'

    ], function(mainController) {
        mainController.render();
    });
});