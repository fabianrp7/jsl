import javaposse.jobdsl.dsl.DslFactory

def scriptDir = getClass().protectionDomain.codeSource.location.path
//projects passed in additionalParameters
for (project in projects) {
    def multiPipeline = new GithubMultibranch(
        description: project.description,
        name: project.name,
        displayName: project.displayName,
        includeBranches: project.includeBranches,
        excludeBranches: project.excludeBranches,
        gitRepository: project.gitRepository,
        idUnico: project.idUnico,
        credenciales: project.credenciales,
        ).build(this)   
}
 
class GithubMultibranch {
 
    String name
    String description
    String displayName
    String repositoryOwner
    String repositoryName
    String credentials
    String includeBranches
    String excludeBranches
    String gitRepository
    String idUnico
    String credenciales
 
 
    void build(DslFactory dslFactory) {
        def job = dslFactory.multibranchPipelineJob(name) {
            description(description)
            displayName(displayName)
            branchSources {
                git {
                    id(idUnico) // IMPORTANT: use a constant and unique identifier
                    remote(gitRepository)
                    credentialsId(credenciales)
                    excludes(excludeBranches)
                    includes(includeBranches)
                }
            }
            triggers{
                cron('5 0 * 8 *')
            }
            factory {
                workflowBranchProjectFactory {
                    scriptPath('Jenkinsfile')
                }
            }
            orphanedItemStrategy {
                discardOldItems {
                    numToKeep(15)
                }
            }
        }
    }
}