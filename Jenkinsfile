pipeline {
  agent {
    dockerfile {
      filename 'Dockerfile.build'
    }
  }
  triggers{
    bitbucketPush()
  }
  environment {
    MAVEN_SETTINGS_XML = credentials('smart-inspect-maven-central-settings-xml')
    GPG_SECRET_KEY = credentials('smart-inspect-maven-central-pgp-key')
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
    stage('Staging release') {
      when {
        expression { BRANCH_NAME.startsWith('release-') }
      }
      steps {
        sh '''#!/bin/bash -e
          gpg --import --batch ${GPG_SECRET_KEY}

          ./mvnw -s ${MAVEN_SETTINGS_XML} clean deploy
        '''
      }
    }
  }
}