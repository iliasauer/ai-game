//  Load RequireJS configuration before any other actions
require(["./requirejs/config"], function() {
    //  App entry point
    require([
        './controllers/mainController',
        './controllers/webSocketController'

    ], function(mainController, webSocketController) {
        mainController.render();
        webSocketController.connectWs();
    });
});