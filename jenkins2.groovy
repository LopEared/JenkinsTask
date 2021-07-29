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
                    echo "<-----------Remove unnecessary after build image------------->"
                    docker rmi -f "$ImageName-$BUILD_NUMBER:$BUILD_NUMBER" #Remove unnecessary image 
                    echo
                    docker images
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
                echo "<----------------------------Start BackUp image---------------------------------------->"
                sh '''
                    echo
                        pwd
                    echo
                        ls -l
                    #Prepare directories
                    mkdir -p ~/JenkWorkpl/$JOB_NAME                                          # Common directory for  project
                    mkdir -p ~/JenkWorkpl/$JOB_NAME/ACTUAL_VER                               # Actual version directory
                    mkdir -p ~/JenkWorkpl/$JOB_NAME/BackUps                                  #Common directory for  BackUps
                    mkdir -p ~/JenkWorkpl/$JOB_NAME-$BUILD_NUMBER                            # Current version project image
                    cd ~/JenkWorkpl/$JOB_NAME/ACTUAL_VER                                     # Go to Actual version directory
                    rm -f ./*                                                                # Delete all files from  Actual version directory
                    cd ~/Warehous                                                            # Go to temporary directory 
                    cp * ~/JenkWorkpl/$JOB_NAME/ACTUAL_VER                                   # Copy  file to Actual version directory
                    cp * ~/JenkWorkpl/$JOB_NAME-$BUILD_NUMBER                                # Copy  file from  temporary directory  to Current version project image folder
                    pwd
                    rm -f ./*                                                                #  Delete all files from  temporary directory 
                    mv ~/JenkWorkpl/$JOB_NAME-$BUILD_NUMBER ~/JenkWorkpl/$JOB_NAME           # Move Current version project image folder to Common directory for  project
                    echo "<----------------------------Start BackUp image---------------------------------------->"
                    echo
                    cd ~/JenkWorkpl/$JOB_NAME/BackUps  
                    echo
                    pwd
                    echo
                    cp ~/JenkWorkpl/$JOB_NAME/ACTUAL_VER/* ~/JenkWorkpl/$JOB_NAME/BackUps
                    echo
                    rm -f "$ImageName-$(($BUILD_NUMBER-3)):$(($BUILD_NUMBER-3))"
                    echo
                    ls -lsh                     
                '''
                echo "<----------------------------Finish BackUp image---------------------------------------->"
            }
        }
        stage('Deployment'){
            agent {
                label 'Slave3'
            }
            steps {
                echo "<----------------------------Start Deployment service----------------------------------------->"
                sh'''
                    cd ~/JenkWorkpl/$JOB_NAME/ACTUAL_VER 
                    pwd
                    ls -lsh
                    docker load -i "$ImageName-$BUILD_NUMBER:$BUILD_NUMBER".tar
                    echo
                    docker rmi -f "$ImageName-$(($BUILD_NUMBER-1)):$(($BUILD_NUMBER-1))"
                    echo
                    docker images 
                    echo
                    #docker run --rm -d --name ExperCat -p 4040:8080 tomcat:8.5.38
                    docker run --rm -d --name "ServiceCurrent-$BUILD_NUMBER" -p 4040:7070 "$ImageName-$BUILD_NUMBER:$BUILD_NUMBER" 
                    #curl http://172.18.144.193:4040/                           # View the web-servcice page 
                    echo
                    docker ps
                    echo
                    docker ps -a
                    echo
                    docker stop "ServiceCurrent-$BUILD_NUMBER"
                    echo
                    docker image prune
                    echo
                    docker rmi -f "$ImageName-$BUILD_NUMBER:$BUILD_NUMBER"
                    echo
                    docker images
                '''
                echo "<----------------------------Finish Deployment service---------------------------------------->"
            }
                                    
        }
    }
    post('Last_Actions'){
        always{
            steps(){
                echo "<------------Post build actions START-------------->"
                echo "<------------Post build actions Finish------------->"
            }
        }
        cleanup{
            steps(){
                echo "<------------Cleaning START-------------->"
                echo "<------------Cleaning Finish------------->"
            }
        }
    }    
    
    
    
}
