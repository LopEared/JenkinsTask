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
    stages('WorkFlow') {
        stage('Docker_CI') {
            steps('Create_artifact') {
                echo "<------------Start build image-------------->"
               
                sh 'docker build -t "trainimage-1:1" .'
               
                echo "<------------Finish build image------------->"
            }
        }
        stage('Docker_CD') {
            steps {
                echo "<------------Start Dispatching image-------------->"
                echo "<------------Finish Dispatching image------------->"
            }
        }
        stage('Docker_BackUp') {
            steps {
                echo "<------------Start BackUp image-------------->"
                echo "<------------Finish BackUp image------------->"
            }
        }
    }
}
