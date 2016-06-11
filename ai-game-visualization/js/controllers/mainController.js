define(['jquery',
		'jqueryQtip',
		'text!../../templates/app.hbs',
		'../common/util/handlebarsUtil',
		'../common/util/templateUtil',
		'../common/util/cssUtil',
		'./webSocketController',
		'../common/fieldBuilder'],
	function ($,
	          jqQtip,
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
			$('#game-header').qtip({
				content: {
					text: 'My common piece of text here'
				}
			});
			webSocketController.connectWs();
		}

		return {
			render: render
		};
	});