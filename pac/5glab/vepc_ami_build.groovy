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
      stage ('edgedpi clone'){
          steps {
            withCredentials ([[
              $class: 'UsernamePasswordMultiBinding',
              credentialsId: '<Insert_github_credentials>',
              usernameVariable: 'GIT_USERNAME',
              passwordVariable: 'GIT_PASSWORD'
            ]]) {
                dir ("${WORKSPACE}/iac/packer/5g") {
                    checkout([$class: 'GitSCM',
                        branches: [[name: '*/master']],
                        extensions: [[$class: 'RelativeTargetDirectory',
                            relativeTargetDir: 'edgedpi']],
                        userRemoteConfigs: [[url: 'https://github.com/Telefonica/edgedpi.git',credentialsId: '<Insert_github_credentials>']]])
                }
            }
            withCredentials ([[
              $class: 'UsernamePasswordMultiBinding',
              credentialsId: 'github_cred',
              usernameVariable: 'GIT_USERNAME',
              passwordVariable: 'GIT_PASSWORD'
            ]]) {
                dir ("${WORKSPACE}/iac/packer/5g") {
                    sh('''
                        packer build -force -debug                     \
                            -var "git_user=${GIT_USERNAME}"            \
                            -var "git_pwd=${GIT_PASSWORD}"             \
                            -var "cpf_ver=${CPF_VER}"                  \
                            -var "version=${GIT_BRANCH}_${GIT_COMMIT}" \
                            -var "aws_region=${AWS_DEFAULT_REGION}" vEPC_opensource.json
                    ''')
               }
            }
          }
      }
    }
}
