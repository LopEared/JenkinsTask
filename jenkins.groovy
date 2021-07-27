#!groovy
// Check Slave properties

properties([disableConcurrentBuilds()])

pipeline {
    agent {
        label 'Slave1'
    }
    options{
        buildDiscarder(logRotator(numToKeepStr: '3', artifactNumToKeepStr: '3'))
        timestamps()
    }
    environment { 
        CC = 'clang'
    }
    stages('Docker_CI') {
        stage('Create_artifact') {
            steps {
                echo "<------------Start build image-------------->"
                sh '''
                    
                    docker build -t "$trainimage-${env.BUILD_ID}:${env.BUILD_ID}" .'
                '''
                echo "<------------Finish build image------------->"
            }
        }
    }
}