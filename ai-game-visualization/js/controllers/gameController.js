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

                createCrossTransitions();
                bindRouters();
            }

            function createCrossTransitions() {
                // because the source data doesn't connect nodes properly, use the cytoscape api to mend it:

                var i, name, nbin;

                cy.startBatch(); //starts batching manually (an manipulation of elements without triggering style calculations or multiple redraw)

                // put nodes in bins based on name
                var nodes = cy.nodes(); // get nodes in the graph matching the specified selector (all nodes if no selector)
                var bin = {}; // Map<String, List<?>> where key = name, value = List of nodes
                var metanames = []; // names that are repeated
                for (i = 0; i < nodes.length; i++) {
                    var node = nodes[i];
                    name = node.data('name');
                    nbin = bin[name] = bin[name] || []; // reference to List of nodes

                    nbin.push(node); // several nodes with the same name ??? yes

                    if (nbin.length === 2) {
                        metanames.push(name); // if at least 2 nodes with the same name then add them to metanames list
                    }
                }

                // connect nodes with cross transitions
                for (i = 0; i < metanames.length; i++) {
                    name = metanames[i];
                    nbin = bin[name]; // list of nodes with the same name

                    for (var j = 0; j < nbin.length; j++) {
                        for (var k = j + 1; k < nbin.length; k++) {
                            var nj = nbin[j]; // the node
                            var nk = nbin[k]; // the next node

                            cy.add({ // add an edge between the node an the next node
                                group: 'edges',
                                data: {
                                    source: nj.id(),
                                    target: nk.id()
                                }
                            });
                        }
                    }

                }

                cy.endBatch(); //.autolock( true ); ends batching manually
            }

            var start, end; // vars for the start node element and the end node element
            var $body = $('body'); // memorize the html body in the variable (to save state???!!!)

            function selectStart(node) {
                clear(); // see clear() description

                $body.addClass('has-start'); // set has-start state for the map

                start = node; // set the start node element

                start.addClass('start'); // add start class to the start node element
            }

            function selectEnd(node) {
                $body.addClass('has-end calc'); // set has-end state for the map and show a loading cover while the way calculation is executed

                end = node; // set the end node element

                cy.startBatch();  //starts batching manually (an manipulation of elements without triggering style calculations or multiple redraw)

                end.addClass('end');

                setTimeout(function () {
                    // A star search (* if no weight function is defined, a constant weight of 1 is used for each edge)
                    var aStar = cy.elements().aStar({
                        root: start,
                        goal: end
                    });

                    if (!aStar.found) { // if the shortest path was not found then show nothing
                        $body.removeClass('calc'); // remove a loading cover because the way calculation is completed
                        clear(); // see clear() description

                        cy.endBatch(); // ends batching manually
                        return;
                    }

                    cy.elements().not(aStar.path).addClass('not-path'); // mark all elements that are not in the path with class as 'not-path'
                    aStar.path.addClass('path'); // mark all elements that are in the path with class as 'path'

                    cy.endBatch(); // ends batching manually

                    $body.removeClass('calc'); // remove a loading cover because the way calculation is completed
                }, 300);
            }

            function clear() {
                $body.removeClass('has-start has-end'); // remove earlier selected start and end
                cy.elements().removeClass('path not-path start end'); // and remove the path from start to end
            }

            function bindRouters() {

                var $clear = $('#clear'); // memorize the clear button in the variable
                $clear.on('click', clear);

                // for each node qtip is defined by a function
                cy.nodes().qtip({
                    content: {
                        text: function () {
                            var $ctr = $('<div class="select-buttons"></div>'); // buttons block in the var
                            var $start = $('<button id="start">START</button>'); // start button in the var
                            var $end = $('<button id="end">END</button>'); // stop button in the var

                            $start.on('click', function () {
                                var n = cy.$('node:selected'); // memorize selected node element in the var

                                selectStart(n); // mark this node element as the start

                                n.qtip('api').hide(); // hide the start qtip
                            });

                            $end.on('click', function () {
                                var n = cy.$('node:selected'); // memorize selected node element in the var

                                selectEnd(n); // mark this node element as the end

                                n.qtip('api').hide(); // hide the end qtip
                            });

                            $ctr.append($start).append($end); // add the start and the end buttons to the button block

                            return $ctr; // return buttons block
                        }
                    },
                    show: {
                        solo: true // hides all others qtips when shown
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
        }

        function setStartNodes(startVertices) {
            cy.$("#" + 'v' + startVertices.start).select();
            cy.$("#" + 'v' + startVertices.finish).select();
        }

        return {
            drawField: drawField,
            setStartNodes: setStartNodes
        }

    });
