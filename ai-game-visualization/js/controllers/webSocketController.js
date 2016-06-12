define(['jquery',
		'../common/constants',
		'./gameController'],
	function ($,
	          constants, 
	          gameController) {

		const WS_URL = constants.WS_URL;
		const GREETING = 'The connection is open';
		var ws;
		var gameField;
		var startNodes;

		function getGameField() {
			return gameField;
		}

		function greetServer() {
			sendMessage(GREETING);
			console.log(GREETING);
		}

		function handleMessage(message) {
			const JSON_MESSAGE = JSON.parse(message.data);
			if (JSON_MESSAGE.elements) {
				gameField = JSON_MESSAGE;
				gameController.drawField(gameField);
			} else {
				if (JSON_MESSAGE.start !== undefined) {
					startNodes = JSON_MESSAGE;
					gameController.setStartNodes(startNodes);
				}
			}
		}

		function handleServerError(error) {
			console.log('An error occurred on the server')
		}

		function handleClose() {
			console.log('The connection was closed')
		}
		
		function sendMessage(message) {
			if (ws) {
				ws.send(message);
			} else {
				console.log('Failed to send message to the server');
			}
		}
		
		function connectWs() {

			if (!ws) {

				ws = new WebSocket(WS_URL);
				
				ws.onopen = greetServer;

				ws.onclose = handleClose;

				ws.onerror = handleServerError;

				ws.onmessage = handleMessage;
			}
		}

		return {
			connectWs: connectWs,
			sendMessage: sendMessage,
			gameField: getGameField
		}

	});