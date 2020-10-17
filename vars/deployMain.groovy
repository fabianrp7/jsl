#!/usr/bin/env groovy
def call() {
    node{
        try {
            if(env.JOB_NAME == 'WebHook'){
                deployWebHook(git_post)
                return
            }
            def cmnStgs = new com.devops.jenkins.cmn.CmnStgs(this)
            def projectFullPath = env.JOB_NAME
            def listPath = projectFullPath.split("/")
            def projectName = listPath[0]
            def project = cmnStgs.stgReadParameters(projectName)
            switch(project.technology) {
                case 'Java':
                    deployJava(project) 
                    break               
                default:
                    script {
                        error "Invalid poject"
                    }
                    break
            }
        }catch(e){
                throw e
        }   
        return this
    }
}