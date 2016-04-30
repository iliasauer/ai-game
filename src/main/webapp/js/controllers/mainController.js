define(['jquery',
        'text!../../templates/app.hbs',
        '../util/handlebarsUtil',
        '../util/templateUtil',
        '../util/cssUtil',
        './webSocketController'],
    function ($,
              appTemplate,
              hbUtil,
              templateUtil,
              cssUtil,
              webSocketController) {

        const plainId = templateUtil.plainId;
        const jqId = templateUtil.jqId;
        const jqElem = templateUtil.jqElem;



        function render(prerunChartArr) {
            function renderApp() {
                hbUtil.compileAndInsertInside(jqId(['app']), appTemplate);
            }


            renderApp();
            // webSocketController.connectWs();
        }

        return {
            render: render
        };
    });