pipeline {
  agent {
    docker {
      image 'openjdk:8-jre-slim'
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