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
				{
					group: 'nodes',
					data: { id: 'a' }
				},
				{
					group: 'nodes',
					data: { id: 'b' }
				},
				{ // edge ab
					group: 'edges',
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
						'line-color': '#ccc'
					}
				}
			],
			layout: {
				name: 'grid',
				rows: 10
			}
		};

		function setFieldElement(elem) {
			fieldFrame.container = elem;
		}


		function setFieldData(data) {
			fieldFrame.elements = data;
		}

		function build() {
			cytoscape(fieldFrame);
		}

		return {
			setFieldElement: setFieldElement,
			setFieldData: setFieldData,
			build: build
		}
	});