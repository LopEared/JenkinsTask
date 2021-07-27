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
        BUILD_NUMBER    = "${env.BUILD_NUMBER}"
        JOB_NAME        = "${env.JOB_NAME}"
        NODE_NAME       = "${env.NODE_NAME}"
        WORKSPACE       = "${env.WORKSPACE}"
        ImageName       = "trainimage"
    }
    stages('WorkFlow') {
        stage('Docker_CI') {
            steps('Create_artifact') {
                echo "<------------Start build image-------------->"
               
                sh '''
                    echo "$BUILD_NUMBER"
                    echo "$JOB_NAME"
                    echo "$NODE_NAME"
                    echo "$WORKSPACE"
                    echo
                    docker build -t "$ImageName-$BUILD_NUMBER:$BUILD_NUMBER" .
                    echo
                    docker rmi -f "$ImageName-$(($BUILD_NUMBER-1)):$(($BUILD_NUMBER-1))"
                    echo
                    docker images
                    echo
                    rm -f *.tar
                    echo
                    docker save "$ImageName-$BUILD_NUMBER:$BUILD_NUMBER" > "$ImageName-$BUILD_NUMBER:$BUILD_NUMBER".tar
                    echo 
                    ls -lsh
                '''
                echo "<------------Finish build image------------->"
            }
        }
        stage('Docker_CD') {
            steps {
                echo "<------------Start Dispatching image-------------->"
                sh '''
                    scp -Cv *.tar mikuser@test:/home/mikuser/Warehous
                '''
                
                
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
