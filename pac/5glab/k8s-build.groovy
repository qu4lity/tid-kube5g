pipeline {
    agent { node {label 'prod-nc-slave-02'}  }
    options {
        disableConcurrentBuilds()
    }
    stages {
        stage ('Provision K8S cluster'){
            steps {
                dir ("${WORKSPACE}/ansible/") {
                    sh '''
                       docker run                                        \
                         --rm                                            \
                         -w /srv                                         \
                         -v $(pwd):/srv                                  \
                         --net=host                                      \
                         dockerhub.hi.inet/5ghacking/ansible:2.9.7-2     \
                         -i /srv/demo                                    \
                         /srv/bootstrap.yml                              \
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
