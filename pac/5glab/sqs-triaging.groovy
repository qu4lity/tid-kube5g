import groovy.json.JsonSlurper

def deployDemoK8s = false
def deployDevelopmentK8s = false
def deployst = false
def destroyst = false
def deployDemo = false
def deployDevelopment= false

pipeline {
    agent { node {label 'prod-nc-slave-02'} }
    parameters {
        string(name: 'sqs_body')
    }
    environment {
        CURRENT_BRANCH = """${sh(
                    returnStdout: true,
                    script: "basename ${GIT_BRANCH} | tr -d '\n'"
                )}"""
    }
    options {
        disableConcurrentBuilds()
    }
    stages {
        stage('Get parameters from SQS body') {
            steps {
                echo "${env.sqs_body}"
                script {
                    if (env.sqs_body != null) {
                        if (env.sqs_body == "machine-installed corenet2.lab") {
                            sleep (330)
			                print "event demo machine-installed"
                            deployDemoK8s = true
                            env.K8S_BRANCH = "master"
                            env.EXECUTION_JOB = "awesome-tid-kube5g/production/deployments/k8s_deploy"
                        }
                        else if (env.sqs_body == "machine-installed quantadu.lab") {
                            sleep (330)
                            print "event development machine-installed"
                            deployDevelopmentK8s = true
                            env.K8S_BRANCH = "master"
                            env.EXECUTION_JOB = "awesome-tid-kube5g/development/deployments/k8s_deploy"
                        }
                        else if (env.sqs_body == "speedtest") {
                            print "event deploy SpeedTest"
                            deployst = true
                            env.EXECUTION_JOB = "awesome-tid-kube5g/develop/deployments/speedtest"
                        }
                        else if (env.sqs_body == "speedtest-destroy") {
                            print "Event destroy SpeedTest"
                            destroyst = true
                            env.EXECUTION_JOB = "awesome-tid-kube5g/develop/deployments/speedtest_destroy"
                        }
                        else if (env.sqs_body == "demo-k8s-deployed") {
                            print "event deploy demo"
                            deployDemo = true
                            env.DEMO_BRANCH = "develop"
                            env.EXECUTION_JOB = "awesome-tid-kube5g/production/demo_create"
                        }
                        else if (env.sqs_body == "development k8s-deployed") {
                            print "event deploy demo"
                            deployDevelopment = true
                            env.DEMO_BRANCH = "develop"
                            env.EXECUTION_JOB = "awesome-tid-kube5g/development/demo_create"
                        }
                        else {
                            return
                        }
                    }
                }
            }
        }
        stage ('Deploy Demo K8s cluster'){
            when {
                expression { deployDemoK8s }
            }
            steps {
                build job: env.EXECUTION_JOB,
                    parameters: [
                        string(name: 'BRANCH_NAME', value: env.K8S_BRANCH)
                    ]
            }
        }
        stage ('Deploy Development K8s cluster'){
            when {
                expression { deployDevelopmentK8s }
            }
            steps {
                build job: env.EXECUTION_JOB,
                    parameters: [
                        string(name: 'BRANCH_NAME', value: env.K8S_BRANCH)
                    ]
            }
        }
        stage ('Deploy SpeedTest'){
            when {
                expression { deployst }
            }
            steps {
                build job: env.EXECUTION_JOB,
                    parameters: [
                        string(name: 'BRANCH_NAME', value: CURRENT_BRANCH)
                    ]
            }
        }
        stage ('Destroy SpeedTest'){
            when {
                expression { destroyst }
            }
            steps {
                build job: env.EXECUTION_JOB,
                    parameters: [
                        string(name: 'BRANCH_NAME', value: CURRENT_BRANCH)
                    ]
            }
        }
        stage ('Deploy Development'){
            when {
                expression { deployDevelopment }
            }
            steps {
                build job: env.EXECUTION_JOB,
                    parameters: [
                        string(name: 'BRANCH_NAME', value: env.DEMO_BRANCH)
                    ]
            }
        }
        stage ('Deploy Demo'){
            when {
                expression { deployDemo }
            }
            steps {
                build job: env.EXECUTION_JOB,
                    parameters: [
                        string(name: 'BRANCH_NAME', value: env.DEMO_BRANCH)
                    ]
            }
        }
    }
    post {
        cleanup {
            echo 'clean'
            deleteDir()
        }
    }
}
