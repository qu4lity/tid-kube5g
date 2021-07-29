// @TODO: Introduce awsCredentials
def awsCredentials = [[$class: 'AmazonWebServicesCredentialsBinding', credentialsId: '<INSERT_AWS_Credentials_ID>', accessKeyVariable: 'AWS_ACCESS_KEY_ID', secretKeyVariable: 'AWS_SECRET_ACCESS_KEY']]

pipeline {
    agent { node {label 'prod-nc-slave-02'}  }
    options {
        disableConcurrentBuilds()
        withCredentials(awsCredentials)
    }
    environment {
        ENVIRONMENT_NAME = '5g-development'
        SERVICE_PATH = '5g/aws-development'
        GIT_BRANCH = 'develop'
        GIT_REPONAME = 'awesome-tid-kube5g'
        AWS_DEFAULT_REGION = 'eu-west-3'
        AWS_DEFAULT_OUTPUT = 'json'
        AWS_KEY = 'tid-kube5gkey.pem'
        REMOTE_NODE_PRIVATE_SUBNET_1 = '172.30.0.0/16'
        REMOTE_NODE_PRIVATE_SUBNET_2 = '192.168.50.0/24'
        PUBLIC_SUBNET_1_CIDR = '10.0.4.0/24'

    }
    stages {
        stage ('Initialize infrastructure') {
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
                            docker run                                                                                \
                                 --env AWS_SECRET_ACCESS_KEY                                                          \
                                 --env AWS_ACCESS_KEY_ID                                                              \
                                 --rm                                                                                 \
                                 -w /srv                                                                              \
                                 -v $(pwd):/srv                                                                       \
                                 -u $(id -u):$(id -g)                                                                 \
                                 --net=host                                                                           \
                                 dockerhub.hi.inet/tid-kube5ging/terraform:latest                                         \
                                   init                                                                               \
                                 --force-copy                                                                         \
                                 -backend-config="bucket=tid-kube5g-terraform-state-${AWS_DEFAULT_REGION}" \
                                 -backend-config="region=${AWS_DEFAULT_REGION}"                                       \
                                 -backend-config="key=$GIT_REPONAME/${GIT_BRANCH}/$SERVICE_PATH/terraform.tfstate"
                            '''
                }
            }
        }
        stage ('Deploy cloud-native core') {
            parallel {
                stage ('Provision CPF'){
                    steps {
                        dir ("${WORKSPACE}/iac/terraform/${SERVICE_PATH}") {
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
                                  apply                                                             \
                                  --auto-approve                                                    \
                                  -var environment_name=${ENVIRONMENT_NAME}                         \
                                  -var region=${AWS_DEFAULT_REGION}                                 \
                                  -var remote_node_private_subnet_1=${REMOTE_NODE_PRIVATE_SUBNET_1} \
                                  -var remote_node_private_subnet_2=${REMOTE_NODE_PRIVATE_SUBNET_2}
                             '''
                        }
                    }
                }
                stage ('Provision UPF'){
                    steps {
                        dir ("${WORKSPACE}/iac/ansible/5g") {
                            sh '''
                               docker run                                        \
                                 --rm                                            \
                                 -w /srv                                         \
                                 -v $(pwd):/srv                                  \
                                 -v $(pwd)/ssh_config_5g:/etc/ssh/ssh_config     \
                                 -v $(pwd)/../keys/bastion_key:/etc/ssh/ssh_key  \
                                 --net=host                                      \
                                 dockerhub.hi.inet/tid-kube5ging/ansible:2.9.7-2     \
                                 -i /srv/inventory_5g                            \
                                 /srv/onpremise.yml
                            '''
                        }
                    }
                }
            }
        }
        stage ('Provision UPF-CPF connectivity'){
            steps {
                sh 'chmod 400 ${WORKSPACE}/iac/ansible/keys/${AWS_KEY}'
                sh '${WORKSPACE}/tools/sshCheck.sh bastion.tid-kube5g.tk'
                dir ("${WORKSPACE}/iac/ansible/5g") {
                    sh '''
                       docker run                                                                                           \
                         --rm                                                                                               \
                         -w /srv                                                                                            \
                         -v $(pwd):/srv                                                                                     \
                         -v $(pwd)/../keys:/cred                                                                            \
                         --net=host                                                                                         \
                         dockerhub.hi.inet/tid-kube5ging/ansible:2.9.7                                                          \
                         --private-key /cred/${AWS_KEY}                                                                     \
                         -i /srv/inventory                                                                                 \
                         /srv/playbookBastion.yml                                                                           \
                         -e bastion_private_subnet=\"${PUBLIC_SUBNET_1_CIDR}\"                                              \
                         -e remote_node_private_subnet=\"${REMOTE_NODE_PRIVATE_SUBNET_1},${REMOTE_NODE_PRIVATE_SUBNET_2}\" -vvvv
                    '''
                }
                dir ("${WORKSPACE}/iac/bash") {
                    sh 'chmod a+x ./UporDown.sh'
                    sh './UporDown.sh bastion.tid-kube5g.tk 10.95.164.110 labuser <Insert_user_password>'
                }
            }
        }
        stage ('Provision antenna') {
            steps {
                dir ("${WORKSPACE}/iac/js") {
                    sh 'docker login dockerhub.hi.inet'
                    sh 'docker run -v $(pwd):/home/node --rm dockerhub.hi.inet/networkcloud/aw5g-node:demo1 switch_on.js'
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
            echo 'One way or anoher we clean it'
            deleteDir()
        }
    }
}
