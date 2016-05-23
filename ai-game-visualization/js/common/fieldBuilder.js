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
			elements: undefined,
			style: [
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
						'label': 'data(weight)',
						'edge-text-rotation': 'autorotate'
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