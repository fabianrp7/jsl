#!/usr/bin/env groovy
import groovy.json.JsonSlurperClassic;
def call(git_post) {
    node{
        stage('Calling JOB'){
            def jobName = git_post_repository_name
            def jobTargetBranch = git_post_ref.replaceAll("refs/heads/","")
            if(git_post_repository_full_name.contains("Bus12c")){
                build job: "/GenericServiceBus/${jobTargetBranch}", propagate: false, wait: false, parameters: [string(name: 'serviceDefinitionName', value: "${jobName}")]
            }else{
                build job: "/${jobName}/${jobTargetBranch}", propagate: false, wait: false
            }
        }
    }
}