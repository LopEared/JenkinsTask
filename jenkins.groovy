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
<<<<<<< HEAD
                    #echo "Number build#:${env.BUILD_ID}"
                    echo "Job name is:${env.JOB_NAME}"
                    echo "Node name is:${env.NODE_NAME}"
                    docker build -t "trainimage-${env.BUILD_ID}:${env.BUILD_ID}" .'
=======
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
                    echo "<-----------Remove unnecessary after build image------------->"
                    docker rmi -f "$ImageName-$BUILD_NUMBER:$BUILD_NUMBER" #Remove unnecessary image 
                    echo
                    docker images
                    echo
                    ls -lsh
>>>>>>> 90bdc5853d55d8763b60ce72cf31d5de289868b3
                '''
                echo "<------------Finish build image------------->"
            }
        }
        stage('Docker_CD') {
            steps {
                echo "<------------Start Dispatching image-------------->"
                sh '''
                    echo "<-------Copy image archive to deployment server--------->"
                    scp ./*.tar mikuser@test:/home/mikuser/Warehous
                    echo "<-------#Remove unnecessary archives --------->"
                    rm -f *.tar
                    echo
                    ls -lsh                    
                '''              
                echo "<------------Finish Dispatching image------------->"
            }
        }
        stage('Docker_BackUp') {
            agent {
                label 'Slave3'
            }
            steps {
                echo "<------------Start BackUp image-------------->"
                sh '''
                    echo
                        pwd
                    echo
                        ls -l

                    #Prepare directories
                    mkdir -p ~/JenkWorkpl/$JOB_NAME                                          # Common directory for  project
                    mkdir -p ~/JenkWorkpl/$JOB_NAME/ACTUAL_VER                               # Actual version directory
                    mkdir -p ~/JenkWorkpl/$JOB_NAME-$BUILD_NUMBER                            # Current version project image

                    cd ~/JenkWorkpl/$JOB_NAME/ACTUAL_VER                                     # Go to Actual version directory
                    rm -f ./*                                                                # Delete all files from  Actual version directory
                    cd ~/Warehous                                                            # Go to temporary directory 
                    cp * ~/JenkWorkpl/$JOB_NAME/ACTUAL_VER                                   # Copy  file to Actual version directory
                    cp * ~/JenkWorkpl/$JOB_NAME-$BUILD_NUMBER                                # Copy  file from  temporary directory  to Current version project image folder
                    pwd
                    rm -f ./*                                                             #  Delete all files from  temporary directory 
                    mv ~/JenkWorkpl/$JOB_NAME-$BUILD_NUMBER ~/JenkWorkpl/$JOB_NAME           # Move Current version project image folder to Common directory for  project
                    
                '''
                echo "<------------Finish BackUp image------------->"
            }
        }
    }
}
