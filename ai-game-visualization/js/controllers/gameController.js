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

        function setTips() {
            cy.elements.qtip({
                text: '',
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
            cy.elements.qtip('api')
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

        function nameSelector(name) {
            return "[name='" + name + "']";
        }

        const figureColors = ['red', 'blue', 'green', 'yellow'];

        function getColor() {
            const index = Math.floor(Math.random() * figureColors.length);
            const color = figureColors[index];
            figureColors.splice(index, 1);
            return color;
        }

        function addPlayerFigure(playerName, startNodeName) {
            const startNode = cy.nodes(nameSelector(startNodeName))[0];
            const x = startNode.position('x');
            const y = startNode.position('y');
            const playerFigure = cy.add({
                group: "nodes",
                data: { id: playerName },
                position: { x: x, y: y },
                classes: 'upperElement'
            });
            const figureColor = getColor();
            playerFigure.style('background-color', figureColor);
            console.log("Figure of " + playerName + " is " + figureColor);
        }

        function movePlayerFigure(playerName, aimElementName) {
            const aimElement = cy.elements(nameSelector(aimElementName))[0];
            var x, y;
            if (aimElement.isEdge()) {
                const nodes = aimElement.connectedNodes();
                const node1 = nodes[0];
                const node2 = nodes[1];
                x = (node1.position('x') + node2.position('x')) / 2;
                y = (node1.position('y') + node2.position('y')) / 2;
            } else {
                x = aimElement.position('x');
                y = aimElement.position('y');
            }
            cy.$('#' + playerName).animate({
                position: { x: x, y: y }
            }, {
                duration: 100
            });
        }

        function showTip(playerName, event) {
            var message;
            switch (event) {
                case 'OBSTACLE':
                    message = 'Stumbled upon bandits!';
                    break;
                case 'BONUS':
                    message = 'Got an acceleration!';
                    break;
                case 'LOSE':
                    message = 'I lost!';
                    break;
                case 'WIN':
                    message = 'I won!';
                    break;
            }
            const playerFigure = cy.$('#' + playerName);
            playerFigure.qtip({
                content: {
                    text: message
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
            setTimeout(function () {
                playerFigure.qtip('api').destroy();
            }, 1500);
        }


        return {
            drawField: drawField,
            setStartNodes: setStartNodes,
            addPlayerFigure: addPlayerFigure,
            movePlayerFigure: movePlayerFigure,
            showTip: showTip
        }

    });
