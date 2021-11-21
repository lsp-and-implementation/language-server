# Language Server and Implementation
This repository contains the source code for the example implementation of a language server referenced in the book \title of the book\.
This language server implementation is associated with an example client implementation as well. The referenced client implementation is done for the [VS Code](https://code.visualstudio.com/) client.

## About the Book
### Authors
- Nadeeshaan Gunasinghe - Technical Lead, WSO2 Inc (Pvt) Ltd.
**Contact:**
Email - nadeeshaangunasinghe@gmail.com
LinkedIn - https://www.linkedin.com/in/nadeeshaan/
Github: https://github.com/nadeeshaan

- Nipuna Marcus - Technical Lead, WSO2 Inc (Pvt) Ltd
**Contact:**
Email - nipunamarcus2@gmail.com
LinkedIn - linkedin.com/in/nipuna-marcus-0b721143
Github: https://github.com/NipunaMarcus

### Introduction

## Technologies
- The client implementation is done with [TypeScript](https://www.typescriptlang.org/).
- Language Server implementation is done with Java and Java-11 would be preffered
- Language Server implementation is done for the [Ballerina](https://ballerina.io/) programming language. The default implementation is done for [Ballerina Swan Lake Beta 3](https://ballerina.io/downloads/). You can read more and learn about Ballerina programming language in the [official doccumentation](https://ballerina.io/learn/)
- [Gradle](https://gradle.org/) is used as the build tool for the server component
- [Node JS](https://nodejs.org/en/) [v12.20.0 at least] and [NPM](https://www.npmjs.com/) [6.14.x at least] is used as for the client component

## How to build
This repository contains two components as the client and the server implementation. You have to build these components individually.

##### Step 1
Clone the repo

##### Step 2
**Build the Server Implementation**
Go to the repo root and execute the following command to build the server implementation.
`./gradlew clean build`
The server implementation will copy the particular uber-jar artifact to `REPO_ROOT/client` directory.

**Build the Client Implementation**
Go to the `REPO_ROOT/client/ls-client` directory and execute the following command to install the Dependencies
`npm install`
Now execute the following command to build the client and generate the `.vsix` VS Code plugin artifact
`npm run build`

## Using the Extension and the Language Server
### Prerequisites
- Install [Ballerina SwanLake Beta3](https://ballerina.io/downloads/swan-lake-archived/)
- Install [Java](https://www.oracle.com/java/technologies/downloads/)
- Install [VS Code](https://code.visualstudio.com/Download)

### Usage
- Install the built `.vsix` extension artifact. The plugin itself will start the language server once a `.bal` file is opened
- Open a Ballerina source file from the VSCode editor.
- Explore the language features such as auto-completions, find references, and etc

## Debugging the Implementation
### Enable trace logs for the server implementation
- You can explore the trace logs for the messages passed between the language server and the client by adding the following configuration option in the user settings.
`"ballerina-lang-client.trace.server": "verbose"`

### Debugging the Language Server
##### Step 1
Open the `REPO_ROOT/client/ls-client/` directory in VS Code.

##### Step 2
Go to `REPO_ROOT/client/ls-client/.vscode/launch.json` file and set the `LSDEBUG` config value to `true`

##### Step 3
Run the extension as shown in the figure
