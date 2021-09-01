// @TODO: Introduce awsCredentials
def awsCredentials = [[$class: 'AmazonWebServicesCredentialsBinding', credentialsId: '<INSERT_AWS_Credentials_ID>', accessKeyVariable: 'AWS_ACCESS_KEY_ID', secretKeyVariable: 'AWS_SECRET_ACCESS_KEY']]

pipeline {
    agent { node { label 'prod-nc-slave-01' } }
    options {
        disableConcurrentBuilds()
        withCredentials(awsCredentials)
    }
    environment {
        ENVIRONMENT_NAME = 'tid-kube5g'
        GIT_BRANCH = "${GIT_BRANCH}"
        GIT_REPONAME = 'awesome-tid-kube5g'
        AWS_DEFAULT_REGION = 'eu-west-3'
        AWS_DEFAULT_OUTPUT = 'json'
        CPF_VER = 'v1.2.4'
    }
    stages {
      stage ('open5gs clone'){
          steps {
             withCredentials ([[
              $class: 'UsernamePasswordMultiBinding',
              credentialsId: '<Insert_github_credentials>',
              usernameVariable: 'GIT_USERNAME',
              passwordVariable: 'GIT_PASSWORD'
             ]]) {
                dir ("${WORKSPACE}") {
                    checkout([$class: 'GitSCM',
                        branches: [[name: '*/develop']],
                        extensions: [[$class: 'RelativeTargetDirectory',
                            relativeTargetDir: 'docker-open5gs']],
                        userRemoteConfigs: [[url: 'https://github.com/Telefonica/docker-open5gs.git',credentialsId: '<Insert_github_credentials>']]])
                }
             }
          }
      }
      stage ('build upf base'){
          steps {
               dir ("${WORKSPACE}/docker-open5gs/"){
                   sh '''
                       docker build -t open5gs:1.2.4 .
                   '''
               }
          }
      }
      stage ('build upf: pgw'){
          steps {
               dir ("${WORKSPACE}/docker-open5gs/dissag-cfg/config-diss-upf/pgw"){
                    withCredentials([usernamePassword(
                       credentialsId: "docker_pull_cred",
                       usernameVariable: "USER",
                       passwordVariable: "PASS"
                   )]) {
                        sh '''
                           docker login dockerhub.hi.inet --username ${USER} --password ${PASS}
                           docker build -t dockerhub.hi.inet/open5gs_pgw:1.2.4 .
                           docker push dockerhub.hi.inet/open5gs_pgw:1.2.4
                        '''
                   }
               }
          }
      }
      stage ('build upf: sgw'){
          steps {
               dir ("${WORKSPACE}/docker-open5gs/dissag-cfg/config-diss-upf/sgw"){
                   sh '''
                       docker login dockerhub.hi.inet
                       docker build -t dockerhub.hi.inet/open5gs_sgw:1.2.4 .
                       docker push dockerhub.hi.inet/open5gs_sgw:1.2.4
                   '''
               }
          }
      }

      stage ('build upf: pcrf'){
          steps {
               dir ("${WORKSPACE}/docker-open5gs/dissag-cfg/config-diss-upf/pcrf"){
                   sh '''
                       docker login dockerhub.hi.inet
                       docker build -t dockerhub.hi.inet/open5gs_pcrf:1.2.4 .
                       docker push dockerhub.hi.inet/open5gs_pcrf:1.2.4
                   '''
               }
          }
      }
  }
}
