def awsCredentials = [[$class: 'AmazonWebServicesCredentialsBinding', credentialsId: '047a2523-0a76-477f-a24d-17efc60b6a82', accessKeyVariable: 'AWS_ACCESS_KEY_ID', secretKeyVariable: 'AWS_SECRET_ACCESS_KEY']]
pipeline {
    agent { node {label 'prod-nc-slave-02'}  }
    options {
        disableConcurrentBuilds()
        withCredentials(awsCredentials)
    }
    environment {
        MESSAGE   = 'demo k8s-deployed'
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
                         -v $(pwd)/pac/5glab/ssh_config_demo:/etc/ssh/ssh_config_demo   \
                         -v $(pwd)/pac/5glab/ssh_key:/etc/ssh/ssh_key                   \
                         --net=host                                                     \
                         dockerhub.hi.inet/5ghacking/ansible:2.9.7-2                    \
                         -i /5glab/inventory-demo                                       \
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
