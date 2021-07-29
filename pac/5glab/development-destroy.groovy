// @TODO: Introduce awsCredentials
def awsCredentials = [[$class: 'AmazonWebServicesCredentialsBinding', credentialsId: '<INSERT_AWS_Credentials_ID>', accessKeyVariable: 'AWS_ACCESS_KEY_ID', secretKeyVariable: 'AWS_SECRET_ACCESS_KEY']]

pipeline {
    agent { node { label 'prod-nc-slave-02' } }
    options {
        disableConcurrentBuilds()
        withCredentials(awsCredentials)
    }
    environment {
        ENVIRONMENT_NAME = '5g-development'
        SERVICE_PATH = '5g/aws-development'
        GIT_BRANCH = 'develop'
        GIT_COMMIT = "${GIT_COMMIT}"
        GIT_REPONAME = 'awesome-tid-kube5g'
        AWS_DEFAULT_REGION = 'eu-west-3'
        AWS_DEFAULT_OUTPUT = 'json'
        AWS_KEY = 'tid-kube5gkey.pem'
        AWS_SECRET_ACCESS_KEY = "${AWS_SECRET_ACCESS_KEY}"
        AWS_VERSION='2.0.12'
        AWS_ACCESS_KEY_ID = "$AWS_ACCESS_KEY_ID"
    }
    stages {
        stage ('destroy the 5g aws infrastructure') {
            steps {
                dir ("${WORKSPACE}/iac/terraform/${SERVICE_PATH}") {
                  withCredentials([usernamePassword(
                       credentialsId: "docker_pull_cred",
                       usernameVariable: "USER",
                       passwordVariable: "PASS"
                   )]) {
                        sh '''
                            docker login --username ${USER} --password ${PASS} dockerhub.hi.inet
                           '''
                    }
                }
                dir ("${WORKSPACE}/iac/terraform/${SERVICE_PATH}") {
                    sh '''
                       docker run                                                                   \
                           --env AWS_SECRET_ACCESS_KEY                                              \
                           --env AWS_ACCESS_KEY_ID                                                  \
                           --rm                                                                     \
                           -w /srv                                                                  \
                           -v $(pwd):/srv                                                           \
                           -u $(id -u):$(id -g)                                                     \
                           --net=host                                                               \
                           dockerhub.hi.inet/tid-kube5ging/terraform:latest                             \
                             init                                                                   \
                             -backend-config="bucket=tid-kube5g-terraform-state-${AWS_DEFAULT_REGION}"  \
                             -backend-config="region=${AWS_DEFAULT_REGION}"                         \
                             -backend-config="key=$GIT_REPONAME/${GIT_BRANCH}/$SERVICE_PATH/terraform.tfstate"
                       '''

                    sh '''
                       docker run                                                                                                                                        \
                           --env AWS_SECRET_ACCESS_KEY                                                                                                                   \
                           --env AWS_ACCESS_KEY_ID                                                                                                                       \
                           --rm                                                                                                                                          \
                           amazon/aws-cli:${AWS_VERSION}                                                                                                                 \
                             s3                                                                                                                                          \
                             cp                                                                                                                                          \
                             s3://tid-kube5g-terraform-state-${AWS_DEFAULT_REGION}/$GIT_REPONAME/${GIT_BRANCH}/$SERVICE_PATH/terraform.tfstate  \
                             s3://tid-kube5g-terraform-state-${AWS_DEFAULT_REGION}/$GIT_REPONAME/${GIT_BRANCH}/$SERVICE_PATH/tf.bck
                       '''

                    sh '''
                       docker run                                                              \
                           --env AWS_SECRET_ACCESS_KEY                                         \
                           --env AWS_ACCESS_KEY_ID                                             \
                           --rm                                                                \
                           -w /srv                                                             \
                           -v $(pwd):/srv                                                      \
                           -u $(id -u):$(id -g)                                                \
                           --net=host                                                          \
                           dockerhub.hi.inet/tid-kube5ging/terraform:latest                        \
                             destroy                                                           \
                             --auto-approve
                       '''
                }
            }
        }
        stage ('destroy the 5G on-premise infra') {
            steps {
                dir ("${WORKSPACE}/iac/ansible/5g") {
                    sh '''
                       docker run                                                  \
                           --rm                                                    \
                           -w /srv                                                 \
                           -v $(pwd):/srv                                          \
                           -v $(pwd)/ssh_config:/etc/ssh/ssh_config                \
                           --net=host                                              \
                           dockerhub.hi.inet/tid-kube5ging/ansible:2.9.7-2             \
                              -i /srv/inventory                                    \
                              /srv/onpremise_destroy.yml
                       '''
                }
            }
        }
        stage ('switch off antenna') {
            steps {
                dir ("${WORKSPACE}/iac/js") {
                    sh 'docker login dockerhub.hi.inet'
                    sh 'docker run -v $(pwd):/home/node --rm dockerhub.hi.inet/networkcloud/aw5g-node:demo1 switch_off.js'
                }
            }
        }
    }
    post {
        success {
            echo 'wow antenna connected to 5G-Core'
            echo ' clean dockerhub credentials'
            sh 'rm -f ${HOME}/.docker/config.json'
        }
        cleanup {
            echo 'one way or another I destroy'
            deleteDir()
        }

    }
}
