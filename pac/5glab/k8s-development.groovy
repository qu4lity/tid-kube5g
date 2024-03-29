// @TODO: Introduce awsCredentials
def awsCredentials = [[$class: 'AmazonWebServicesCredentialsBinding', credentialsId: '<INSERT_AWS_Credentials_ID>', accessKeyVariable: 'AWS_ACCESS_KEY_ID', secretKeyVariable: 'AWS_SECRET_ACCESS_KEY']]

pipeline {
    agent { node {label 'prod-nc-slave-02'}  }
    options {
        disableConcurrentBuilds()
        withCredentials(awsCredentials)
    }
    environment {
        MESSAGE   = 'development k8s-deployed'
        QUEUE_URL = 'https://sqs.eu-west-3.amazonaws.com/709233559969/5GnowSQS'
    }
    stages {
        stage ('Provision K8S cluster on baremetal'){
            steps {
                dir ("${WORKSPACE}") {
                    sh '''
                       docker run                                                       \
                         --rm                                                           \
                         -w /srv                                                        \
                         -v $(pwd)/ansible:/srv                                         \
                         -v $(pwd)/pac/5glab:/5glab                                     \
                         -v $(pwd)/ansible/ssh_config_development:/etc/ssh/ssh_config_development \
                         -v $(pwd)/ansible/ssh_key_development:/etc/ssh/ssh_key                   \
                         --net=host                                                     \
                         dockerhub.hi.inet/tid-kube5ging/ansible:2.9.7-2                    \
                         -i /srv/inventory-development                                \
                         bootstrap.yml
                       '''
                }
            }
        }
        stage ('send event to run one-click demo'){
            steps {
                dir ("${WORKSPACE}") {
                    sh '''
                       export AWS_SECRET_ACCESS_KEY="${AWS_SECRET_ACCESS_KEY}"
                       export AWS_DEFAULT_REGION=eu-west-3
                       export AWS_ACCESS_KEY_ID="${AWS_ACCESS_KEY_ID}"
                       aws sqs send-message                                   \
                         --queue-url ${QUEUE_URL}                             \
                         --message-body ${MESSAGE}
                       '''
                }
            }
        }
    }
    post {
        success {
            echo 'wow k8s cluster provisioned'
            echo ' clean dockerhub credentials'
            sh 'rm -f ${HOME}/.docker/config.json'
        }
        cleanup {
            echo 'One way or anoher we clean it'
            deleteDir()
        }
    }
}
