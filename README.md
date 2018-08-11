# FitbitFS - Bi-directional synchronization with Fitbit Studio

Work locally with [Fitbit Studio](https://studio.fitbit.com) projects instead of having to use your web browser as an IDE. Synchronization is done bi-directionally, allowing you to edit your project either in the web browser or in your preferred local editor.

## Disclaimer

This project is just a hobby project for myself, and is not meant to be widely used. There will be bugs, and I take no responsibility for them.

## Why?

This project was created mainly to make it easier (read: possible) to use a different editor when creating Fitbit Studio applications. This also makes it a lot easier to keep your project in version control, and allows for use of tools such as [Prettier](https://prettier.io/) to standardize code formatting.

## Prerequisites

- Java 1.8
- Maven
- MacOS (Should work on Linux, but this has not been tested)

## Building

```bash
$ mvn package
$ cp target/fitbitfs-1.0-SNAPSHOT-jar-with-dependencies.jar ~/fitbitfs.jar
```

## Usage

FitbitFS supports three commands: `projects`, `init`, and `sync`. `projects` lists your Fitbit Studi projects, `init` initializes the current working directory as a fitbit project, and takes the project ID as an argument. `sync` synchronizes all files between Fitbit Studio and the current working directory.

```bash
# Create a new folder for the project
~ $ mkdir my-fitbit-project
~ $ cd my-fitbit-project

# Set up authorization
my-fitbit-project $ export FITBITFS_JWT='<authorization token>'

# List projects in Fitbit Studio
my-fitbit-project $ java -jar ~/fitbitfs.jar projects

# Initialize fitbitfs for a project
my-fitbit-project $ java -jar ~/fitbitfs.jar init $projectId

# Synchronize project with Fitbit Studio
my-fitbit-project $ java -jar ~/fitbitfs.jar sync
```
