/*
 This demo visualises the railway stations in Tokyo (東京) as a graph.

 This demo gives examples of

 - loading elements via ajax
 - loading style via ajax
 - using the preset layout with predefined positions in each element
 - using motion blur for smoother viewport experience
 - using `min-zoomed-font-size` to show labels only when needed for better performance
 */
define([
        '../common/util/templateUtil',
        'text!../../templates/app.hbs',
        '../common/util/handlebarsUtil',
        'jquery'],
    function (templateUtil,
              appTemplate,
              hbUtil,
              $) {

        const plainId = templateUtil.plainId;
        const jqId = templateUtil.jqId;
        const jqElem = templateUtil.jqElem;

        function render() {
            renderApp();
        }
        
        function renderApp() {
            hbUtil.compileAndInsertInside(jqId(['app']), appTemplate);
        }

        return {
            render: render
        }
    });
