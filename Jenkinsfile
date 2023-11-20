pipeline {
  agent {
    docker {
      image 'openjdk:8'
    }
  }
  triggers{
    bitbucketPush()
  }
  stages {
    stage('Build artifact') {
      steps {
        sh '''#!/bin/bash -e
          chmod +x mvnw
          
          ./mvnw package
        '''
      }
    }
  }
}