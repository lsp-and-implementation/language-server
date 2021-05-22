// The module 'vscode' contains the VS Code extensibility API
// Import the module and reference it with the alias vscode in your code below
import { exec, execSync } from 'child_process';
import * as vscode from 'vscode';
import * as path from 'path';
import { LanguageClient, LanguageClientOptions, ServerOptions } from 'vscode-languageclient/node';

export function activate(context: vscode.ExtensionContext) {
	var logChannel = vscode.window.createOutputChannel("LangServerDemo");
	const main: string = 'org.lsp.launcher.stdio.StdioLauncher';
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

		logChannel.appendLine(classPath);

		let clientOptions: LanguageClientOptions = {
			// Register the server for ballerina documents
			documentSelector: [{ scheme: 'file', language: 'ballerina' }],
			initializationOptions: {
				enableDocumentationCodeLenses: false
			}
		};

		let disposable = new LanguageClient('bal-ls-demo', serverOptions, clientOptions).start();
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
