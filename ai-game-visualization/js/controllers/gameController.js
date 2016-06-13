define([
        '../common/content',
        'cytoscape',
        'jquery',
        'jqueryQtip',
        'cytoscapeQtip'],
    function (content,
              cytoscape,
              $,
              jqQtip,
              cyQtip) {

        var cy;

        function drawField(field) {

            // also get style
            var styleP = content.FIELD_STYLE;

            initCy(field, styleP);

            function initCy(expJson, styleArr) {
                var loading = document.getElementById('loading');
                var elements = expJson.elements;

                loading.classList.add('loaded');

                cy = window.cy = cytoscape({
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
            }
        }

        function setStartNodes(startVertices) {
            const start = cy.$("#" + 'v' + startVertices.start);
            start.select();
            start.unselectify();
            start.qtip({
                content: {
                    text: function () {
                         // start button in the var
                        return $('<button id="start" disabled>START</button>');
                    }
                },
                show: {
                    ready: true,
                    solo: false // hides all others qtips when shown
                },
                hide: {
                    event: false
                },
                // we want to position my qtip {my} corner at the {at} of my target
                position: {
                    my: 'top center',
                    at: 'bottom center'
                },
                style: {
                    classes: 'qtip-bootstrap',
                    tip: {
                        width: 16,
                        height: 8
                    }
                }
            });
            const finish = cy.$("#" + 'v' + startVertices.finish);
            finish.select();
            finish.unselectify();
            finish.qtip({
                content: {
                    text: function () {
                        // start button in the var
                        return $('<button id="end" disabled>FINISH</button>');
                    }
                },
                show: {
                    ready: true,
                    solo: false // hides all others qtips when shown
                },
                hide: {
                    event: false
                },
                // we want to position my qtip {my} corner at the {at} of my target
                position: {
                    my: 'top center',
                    at: 'bottom center'
                },
                style: {
                    classes: 'qtip-bootstrap',
                    tip: {
                        width: 16,
                        height: 8
                    }
                }
            });
        }

        return {
            drawField: drawField,
            setStartNodes: setStartNodes
        }

    });