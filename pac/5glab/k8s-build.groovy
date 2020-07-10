pipeline {
    agent { node {label 'prod-nc-slave-02'}  }
    options {
        disableConcurrentBuilds()
    }
    stages {
        stage ('Prepare KVM hosts networking'){
            steps {
                dir ("${WORKSPACE}") {
                    sh '''
                       docker run                                           \
                         --rm                                               \
                         -w /srv                                            \
                         -v $(pwd)/ansible:/srv                             \
                         -v $(pwd)/pac/5glab:/5glab                         \
                         -v $(pwd)/pac/5glab/ssh_config:/etc/ssh/ssh_config \
                         --net=host                                         \
                         dockerhub.hi.inet/5ghacking/ansible:2.9.7-2        \
                         -i /assets/5glab-inventory                         \
                         /5glab/kvm.yml
                       '''
                }
            }
        }
        stage ('Provision K8S cluster'){
            steps {
                dir ("${WORKSPACE}") {
                    sh '''
                       docker run                                           \
                         --rm                                               \
                         -w /srv                                            \
                         -v $(pwd)/ansible:/srv                             \
                         -v $(pwd)/pac/5glab:/5glab                         \
                         -v $(pwd)/pac/5glab/ssh_config:/etc/ssh/ssh_config \
                         --net=host                                         \
                         dockerhub.hi.inet/5ghacking/ansible:2.9.7-2        \
                         -i /assets/5glab-inventory                         \
                         /5glab/bootstrap.yml
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
