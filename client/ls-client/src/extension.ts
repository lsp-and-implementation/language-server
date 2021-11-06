import { execSync, exec, spawnSync } from 'child_process';
import { ChildProcess, spawn } from 'mz/child_process';
import * as vscode from 'vscode';
import * as path from 'path';
import * as net from 'net';
import { LanguageClient, LanguageClientOptions, ServerOptions, StreamInfo } from 'vscode-languageclient/node';

export function activate(context: vscode.ExtensionContext) {
	var logChannel = vscode.window.createOutputChannel("Bal_LSP_and_Impl");
	const main: string = 'com.lspandimpl.launcher.stdio.StdioLauncher';
	const tcpMain: string = 'TCPLauncher'
	logChannel.appendLine("Starting the Ballerina Language Server Extension!");

	getJavaHome().then(val => {
		let javaHome = ((val + '').split('java.home =')[1]).trim();
		logChannel.appendLine(javaHome);
		let excecutable: string = path.join(javaHome, 'bin', 'java');

		let classPath = path.join(__dirname, '..', '*');
		const args: string[] = ['-cp', classPath];
		let ballerinaHome = getBallerinaHome();
		args.push('-Dballerina.home=' + ballerinaHome);
		if (process.env.LSDEBUG === "true") {
			logChannel.appendLine('LSDEBUG is set to "true". Language Server is starting on debug mode');
			args.push('-agentlib:jdwp=transport=dt_socket,server=y,suspend=y,address=5005,quiet=y');
		}

		let serverOptions: ServerOptions = {
			command: excecutable,
			args: [...args, main],
			options: {}
		};

		// ######## Start of TCP connection ########
		// https://github.com/microsoft/vscode-languageserver-node/issues/662
		// https://github.com/felixfbecker/vscode-php-intellisense/blob/master/src/extension.ts
		const tcpServerOptions = () =>
			new Promise<ChildProcess | StreamInfo>((resolve, reject) => {
				// Use a TCP socket because of problems with blocking STDIO
				const server = net.createServer(socket => {
					// 'connection' listener
					logChannel.appendLine('Ballerina Language Server Process Disconnected')
					socket.on('end', () => {
						logChannel.appendLine('Ballerina Language Server Process Disconnected')
					});
					server.close()
					resolve({ reader: socket, writer: socket })
				});
				// Listen on port 9925
				server.listen(9925, '127.0.0.1', () => {
					const childProcess = spawn(excecutable, [...args, tcpMain]);
					childProcess.stderr.on('data', (chunk: Buffer) => {
						const str = chunk.toString();
						logChannel.appendLine('Ballerina Language Server:' + str);
					});
					childProcess.stdout.on('data', (chunk: Buffer) => {
						logChannel.appendLine('Ballerina Language Server:' + chunk + '');
					});
					childProcess.on('exit', (code, signal) => {
						logChannel.appendLine(
							`Language server exited ` + (signal ? `from signal ${signal}` : `with exit code ${code}`)
						);
						if (code !== 0) {
							logChannel.show()
						}
					});
					return childProcess
				});
			});

		// ######## End of TCP Connection ########

		let clientOptions: LanguageClientOptions = {
			// Register the server for ballerina documents
			documentSelector: [{ scheme: 'file', language: 'ballerina' }],
			// Set the initialization options
			initializationOptions: {
				enableDocumentationCodeLenses: false,
			},
			synchronize: { configurationSection: ['ballerina'] },
			outputChannel: logChannel
		};

		let disposable = new LanguageClient('ballerina-lang-client', serverOptions, clientOptions).start();
		// let disposable = new LanguageClient('ballerina-lang-client', tcpServerOptions, clientOptions).start();
		context.subscriptions.push(disposable);
	});
}

export function deactivate() { }

function getJavaHome() {
	let cmd: string;

	if (process.platform == 'win32') {
		cmd = 'java -XshowSettings:properties -version 2>&1 | findstr "java.home"';
	} else {
		cmd = "java -XshowSettings:properties -version 2>&1 > /dev/null | grep 'java.home'";
	}

	return new Promise(async (resolve, reject) => {
		try {
			resolve(execSync(cmd));
		} catch (error) {
			reject(error)
		}
	});
}

function getBallerinaHome() {
	let cmd: string;

	if (process.platform == 'win32') {
		cmd = 'java -XshowSettings:properties -version 2>&1 | findstr "java.home"';
	} else {
		cmd = "bal home";
	}

	try {
		let response = spawnSync('bal', ['home']);
		if (response.stdout.length > 0) {
			return response.stdout.toString().trim();
		}
	} catch (er) {
		if (er instanceof Error) {
			return "Failed"
		}
	}
}
