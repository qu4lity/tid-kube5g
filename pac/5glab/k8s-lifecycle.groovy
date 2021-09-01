// @TODO: Introduce awsCredentials
def awsCredentials = [[$class: 'AmazonWebServicesCredentialsBinding', credentialsId: '<INSERT_AWS_Credentials_ID>', accessKeyVariable: 'AWS_ACCESS_KEY_ID', secretKeyVariable: 'AWS_SECRET_ACCESS_KEY']]

pipeline {
    agent { node { label 'pro-magne-slave-02' } }
    options {
        disableConcurrentBuilds()
        withCredentials(awsCredentials)
    }
    environment {
        ENVIRONMENT_NAME = 'tid-kube5g'
        SERVICE_PATH = '5g/kvm'
        CFG_PATH = '5g/cfg'
        GIT_BRANCH = 'develop'
        GIT_REPONAME = 'awesome-tid-kube5g'

    }
    stages {

        stage ('initialize infrastructure') {
            steps {
                dir ("${WORKSPACE}/iac/terraform/${SERVICE_PATH}") {
                    sh '''
                       terraform                                                                \
                         init
                       '''
                }
            }
        }
        stage ('prov kubernetes vms in server') {
                        dir ("${WORKSPACE}/iac/terraform/${SERVICE_PATH}") {
                        sh '''
                           terraform                                                           \
                             apply                                                             \
                             --auto-approve
                        '''
                        }
        }
        stage ('initialize kube cfg') {
            steps {
                dir ("${WORKSPACE}/iac/terraform/${CFG_PATH}") {
                    sh '''
                       terraform                                                                \
                         init
                       '''
                }
            }
        }
        stage ('config k8s') {
                        dir ("${WORKSPACE}/iac/terraform/${CFG_PATH}") {
                        sh '''
                           terraform                                                           \
                             apply                                                             \
                             --auto-approve
                        '''
                        }
        }
    }
    post {
        success {
            echo 'K8S deployed'
        }
    }
}
