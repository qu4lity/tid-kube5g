// @TODO: Introduce awsCredentials
def awsCredentials = [[$class: 'AmazonWebServicesCredentialsBinding', credentialsId: '<INSERT_AWS_Credentials_ID>', accessKeyVariable: 'AWS_ACCESS_KEY_ID', secretKeyVariable: 'AWS_SECRET_ACCESS_KEY']]

pipeline {
    agent { node { label 'prod-nc-slave-02' }  }
    options {
        disableConcurrentBuilds()
        withCredentials(awsCredentials)
    }
    environment {
        ENVIRONMENT_NAME = '5g-robot-demo'
        SERVICE_PATH = '5g'
        GIT_BRANCH = 'develop'
        GIT_REPONAME = 'awesome-tid-kube5g'
        AWS_DEFAULT_REGION = 'eu-west-3'
        AWS_DEFAULT_OUTPUT = 'json'
        AWS_KEY = 'tid-kube5gkey.pem'
        REMOTE_NODE_PRIVATE_SUBNET_1 = '172.30.0.0/16'
        REMOTE_NODE_PRIVATE_SUBNET_2 = '192.168.50.0/24'
        PUBLIC_SUBNET_1_CIDR = '10.0.4.0/24'
        ROBOT_TESTS_DIRECTORY = "${WORKSPACE}/tests"
        ROBOT_COMMON_DIRECTORY = "${WORKSPACE}/common"
        ROBOT_RESULTS_DIRECTORY = "${WORKSPACE}/results"
    }
    stages {
        stage ('Prepare testing tools') {
            steps {
                dir ("${WORKSPACE}/iac/robot/${SERVICE_PATH}") {
                    withCredentials([usernamePassword(
                       credentialsId: 'docker_pull_cred',
                       usernameVariable: 'USER',
                       passwordVariable: 'PASS'
                   )]) {
                        sh '''
                            docker login --username ${USER} --password ${PASS} dockerhub.hi.inet
                           '''
                   }
                }
                dir ("${WORKSPACE}") {
                    withCredentials([usernamePassword(
                       credentialsId: 'github_cred',
                       usernameVariable: 'GIT_USER',
                       passwordVariable: 'GIT_PASS'
                   )]) {
                        sh 'git clone --branch ${GIT_BRANCH} https://${GIT_USER}:${GIT_PASS}@github.com/Telefonica/robot_test_automation_common.git common'
                        sh 'git clone --branch ${GIT_BRANCH} https://${GIT_USER}:${GIT_PASS}@github.com/Telefonica/robot_test_automation_5g_now.git tests'
                        sh "mkdir ${ROBOT_RESULTS_DIRECTORY}"
                   }
                }
            }
        }
        stage('Launch tests') {
            steps {
                dir ("${WORKSPACE}/iac/robot/${SERVICE_PATH}") {
                    sh """
                            docker run -t \
                                --rm \
                                -v ${ROBOT_COMMON_DIRECTORY}:/opt/robot-tests/common \
                                -v ${ROBOT_TESTS_DIRECTORY}:/opt/robot-tests/tests \
                                -v ${ROBOT_RESULTS_DIRECTORY}:/opt/robot-tests/results \
                                dockerhub.hi.inet/tid-kube5ging/robot-test-image:latest
                        """
                }
            }
        }
    }
    post {
        always {
            publishHTML([allowMissing: false,
                    alwaysLinkToLastBuild: false,
                    keepAll: true,
                    reportDir: 'results',
                    reportFiles: 'report.html',
                    reportName: 'Robot Framework Tests Report',
                    reportTitles: '',
                    includes:'**/*'])
        }
        success {
            echo 'Robot test executed'
            echo ' clean dockerhub credentials'
            sh 'rm -f ${HOME}/.docker/config.json'
        }
        cleanup {
            echo 'One way or anoher we clean it'
            deleteDir()
        }
    }
}
