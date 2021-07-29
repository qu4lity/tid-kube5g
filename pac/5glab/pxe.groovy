// @TODO: Introduce awsCredentials
def awsCredentials = [[$class: 'AmazonWebServicesCredentialsBinding', credentialsId: '<INSERT_AWS_Credentials_ID>', accessKeyVariable: 'AWS_ACCESS_KEY_ID', secretKeyVariable: 'AWS_SECRET_ACCESS_KEY']]

pipeline {
    agent { node {label 'prod-nc-slave-02'}  }
    options {
        disableConcurrentBuilds()
        withCredentials(awsCredentials)
    }
    environment {
        ENVIRONMENT_NAME = 'pxe'
        GIT_BRANCH = 'pxe'
        GIT_REPONAME = 'awesome-tid-kube5g'
        IPMI_CORENET1 = '172.30.0.22'
        IPMI_CORENET2 = '172.30.0.23'

    }
    stages {
        stage ('power cycling baremetal servers') {
            steps {
                dir ("${WORKSPACE}/iac/ansible/5g") {
                        sh '''
                            docker run                                        \
                                 --rm                                         \
                                 -w /srv                                      \
                                 -v $(pwd):/srv                               \
                                 --net=host                                   \
                                 dockerhub.hi.inet/tid-kube5ging/ansible:2.9.7-2  \
                                 -i /srv/inventory                            \
                                 /srv/pxe.yml
                           '''
                }
            }
        }
    }
    post {
        cleanup {
            echo 'One way or anoher we clean it'
            deleteDir()
        }
    }
}
