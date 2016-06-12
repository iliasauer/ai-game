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
    '../common/content',
    'cytoscape', 
    'jquery', 
    'jqueryQtip', 
    'cytoscapeQtip',
    './webSocketController'],
    function (templateUtil,
              appTemplate,
              hbUtil,
              content,
              cytoscape, 
              $, 
              jqQtip,
              cyQtip,
              webSocketController) {

        const plainId = templateUtil.plainId;
        const jqId = templateUtil.jqId;
        const jqElem = templateUtil.jqElem;

        function render() {
            renderApp();
            renderField();
        }

        function renderApp() {
            hbUtil.compileAndInsertInside(jqId(['app']), appTemplate);
        }

        function renderField() {

            // get exported json from cytoscape desktop
            var graphP = content.FIELD_CONTENT;

            // also get style
            var styleP = content.FIELD_STYLE;

            initCy(graphP, styleP);

            function initCy(expJson, styleArr) {
                var loading = document.getElementById('loading');
                // var expJson = then[0];
                // var styleJson = then[1];
                var elements = expJson.elements;

                loading.classList.add('loaded');

                var cy = window.cy = cytoscape({
                    // common
                    container: document.getElementById('cy'),
                    elements: elements,
                    style: styleArr,
                    layout: {name: 'preset'}, // preset because node positions are already specified in elements JSON
                    // viewport
                    boxSelectionEnabled: false,
                    selectionType: 'single', // for 'single' a previously selected element becomes unselected otherwise - 'additive'
                    // render
                    motionBlur: true // this is beautiful but can decrease the performance
                });
                
                bindRouters();
            }

            var start, end;
            var $body = $('body');

            function selectStart(node) {
                clear();

                $body.addClass('has-start');

                start = node;

                start.addClass('start');
            }

            function selectEnd(node) {
                $body.addClass('has-end calc');

                end = node;

                cy.startBatch();

                end.addClass('end');

                setTimeout(function () {
                    var aStar = cy.elements().aStar({
                        root: start,
                        goal: end,
                        weight: function (e) {
                            if (e.data('is_walking')) {
                                return 0.25; // assume very little time to walk inside stn
                            }

                            return e.data('is_bullet') ? 1 : 3; // assume bullet is ~3x faster
                        }
                    });

                    if (!aStar.found) {
                        $body.removeClass('calc');
                        clear();

                        cy.endBatch();
                        return;
                    }

                    cy.elements().not(aStar.path).addClass('not-path');
                    aStar.path.addClass('path');

                    cy.endBatch();

                    $body.removeClass('calc');
                }, 300);
            }

            function clear() {
                $body.removeClass('has-start has-end');
                cy.elements().removeClass('path not-path start end');
            }

            function bindRouters() {

                var $clear = $('#clear');

                cy.nodes().qtip({
                    content: {
                        text: function () {
                            var $ctr = $('<div class="select-buttons"></div>');
                            var $start = $('<button id="start">START</button>');
                            var $end = $('<button id="end">END</button>');

                            $start.on('click', function () {
                                var n = cy.$('node:selected');

                                selectStart(n);

                                n.qtip('api').hide();
                            });

                            $end.on('click', function () {
                                var n = cy.$('node:selected');

                                selectEnd(n);

                                n.qtip('api').hide();
                            });

                            $ctr.append($start).append($end);

                            return $ctr;
                        }
                    },
                    show: {
                        solo: true
                    },
                    position: {
                        my: 'top center',
                        at: 'bottom center',
                        adjust: {
                            method: 'flip'
                        }
                    },
                    style: {
                        classes: 'qtip-bootstrap',
                        tip: {
                            width: 16,
                            height: 8
                        }
                    }
                });

                $clear.on('click', clear);
            }
        }

        return {
            render: render
        }
    });
