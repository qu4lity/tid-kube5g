pipeline {
    agent { node {label 'prod-nc-slave-02'}  }
    options {
        disableConcurrentBuilds()
    }
    stages {
        stage ('Provision SpeedTest'){
            steps {
                dir ("${WORKSPACE}/iac/ansible/5g") {
                    sh '''
                          docker run                                        \
                               --rm                                         \
                               -w /srv                                      \
                               -v $(pwd):/srv                               \
                               -v $(pwd)/ssh_config:/etc/ssh/ssh_config     \
                               --net=host                                   \
                               dockerhub.hi.inet/tid-kube5ging/ansible:2.9.7-2  \
                               -i /srv/inventory_st                         \
                               /srv/onpremise.yml
                       '''
                }
            }
        }
    }
    post {
        success {
            echo 'wow speedtest'
            echo ' clean dockerhub credentials'
            sh 'rm -f ${HOME}/.docker/config.json'
        }
        cleanup {
            echo 'One way or anoher we clean it'
            deleteDir()
        }
    }
}
