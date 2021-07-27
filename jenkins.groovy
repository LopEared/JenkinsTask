#!groovy
// Check Slave properties

properties([disableConcurrentBuilds()])

pipeline {
    agent {
        label 'Slave_1'
    }
    options{
        buildDiscarder(logRotator(numToKeepStr: '3', artifactNumToKeepStr: '3'))
        timestamps()
    }
    environment { 
        CC = 'clang'
    }
    stages {
        stage('Example') {
            steps {
                sh 'printenv'
                sh 'ssh mikuser@ubServ2 \ 'hostname\''
            }
        }
    }
}