define(['jquery',
		'cytoscape',
		'./util/handlebarsUtil',
		'./util/templateUtil',
		'./util/cssUtil'],
	function ($,
	          cytoscape,
	          hbUtil,
	          templateUtil,
	          cssUtil) {

		var fieldFrame = {
			container: undefined,
			elements: [ // list of graph elements to start with
				{ // node a
					data: { id: 'a' }
				},
				{ // node b
					data: { id: 'b' }
				},
				{ // edge ab
					data: { id: 'ab', source: 'a', target: 'b' }
				}
			],
			style: [ // the stylesheet for the graph
				{
					selector: 'node',
					style: {
						'background-color': '#666',
						'label': 'data(id)'
					}
				},

				{
					selector: 'edge',
					style: {
						'width': 3,
						'line-color': '#ccc',
						'target-arrow-color': '#ccc',
						'target-arrow-shape': 'triangle'
					}
				}
			],
			layout: {
				name: 'grid',
				rows: 1
			}
		};

		function setFieldElement(elem) {
			fieldFrame.container = elem;
		}

		function build() {
			cytoscape(fieldFrame);
		}

		return {
			setFieldElement: setFieldElement,
			build: build
		}
	});