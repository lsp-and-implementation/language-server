// The module 'vscode' contains the VS Code extensibility API
// Import the module and reference it with the alias vscode in your code below
import { exec, execSync } from 'child_process';
import {ChildProcess, spawn } from 'mz/child_process';
// import * as cp from 'child_process';
// import ChildProcess = cp.ChildProcess;
import * as vscode from 'vscode';
import * as path from 'path';
import * as net from 'net';
import { LanguageClient, LanguageClientOptions, ServerOptions, StreamInfo, ChildProcessInfo } from 'vscode-languageclient/node';

export function activate(context: vscode.ExtensionContext) {
	var logChannel = vscode.window.createOutputChannel("LangServerDemo");
	const main: string = 'org.lsp.launcher.stdio.StdioLauncher';
	const tcpMain: string = 'org.lsp.launcher.tcp.TCPLauncher'
	logChannel.appendLine("Starting the Demo Ballerina Language Server Extension!");

	getJavaHome().then(val => {
		let javaHome = ((val + '').split('java.home =')[1]).trim();
		logChannel.appendLine(javaHome);
		let excecutable: string = path.join(javaHome, 'bin', 'java');

		let classPath = path.join(__dirname, '..', '*');
		const args: string[] = ['-cp', classPath];
		let BAL_HOME = "/Users/nadeeshaan/Development/BalWS/jballerina-tools-2.0.0-beta.2-SNAPSHOT";
		args.push('-Dballerina.home=' + BAL_HOME);
		// if (process.env.LSDEBUG === "true") {
		logChannel.appendLine('LSDEBUG is set to "true". Language Server is starting on debug mode');
		args.push('-agentlib:jdwp=transport=dt_socket,server=y,suspend=y,address=5005,quiet=y');

		// }

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
					console.log('PHP process connected')
					socket.on('end', () => {
						console.log('PHP process disconnected')
					});
					server.close()
					resolve({ reader: socket, writer: socket })
				});
				// Listen on random port
				server.listen(9925, '127.0.0.1', () => {
					// The server is implemented in PHP
					const childProcess = spawn(excecutable, [...args, tcpMain]);
					childProcess.stderr.on('data', (chunk: Buffer) => {
						const str = chunk.toString()
						console.log('PHP Language Server:', str)
						// client.outputChannel.appendLine(str)
					});
					childProcess.stdout.on('data', (chunk: Buffer) => {
					    console.log('PHP Language Server:', chunk + '');
					});
					childProcess.on('exit', (code, signal) => {
						logChannel.appendLine(
							`Language server exited ` + (signal ? `from signal ${signal}` : `with exit code ${code}`)
						)
						if (code !== 0) {
							logChannel.show()
						}
					})
					return childProcess
				})
			})

		// ######## End of TCP Connection ########

		logChannel.appendLine(classPath);

		let clientOptions: LanguageClientOptions = {
			// Register the server for ballerina documents
			documentSelector: [{ scheme: 'file', language: 'ballerina' }],
			initializationOptions: {
				enableDocumentationCodeLenses: false,
				synchronize: { configurationSection: ['editor'] },
			}
		};

		// let disposable = new LanguageClient('bal-ls-demo', serverOptions, clientOptions).start();
		let disposable = new LanguageClient('bal-ls-demo', tcpServerOptions, clientOptions).start();
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
