#!/usr/bin/env groovy   
def call(project) {
    def cmnStgs = new com.devops.jenkins.cmn.CmnStgs(this)
    def javaStgs = new com.devops.jenkins.java.JavaStgs(this)
    node(label:project.node){
        cmnStgs.stgCheckoutSCM(project)  
        javaStgs.stgBuild()
        if(project.stgUnitTests){
            javaStgs.stgTest()
        }        
        javaStgs.stgSonarQubeAnalysis(project)
        if(project.stgSQualityGate){
            cmnStgs.stgSonarQubeQGate(project)
        }
        if(project.stgKiuwan){
            cmnStgs.stgKiuwanAnalysis(project) 
        }                 
        cmnStgs.stgDeployUrbanCodeDeploy(project)
    }
}