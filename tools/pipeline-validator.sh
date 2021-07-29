# curl (REST API)
# Assuming "anonymous read access" has been enabled on your Jenkins instance.
JENKINS_URL=http://127.0.0.1:8080
# JENKINS_CRUMB is needed if your Jenkins master has CRSF protection enabled as it should
JENKINS_CRUMB=`curl "$JENKINS_URL/crumbIssuer/api/xml?xpath=concat(//crumbRequestField,\":\",//crumb)"`
curl -X POST -F "jenkinsfile=<demok8s-destroy.groovy" $JENKINS_URL/pipeline-model-converter/validate
