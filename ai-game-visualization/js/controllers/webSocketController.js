define(['jquery',
		'../common/constants',
		'../common/fieldBuilder'],
	function ($,
	          constants,
	          fieldBuilder) {

		const WS_URL = constants.WS_URL;
		const GREETING = 'The connection is open';
		var ws;
		var gameField;

		function getGameField() {
			if (gameField) {
				return gameField;
			} else {
				setTimeout(getGameField, 100);
			}
		}

		function greetServer() {
			sendMessage(GREETING);
			console.log(GREETING);
		}

		function handleMessage(message) {
			const JSON_MESSAGE = JSON.parse(message.data);
			if (JSON_MESSAGE.elements) {
				gameField = JSON_MESSAGE;
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