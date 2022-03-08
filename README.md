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
The Language Server Protocol (LSP) has been one of the most talked about topics during the past few years when it comes to the tooling for programming languages. With the advancement of the developer tools and the programming languages, developers started to rely more and more on advanced tools and enhanced language services. When we consider one of the most focused branches of developer tools which is IDEs and text editors, there are many vendors who have released various editing tools in the past couple of decades. When we consider the number of programming languages along with the number of smart editors nowadays, in order to support language intelligence among the editors, these vendors have to repeat the same thing. The Language Server Protocol was introduced to solve this particular problem, and today it has become the norm of the development tools’ language intelligence provider. By adopting the LSP, tools such as text editors and integrated development environments (IDEs) could expand the capabilities and avoid the users’ burden of switching between the development tools for trying new programming languages and frameworks.
This book is for the developers who are passionate about developing programming language tools. In this book, we provide the readers a comprehensive understanding about the Language Server Protocol and how to develop a Language Server from scratch. The readers will be guided with code samples to provide a better understanding about the server implementation by adhering to the user experience best practices as well as the LSP best practices. The readers are expected to use the book along with the example implementation, in order to get a better understanding about the concepts described
in the book. In the example implementation, the book refers to VS Code as the client; however, the readers can use any other client and integrate the server implementation as desired.

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
**Set the github access token**
In order to build the repo, you have the set the github access token in the first place. In order to do so, execute the following command

In Unix
```
export packageUser='your_github_user_name'
export packagePAT='your_github_personal_access_token'
```

In Windows
```
set packageUser='your_github_user_name'
set packagePAT='your_github_personal_access_token'
```
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
Run the extension from VSCode
