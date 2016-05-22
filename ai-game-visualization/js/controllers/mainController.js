define(['jquery',
        'text!../../templates/app.hbs',
        '../common/util/handlebarsUtil',
        '../common/util/templateUtil',
        '../common/util/cssUtil',
        './webSocketController',
        '../common/fieldBuilder'],
    function ($,
              appTemplate,
              hbUtil,
              templateUtil,
              cssUtil,
              webSocketController, 
              fieldBuilder) {

        const plainId = templateUtil.plainId;
        const jqId = templateUtil.jqId;
        const jqElem = templateUtil.jqElem;



        function render() {
            function renderApp() {
                hbUtil.compileAndInsertInside(jqId(['app']), appTemplate);
            }
            renderApp();
            webSocketController.connectWs();
        }

        return {
            render: render
        };
    });